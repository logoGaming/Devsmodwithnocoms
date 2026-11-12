#version 150

uniform sampler2D DiffuseSampler;

uniform vec2 OutSize;
uniform vec3 CameraPos;
uniform vec3 SunDir;
uniform float BrineTime;
uniform float SurfaceY;
uniform float ExitWater;
uniform float Daylight;
uniform float CalmFloor;
uniform float Drowning;

in vec2 texCoord;

out vec4 fragColor;

float hash(vec2 p) {
    return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453);
}

float valueNoise(vec2 p) {
    vec2 i = floor(p);
    vec2 f = fract(p);
    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
               mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float interleavedNoise(vec2 p) {
    return fract(52.9829189 * fract(dot(p, vec2(0.06711056, 0.00583715))));
}

float swellEnergy(vec2 xz) {
    float gust = smoothstep(0.36, 0.64, valueNoise(vec2(BrineTime * 0.035, 11.0)) * 0.6 + valueNoise(vec2(BrineTime * 0.011, 41.0)) * 0.4);
    float patch = smoothstep(0.25, 0.75, valueNoise(xz * 0.03 + vec2(BrineTime * 0.02, 5.0)));
    return CalmFloor + (1.0 - CalmFloor) * gust * (0.3 + 0.7 * patch);
}

void main() {
    float aspect = OutSize.x / OutSize.y;
    vec2 screen = texCoord - 0.5;
    screen.x *= aspect;
    bool inWater = CameraPos.y < SurfaceY;
    vec2 uv = texCoord;
    float dropMask = 0.0;

    if (inWater) {
        vec2 field = vec2(texCoord.x * aspect, texCoord.y) * 7.0;
        vec2 wobble = vec2(valueNoise(field + vec2(BrineTime * 0.45, 0.0)),
                           valueNoise(field + vec2(5.0, -BrineTime * 0.4))) - 0.5;
        uv += wobble * (0.004 + 0.005 * swellEnergy(CameraPos.xz));
    } else if (ExitWater > 0.001) {
        vec2 p = vec2(texCoord.x * aspect * 5.0, (texCoord.y - ExitWater) * 2.6);
        float n = valueNoise(p) * 0.6 + valueNoise(p * 2.3 + 9.0) * 0.4;
        n = clamp((n - 0.25) * 1.7, 0.0, 1.0);
        float threshold = (1.0 - sqrt(ExitWater)) * 0.7;
        dropMask = sqrt(min(max(n - threshold, 0.0) * (1.0 + ExitWater), 1.0)) * 0.3;
        uv = 0.5 + (uv - 0.5) * (1.0 - dropMask);
    }

    vec2 aberration = (uv - 0.5) * 0.0012;
    vec3 col = vec3(
        texture(DiffuseSampler, uv + aberration).r,
        texture(DiffuseSampler, uv).g,
        texture(DiffuseSampler, uv - aberration).b
    );

    float sunHeight = normalize(SunDir).y;
    float twilight = clamp(1.0 - abs(sunHeight) * 4.0, 0.0, 1.0);
    vec3 dropTint = mix(vec3(0.10, 0.30, 0.38), vec3(0.85, 0.97, 1.0), Daylight);
    dropTint = mix(dropTint, vec3(1.0, 0.72, 0.5), twilight * 0.5);
    col += dropTint * dropMask * mix(0.06, 0.2, Daylight);

    float luma = dot(col, vec3(0.299, 0.587, 0.114));
    col = mix(vec3(luma), col, 1.15);
    col = mix(col * vec3(0.82, 0.95, 1.12), col * vec3(1.05, 1.02, 0.96), smoothstep(0.2, 0.8, luma));
    col *= mix(0.5, 1.0, smoothstep(1.15, 0.3, length(screen)));

    float tunnel = mix(1.0, smoothstep(1.3 * (1.0 - Drowning) + 0.05, 0.15, length(screen)), Drowning);
    col = mix(vec3(dot(col, vec3(0.299, 0.587, 0.114))), col, 1.0 - 0.6 * Drowning);
    col *= tunnel * pow(1.0 - Drowning, 1.4);

    col += (interleavedNoise(gl_FragCoord.xy + 17.0) - 0.5) / 255.0;
    fragColor = vec4(max(col, vec3(0.0)), 1.0);
}
