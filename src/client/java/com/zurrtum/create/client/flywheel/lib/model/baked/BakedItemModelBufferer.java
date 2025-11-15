package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.lib.material.CutoutShaders;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollection;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;

import java.util.*;

public class BakedItemModelBufferer {
    static final Map<BlendFunction, Transparency> TRANSPARENCY = Map.of(
        BlendFunction.ADDITIVE,
        Transparency.ADDITIVE,
        BlendFunction.LIGHTNING,
        Transparency.LIGHTNING,
        BlendFunction.GLINT,
        Transparency.GLINT,
        BlendFunction.TRANSLUCENT,
        Transparency.TRANSLUCENT
    );
    static final List<RenderType> CHUNK_LAYERS = List.of(
        Sheets.solidBlockSheet(),
        Sheets.cutoutBlockSheet(),
        Sheets.translucentItemSheet(),
        RenderTypes.glint(),
        RenderTypes.glintTranslucent(),
        RenderTypes.entityGlint()
    );

    public static void bufferItemStack(
        ItemStack stack,
        BlockAndTintGetter level,
        ItemDisplayContext displayContext,
        ResultConsumer resultConsumer,
        MeshResultConsumer meshResultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        PoseStack poseStack = objects.identityPoseStack;
        ClientLevel world = level instanceof ClientLevel clientWorld ? clientWorld : null;
        ItemMeshEmitterProvider provider = objects.provider;
        provider.setResultConsumer(resultConsumer, meshResultConsumer);
        ItemStackRenderState state = objects.state;
        SubmitNodeStorage queue = objects.queue;
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(state, stack, displayContext, world, null, 0);
        state.submit(poseStack, queue, 0, OverlayTexture.NO_OVERLAY, 0);
        for (SubmitNodeCollection commandQueue : queue.getSubmitsPerOrder().values()) {
            ModelCommandRendererHelper.render(poseStack, commandQueue, provider, provider, provider);
            ModelPartCommandRendererHelper.render(poseStack, commandQueue, provider, provider, provider);
            ItemCommandRendererHelper.render(poseStack, commandQueue, provider, provider);
            CustomCommandRendererHelper.render(commandQueue, provider);
        }
        queue.clear();
        provider.end();
    }

    public static class ItemMeshEmitterProvider implements MultiBufferSource {
        private final ThreadLocalObjects objects;
        private ResultConsumer resultConsumer;
        private MeshResultConsumer meshResultConsumer;

        private ItemMeshEmitterProvider(ThreadLocalObjects objects) {
            this.objects = objects;
        }

        public void setResultConsumer(ResultConsumer resultConsumer, MeshResultConsumer meshResultConsumer) {
            this.resultConsumer = resultConsumer;
            this.meshResultConsumer = meshResultConsumer;
        }

        private void emitMesh(RenderType renderType, Mesh mesh) {
            Material material = objects.materials.computeIfAbsent(renderType, ItemMeshEmitterProvider::createMaterial);
            meshResultConsumer.accept(renderType, material, mesh);
        }

        private static Material createMaterial(RenderType renderLayer) {
            RenderType.CompositeRenderType layer = (RenderType.CompositeRenderType) renderLayer;
            Optional<Identifier> id = layer.state.textureState.cutoutTexture();
            if (id.isPresent()) {
                SimpleMaterial.Builder builder = SimpleMaterial.builder().texture(id.get()).mipmap(false);
                Optional<BlendFunction> blendFunction = layer.renderPipeline.getBlendFunction();
                if (blendFunction.isPresent()) {
                    Transparency transparency = TRANSPARENCY.get(blendFunction.get());
                    if (transparency != null) {
                        builder.transparency(transparency);
                    }
                }
                String cutout = layer.renderPipeline.getShaderDefines().values().get("ALPHA_CUTOUT");
                if (cutout != null) {
                    if (cutout.equals("0.1")) {
                        builder.cutout(CutoutShaders.ONE_TENTH);
                    } else if (cutout.equals("0.5")) {
                        builder.cutout(CutoutShaders.HALF);
                    }
                }
                return builder.build();
            }
            return Materials.TRANSLUCENT_ENTITY;
        }

        @Override
        public VertexConsumer getBuffer(RenderType layer) {
            Integer index = objects.chunkLayers.get(layer);
            ItemMeshEmitter emitter;
            if (index == null) {
                objects.chunkLayers.put(layer, objects.chunkLayers.size());
                emitter = new ItemMeshEmitter(layer);
                emitter.prepare(resultConsumer, this::emitMesh);
                objects.emitters.add(emitter);
            } else {
                emitter = objects.emitters.get(index);
                if (emitter.isEnd()) {
                    emitter.prepare(resultConsumer, this::emitMesh);
                }
            }
            return emitter;
        }

        public void end() {
            for (ItemMeshEmitter emitter : objects.emitters) {
                emitter.end();
            }
        }
    }

    public interface ResultConsumer {
        void accept(RenderType renderType, boolean shaded, MeshData data);
    }

    public interface MeshResultConsumer {
        void accept(RenderType renderType, Material material, Mesh mesh);
    }

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    public static Map<RenderType, Integer> getChunkLayers() {
        return THREAD_LOCAL_OBJECTS.get().chunkLayers;
    }

    private static class ThreadLocalObjects {
        public final PoseStack identityPoseStack = new PoseStack();
        public final SubmitNodeStorage queue = new SubmitNodeStorage();
        public final ItemStackRenderState state = new ItemStackRenderState();
        public final ItemMeshEmitterProvider provider = new ItemMeshEmitterProvider(this);
        public final Map<RenderType, Material> materials = new HashMap<>();
        public final Map<RenderType, Integer> chunkLayers = new HashMap<>();
        public final List<ItemMeshEmitter> emitters = new ArrayList<>();

        {
            for (int i = 0, size = CHUNK_LAYERS.size(); i < size; i++) {
                RenderType renderType = CHUNK_LAYERS.get(i);
                chunkLayers.put(renderType, i);
                emitters.add(new ItemMeshEmitter(renderType));
            }
        }
    }
}
