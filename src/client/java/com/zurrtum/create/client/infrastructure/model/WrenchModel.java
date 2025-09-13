package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.LayerRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class WrenchModel implements ItemModel, SpecialModelRenderer<LayerRenderState> {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/wrench");
    public static final Identifier ITEM_ID = Identifier.of(MOD_ID, "item/wrench/item");
    public static final Identifier GEAR_ID = Identifier.of(MOD_ID, "item/wrench/gear");

    private final RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final List<BakedQuad> itemQuads;
    private final ModelSettings itemSettings;
    private final Supplier<Vector3f[]> itemVector;
    private final List<BakedQuad> gearQuads;
    private final ModelSettings gearSettings;
    private final Supplier<Vector3f[]> gearVector;

    public WrenchModel(Pair<List<BakedQuad>, ModelSettings> item, Pair<List<BakedQuad>, ModelSettings> gear) {
        itemQuads = item.getLeft();
        itemSettings = item.getRight();
        itemVector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(itemQuads));
        gearQuads = gear.getLeft();
        gearSettings = gear.getRight();
        gearVector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(gearQuads));
    }

    @Override
    public void update(
        ItemRenderState state,
        ItemStack stack,
        ItemModelManager resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientWorld world,
        @Nullable LivingEntity user,
        int seed
    ) {
        state.addModelKey(this);
        state.markAnimated();
        update(state, displayContext, itemQuads, itemSettings, itemVector, false);
        update(state, displayContext, gearQuads, gearSettings, gearVector, true);
    }

    private void update(
        ItemRenderState state,
        ItemDisplayContext displayContext,
        List<BakedQuad> quads,
        ModelSettings settings,
        Supplier<Vector3f[]> vector,
        boolean rotation
    ) {
        LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderLayer(layer);
        layerRenderState.setVertices(vector);
        settings.addSettings(layerRenderState, displayContext);
        layerRenderState.getQuads().addAll(quads);
        if (rotation) {
            layerRenderState.setSpecialModel(this, layerRenderState);
        }
    }

    @Override
    public void render(
        LayerRenderState layer,
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        boolean glint
    ) {
        assert layer != null;
        matrices.push();
        matrices.translate(0.5625f, 0.5f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ScrollValueHandler.getScroll(AnimationTickHolder.getPartialTicks())));
        matrices.translate(-0.5625f, -0.5f, -0.5f);
        ItemRenderer.renderItem(displayContext, matrices, vertexConsumers, light, overlay, layer.tints, layer.quads, layer.renderLayer, layer.glint);
        matrices.pop();
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LayerRenderState getData(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public void resolve(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(GEAR_ID);
        }

        @Override
        public ItemModel bake(BakeContext context) {
            Baker baker = context.blockModelBaker();
            return new WrenchModel(bake(baker, ITEM_ID), bake(baker, GEAR_ID));
        }

        private static Pair<List<BakedQuad>, ModelSettings> bake(Baker baker, Identifier id) {
            BakedSimpleModel model = baker.getModel(id);
            ModelTextures textures = model.getTextures();
            List<BakedQuad> quads = model.bakeGeometry(textures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, model, textures);
            return new Pair<>(quads, settings);
        }
    }
}
