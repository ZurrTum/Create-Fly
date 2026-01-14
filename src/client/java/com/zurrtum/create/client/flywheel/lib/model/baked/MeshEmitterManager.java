package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceArrayMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BufferBuilder;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

class MeshEmitterManager<T extends MeshEmitter> {
    private static final BlockRenderLayer[] CHUNK_LAYERS = BlockRenderLayer.values();

    private final Reference2ReferenceMap<BlockRenderLayer, T> emitterMap = new Reference2ReferenceArrayMap<>();
    private final ByteBufferBuilderStack byteBufferBuilderStack = new ByteBufferBuilderStack();

    @UnknownNullability
    private BlockMaterialFunction blockMaterialFunction;

    MeshEmitterManager(TriFunction<MeshEmitterManager<T>, ByteBufferBuilderStack, BlockRenderLayer, T> meshEmitterFactory) {
        for (BlockRenderLayer renderType : CHUNK_LAYERS) {
            emitterMap.put(renderType, meshEmitterFactory.apply(this, byteBufferBuilderStack, renderType));
        }
    }

    public T getEmitter(BlockRenderLayer renderType) {
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
    public Material getMaterial(BlockRenderLayer renderType, boolean shade, boolean ao) {
        return blockMaterialFunction.apply(renderType, shade, ao);
    }

    @Nullable
    public BufferBuilder getBuffer(BlockRenderLayer renderType, boolean shade, boolean ao) {
        return emitterMap.get(renderType).getBuffer(shade, ao);
    }
}
