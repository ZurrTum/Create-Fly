package com.zurrtum.create.client.flywheel.backend;

import com.zurrtum.create.client.flywheel.api.layout.FloatRepr;
import com.zurrtum.create.client.flywheel.api.layout.Layout;
import com.zurrtum.create.client.flywheel.api.layout.LayoutBuilder;
import com.zurrtum.create.client.flywheel.backend.gl.array.VertexAttribute;
import com.zurrtum.create.client.flywheel.lib.util.ResourceUtil;
import com.zurrtum.create.client.flywheel.lib.vertex.FullVertexView;
import com.zurrtum.create.client.flywheel.lib.vertex.VertexView;
import net.minecraft.resources.Identifier;

import java.util.List;

public final class InternalVertex {
    public static final Layout LAYOUT = LayoutBuilder.create().vector("position", FloatRepr.FLOAT, 3)
        .vector("color", FloatRepr.NORMALIZED_UNSIGNED_BYTE, 4).vector("tex", FloatRepr.FLOAT, 2)
        .vector("overlay", FloatRepr.SHORT, 2).vector("light", FloatRepr.UNSIGNED_SHORT, 2)
        .vector("normal", FloatRepr.NORMALIZED_BYTE, 3).build();

    public static final List<VertexAttribute> ATTRIBUTES = LayoutAttributes.attributes(LAYOUT);
    public static final int STRIDE = LAYOUT.byteSize();

    public static final Identifier LAYOUT_SHADER = ResourceUtil.rl("internal/vertex_input.vert");
    /**
     * Vulkan variant of {@link #LAYOUT_SHADER}.
     *
     * <p>Declares the {@code overlay}/{@code light} attributes with the integer GLSL types that
     * actually match the {@link #LAYOUT} element types ({@code SHORT} and {@code UNSIGNED_SHORT}),
     * since Vulkan derives the vertex input format straight from the VertexFormat. The OpenGL
     * variant keeps its float-based workaround for drivers that mishandle integer attributes.
     */
    public static final Identifier LAYOUT_SHADER_VK = ResourceUtil.rl("internal/vertex_input_vk.vert");

    private InternalVertex() {
    }

    public static VertexView createVertexView() {
        return new FullVertexView();
    }
}
