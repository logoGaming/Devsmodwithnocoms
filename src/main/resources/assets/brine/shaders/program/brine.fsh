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

float caustic(vec2 xz, float depth) {
    float a = 0.0;
    float b = 0.0;
    float c = 0.0;
    for (int i = 0; i < 7; i++) {
        float fi = float(i);
        float ang = fi * 2.399963 + 0.7;
        vec2 wave = vec2(cos(ang), sin(ang));
        float freq = 0.55 + 0.34 * fi;
        float speed = 0.8 + 0.19 * fi;
        float amp = 0.34 / (1.0 + fi * 0.75);
        float phase = dot(xz, wave) * freq + BrineTime * speed + fi * 1.7;
        float w = -amp * freq * freq * sin(phase);
        a += w * wave.x * wave.x;
        b += w * wave.x * wave.y;
        c += w * wave.y * wave.y;
    }
    float f = depth * 0.25;
    float det = (1.0 + f * a) * (1.0 + f * c) - f * f * b * b;
    float focus = 1.0 / max(abs(det), 0.07);
    return pow(clamp((focus - 0.8) / 4.5, 0.0, 1.0), 1.25) * 2.4 + 0.06;
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

vec3 spaceSky(vec3 d) {
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
    return col;
}

void main() {
    vec2 aberration = (texCoord - 0.5) * 0.0035;
    vec3 scene = vec3(
        texture(DiffuseSampler, texCoord + aberration).r,
        texture(DiffuseSampler, texCoord).g,
        texture(DiffuseSampler, texCoord - aberration).b
    );
    vec3 col = scene;

    float depth = texture(DepthSampler, texCoord).r;
    bool open = depth >= 0.99999;
    bool hand = texture(LiveDepthSampler, texCoord).r < depth - 0.00002;

    vec4 view = InvProjMat * vec4(texCoord * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
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

    vec4 beamPos[4] = vec4[4](BeamPos0, BeamPos1, BeamPos2, BeamPos3);
    vec3 beamDir[4] = vec3[4](BeamDir0, BeamDir1, BeamDir2, BeamDir3);

    if (hand) {
        col *= submerged ? vec3(0.65, 0.88, 0.95) * mix(0.55, 1.0, Daylight) : vec3(1.0);
    } else if (submerged) {
        col *= mix(0.07, 1.0, Daylight);

        if (!open && pos.y < SurfaceY) {
            float depthBelow = SurfaceY - pos.y;
            vec2 surfaceXZ = pos.xz + light.xz * (depthBelow / lightY);
            float lit = max(dot(normal, light), 0.0) * exp(-depthBelow * 0.045) * Daylight;
            col *= 1.0 + caustic(surfaceXZ, depthBelow) * lit * 2.6;
        }

        if (!open) {
            float torchLit = 0.0;
            for (int b = 0; b < 4; b++) {
                if (beamPos[b].w > 0.5) {
                    vec3 toPoint = pos - beamPos[b].xyz;
                    float torchDist = max(length(toPoint), 0.01);
                    vec3 torchDir = toPoint / torchDist;
                    float spot = smoothstep(0.86, 0.96, dot(torchDir, beamDir[b]));
                    float falloff = exp(-torchDist * 0.045) / (1.0 + torchDist * torchDist * 0.012);
                    torchLit += spot * falloff * max(dot(normal, -torchDir), 0.0) * 2.6;
                }
            }
            col += scene * vec3(1.0, 0.97, 0.9) * torchLit;
        }

        if (!open && abs(pos.y - SurfaceY) < 0.9 && dir.y > 0.05) {
            float window = smoothstep(0.62, 0.72, dir.y);
            vec3 brightWindow = vec3(0.55, 0.95, 1.0) * (0.7 + 0.6 * caustic(pos.xz * 0.5, 6.0)) * mix(0.05, 1.0, Daylight);
            vec3 mirror = vec3(0.015, 0.06, 0.09) * mix(0.1, 1.0, Daylight);
            col = mix(col, mix(mirror, brightWindow, window), 0.8);
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
        for (int i = 0; i < SHAFT_STEPS; i++) {
            float t = (float(i) + jitter) / float(SHAFT_STEPS) * marchDist;
            vec3 s = CameraPos + dir * t;
            float below = SurfaceY - s.y;
            if (below > 0.0) {
                vec2 sp = s.xz + light.xz * (below / lightY);
                scatter += shaft(sp, below) * exp(-below * 0.035);
            }
            for (int b = 0; b < 4; b++) {
                if (beamPos[b].w > 0.5) {
                    vec3 fromTorch = s - beamPos[b].xyz;
                    float along = dot(fromTorch, beamDir[b]);
                    if (along > 0.0) {
                        float cone = smoothstep(0.88, 0.97, along / length(fromTorch));
                        beam += cone * exp(-along * 0.05) / (1.0 + along * 0.08);
                    }
                }
            }
        }
        float stepLen = marchDist / float(SHAFT_STEPS);
        scatter *= stepLen * 0.06;
        beam *= stepLen * 0.05;
        float phase = henyeyGreenstein(dot(dir, light), 0.55) * 4.0 + 0.15;
        col += vec3(0.30, 0.82, 0.92) * scatter * phase * rayGain * Daylight * 1.8;
        col += vec3(0.85, 1.0, 0.95) * beam * 0.6;
    } else {
        if (open) {
            col = spaceSky(dir);
        } else {
            col *= mix(0.1, 1.0, Daylight);
            if (abs(pos.y - SurfaceY) < 0.5 && dir.y < 0.0) {
                vec2 w = pos.xz * 0.9 + vec2(BrineTime * 0.35, BrineTime * 0.22);
                vec2 slope = vec2(valueNoise(w) - valueNoise(w + vec2(5.2, 1.3)),
                                  valueNoise(w + vec2(2.7, 8.1)) - valueNoise(w + vec2(9.4, 4.4)));
                vec3 n = normalize(vec3(slope.x * 0.22, 1.0, slope.y * 0.22));
                float fresnel = 0.04 + 0.96 * pow(1.0 - max(dot(-dir, n), 0.0), 5.0);
                vec3 reflected = spaceSky(reflect(dir, n));
                vec3 body = col * vec3(0.55, 0.85, 0.95) + WATER_DEEP * 2.0 * mix(0.1, 1.0, Daylight);
                col = mix(body, reflected, clamp(fresnel * 1.2, 0.0, 1.0));
            }
            vec3 haze = mix(WATER_SHALLOW * 0.55, vec3(0.20, 0.12, 0.30), dir.y * 0.5 + 0.5) * mix(0.05, 1.0, Daylight);
            float airT = exp(-dist * 0.0035);
            col = col * airT + haze * (1.0 - airT);
        }

        vec4 sunClip = ScreenProj * vec4(transpose(mat3(CameraRot)) * sun, 0.0);
        if (sunClip.w > 0.0 && sun.y > -0.02) {
            vec2 sunUV = sunClip.xy / sunClip.w * 0.5 + 0.5;
            vec2 stepUV = (sunUV - texCoord) / float(RAY_STEPS);
            vec2 uv = texCoord + stepUV * interleavedNoise(gl_FragCoord.xy);
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

    col = 1.0 - exp(-col * mix(2.0, 2.5, Daylight));
    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.15);
    col = mix(col * vec3(0.82, 0.95, 1.12), col * vec3(1.05, 1.02, 0.96), smoothstep(0.2, 0.8, luma));

    vec2 centered = (texCoord - 0.5) * vec2(OutSize.x / OutSize.y, 1.0);
    col *= mix(0.5, 1.0, smoothstep(1.15, 0.3, length(centered)));

    fragColor = vec4(max(col, vec3(0.0)), 1.0);
}
