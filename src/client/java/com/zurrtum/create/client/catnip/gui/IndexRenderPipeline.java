package com.zurrtum.create.client.catnip.gui;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.platform.LogicOp;
import com.mojang.blaze3d.platform.PolygonMode;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gl.Defines;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Optional;

public class IndexRenderPipeline extends RenderPipeline {
    private final RenderSystem.ShapeIndexBuffer sequentialBuffer;
    private final VertexFormat.IndexType indexType;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public IndexRenderPipeline(
        RenderSystem.ShapeIndexBuffer sequentialBuffer,
        VertexFormat.IndexType indexType,
        Identifier location,
        Identifier vertexShader,
        Identifier fragmentShader,
        Defines shaderDefines,
        List<String> samplers,
        List<UniformDescription> uniforms,
        Optional<BlendFunction> blendFunction,
        DepthTestFunction depthTestFunction,
        PolygonMode polygonMode,
        boolean cull,
        boolean writeColor,
        boolean writeAlpha,
        boolean writeDepth,
        LogicOp colorLogic,
        VertexFormat vertexFormat,
        VertexFormat.DrawMode vertexFormatMode,
        float depthBiasScaleFactor,
        float depthBiasConstant,
        int sortKey
    ) {
        super(
            location,
            vertexShader,
            fragmentShader,
            shaderDefines,
            samplers,
            uniforms,
            blendFunction,
            depthTestFunction,
            polygonMode,
            cull,
            writeColor,
            writeAlpha,
            writeDepth,
            colorLogic,
            vertexFormat,
            vertexFormatMode,
            depthBiasScaleFactor,
            depthBiasConstant,
            sortKey
        );
        this.sequentialBuffer = sequentialBuffer;
        this.indexType = indexType;
    }

    public GpuBuffer getIndexBuffer(int requiredSize) {
        return sequentialBuffer.getIndexBuffer(requiredSize);
    }

    public VertexFormat.IndexType getIndexType() {
        return indexType;
    }

    public static IndexBuilder builder(Snippet... snippets) {
        IndexBuilder builder = new IndexBuilder();

        for (Snippet snippet : snippets) {
            builder.withSnippet(snippet);
        }

        return builder;
    }

    public static class IndexBuilder extends Builder {
        @Override
        public RenderPipeline build() {
            RenderPipeline pipeline = super.build();
            VertexFormat.DrawMode vertexFormatMode = pipeline.getVertexFormatMode();
            RenderSystem.ShapeIndexBuffer sequentialBuffer = RenderSystem.getSequentialBuffer(vertexFormatMode);
            return new IndexRenderPipeline(
                sequentialBuffer,
                sequentialBuffer.getIndexType(),
                pipeline.getLocation(),
                pipeline.getVertexShader(),
                pipeline.getFragmentShader(),
                pipeline.getShaderDefines(),
                pipeline.getSamplers(),
                pipeline.getUniforms(),
                pipeline.getBlendFunction(),
                pipeline.getDepthTestFunction(),
                pipeline.getPolygonMode(),
                pipeline.isCull(),
                pipeline.isWriteColor(),
                pipeline.isWriteAlpha(),
                pipeline.isWriteDepth(),
                pipeline.getColorLogic(),
                pipeline.getVertexFormat(),
                vertexFormatMode,
                pipeline.getDepthBiasScaleFactor(),
                pipeline.getDepthBiasConstant(),
                pipeline.getSortKey()
            );
        }
    }
}
