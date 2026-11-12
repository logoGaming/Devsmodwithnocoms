#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler2D LiveDepthSampler;

uniform vec2 OutSize;
uniform mat4 InvProjMat;
uniform mat4 ScreenProj;
uniform mat4 CameraRot;
uniform vec3 CameraPos;
uniform float BrineTime;
uniform float SurfaceY;
uniform vec3 LightDir;
uniform vec3 SunDir;
uniform float Daylight;
uniform vec3 SplashPos;
uniform float SplashAge;
uniform float SplashPower;
uniform float SplashEnter;
uniform float ExitWater;
uniform float WaveStrength;
uniform float CalmFloor;
uniform vec4 BeamPos0;
uniform vec4 BeamPos1;
uniform vec4 BeamPos2;
uniform vec4 BeamPos3;
uniform vec3 BeamDir0;
uniform vec3 BeamDir1;
uniform vec3 BeamDir2;
uniform vec3 BeamDir3;

in vec2 texCoord;

out vec4 fragColor;

const float FAR_DIST = 192.0;
const float SHAFT_DIST = 110.0;
const int SHAFT_STEPS = 28;
const int RAY_STEPS = 24;
const vec3 SIGMA = vec3(0.075, 0.032, 0.022);
const vec3 PLANET_DIR = vec3(-0.45, 0.38, 0.80);
const vec3 WATER_SHALLOW = vec3(0.07, 0.34, 0.40);
const vec3 WATER_DEEP = vec3(0.005, 0.03, 0.07);

float interleavedNoise(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float hash3(vec3 p) {
    return fract(sin(dot(p, vec3(127.1, 311.7, 74.7))) * 43758.5453);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float swellEnergy(vec2 xz) {
    float gust = smoothstep(0.36, 0.64, valueNoise(vec2(BrineTime * 0.035, 11.0)) * 0.6 + valueNoise(vec2(BrineTime * 0.011, 41.0)) * 0.4);
    float patch = smoothstep(0.25, 0.75, valueNoise(xz * 0.03 + vec2(BrineTime * 0.02, 5.0)));
    return CalmFloor + (1.0 - CalmFloor) * gust * (0.3 + 0.7 * patch);
}

void waveField(vec2 xz, out vec2 grad, out vec3 hess) {
    float energy = swellEnergy(xz);
    vec2 g = vec2(0.0);
    float a = 0.0;
    float b = 0.0;
    float c = 0.0;
    for (int i = 0; i < 10; i++) {
        float fi = float(i);
        float freq = 0.45 * pow(1.34, fi);
        float ang = 0.6 + sin(fi * 2.7 + 1.0) * 0.95;
        vec2 wave = vec2(cos(ang), sin(ang));
        float amp = 0.075 * pow(freq, -1.25) * mix(energy, 0.5 + 0.5 * energy, fi / 9.0) * WaveStrength;
        float phase = dot(xz, wave) * freq + BrineTime * sqrt(freq) * 1.5 + fi * 1.9;
        g += amp * freq * cos(phase) * wave;
        float w = -amp * freq * freq * sin(phase);
        a += w * wave.x * wave.x;
        b += w * wave.x * wave.y;
        c += w * wave.y * wave.y;
    }

    vec2 delta = xz - SplashPos.xz;
    float r = max(length(delta), 0.6);
    float ringRadius = 5.5 * SplashAge;
    float width = 1.2 + SplashAge * 0.8;
    float envelope = SplashPower * 0.35 * exp(-pow((r - ringRadius) / width, 2.0)) * exp(-SplashAge * 0.7);
    if (envelope > 0.0001) {
        vec2 u = delta / r;
        float k = 2.4;
        float phase = k * (r - ringRadius);
        float slope = envelope * k * cos(phase);
        float curve = -envelope * k * k * sin(phase);
        float tangential = slope / r;
        g += slope * u;
        a += curve * u.x * u.x + tangential * (1.0 - u.x * u.x);
        b += (curve - tangential) * u.x * u.y;
        c += curve * u.y * u.y + tangential * (1.0 - u.y * u.y);
    }
    grad = g;
    hess = vec3(a, b, c);
}

float splashFoam(vec2 xz) {
    vec2 delta = xz - SplashPos.xz;
    float r = length(delta);
    float ringRadius = 5.5 * SplashAge;
    float width = 0.9 + SplashAge * 0.6;
    float ring = exp(-pow((r - ringRadius) / width, 2.0)) * exp(-SplashAge * 0.9) * SplashPower;
    float center = exp(-r * 0.5) * exp(-SplashAge * 0.6) * SplashPower;
    return clamp((ring + center) * (0.6 + 0.8 * valueNoise(xz * 3.0 + BrineTime)), 0.0, 1.0);
}

float ridge(float n) {
    float r = abs(abs(n * 2.0 - 1.0) * 2.0 - 1.0);
    return r * r;
}

float causticWeb(vec2 p, float large, float t) {
    const mat2 turn = mat2(-0.7374, -0.6755, 0.6755, -0.7374);
    const vec2 sizes[3] = vec2[](vec2(0.55, 1.9), vec2(1.9, 0.55), vec2(1.1, 1.1));
    float sum = 0.0;
    for (int i = 0; i < 3; i++) {
        p = turn * p;
        sum += ridge(valueNoise(p * sizes[i] + large * 0.5 + t));
    }
    return exp((1.0 + 5.0 * sqrt(large)) * (sum / 3.0 - 0.5));
}

vec3 causticRGB(vec2 xz, float depth) {
    vec2 slope;
    vec3 curvature;
    waveField(xz, slope, curvature);
    float energy = swellEnergy(xz);
    vec2 shifted = xz + slope * depth * 0.7;
    float large = max(valueNoise(shifted * 0.02), 0.1);
    float t = BrineTime * 0.05 * (0.4 + energy);
    float spread = 0.004 * depth;
    vec3 web = vec3(
        causticWeb(shifted - vec2(spread, 0.0), large, t),
        causticWeb(shifted, large, t),
        causticWeb(shifted + vec2(spread, 0.0), large, t)
    );
    float strength = 0.3 + 0.7 * min(energy * 1.5, 1.0);
    return mix(vec3(0.35), min(web, vec3(4.0)) * 0.6, strength);
}

float caustic(vec2 xz, float depth) {
    return causticRGB(xz, depth).g;
}

float dustField(vec3 p) {
    vec3 q = p * 3.0 + vec3(sin(BrineTime * 0.21) * 0.6, BrineTime * 0.22, cos(BrineTime * 0.17) * 0.6);
    vec3 cell = floor(q);
    vec3 local = fract(q);
    vec3 id = mod(cell, 289.0);
    float h = hash3(id);
    if (h < 0.88) {
        return 0.0;
    }
    vec3 center = vec3(hash3(id + 1.7), hash3(id + 4.1), hash3(id + 9.3));
    float twinkle = 0.6 + 0.4 * sin(BrineTime * 2.0 + h * 60.0);
    return smoothstep(0.4, 0.0, length(local - center)) * (0.35 + (h - 0.88) * 5.0) * twinkle;
}

float shaft(vec2 xz, float depth) {
    float n = valueNoise(xz * 0.11 + vec2(BrineTime * 0.04, -BrineTime * 0.025));
    n = n * 0.65 + valueNoise(xz * 0.31 - vec2(BrineTime * 0.06, BrineTime * 0.03)) * 0.35;
    return smoothstep(0.38, 0.85, n) * (0.55 + 0.45 * clamp(caustic(xz, depth), 0.0, 1.0));
}

float henyeyGreenstein(float cosTheta, float g) {
    float g2 = g * g;
    return (1.0 - g2) / (12.566 * pow(1.0 + g2 - 2.0 * g * cosTheta, 1.5));
}

float fbm(vec2 p) {
    float amplitude = 0.5;
    float sum = 0.0;
    for (int i = 0; i < 5; i++) {
        sum += amplitude * valueNoise(p);
        p = p * 2.03 + vec2(17.0, 9.0);
        amplitude *= 0.5;
    }
    return sum;
}

vec4 stratus(vec3 d, vec3 sun, float bright) {
    if (d.y <= 0.0) {
        return vec4(0.0);
    }
    float h = 1.0 / (d.y * 0.8 + 0.3);
    vec2 uv = d.xz * h;
    vec2 wind = vec2(BrineTime * 0.010, BrineTime * 0.003);
    vec2 toSun = sun.xz * 0.12 * h;

    float coverage = fbm(uv * vec2(0.32, 0.95) + wind);
    float detail = fbm(uv * vec2(1.1, 3.0) + wind * 2.5);
    float density = smoothstep(0.40, 0.72, coverage) * (0.55 + 0.75 * detail);
    float shadowSample = smoothstep(0.40, 0.72, fbm((uv + toSun) * vec2(0.32, 0.95) + wind)) * (0.55 + 0.75 * detail);

    float high = fbm(uv * vec2(0.5, 1.5) * 0.6 + wind * 1.7 + 31.0);
    float highDensity = smoothstep(0.50, 0.80, high) * 0.45;

    float alpha = clamp(density * 0.85 + highDensity, 0.0, 0.92) * smoothstep(0.0, 0.18, d.y);
    float lit = clamp(1.0 - (shadowSample - density) * 1.8, 0.25, 1.0);
    float sunSide = pow(max(dot(d, sun), 0.0), 4.0) * smoothstep(-0.05, 0.2, sun.y);
    vec3 litCol = mix(vec3(0.55, 0.78, 0.82), vec3(1.0, 0.93, 0.82), sunSide);
    vec3 shadowCol = vec3(0.12, 0.20, 0.27);
    vec3 col = mix(shadowCol, litCol, lit) * bright;
    return vec4(col, alpha);
}

float fbm3(vec2 p) {
    float amplitude = 0.5;
    float sum = 0.0;
    for (int i = 0; i < 3; i++) {
        sum += amplitude * valueNoise(p);
        p = p * 2.03 + vec2(17.0, 9.0);
        amplitude *= 0.5;
    }
    return sum;
}

float volumeNoise(vec3 p) {
    vec3 i = floor(p);
    vec3 f = fract(p);
    vec3 u = f * f * (3.0 - 2.0 * f);
    return mix(
        mix(mix(hash3(i), hash3(i + vec3(1.0, 0.0, 0.0)), u.x),
            mix(hash3(i + vec3(0.0, 1.0, 0.0)), hash3(i + vec3(1.0, 1.0, 0.0)), u.x), u.y),
        mix(mix(hash3(i + vec3(0.0, 0.0, 1.0)), hash3(i + vec3(1.0, 0.0, 1.0)), u.x),
            mix(hash3(i + vec3(0.0, 1.0, 1.0)), hash3(i + vec3(1.0, 1.0, 1.0)), u.x), u.y),
        u.z);
}

const float CLOUD_BASE = 160.0;
const float CLOUD_THICK = 55.0;

float cumulusDensity(vec3 p, bool detailed) {
    float h = clamp((p.y - CLOUD_BASE) / CLOUD_THICK, 0.0, 1.0);
    float profile = smoothstep(0.0, 0.18, h) * smoothstep(1.0, 0.5, h);
    vec2 wind = vec2(BrineTime * 0.9, BrineTime * 0.35);
    float shape = smoothstep(0.50, 0.70, fbm3((p.xz + wind) * 0.0115));
    float density = shape * profile;
    if (detailed && density > 0.0) {
        float detail = volumeNoise(p * 0.07 + vec3(wind.x * 0.05, 0.0, wind.y * 0.05)) * 0.65 + volumeNoise(p * 0.17) * 0.35;
        density = max(density - detail * 0.5 * (1.0 - 0.5 * density), 0.0);
    }
    return density;
}

vec4 cumulus(vec3 origin, vec3 d, vec3 sun, vec3 lightCol, vec3 ambient, int steps) {
    if (d.y < 0.03 || origin.y > CLOUD_BASE + CLOUD_THICK) {
        return vec4(0.0);
    }
    float t0 = max((CLOUD_BASE - origin.y) / d.y, 0.0);
    float t1 = (CLOUD_BASE + CLOUD_THICK - origin.y) / d.y;
    float len = min(t1 - t0, 260.0);
    if (len <= 0.0) {
        return vec4(0.0);
    }
    float dt = len / float(steps);
    float jitter = interleavedNoise(gl_FragCoord.xy);
    float phase = 0.3 + henyeyGreenstein(dot(d, sun), 0.55) * 2.0;
    float transmittance = 1.0;
    vec3 light = vec3(0.0);
    for (int i = 0; i < steps; i++) {
        vec3 p = origin + d * (t0 + (float(i) + jitter) * dt);
        float density = cumulusDensity(p, true);
        if (density > 0.004) {
            float shadow = 0.0;
            for (int j = 0; j < 3; j++) {
                shadow += cumulusDensity(p + sun * (float(j) + 1.0) * 9.0, false);
            }
            float powder = 1.0 - exp(-8.0 * density);
            float height = clamp((p.y - CLOUD_BASE) / CLOUD_THICK, 0.0, 1.0);
            vec3 direct = lightCol * exp(-shadow * 1.4) * (0.55 + 0.45 * powder) * phase;
            vec3 indirect = ambient * (0.45 + 0.55 * height);
            float alpha = 1.0 - exp(-density * dt * 0.09);
            light += transmittance * alpha * (direct + indirect);
            transmittance *= 1.0 - alpha;
            if (transmittance < 0.02) {
                break;
            }
        }
    }
    float fade = exp(-t0 * 0.0035);
    return vec4(light * fade, (1.0 - transmittance) * fade);
}

vec3 spaceSky(vec3 origin, vec3 d, int steps) {
    float up = clamp(d.y, -1.0, 1.0);
    float bright = mix(0.1, 1.0, Daylight);
    vec3 horizon = WATER_SHALLOW * 0.9 * bright;
    vec3 zenith = vec3(0.01, 0.02, 0.06) * mix(0.6, 1.0, Daylight);
    vec3 col = mix(horizon, zenith, smoothstep(-0.05, 0.75, up));
    col = mix(col, horizon * 0.45, smoothstep(0.0, -0.4, up));

    float nebula = valueNoise(d.xz / (abs(d.y) + 0.6) * 1.5 + 3.0) * valueNoise(d.xy * 4.0 + d.zz * 2.0);
    vec3 nebulaTint = mix(vec3(0.05, 0.18, 0.22), vec3(0.22, 0.06, 0.20), valueNoise(d.xy * 3.0 + 9.0));
    col += nebulaTint * smoothstep(0.2, 0.7, nebula) * 0.9 * smoothstep(0.0, 0.4, up) * mix(1.0, 0.35, Daylight);

    vec3 q = d * 150.0;
    vec3 cell = floor(q);
    vec3 local = fract(q) - 0.5;
    float h = hash3(cell);
    float star = step(0.9965, h) * (h - 0.9965) / 0.0035;
    star *= smoothstep(0.45, 0.0, length(local));
    float twinkle = 0.6 + 0.4 * sin(BrineTime * 3.0 + h * 100.0);
    col += star * vec3(0.9, 0.95, 1.0) * twinkle * smoothstep(0.0, 0.3, up) * 1.6 * (1.0 - Daylight * 0.85);

    float az = atan(d.z, d.x);
    float curtain = smoothstep(0.22, 0.0, abs(d.y - 0.42 - 0.08 * sin(az * 3.0 + BrineTime * 0.15)));
    float rays = 0.5 + 0.5 * sin(az * 11.0 + BrineTime * 0.4 + 3.0 * sin(az * 4.0 + BrineTime * 0.1));
    col += vec3(0.08, 0.75, 0.55) * curtain * rays * 0.28 * mix(1.0, 0.4, Daylight);

    vec3 sun = normalize(SunDir);
    float sunCos = dot(d, sun);
    float sunUp = smoothstep(-0.08, 0.04, sun.y);
    col += (smoothstep(0.9994, 0.9998, sunCos) * vec3(1.0, 0.96, 0.82) * 7.0
            + pow(max(sunCos, 0.0), 96.0) * vec3(0.45, 0.85, 0.95) * 0.9
            + pow(max(sunCos, 0.0), 8.0) * vec3(0.10, 0.28, 0.32) * 0.5) * sunUp;
    float moonCos = dot(d, -sun);
    float moonUp = smoothstep(-0.08, 0.04, -sun.y);
    col += (smoothstep(0.9990, 0.9994, moonCos) * vec3(0.62, 0.76, 0.92) * 1.3
            + pow(max(moonCos, 0.0), 64.0) * vec3(0.22, 0.38, 0.55) * 0.5) * moonUp;

    vec3 pd = normalize(PLANET_DIR);
    float cosAng = clamp(dot(d, pd), -1.0, 1.0);
    float ang = acos(cosAng);
    float radius = 0.30;
    if (ang < radius + 0.08) {
        float depthFactor = sqrt(max(1.0 - (ang / radius) * (ang / radius), 0.0));
        vec3 tangent = normalize(d - pd * cosAng + vec3(1e-6));
        vec3 nrm = normalize(pd * depthFactor + tangent * (ang / radius));
        float lambert = clamp(dot(nrm, sun) * 0.95 + 0.08, 0.04, 1.0);
        float bands = 0.5 + 0.5 * sin(nrm.y * 14.0 + valueNoise(nrm.xz * 4.0 + BrineTime * 0.02) * 3.0);
        vec3 surface = mix(vec3(0.05, 0.28, 0.32), vec3(0.58, 0.28, 0.46), bands) * lambert * 1.5;
        col = mix(col, surface, smoothstep(radius, radius - 0.01, ang));
        float halo = smoothstep(radius + 0.08, radius, ang);
        col += vec3(0.10, 0.36, 0.46) * halo * halo * 0.7 * mix(0.4, 1.0, Daylight);
    }

    vec4 high = stratus(d, sun, mix(0.16, 1.0, Daylight));
    col = mix(col, high.rgb, high.a);

    float sunUpLight = smoothstep(-0.05, 0.25, sun.y);
    float moonUpLight = smoothstep(-0.05, 0.25, -sun.y);
    vec3 lightCol = mix(vec3(1.0, 0.78, 0.6), vec3(0.95, 1.0, 1.0), clamp(sun.y, 0.0, 1.0)) * 2.2 * sunUpLight
            + vec3(0.25, 0.38, 0.55) * 0.35 * moonUpLight;
    vec3 ambient = (WATER_SHALLOW * 0.9 + vec3(0.12, 0.16, 0.22)) * mix(0.1, 1.0, Daylight);
    vec4 low = cumulus(origin, d, sun, lightCol, ambient, steps);
    return col * (1.0 - low.a) + low.rgb;
}

vec4 gBeamPos[4];
vec3 gBeamDir[4];

float torchLight(vec3 p, vec3 n, float absorb) {
    float lit = 0.0;
    for (int b = 0; b < 4; b++) {
        if (gBeamPos[b].w > 0.5) {
            vec3 toPoint = p - gBeamPos[b].xyz;
            float d = max(length(toPoint), 0.01);
            vec3 toward = toPoint / d;
            float spot = smoothstep(0.86, 0.96, dot(toward, gBeamDir[b]));
            float falloff = exp(-d * absorb) / (1.0 + d * d * 0.012);
            lit += spot * falloff * max(dot(n, -toward), 0.0) * 2.6;
        }
    }
    return lit;
}

vec3 torchWater(vec3 p, vec3 viewDir, vec3 n) {
    vec3 sum = vec3(0.0);
    for (int b = 0; b < 4; b++) {
        if (gBeamPos[b].w > 0.5) {
            vec3 toLight = gBeamPos[b].xyz - p;
            float d = max(length(toLight), 0.01);
            vec3 l = toLight / d;
            float spot = smoothstep(0.86, 0.96, dot(-l, gBeamDir[b]));
            float atten = 1.0 / (1.0 + d * d * 0.012);
            float spec = pow(max(dot(reflect(viewDir, n), l), 0.0), 48.0);
            float diffuse = max(dot(n, l), 0.0) * 0.3;
            sum += vec3(1.0, 0.97, 0.9) * spot * atten * (spec * 3.0 + diffuse) * 2.0;
        }
    }
    return sum;
}

void main() {
    gBeamPos = vec4[4](BeamPos0, BeamPos1, BeamPos2, BeamPos3);
    gBeamDir = vec3[4](BeamDir0, BeamDir1, BeamDir2, BeamDir3);
    float aspect = OutSize.x / OutSize.y;
    vec2 screen = texCoord - 0.5;
    screen.x *= aspect;
    bool inWater = CameraPos.y < SurfaceY;
    vec2 uv = texCoord;
    float plungeTime = 1.2 + 0.6 * clamp(SplashPower, 0.5, 1.5);
    float plunge = SplashEnter * step(SplashAge, plungeTime);
    float foam = 0.0;
    float frontGlow = 0.0;
    float dots = 0.0;
    float dropMask = 0.0;

    if (inWater) {
        vec2 field = vec2(texCoord.x * aspect, texCoord.y) * 7.0;
        vec2 wobble = vec2(valueNoise(field + vec2(BrineTime * 0.45, 0.0)),
                           valueNoise(field + vec2(5.0, -BrineTime * 0.4))) - 0.5;
        uv += wobble * (0.004 + 0.005 * swellEnergy(CameraPos.xz));

        if (plunge > 0.5) {
            float age = SplashAge;
            float front = age / plungeTime * 1.5 - 0.25;
            float tail = smoothstep(plungeTime, plungeTime * 0.7, age);
            float lifted = smoothstep(front - 0.2, front + 0.2, texCoord.y);

            vec2 drift = vec2(texCoord.x * aspect * 8.0, texCoord.y * 6.0 - age * 2.5);
            float fine = smoothstep(0.42, 0.70, fbm3(drift) + 0.1);
            float coarse = fbm3(vec2(texCoord.x * aspect * 1.6, texCoord.y * 1.2 - age * 0.8));
            vec2 streakCoord = vec2(texCoord.x * aspect * 22.0, texCoord.y * 2.4 - age * 5.0);
            float streaks = smoothstep(0.55, 0.9, valueNoise(streakCoord)) * 0.7;
            foam = clamp(fine * coarse * 1.3 + streaks * 0.4 * coarse, 0.0, 1.0) * lifted * tail;

            vec2 wobble = vec2(valueNoise(drift * 0.8 + 1.0), valueNoise(drift * 0.8 + 9.0)) - 0.5;
            uv += wobble * 0.012 * foam;
            uv = 0.5 + (uv - 0.5) * (1.0 - 0.07 * exp(-age * 6.0));

            frontGlow = exp(-pow((texCoord.y - front) / 0.06, 2.0)) * 0.5 * tail;

            vec2 bubbleCoord = vec2(texCoord.x * aspect, texCoord.y) * 36.0;
            bubbleCoord.y -= age * 7.0;
            vec2 cell = floor(bubbleCoord);
            float present = step(0.3, hash(cell + 5.3));
            vec2 center = vec2(0.5) + (vec2(hash(cell + 3.1), hash(cell + 7.7)) - 0.5) * 0.5;
            float radius = 0.12 + 0.1 * hash(cell + 1.9);
            float bubbleDistance = length(fract(bubbleCoord) - center);
            dots = smoothstep(radius, radius - 0.05, bubbleDistance) * present * foam * 0.8;
        }
    } else if (ExitWater > 0.001) {
        vec2 p = vec2(texCoord.x * aspect * 5.0, (texCoord.y - ExitWater) * 2.6);
        float n = valueNoise(p) * 0.6 + valueNoise(p * 2.3 + 9.0) * 0.4;
        n = clamp((n - 0.25) * 1.7, 0.0, 1.0);
        float threshold = (1.0 - sqrt(ExitWater)) * 0.7;
        dropMask = sqrt(min(max(n - threshold, 0.0) * (1.0 + ExitWater), 1.0)) * 0.3;
        uv = 0.5 + (uv - 0.5) * (1.0 - dropMask);
    }

    if (texture(LiveDepthSampler, texCoord).r < texture(DepthSampler, texCoord).r - 0.00002) {
        uv = texCoord;
    }
    vec2 aberration = (uv - 0.5) * 0.0035;
    vec3 scene = vec3(
        texture(DiffuseSampler, uv + aberration).r,
        texture(DiffuseSampler, uv).g,
        texture(DiffuseSampler, uv - aberration).b
    );
    vec3 col = scene;

    float depth = texture(DepthSampler, uv).r;
    bool open = depth >= 0.99999;
    bool hand = texture(LiveDepthSampler, uv).r < depth - 0.00002;

    vec4 view = InvProjMat * vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    view.xyz /= view.w;
    vec3 rel = (CameraRot * vec4(view.xyz, 0.0)).xyz;
    vec3 dir = normalize(rel);
    vec3 pos = CameraPos + rel;
    float dist = open ? FAR_DIST : min(length(rel), FAR_DIST);

    vec3 faceCross = cross(dFdx(pos), dFdy(pos));
    vec3 normal = dot(faceCross, faceCross) > 1e-10 ? normalize(faceCross) : -dir;
    if (dot(normal, -dir) < 0.0) {
        normal = -normal;
    }

    vec3 light = normalize(LightDir);
    float lightY = max(light.y, 0.2);
    vec3 sun = normalize(SunDir);
    float rayGain = smoothstep(-0.05, 0.25, sun.y) * (0.5 + 0.5 * (1.0 - clamp(sun.y, 0.0, 1.0)));
    bool submerged = CameraPos.y < SurfaceY;


    if (hand) {
        col *= submerged ? vec3(0.65, 0.88, 0.95) * mix(0.55, 1.0, Daylight) : vec3(1.0);
    } else if (submerged) {
        col *= mix(0.07, 1.0, Daylight);

        if (!open && pos.y < SurfaceY) {
            float depthBelow = SurfaceY - pos.y;
            vec2 surfaceXZ = pos.xz + light.xz * (depthBelow / lightY);
            float lit = max(dot(normal, light), 0.0) * exp(-depthBelow * 0.045) * Daylight;
            col *= 1.0 + causticRGB(surfaceXZ, depthBelow) * lit * 2.6;
        }

        if (!open) {
            float torchLit = 0.0;
            for (int b = 0; b < 4; b++) {
                if (gBeamPos[b].w > 0.5) {
                    vec3 toPoint = pos - gBeamPos[b].xyz;
                    float torchDist = max(length(toPoint), 0.01);
                    vec3 torchDir = toPoint / torchDist;
                    float spot = smoothstep(0.86, 0.96, dot(torchDir, gBeamDir[b]));
                    float falloff = exp(-torchDist * 0.045) / (1.0 + torchDist * torchDist * 0.012);
                    torchLit += spot * falloff * max(dot(normal, -torchDir), 0.0) * 2.6;
                }
            }
            col += scene * vec3(1.0, 0.97, 0.9) * torchLit;
        }

        if (!open && abs(pos.y - SurfaceY) < 0.9 && dir.y > 0.05) {
            vec2 slope;
            vec3 curvature;
            waveField(pos.xz, slope, curvature);
            vec3 up = normalize(vec3(-slope.x * 2.2, 1.0, -slope.y * 2.2));
            vec3 outRay = refract(dir, -up, 1.333);
            vec3 mirror = mix(WATER_DEEP, WATER_SHALLOW * 0.5, 0.5 + 0.5 * dir.y) * mix(0.05, 1.0, Daylight);
            if (dot(outRay, outRay) < 0.001) {
                col = mirror;
            } else {
                float escape = 1.0 - pow(1.0 - max(dot(-dir, -up), 0.0), 5.0) * 0.6;
                col = mix(mirror, spaceSky(pos, normalize(outRay), 7), escape);
            }
            col += vec3(0.8, 0.95, 1.0) * splashFoam(pos.xz) * 0.6;
        }

        float camDepth = clamp((SurfaceY - CameraPos.y) / 70.0, 0.0, 1.0);
        vec3 fogCol = mix(WATER_SHALLOW, WATER_DEEP, camDepth);
        fogCol *= mix(0.5, 1.4, dir.y * 0.5 + 0.5) * mix(0.025, 1.3, Daylight);
        vec3 transmittance = exp(-SIGMA * dist);
        col = col * transmittance + fogCol * (1.0 - transmittance);

        float marchDist = min(dist, SHAFT_DIST);
        float jitter = interleavedNoise(gl_FragCoord.xy);
        float scatter = 0.0;
        float beam = 0.0;
        float dust = 0.0;
        for (int i = 0; i < SHAFT_STEPS; i++) {
            float t = (float(i) + jitter) / float(SHAFT_STEPS) * marchDist;
            vec3 s = CameraPos + dir * t;
            float below = SurfaceY - s.y;
            if (below > 0.0) {
                vec2 sp = s.xz + light.xz * (below / lightY);
                scatter += shaft(sp, below) * exp(-below * 0.035);
            }
            float dustLight = 0.0;
            if (below > 0.0) {
                dustLight += exp(-below * 0.05) * rayGain * Daylight * 0.35;
            }
            for (int b = 0; b < 4; b++) {
                if (gBeamPos[b].w > 0.5) {
                    vec3 fromTorch = s - gBeamPos[b].xyz;
                    float along = dot(fromTorch, gBeamDir[b]);
                    if (along > 0.0) {
                        float cone = smoothstep(0.88, 0.97, along / length(fromTorch));
                        float weight = cone * exp(-along * 0.05) / (1.0 + along * 0.08);
                        beam += weight;
                        dustLight += weight * 2.2;
                    }
                }
            }
            if (dustLight > 0.001 && t < 48.0) {
                dust += dustField(s) * dustLight * (1.0 - t / 48.0);
            }
        }
        float stepLen = marchDist / float(SHAFT_STEPS);
        scatter *= stepLen * 0.06;
        beam *= stepLen * 0.05;
        float phase = henyeyGreenstein(dot(dir, light), 0.55) * 4.0 + 0.15;
        col += vec3(0.30, 0.82, 0.92) * scatter * phase * rayGain * Daylight * 1.8;
        col += vec3(0.85, 1.0, 0.95) * beam * 0.6;
        col += vec3(0.80, 0.95, 0.90) * dust * stepLen * 0.9;
    } else {
        if (open) {
            col = spaceSky(CameraPos, dir, 14);
        } else {
            col *= mix(0.1, 1.0, Daylight);
            if (abs(pos.y - SurfaceY) < 0.5 && dir.y < 0.0) {
                vec2 slope;
                vec3 curvature;
                waveField(pos.xz, slope, curvature);
                float flatten = 1.0 / (1.0 + length(rel) * 0.025);
                vec3 n = normalize(vec3(-slope.x * 2.2 * flatten, 1.0, -slope.y * 2.2 * flatten));
                vec2 bend = clamp(n.xz * 0.02 * (0.3 + 0.7 * -dir.y), vec2(-0.015), vec2(0.015));
                vec3 seen = texture(DiffuseSampler, uv + bend).rgb * mix(0.1, 1.0, Daylight);
                float fresnel = 0.02 + 0.98 * pow(1.0 - max(dot(-dir, n), 0.0), 5.0);
                vec3 mirrorDir = reflect(dir, n);
                mirrorDir.y = max(mirrorDir.y, 0.03);
                vec3 reflected = spaceSky(pos, normalize(mirrorDir), 7);
                float pathLength = 16.0 / max(-dir.y, 0.12);
                vec3 absorbed = exp(-SIGMA * 1.6 * pathLength);
                vec3 body = seen * absorbed + WATER_SHALLOW * 0.6 * mix(0.1, 1.0, Daylight) * (1.0 - absorbed);
                col = mix(body, reflected, clamp(fresnel * 1.2, 0.0, 1.0));
                col = mix(col, vec3(0.8, 0.95, 1.0) * mix(0.3, 1.0, Daylight), splashFoam(pos.xz) * 0.7);
                col += torchWater(pos, dir, n);
            } else {
                col += scene * vec3(1.0, 0.97, 0.9) * torchLight(pos, normal, 0.012);
            }
            vec3 haze = mix(WATER_SHALLOW * 0.55, vec3(0.20, 0.12, 0.30), dir.y * 0.5 + 0.5) * mix(0.05, 1.0, Daylight);
            float airT = exp(-dist * 0.0035);
            col = col * airT + haze * (1.0 - airT);
        }

        float airMax = min(dist, 50.0);
        float airJitter = interleavedNoise(gl_FragCoord.xy);
        float airBeam = 0.0;
        for (int i = 0; i < 16; i++) {
            float t = (float(i) + airJitter) / 16.0 * airMax;
            vec3 s = CameraPos + dir * t;
            for (int b = 0; b < 4; b++) {
                if (gBeamPos[b].w > 0.5) {
                    vec3 fromTorch = s - gBeamPos[b].xyz;
                    float along = dot(fromTorch, gBeamDir[b]);
                    if (along > 0.0) {
                        float cone = smoothstep(0.88, 0.97, along / length(fromTorch));
                        float weight = cone * exp(-along * 0.02) / (1.0 + along * 0.05);
                        airBeam += weight;
                    }
                }
            }
        }
        float airStep = airMax / 16.0;
        col += vec3(0.85, 1.0, 0.95) * airBeam * airStep * 0.012;

        vec4 sunClip = ScreenProj * vec4(transpose(mat3(CameraRot)) * sun, 0.0);
        if (sunClip.w > 0.0 && sun.y > -0.02) {
            vec2 sunUV = sunClip.xy / sunClip.w * 0.5 + 0.5;
            vec2 stepUV = (sunUV - uv) / float(RAY_STEPS);
            vec2 uv = uv + stepUV * interleavedNoise(gl_FragCoord.xy);
            float rays = 0.0;
            float weight = 1.0;
            for (int i = 0; i < RAY_STEPS; i++) {
                uv += stepUV;
                if (uv.x > 0.0 && uv.x < 1.0 && uv.y > 0.0 && uv.y < 1.0) {
                    rays += step(0.99999, texture(DepthSampler, uv).r) * weight;
                }
                weight *= 0.94;
            }
            float facing = pow(max(dot(dir, sun), 0.0), 3.0);
            col += vec3(0.55, 0.95, 1.0) * rays / float(RAY_STEPS) * facing * 0.8 * smoothstep(-0.02, 0.2, sun.y);
        }
    }

    vec3 foamColor = mix(vec3(0.10, 0.26, 0.32), mix(vec3(0.55, 0.85, 0.92), vec3(0.92, 1.0, 1.0), foam), Daylight);
    col = mix(col, foamColor, clamp(foam, 0.0, 1.0) * mix(0.4, 0.88, Daylight));
    col += vec3(0.6, 0.92, 1.0) * (frontGlow * mix(0.3, 1.0, Daylight) + dots * mix(0.8, 1.0, Daylight));
    col += vec3(0.8, 0.95, 1.0) * dropMask * 0.35;

    col = 1.0 - exp(-col * mix(2.0, 2.5, Daylight));
    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.15);
    col = mix(col * vec3(0.82, 0.95, 1.12), col * vec3(1.05, 1.02, 0.96), smoothstep(0.2, 0.8, luma));

    vec2 centered = screen;
    col *= mix(0.5, 1.0, smoothstep(1.15, 0.3, length(centered)));

    fragColor = vec4(max(col, vec3(0.0)), 1.0);
}
