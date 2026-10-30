package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.render.FabricLayerRenderState;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector.CustomGeometryRenderer;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.List;
import java.util.function.Consumer;

@Mixin(ItemModelRenderHelper.class)
public class ItemModelRenderHelperMixin {
    @Overwrite(remap = false)
    public static LayerRenderState submitQuads(
        ItemStackRenderState state,
        ModelRenderProperties settings,
        ItemDisplayContext displayContext,
        ItemQuads quads
    ) {
        LayerRenderState layer = state.newLayer();
        settings.applyToLayer(layer, displayContext);
        QuadEmitter emitter = ((FabricLayerRenderState) layer).emitter();
        for (BakedQuad quad : quads.all()) {
            emitter.fromBakedQuad(quad);
            emitter.emit();
        }
        return layer;
    }

    @Overwrite(remap = false)
    public static LayerRenderState submitCustomLayerWithLightTint(
        ItemStackRenderState state,
        ModelRenderProperties settings,
        ItemDisplayContext displayContext,
        int lightCoords,
        int[] tints,
        ItemQuads quads
    ) {
        LayerRenderState layer = state.newLayer();
        settings.applyToLayer(layer, displayContext);
        CustomRenderLayer.setup(layer, lightCoords, tints, quads.all());
        return layer;
    }

    private record CustomRenderLayer(int light, int[] tints,
                                     List<BakedQuad> quads) implements CustomGeometryRenderer, SpecialModelRenderer<RenderType> {
        private static final QuadInstance quadInstance = new QuadInstance();

        public static void setup(LayerRenderState layer, int lightCoords, int[] tints, List<BakedQuad> quads) {
            CustomRenderLayer renderer = new CustomRenderLayer(lightCoords, tints, quads);
            layer.setupSpecialModel(renderer, quads.getFirst().materialInfo().itemRenderType());
        }

        @Override
        public void submit(
            @Nullable RenderType argument,
            @NonNull PoseStack matrices,
            @NonNull SubmitNodeCollector queue,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
        ) {
            assert argument != null;
            queue.submitCustomGeometry(matrices, argument, this);
        }

        @Override
        public void render(@NonNull Pose pose, @NonNull VertexConsumer buffer) {
            quadInstance.setLightCoords(light);
            for (BakedQuad quad : quads) {
                quadInstance.setColor(ItemFeatureRenderer.getLayerColorSafe(tints, quad.materialInfo()));
                buffer.putBakedQuad(pose, quad, quadInstance);
            }
        }

        @Override
        public void getExtents(@NonNull Consumer<Vector3fc> output) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable RenderType extractArgument(@NonNull ItemStack stack) {
            throw new UnsupportedOperationException();
        }
    }
}
