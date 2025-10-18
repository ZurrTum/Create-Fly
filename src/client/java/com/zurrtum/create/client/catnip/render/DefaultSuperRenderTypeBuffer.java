package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.client.foundation.render.RenderTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.render.*;
import net.minecraft.client.render.chunk.BlockBufferAllocatorStorage;
import net.minecraft.client.render.model.ModelBaker;
import net.minecraft.client.util.BufferAllocator;
import net.minecraft.util.Util;

import java.util.SortedMap;

public class DefaultSuperRenderTypeBuffer implements SuperRenderTypeBuffer {

    private static final DefaultSuperRenderTypeBuffer INSTANCE = new DefaultSuperRenderTypeBuffer();

    public static DefaultSuperRenderTypeBuffer getInstance() {
        return INSTANCE;
    }

    protected SuperRenderTypeBufferPhase earlyBuffer;
    protected SuperRenderTypeBufferPhase defaultBuffer;
    protected SuperRenderTypeBufferPhase lateBuffer;

    public DefaultSuperRenderTypeBuffer() {
        earlyBuffer = new SuperRenderTypeBufferPhase();
        defaultBuffer = new SuperRenderTypeBufferPhase();
        lateBuffer = new SuperRenderTypeBufferPhase();
    }

    @Override
    public VertexConsumer getEarlyBuffer(RenderLayer type) {
        return earlyBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public VertexConsumer getBuffer(RenderLayer type) {
        return defaultBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public VertexConsumer getLateBuffer(RenderLayer type) {
        return lateBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public void draw() {
        earlyBuffer.bufferSource.draw();
        defaultBuffer.bufferSource.draw();
        lateBuffer.bufferSource.draw();
    }

    @Override
    public void draw(RenderLayer type) {
        earlyBuffer.bufferSource.draw(type);
        defaultBuffer.bufferSource.draw(type);
        lateBuffer.bufferSource.draw(type);
    }

    public static class SuperRenderTypeBufferPhase {
        // Visible clones from RenderBuffers
        private final BlockBufferAllocatorStorage fixedBufferPack = new BlockBufferAllocatorStorage();
        private final SortedMap<RenderLayer, BufferAllocator> fixedBuffers = Util.make(
            new Object2ObjectLinkedOpenHashMap<>(), map -> {
                map.put(TexturedRenderLayers.getEntitySolid(), fixedBufferPack.get(BlockRenderLayer.SOLID));
                map.put(TexturedRenderLayers.getEntityCutout(), fixedBufferPack.get(BlockRenderLayer.CUTOUT));
                map.put(TexturedRenderLayers.getBannerPatterns(), fixedBufferPack.get(BlockRenderLayer.CUTOUT_MIPPED));
                map.put(TexturedRenderLayers.getItemEntityTranslucentCull(), fixedBufferPack.get(BlockRenderLayer.TRANSLUCENT));
                put(map, TexturedRenderLayers.getShieldPatterns());
                put(map, TexturedRenderLayers.getBeds());
                put(map, TexturedRenderLayers.getShulkerBoxes());
                put(map, TexturedRenderLayers.getSign());
                put(map, TexturedRenderLayers.getHangingSign());
                map.put(TexturedRenderLayers.getChest(), new BufferAllocator(786432));
                put(map, RenderLayer.getArmorEntityGlint());
                put(map, RenderLayer.getGlint());
                put(map, RenderLayer.getGlintTranslucent());
                put(map, RenderLayer.getEntityGlint());
                put(map, RenderLayer.getWaterMask());
                ModelBaker.BLOCK_DESTRUCTION_RENDER_LAYERS.forEach(renderType -> put(map, renderType));

                //extras
                put(map, PonderRenderTypes.outlineSolid());
                put(map, PonderRenderTypes.translucent());
                put(map, PonderRenderTypes.fluid());
                put(map, RenderTypes.translucent());
                put(map, RenderTypes.additive());
            }
        );
        private final Immediate bufferSource = VertexConsumerProvider.immediate(fixedBuffers, new BufferAllocator(256));

        private static void put(Object2ObjectLinkedOpenHashMap<RenderLayer, BufferAllocator> map, RenderLayer type) {
            map.put(type, new BufferAllocator(type.getExpectedBufferSize()));
        }

    }
}
