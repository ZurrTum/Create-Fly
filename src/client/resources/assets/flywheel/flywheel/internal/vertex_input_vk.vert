// Vulkan variant of vertex_input.vert.
//
// The OpenGL variant declares overlay/light as floating-point `in vec2` even though the
// VertexFormat stores them as SHORT / UNSIGNED_SHORT. That is a deliberate workaround for
// buggy OpenGL drivers, and it costs an int -> float -> int round trip:
//
//     // Integer vertex attributes explode on some drivers for some draw calls, so get the
//     // driver to cast the int to a float so we can cast it back to an int and reliably
//     // get a sane value.
//     flw_vertexOverlay = ivec2(_flw_aOverlay);
//
// Vulkan has no such problem -- integer vertex formats are first-class there, and
// VulkanMod derives the VkVertexInputFormat directly from the VertexFormat. Declaring
// `in vec2` therefore does not just cost a round trip, it mismatches the format outright:
//
//     overlay  SHORT          -> VK_FORMAT_R16G16_SINT   but declared `in vec2`
//     light    UNSIGNED_SHORT -> VK_FORMAT_R16G16_UINT   but declared `in vec2`
//
// Declaring ivec2/uvec2 matches the format exactly and is exact rather than lossy.
// vertex_input.vert is left untouched so the OpenGL backend keeps its workaround.
//
// Locations are assigned by VkBindings.VERTEX_INPUT_LOCATIONS, not written here.

in vec3 _flw_aPos;
in vec4 _flw_aColor;
in vec2 _flw_aTexCoord;
in ivec2 _flw_aOverlay;
in uvec2 _flw_aLight;
in vec3 _flw_aNormal;

void _flw_layoutVertex() {
    flw_vertexPos = vec4(_flw_aPos, 1.0);
    flw_vertexColor = _flw_aColor;
    flw_vertexTexCoord = _flw_aTexCoord;
    // Already the right integer type, so this is a plain assignment -- no cast, and no
    // truncation of a value that was widened to float and has to be truncated back.
    flw_vertexOverlay = _flw_aOverlay;
    // Cast to float before dividing. `uvec2 / 256.0` would integer-divide and zero out.
    flw_vertexLight = vec2(_flw_aLight) / 256.0;
    flw_vertexNormal = _flw_aNormal;
}