#version 330
#extension GL_ARB_separate_shader_objects : require

#include <minecraft:fog.glsl>
#include <minecraft:dynamictransforms.glsl>
#include <minecraft:projection.glsl>
#include <minecraft:sample_lightmap.glsl>

layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
#if defined(OVERWORLD) || defined(NETHER)
layout(location = 3) in ivec2 UV1;
layout(location = 4) in ivec2 UV2;
layout(location = 5) in vec3 Normal;
#else
layout(location = 3) in ivec2 UV2;
#endif

#ifndef OIT_ALPHA_ONLY
uniform sampler2D Sampler2;
#endif

layout(location = 0) out float sphericalVertexDistance;
layout(location = 1) out float cylindricalVertexDistance;
layout(location = 2) out vec4 vertexColor;
#ifndef OIT_ALPHA_ONLY
layout(location = 3) out vec4 lightMapColor;
#endif
layout(location = 4) out vec2 texCoord0;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    sphericalVertexDistance = fog_spherical_distance(Position);
    cylindricalVertexDistance = fog_cylindrical_distance(Position);

    vertexColor = Color;
    #ifdef NETHER_LIGHT
    vertexColor.rgb *= 0.9;
    #elif defined(OVERWORLD)
    vec3 normal = normalize(Normal);
    vec3 n2 = normal * normal * vec3(0.6, 0.25, 0.8);
    vertexColor.rgb *= min(n2.x + n2.y * (3.0 + normal.y) + n2.z, 1.0);
    #elif defined(NETHER)
    vec3 normal = normalize(Normal);
    vec3 n2 = normal * normal * vec3(0.6, 0.9, 0.8);
    vertexColor.rgb *= min(n2.x + n2.y + n2.z, 1.0);
    #endif

    #ifndef OIT_ALPHA_ONLY
    lightMapColor = sample_lightmap(Sampler2, UV2);
    #endif

    texCoord0 = UV0;
}
