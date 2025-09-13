#include "flywheel:util/matrix.glsl"

void flw_instanceVertex(in FlwInstance instance) {
    flw_vertexPos = instance.pose * flw_vertexPos;
    flw_vertexNormal = mat3(transpose(inverse(instance.pose))) * flw_vertexNormal;

    vec2 scroll = fract(instance.speed * flw_renderTicks + instance.offset) * instance.scale;

    flw_vertexTexCoord = flw_vertexTexCoord + instance.diff + scroll;
    flw_vertexOverlay = instance.overlay;
    flw_vertexLight = max(vec2(instance.light) / 256., flw_vertexLight);
}
