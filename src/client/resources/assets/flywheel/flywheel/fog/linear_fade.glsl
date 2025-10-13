float linearFogValue(float vertexDistance, float fogStart, float fogEnd) {
    if (vertexDistance <= fogStart) {
        return 0.0;
    } else if (vertexDistance >= fogEnd) {
        return 1.0;
    }

    return (vertexDistance - fogStart) / (fogEnd - fogStart);
}

float totalFogValue(float sphericalVertexDistance, float cylindricalVertexDistance, float environmentalStart, float environmantalEnd, float renderDistanceStart, float renderDistanceEnd) {
    return max(linearFogValue(sphericalVertexDistance, environmentalStart, environmantalEnd), linearFogValue(cylindricalVertexDistance, renderDistanceStart, renderDistanceEnd));
}

vec4 linearFogFade(vec4 color, float sphericalVertexDistance, float cylindricalVertexDistance, float environmentalStart, float environmantalEnd, float renderDistanceStart, float renderDistanceEnd) {
    float fade = 1.0f - totalFogValue(sphericalVertexDistance, cylindricalVertexDistance, environmentalStart, environmantalEnd, renderDistanceStart, renderDistanceEnd);
    return color * fade;
}

vec4 flw_fogFilter(vec4 color) {
    return linearFogFade(color, flw_sphericalDistance, flw_cylindricalDistance, flw_fogEnvironmentalStart, flw_fogEnvironmentalEnd, flw_fogRenderDistanceStart, flw_fogRenderDistanceEnd);
}
