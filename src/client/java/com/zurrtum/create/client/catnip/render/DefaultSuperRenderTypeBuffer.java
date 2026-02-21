package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.GpuSampler;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.foundation.render.CreateRenderTypes;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.SubmitNodeCollector.ParticleGroupRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.feature.ParticleFeatureRenderer.ParticleBufferCache;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.QuadParticleRenderState.PreparedBuffers;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.util.Util;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.Nullable;

import java.util.*;

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
    public VertexConsumer getEarlyBuffer(RenderType type) {
        return earlyBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        return defaultBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public VertexConsumer getLateBuffer(RenderType type) {
        return lateBuffer.bufferSource.getBuffer(type);
    }

    @Override
    public void draw() {
        earlyBuffer.bufferSource.endBatch();
        defaultBuffer.bufferSource.endBatch();
        lateBuffer.bufferSource.endBatch();
    }

    @Override
    public void draw(RenderType type) {
        earlyBuffer.bufferSource.endBatch(type);
        defaultBuffer.bufferSource.endBatch(type);
        lateBuffer.bufferSource.endBatch(type);
    }

    private void drawLast() {
        earlyBuffer.bufferSource.endLastBatch();
        defaultBuffer.bufferSource.endLastBatch();
        lateBuffer.bufferSource.endLastBatch();
    }

    public static class SuperRenderTypeBufferPhase {
        // Visible clones from RenderBuffers
        private final SectionBufferBuilderPack fixedBufferPack = new SectionBufferBuilderPack();
        private final SortedMap<RenderType, ByteBufferBuilder> fixedBuffers = Util.make(
            new Object2ObjectLinkedOpenHashMap<>(), map -> {
                map.put(Sheets.solidBlockSheet(), fixedBufferPack.buffer(ChunkSectionLayer.SOLID));
                put(map, RenderTypes.solidMovingBlock());
                map.put(Sheets.cutoutBlockSheet(), fixedBufferPack.buffer(ChunkSectionLayer.CUTOUT));
                put(map, RenderTypes.cutoutMovingBlock());
                put(map, RenderTypes.tripwireMovingBlock());
                map.put(Sheets.translucentItemSheet(), fixedBufferPack.buffer(ChunkSectionLayer.TRANSLUCENT));
                put(map, Sheets.translucentBlockItemSheet());
                put(map, RenderTypes.translucentMovingBlock());
                put(map, Sheets.shieldSheet());
                put(map, Sheets.bedSheet());
                put(map, Sheets.shulkerBoxSheet());
                put(map, Sheets.signSheet());
                put(map, Sheets.hangingSignSheet());
                map.put(Sheets.chestSheet(), new ByteBufferBuilder(786432));
                put(map, RenderTypes.armorEntityGlint());
                put(map, RenderTypes.glint());
                put(map, RenderTypes.glintTranslucent());
                put(map, RenderTypes.entityGlint());
                put(map, RenderTypes.waterMask());
                ModelBakery.DESTROY_TYPES.forEach(renderType -> put(map, renderType));

                //extras
                put(map, PonderRenderTypes.outlineSolid());
                put(map, CreateRenderTypes.translucent());
                put(map, CreateRenderTypes.additive());
            }
        );
        private final BufferSource bufferSource = MultiBufferSource.immediateWithBuffers(
            fixedBuffers,
            new ByteBufferBuilder(256)
        );

        private static void put(Object2ObjectLinkedOpenHashMap<RenderType, ByteBufferBuilder> map, RenderType type) {
            map.put(type, new ByteBufferBuilder(type.bufferSize()));
        }
    }

    public static class Dispatcher {
        private final DefaultSuperRenderTypeBuffer buffer;
        private final OutlineBufferSource outline;
        private final FeatureRenderDispatcher renderDispatcher;
        private final Queue<ParticleBufferCache> availableBuffers = new ArrayDeque<>();
        private final List<ParticleBufferCache> usedBuffers = new ArrayList<>();

        public Dispatcher() {
            Minecraft mc = Minecraft.getInstance();
            buffer = getInstance();
            BufferSource bufferSource = buffer.defaultBuffer.bufferSource;
            outline = new OutlineBufferSource();
            renderDispatcher = new FeatureRenderDispatcher(
                new SubmitNodeStorage(),
                mc.getBlockRenderer(),
                bufferSource,
                mc.getAtlasManager(),
                outline,
                bufferSource,
                mc.font
            );
        }

        public DefaultSuperRenderTypeBuffer getBuffer() {
            return buffer;
        }

        public SubmitNodeStorage getSubmitNodeStorage() {
            return renderDispatcher.getSubmitNodeStorage();
        }

        @Nullable
        private List<ParticleRenderState> extractParticles(PoseStack ms) {
            List<ParticleRenderState> list = new ArrayList<>();
            Matrix4fStack stack = RenderSystem.getModelViewStack();
            stack.pushMatrix();
            stack.mul(ms.last().pose());
            for (SubmitNodeCollection commandQueue : renderDispatcher.getSubmitNodeStorage().getSubmitsPerOrder()
                .values()) {
                List<ParticleGroupRenderer> commands = commandQueue.getParticleGroupRenderers();
                if (commands.isEmpty()) {
                    continue;
                }
                for (ParticleGroupRenderer particleGroupRenderer : commands) {
                    ParticleBufferCache particleBufferCache = availableBuffers.poll();
                    if (particleBufferCache == null) {
                        particleBufferCache = new ParticleBufferCache();
                    }
                    usedBuffers.add(particleBufferCache);
                    PreparedBuffers preparedBuffers = particleGroupRenderer.prepare(particleBufferCache);
                    if (preparedBuffers != null) {
                        list.add(new ParticleRenderState(particleGroupRenderer, preparedBuffers, particleBufferCache));
                    }
                }
                commands.clear();
            }
            stack.popMatrix();
            return list.isEmpty() ? null : list;
        }

        private static void renderParticles(List<ParticleRenderState> particles, boolean translucent) {
            if (particles == null) {
                return;
            }
            GpuTextureView colorTexture = RenderSystem.outputColorTextureOverride;
            GpuTextureView depthTexture = RenderSystem.outputDepthTextureOverride;
            Minecraft mc = Minecraft.getInstance();
            GpuTextureView lightTextureView = mc.gameRenderer.lightTexture().getTextureView();
            GpuSampler lightSampler = RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR);
            TextureManager textureManager = mc.getTextureManager();
            GpuBufferSlice projection = RenderSystem.getProjectionMatrixBuffer();
            GpuBufferSlice fog = RenderSystem.getShaderFog();
            for (ParticleRenderState particle : particles) {
                particle.render(
                    colorTexture,
                    depthTexture,
                    projection,
                    fog,
                    lightTextureView,
                    lightSampler,
                    textureManager,
                    translucent
                );
            }
        }

        public void draw(PoseStack ms) {
            List<ParticleRenderState> particles = extractParticles(ms);
            renderDispatcher.renderAllFeatures();
            buffer.drawLast();
            buffer.draw(RenderTypes.solidMovingBlock());
            buffer.draw(Sheets.solidBlockSheet());
            buffer.draw(Sheets.cutoutBlockSheet());
            buffer.draw(RenderTypes.cutoutMovingBlock());
            buffer.draw(RenderTypes.tripwireMovingBlock());
            buffer.draw(Sheets.shieldSheet());
            buffer.draw(Sheets.bedSheet());
            buffer.draw(Sheets.shulkerBoxSheet());
            buffer.draw(Sheets.signSheet());
            buffer.draw(Sheets.hangingSignSheet());
            buffer.draw(Sheets.chestSheet());
            buffer.draw(RenderTypes.armorEntityGlint());
            buffer.draw(RenderTypes.glint());
            buffer.draw(RenderTypes.glintTranslucent());
            buffer.draw(RenderTypes.entityGlint());
            buffer.draw(RenderTypes.waterMask());
            renderParticles(particles, false);
            ModelBakery.DESTROY_TYPES.forEach(buffer::draw);
            buffer.draw(PonderRenderTypes.outlineSolid());
            outline.endOutlineBatch();
            renderParticles(particles, true);
            buffer.draw(Sheets.translucentItemSheet());
            buffer.draw(Sheets.translucentBlockItemSheet());
            buffer.draw(RenderTypes.translucentMovingBlock());
            buffer.draw(CreateRenderTypes.translucent());
            buffer.draw(CreateRenderTypes.additive());
            for (ParticleBufferCache particleBufferCache : usedBuffers) {
                particleBufferCache.rotate();
            }
            availableBuffers.addAll(usedBuffers);
            usedBuffers.clear();
        }
    }

    private record ParticleRenderState(ParticleGroupRenderer particleGroupRenderer, PreparedBuffers preparedBuffers,
                                       ParticleBufferCache particleBufferCache) {
        public void render(
            GpuTextureView colorTexture,
            GpuTextureView depthTexture,
            GpuBufferSlice projection,
            GpuBufferSlice fog,
            GpuTextureView lightTextureView,
            GpuSampler lightSampler,
            TextureManager textureManager,
            boolean translucent
        ) {
            try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(
                    () -> "Immediate draw for particle",
                    colorTexture,
                    OptionalInt.empty(),
                    depthTexture,
                    OptionalDouble.empty()
                )) {
                renderPass.setUniform("Projection", projection);
                renderPass.setUniform("Fog", fog);
                renderPass.bindTexture("Sampler2", lightTextureView, lightSampler);
                particleGroupRenderer.render(
                    preparedBuffers,
                    particleBufferCache,
                    renderPass,
                    textureManager,
                    translucent
                );
            }
        }
    }
}
