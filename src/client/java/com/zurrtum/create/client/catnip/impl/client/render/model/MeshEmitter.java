package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.BufferAllocator;
import org.jetbrains.annotations.UnknownNullability;

// Modified from https://github.com/Engine-Room/Flywheel/blob/2f67f54c8898d91a48126c3c753eefa6cd224f84/fabric/src/lib/java/dev/engine_room/flywheel/lib/model/baked/MeshEmitter.java
class MeshEmitter {
    private final BlockRenderLayer renderType;
    private final BufferAllocator byteBufferBuilder;
    @UnknownNullability
    private BufferBuilder bufferBuilder;

    @UnknownNullability
    private ShadeSeparatedResultConsumer resultConsumer;
    private boolean currentShade;

    MeshEmitter(BlockRenderLayer renderType) {
        this.renderType = renderType;
        this.byteBufferBuilder = new BufferAllocator(renderType.getBufferSize());
    }

    public void prepare(ShadeSeparatedResultConsumer resultConsumer) {
        this.resultConsumer = resultConsumer;
    }

    public void end() {
        if (bufferBuilder != null) {
            emit();
        }
        resultConsumer = null;
    }

    public BufferBuilder getBuffer(boolean shade) {
        prepareForGeometry(shade);
        return bufferBuilder;
    }

    private void prepareForGeometry(boolean shade) {
        if (bufferBuilder == null) {
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        } else if (shade != currentShade) {
            emit();
            bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
        }

        currentShade = shade;
    }

    private void emit() {
        var data = bufferBuilder.endNullable();
        bufferBuilder = null;

        if (data != null) {
            resultConsumer.accept(renderType, currentShade, data);
            data.close();
        }
    }
}
