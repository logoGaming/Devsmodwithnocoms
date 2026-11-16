#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;
in vec3 normalYeee;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0);
    if (color.a < 0.1) {
        discard;
    }
    color *= vertexColor * ColorModulator;
    color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
    color *= lightMapColor;
    color.b += 0.1;
    color.g += 0.05;
    color.rgb -= 0.1;
    color.rgb += dot(normalYeee, normalize(vec3(1.0, 0.2, 0.2))) / 2.0;
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}