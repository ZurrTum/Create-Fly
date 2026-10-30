package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

public class VanillinMeshEmitterManager extends MeshEmitterManager<MeshEmitter> implements BufferAoEmitter, FluidRenderer.Output {
    protected @UnknownNullability FluidBufferBuilder fluidBufferBuilder;
    protected boolean useAo;
    protected @Nullable Object cache;

    VanillinMeshEmitterManager() {
        super(MeshEmitter::new);
        fluidBufferBuilder = new FluidBufferBuilder();
    }

    @SuppressWarnings("CopyConstructorMissesField")
    VanillinMeshEmitterManager(VanillinMeshEmitterManager manager) {
        super(manager);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends BufferEmitterOutput, Emitter> Emitter getOrCreateEmitter(Function<T, Emitter> func) {
        if (cache != null) {
            return (Emitter) cache;
        }
        Emitter emitter = func.apply((T) this);
        cache = emitter;
        return emitter;
    }

    public void prepareForModelLayer(boolean useAo) {
        this.useAo = useAo;
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        VertexConsumer buffer = getBuffer(info.layer(), info.shadeDirectionOverride(), useAo);
        if (buffer != null) {
            buffer.putBlockBakedQuad(x, y, z, quad, instance);
        }
    }

    public void prepareForFluid(BlockPos pos) {
        fluidBufferBuilder.prepare(pos);
    }

    @Override
    public VertexConsumer getBuilder(ChunkSectionLayer layer) {
        BufferBuilder buffer = getFluidBuffer(layer);
        if (buffer != null) {
            return fluidBufferBuilder.wrap(buffer);
        }
        return EmptyVertexConsumer.INSTANCE;
    }

    protected static class FluidBufferBuilder extends AbstractMeshBuffer {
        protected int fluidX, fluidY, fluidZ;
        protected @UnknownNullability BufferBuilder buffer;

        public void prepare(BlockPos pos) {
            fluidX = pos.getX() & -16;
            fluidY = pos.getY() & -16;
            fluidZ = pos.getZ() & -16;
        }

        public VertexConsumer wrap(BufferBuilder buffer) {
            this.buffer = buffer;
            return this;
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
