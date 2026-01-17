package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.pipeline.BlendFunction;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.lib.material.CutoutShaders;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.BlockRenderView;

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
    static final List<RenderLayer> CHUNK_LAYERS = List.of(
        TexturedRenderLayers.getEntitySolid(),
        TexturedRenderLayers.getEntityCutout(),
        TexturedRenderLayers.getItemEntityTranslucentCull(),
        RenderLayer.getGlint(),
        RenderLayer.getGlintTranslucent(),
        RenderLayer.getEntityGlint()
    );

    public static void bufferItemStack(
        ItemStack stack,
        BlockRenderView level,
        ItemDisplayContext displayContext,
        ResultConsumer resultConsumer,
        MeshResultConsumer meshResultConsumer
    ) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        MatrixStack poseStack = objects.identityPoseStack;
        ClientWorld world = level instanceof ClientWorld clientWorld ? clientWorld : null;
        ItemRenderer itemRenderer = MinecraftClient.getInstance().getItemRenderer();
        ItemMeshEmitterProvider provider = objects.provider;
        provider.setResultConsumer(resultConsumer, meshResultConsumer);
        itemRenderer.renderItem(stack, displayContext, 0, OverlayTexture.DEFAULT_UV, poseStack, provider, world, 0);
        provider.end();
    }

    public static class ItemMeshEmitterProvider implements VertexConsumerProvider {
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

        private void emitMesh(RenderLayer renderType, Mesh mesh, boolean translucent) {
            Material material = objects.materials.computeIfAbsent(renderType, ItemMeshEmitterProvider::createMaterial);
            meshResultConsumer.accept(renderType, material, mesh, translucent);
        }

        private static Material createMaterial(RenderLayer renderLayer) {
            RenderLayer.MultiPhase layer = (RenderLayer.MultiPhase) renderLayer;
            Optional<Identifier> id = layer.phases.texture.getId();
            if (id.isPresent()) {
                SimpleMaterial.Builder builder = SimpleMaterial.builder().texture(id.get()).mipmap(false);
                Optional<BlendFunction> blendFunction = layer.pipeline.getBlendFunction();
                if (blendFunction.isPresent()) {
                    Transparency transparency = TRANSPARENCY.get(blendFunction.get());
                    if (transparency != null) {
                        builder.transparency(transparency);
                    }
                }
                String cutout = layer.pipeline.getShaderDefines().values().get("ALPHA_CUTOUT");
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
        public VertexConsumer getBuffer(RenderLayer layer) {
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
        void accept(RenderLayer renderType, boolean shaded, BuiltBuffer data);
    }

    public interface MeshResultConsumer {
        void accept(RenderLayer renderType, Material material, Mesh mesh, boolean translucent);
    }

    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);

    public static Map<RenderLayer, Integer> getChunkLayers() {
        return THREAD_LOCAL_OBJECTS.get().chunkLayers;
    }

    private static class ThreadLocalObjects {
        public final MatrixStack identityPoseStack = new MatrixStack();
        public final ItemMeshEmitterProvider provider = new ItemMeshEmitterProvider(this);
        public final Map<RenderLayer, Material> materials = new HashMap<>();
        public final Map<RenderLayer, Integer> chunkLayers = new HashMap<>();
        public final List<ItemMeshEmitter> emitters = new ArrayList<>();

        {
            for (int i = 0, size = CHUNK_LAYERS.size(); i < size; i++) {
                RenderLayer renderType = CHUNK_LAYERS.get(i);
                chunkLayers.put(renderType, i);
                emitters.add(new ItemMeshEmitter(renderType));
            }
        }
    }
}
