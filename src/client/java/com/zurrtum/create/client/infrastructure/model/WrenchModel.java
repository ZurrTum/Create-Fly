package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class WrenchModel implements ItemModel, SpecialModelRenderer<LayerRenderState> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/wrench");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wrench/item");
    public static final Identifier GEAR_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wrench/gear");

    private final RenderType layer = Sheets.translucentItemSheet();
    private final List<BakedQuad> itemQuads;
    private final ModelRenderProperties itemSettings;
    private final Supplier<Vector3f[]> itemVector;
    private final List<BakedQuad> gearQuads;
    private final ModelRenderProperties gearSettings;
    private final Supplier<Vector3f[]> gearVector;

    public WrenchModel(Tuple<List<BakedQuad>, ModelRenderProperties> item, Tuple<List<BakedQuad>, ModelRenderProperties> gear) {
        itemQuads = item.getA();
        itemSettings = item.getB();
        itemVector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(itemQuads));
        gearQuads = gear.getA();
        gearSettings = gear.getB();
        gearVector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(gearQuads));
    }

    @Override
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner user,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        update(state, displayContext, itemQuads, itemSettings, itemVector, false);
        update(state, displayContext, gearQuads, gearSettings, gearVector, true);
    }

    private void update(
        ItemStackRenderState state,
        ItemDisplayContext displayContext,
        List<BakedQuad> quads,
        ModelRenderProperties settings,
        Supplier<Vector3f[]> vector,
        boolean rotation
    ) {
        LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderType(layer);
        layerRenderState.setExtents(vector);
        settings.applyToLayer(layerRenderState, displayContext);
        layerRenderState.prepareQuadList().addAll(quads);
        if (rotation) {
            layerRenderState.setupSpecialModel(this, layerRenderState);
        }
    }

    @Override
    public void submit(
        LayerRenderState layer,
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert layer != null;
        matrices.pushPose();
        matrices.translate(0.5625f, 0.5f, 0.5f);
        matrices.mulPose(Axis.YP.rotationDegrees(ScrollValueHandler.getScroll(AnimationTickHolder.getPartialTicks())));
        matrices.translate(-0.5625f, -0.5f, -0.5f);
        queue.submitItem(matrices, displayContext, light, overlay, 0, layer.tintLayers, layer.quads, layer.renderType, layer.foilType);
        matrices.popPose();
    }

    @Override
    public void getExtents(Set<Vector3f> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public LayerRenderState extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.WrenchModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.WrenchModel.Unbaked::new);

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.WrenchModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(GEAR_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            return new WrenchModel(bake(baker, ITEM_ID), bake(baker, GEAR_ID));
        }

        private static Tuple<List<BakedQuad>, ModelRenderProperties> bake(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.X0_Y0).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new Tuple<>(quads, settings);
        }
    }
}
