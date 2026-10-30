package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.item.ModelRenderProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class ItemModelRenderHelper {
    private static final PoseStack.Pose POSE = new PoseStack.Pose();

    public static Matrix4f getPose(boolean applyLeftHandFix, ItemTransform transform) {
        POSE.setIdentity();
        transform.apply(applyLeftHandFix, POSE);
        return POSE.pose();
    }

    public static LayerRenderState submitQuads(
        ItemStackRenderState state,
        ModelRenderProperties settings,
        ItemDisplayContext displayContext,
        ItemQuads quads
    ) {
        LayerRenderState layer = state.newLayer();
        settings.applyToLayer(layer, displayContext);
        layer.setQuads(quads);
        return layer;
    }

    public static LayerRenderState submitCustomLayerWithLight(
        ItemStackRenderState state,
        ModelRenderProperties settings,
        ItemDisplayContext displayContext,
        int lightCoords,
        ItemQuads quads
    ) {
        return submitCustomLayerWithLightTint(
            state,
            settings,
            displayContext,
            lightCoords,
            LayerRenderState.EMPTY_TINTS,
            quads
        );
    }

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
        CustomLayer.setup(layer, lightCoords, tints, quads);
        return layer;
    }

    private record CustomLayer(int light, int[] tints, ItemQuads quads) implements SpecialModelRenderer<Object> {
        public static void setup(LayerRenderState layer, int lightCoords, int[] tints, ItemQuads quads) {
            CustomLayer renderer = new CustomLayer(lightCoords, tints, quads);
            layer.setupSpecialModel(renderer, null);
        }

        @Override
        public void submit(
            @Nullable Object argument,
            PoseStack matrices,
            SubmitNodeCollector queue,
            int lightCoords,
            int overlayCoords,
            boolean hasFoil,
            int outlineColor
        ) {
            queue.submitItem(
                matrices,
                ItemDisplayContext.NONE,
                light,
                overlayCoords,
                outlineColor,
                tints,
                quads,
                FoilType.NONE
            );
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            throw new UnsupportedOperationException();
        }

        @Override
        public @Nullable Object extractArgument(ItemStack stack) {
            throw new UnsupportedOperationException();
        }
    }
}
