#version 150

uniform sampler2D DiffuseSampler;
uniform sampler2D DepthSampler;
uniform sampler2D LiveDepthSampler;

uniform vec2 OutSize;
uniform mat4 InvProjMat;
uniform mat4 CameraRot;
uniform vec3 CameraPos;
uniform float Time;
uniform float SurfaceY;
uniform vec3 LightDir;
uniform float Daylight;

in vec2 texCoord;

out vec4 fragColor;

const float FAR_DIST = 192.0;
const float SHAFT_DIST = 110.0;
const int SHAFT_STEPS = 28;
const vec3 SIGMA = vec3(0.075, 0.032, 0.022);
const vec3 PLANET_DIR = vec3(-0.45, 0.38, 0.80);
const vec3 PLANET_SUN = vec3(0.80, 0.25, 0.40);

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

float causticTile(vec2 xz, float t) {
    vec2 p = mod(xz, 6.28318) - 250.0;
    vec2 i = p;
    float c = 1.0;
    for (int n = 0; n < 5; n++) {
        float tt = t * (1.0 - (3.5 / float(n + 1)));
        i = p + vec2(cos(tt - i.x) + sin(tt + i.y), sin(tt - i.y) + cos(tt + i.x));
        c += 1.0 / length(vec2(p.x / (sin(i.x + tt) / 0.005), p.y / (cos(i.y + tt) / 0.005)));
    }
    c /= 5.0;
    c = 1.17 - pow(c, 1.4);
    return pow(abs(c), 8.0);
}

float caustic(vec2 xz) {
    float t = Time * 0.5 + 23.0;
    float a = causticTile(xz * 0.9, t);
    float b = causticTile(xz * 0.37 + 17.0, t * 0.7);
    return clamp((a + b) * 0.5, 0.0, 2.0);
}

float shaft(vec2 xz) {
    float n = valueNoise(xz * 0.11 + vec2(Time * 0.04, -Time * 0.025));
    n = n * 0.65 + valueNoise(xz * 0.31 - vec2(Time * 0.06, Time * 0.03)) * 0.35;
    return smoothstep(0.38, 0.85, n) * (0.55 + 0.45 * clamp(caustic(xz), 0.0, 1.0));
}

float henyeyGreenstein(float cosTheta, float g) {
    float g2 = g * g;
    return (1.0 - g2) / (12.566 * pow(1.0 + g2 - 2.0 * g * cosTheta, 1.5));
}

vec3 spaceSky(vec3 d) {
    float up = clamp(d.y, -1.0, 1.0);
    vec3 horizon = vec3(0.10, 0.22, 0.30);
    vec3 zenith = vec3(0.01, 0.015, 0.05);
    vec3 col = mix(horizon, zenith, smoothstep(-0.05, 0.75, up));
    col = mix(col, horizon * 0.45, smoothstep(0.0, -0.4, up));

    float nebula = valueNoise(d.xz / (abs(d.y) + 0.6) * 1.5 + 3.0) * valueNoise(d.xy * 4.0 + d.zz * 2.0);
    vec3 nebulaTint = mix(vec3(0.05, 0.18, 0.22), vec3(0.22, 0.06, 0.20), valueNoise(d.xy * 3.0 + 9.0));
    col += nebulaTint * smoothstep(0.2, 0.7, nebula) * 0.9 * smoothstep(0.0, 0.4, up);

    vec3 q = d * 150.0;
    vec3 cell = floor(q);
    vec3 local = fract(q) - 0.5;
    float h = hash3(cell);
    float star = step(0.9965, h) * (h - 0.9965) / 0.0035;
    star *= smoothstep(0.45, 0.0, length(local));
    float twinkle = 0.6 + 0.4 * sin(Time * 3.0 + h * 100.0);
    col += star * vec3(0.9, 0.95, 1.0) * twinkle * smoothstep(0.0, 0.3, up) * 1.6;

    float az = atan(d.z, d.x);
    float curtain = smoothstep(0.22, 0.0, abs(d.y - 0.42 - 0.08 * sin(az * 3.0 + Time * 0.15)));
    float rays = 0.5 + 0.5 * sin(az * 11.0 + Time * 0.4 + 3.0 * sin(az * 4.0 + Time * 0.1));
    col += vec3(0.08, 0.75, 0.55) * curtain * rays * 0.28;

    vec3 pd = normalize(PLANET_DIR);
    float cosAng = clamp(dot(d, pd), -1.0, 1.0);
    float ang = acos(cosAng);
    float radius = 0.30;
    if (ang < radius + 0.08) {
        float depthFactor = sqrt(max(1.0 - (ang / radius) * (ang / radius), 0.0));
        vec3 tangent = normalize(d - pd * cosAng + vec3(1e-6));
        vec3 nrm = normalize(pd * depthFactor + tangent * (ang / radius));
        float lambert = clamp(dot(nrm, normalize(PLANET_SUN)) * 0.9 + 0.1, 0.0, 1.0);
        float bands = 0.5 + 0.5 * sin(nrm.y * 14.0 + valueNoise(nrm.xz * 4.0 + Time * 0.02) * 3.0);
        vec3 surface = mix(vec3(0.05, 0.28, 0.32), vec3(0.58, 0.28, 0.46), bands) * lambert * 1.5;
        col = mix(col, surface, smoothstep(radius, radius - 0.01, ang));
        float halo = smoothstep(radius + 0.08, radius, ang);
        col += vec3(0.10, 0.36, 0.46) * halo * halo * 0.7;
    }
    return col;
}

void main() {
    vec2 aberration = (texCoord - 0.5) * 0.0035;
    vec3 col = vec3(
        texture(DiffuseSampler, texCoord + aberration).r,
        texture(DiffuseSampler, texCoord).g,
        texture(DiffuseSampler, texCoord - aberration).b
    );

    float depth = texture(DepthSampler, texCoord).r;
    bool open = depth >= 0.99999;
    bool hand = texture(LiveDepthSampler, texCoord).r < depth - 0.00002;

    vec4 view = InvProjMat * vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    view.xyz /= view.w;
    vec3 rel = (CameraRot * vec4(view.xyz, 0.0)).xyz;
    vec3 dir = normalize(rel);
    vec3 pos = CameraPos + rel;
    float dist = open ? FAR_DIST : min(length(rel), FAR_DIST);

    vec3 light = normalize(LightDir);
    float lightY = max(light.y, 0.2);
    bool submerged = CameraPos.y < SurfaceY;

    if (hand) {
        col *= submerged ? vec3(0.65, 0.88, 0.95) * mix(0.45, 1.0, Daylight) : vec3(1.0);
    } else if (submerged) {
        if (!open && pos.y < SurfaceY) {
            vec3 n = normalize(cross(dFdx(pos), dFdy(pos)));
            if (dot(n, -dir) < 0.0) {
                n = -n;
            }
            float depthBelow = SurfaceY - pos.y;
            vec2 surfaceXZ = pos.xz + light.xz * (depthBelow / lightY);
            float lit = max(dot(n, light), 0.0) * exp(-depthBelow * 0.045) * Daylight;
            col *= 1.0 + caustic(surfaceXZ) * lit * 3.2;
        }

        if (!open && abs(pos.y - SurfaceY) < 0.9 && dir.y > 0.05) {
            float window = smoothstep(0.62, 0.72, dir.y);
            vec3 bright = vec3(0.55, 0.95, 1.0) * (0.7 + 0.6 * caustic(pos.xz * 0.5));
            vec3 mirror = vec3(0.015, 0.06, 0.09);
            col = mix(col, mix(mirror, bright, window), 0.8);
        }

        float camDepth = clamp((SurfaceY - CameraPos.y) / 70.0, 0.0, 1.0);
        vec3 fogCol = mix(vec3(0.07, 0.34, 0.40), vec3(0.005, 0.03, 0.07), camDepth);
        fogCol *= mix(0.5, 1.4, dir.y * 0.5 + 0.5) * mix(0.22, 1.0, Daylight);
        vec3 transmittance = exp(-SIGMA * dist);
        col = col * transmittance + fogCol * (1.0 - transmittance);

        float marchDist = min(dist, SHAFT_DIST);
        float jitter = interleavedNoise(gl_FragCoord.xy);
        float scatter = 0.0;
        for (int i = 0; i < SHAFT_STEPS; i++) {
            float t = (float(i) + jitter) / float(SHAFT_STEPS) * marchDist;
            vec3 s = CameraPos + dir * t;
            float below = SurfaceY - s.y;
            if (below > 0.0) {
                vec2 sp = s.xz + light.xz * (below / lightY);
                scatter += shaft(sp) * exp(-below * 0.035);
            }
        }
        scatter *= marchDist / float(SHAFT_STEPS) * 0.045;
        float phase = henyeyGreenstein(dot(dir, light), 0.55) * 4.0 + 0.15;
        col += vec3(0.30, 0.82, 0.92) * scatter * phase * Daylight;
    } else {
        if (open) {
            col = spaceSky(dir) * mix(0.55, 1.0, Daylight);
        } else {
            if (abs(pos.y - SurfaceY) < 0.5 && dir.y < 0.0) {
                vec2 w = pos.xz * 0.9 + vec2(Time * 0.35, Time * 0.22);
                vec2 slope = vec2(valueNoise(w) - valueNoise(w + vec2(5.2, 1.3)),
                                  valueNoise(w + vec2(2.7, 8.1)) - valueNoise(w + vec2(9.4, 4.4)));
                vec3 n = normalize(vec3(slope.x * 0.22, 1.0, slope.y * 0.22));
                float fresnel = 0.04 + 0.96 * pow(1.0 - max(dot(-dir, n), 0.0), 5.0);
                vec3 reflected = spaceSky(reflect(dir, n));
                vec3 body = col * vec3(0.55, 0.85, 0.95) + vec3(0.01, 0.07, 0.09);
                col = mix(body, reflected, clamp(fresnel * 1.2, 0.0, 1.0));
            }
            vec3 haze = mix(vec3(0.05, 0.13, 0.20), vec3(0.20, 0.12, 0.30), dir.y * 0.5 + 0.5) * 0.9 * mix(0.35, 1.0, Daylight);
            float airT = exp(-dist * 0.0035);
            col = col * airT + haze * (1.0 - airT);
        }
    }

    col = 1.0 - exp(-col * 1.9);
    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.15);
    col = mix(col * vec3(0.82, 0.95, 1.12), col * vec3(1.05, 1.02, 0.96), smoothstep(0.2, 0.8, luma));

    vec2 centered = (texCoord - 0.5) * vec2(OutSize.x / OutSize.y, 1.0);
    col *= mix(0.5, 1.0, smoothstep(1.15, 0.3, length(centered)));

    fragColor = vec4(col, 1.0);
}
