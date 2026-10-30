package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.function.Function;

class MeshEmitterManager<T extends MeshEmitter> {
    private static final ChunkSectionLayer[] CHUNK_LAYERS = ChunkSectionLayer.values();

    private final Reference2ReferenceMap<ChunkSectionLayer, T> emitterMap;
    private final ByteBufferBuilderStack byteBufferBuilderStack;
    private final DirectionBufferBuilder directionBufferBuilder;

    @UnknownNullability
    private BlockMaterialFunction blockMaterialFunction;

    MeshEmitterManager(Function<ByteBufferBuilderStack, T> meshEmitterFactory) {
        emitterMap = new Reference2ReferenceArrayMap<>();
        byteBufferBuilderStack = new ByteBufferBuilderStack();
        directionBufferBuilder = new DirectionBufferBuilder();
        for (ChunkSectionLayer renderType : CHUNK_LAYERS) {
            emitterMap.put(renderType, meshEmitterFactory.apply(byteBufferBuilderStack));
        }
    }

    @SuppressWarnings("CopyConstructorMissesField")
    MeshEmitterManager(MeshEmitterManager<T> manager) {
        emitterMap = manager.emitterMap;
        byteBufferBuilderStack = manager.byteBufferBuilderStack;
        directionBufferBuilder = manager.directionBufferBuilder;
    }

    public T getEmitter(ChunkSectionLayer renderType) {
        return emitterMap.get(renderType);
    }

    public void prepare(BlockMaterialFunction blockMaterialFunction) {
        this.blockMaterialFunction = blockMaterialFunction;
        byteBufferBuilderStack.reset();
    }

    public void prepareForBlock() {
        for (MeshEmitter emitter : emitterMap.values()) {
            emitter.prepareForBlock();
        }
    }

    public SimpleModel end() {
        blockMaterialFunction = null;

        ImmutableList.Builder<Model.ConfiguredMesh> meshes = ImmutableList.builder();

        for (MeshEmitter emitter : emitterMap.values()) {
            emitter.end(meshes);
        }

        return new SimpleModel(meshes.build());
    }

    @Nullable
    public VertexConsumer getBuffer(ChunkSectionLayer layer, @Nullable Direction shadeDirectionOverride, boolean ao) {
        if (shadeDirectionOverride == null) {
            Material key = blockMaterialFunction.apply(layer, true, ao);
            if (key != null) {
                return emitterMap.get(layer).getBuffer(key);
            }
        } else if (shadeDirectionOverride == Direction.UP) {
            Material key = blockMaterialFunction.apply(layer, false, ao);
            if (key != null) {
                return emitterMap.get(layer).getBuffer(key);
            }
        } else {
            Material key = blockMaterialFunction.apply(layer, true, ao);
            if (key != null) {
                return directionBufferBuilder.wrap(this, shadeDirectionOverride, emitterMap.get(layer).getBuffer(key));
            }
        }
        return null;
    }

    @Nullable
    public BufferBuilder getFluidBuffer(ChunkSectionLayer layer) {
        Material key = blockMaterialFunction.apply(layer, true, false);
        if (key != null) {
            return emitterMap.get(layer).getBuffer(key);
        }
        return null;
    }

    protected void shadeDirectionOverride(Direction direction, Vector3f normal) {
        normal.set(direction.getUnitVec3f());
    }

    protected static class DirectionBufferBuilder extends AbstractMeshBuffer {
        private Vector3f normal = new Vector3f();
        private @UnknownNullability BufferBuilder buffer;

        VertexConsumer wrap(MeshEmitterManager<?> manager, Direction shadeDirectionOverride, BufferBuilder buffer) {
            manager.shadeDirectionOverride(shadeDirectionOverride, normal);
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
            buffer.addVertex(x, y, z, color, u, v, overlayCoords, lightCoords, normal.x(), normal.y(), normal.z());
        }
    }
}
