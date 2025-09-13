package com.zurrtum.create.client.catnip.impl.client.render.model;

import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedBufferSource;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.VertexConsumer;

class DefaultShadeSeparatedBufferSource implements ShadeSeparatedBufferSource {
    private static final BlockRenderLayer[] CHUNK_LAYERS = BlockRenderLayer.values();
    private static final int CHUNK_LAYER_AMOUNT = CHUNK_LAYERS.length;

    private final MeshEmitter[] emitters = new MeshEmitter[CHUNK_LAYER_AMOUNT];
    private final Reference2ReferenceMap<BlockRenderLayer, MeshEmitter> emitterMap = new Reference2ReferenceOpenHashMap<>();

    DefaultShadeSeparatedBufferSource() {
        for (int layerIndex = 0; layerIndex < CHUNK_LAYER_AMOUNT; layerIndex++) {
            BlockRenderLayer renderType = CHUNK_LAYERS[layerIndex];
            MeshEmitter emitter = new MeshEmitter(renderType);
            emitters[layerIndex] = emitter;
            emitterMap.put(renderType, emitter);
        }
    }

    public void prepare(ShadeSeparatedResultConsumer resultConsumer) {
        for (MeshEmitter emitter : emitters) {
            emitter.prepare(resultConsumer);
        }
    }

    public void end() {
        for (MeshEmitter emitter : emitters) {
            emitter.end();
        }
    }

    @Override
    public VertexConsumer getBuffer(BlockRenderLayer chunkRenderType, boolean shade) {
        return emitterMap.get(chunkRenderType).getBuffer(shade);
    }
}
