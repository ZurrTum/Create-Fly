package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.AbstractMeshBuffer;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;

public class EntityBlockLevelSbbBuilder extends EntityBlockSbbBuilder implements FluidRenderer.Output {
    private int fluidX, fluidY, fluidZ;
    private final FluidTemplateMeshBuffer[] fluidBuffers = new FluidTemplateMeshBuffer[]{
        new FluidTemplateMeshBuffer(buffers[0]),
        new FluidTemplateMeshBuffer(buffers[1]),
        new FluidTemplateMeshBuffer(buffers[2]),
    };

    public EntityBlockLevelSbbBuilder() {
        super();
    }

    public EntityBlockLevelSbbBuilder(EntityBlockSbbBuilder builder) {
        super(builder);
    }

    public void prepareForFluid(BlockPos pos) {
        fluidX = pos.getX() & -16;
        fluidY = pos.getY() & -16;
        fluidZ = pos.getZ() & -16;
    }

    @Override
    public VertexConsumer getBuilder(ChunkSectionLayer layer) {
        return fluidBuffers[layer.ordinal()];
    }

    private class FluidTemplateMeshBuffer extends AbstractMeshBuffer {
        private final TemplateMeshBuffer buffer;

        public FluidTemplateMeshBuffer(TemplateMeshBuffer buffer) {
            this.buffer = buffer;
        }

        @Override
        public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int overlayCoords,
            int lightCoords,
            float nx,
            float ny,
            float nz
        ) {
            buffer.addVertex(fluidX + x, fluidY + y, fluidZ + z, color, u, v, overlayCoords, lightCoords, nx, ny, nz);
        }
    }
}
