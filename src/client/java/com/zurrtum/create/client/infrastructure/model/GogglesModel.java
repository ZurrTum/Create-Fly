package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class GogglesModel implements ItemModel {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "model/goggles");
    public static final ResourceLocation ITEM_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "item/goggles");
    public static final ResourceLocation BLOCK_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "block/goggles");

    private final RenderType layer = Sheets.translucentItemSheet();
    private final List<BakedQuad> itemQuads;
    private final ModelRenderProperties itemSettings;
    private final Supplier<Vector3f[]> itemVector;
    private final List<BakedQuad> blockQuads;
    private final ModelRenderProperties blockSettings;
    private final Supplier<Vector3f[]> blockVector;

    public GogglesModel(Tuple<List<BakedQuad>, ModelRenderProperties> item, Tuple<List<BakedQuad>, ModelRenderProperties> block) {
        itemQuads = item.getA();
        itemSettings = item.getB();
        itemVector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(itemQuads));
        blockQuads = block.getA();
        blockSettings = block.getB();
        blockVector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(blockQuads));
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
        if (displayContext == ItemDisplayContext.HEAD) {
            update(state, displayContext, blockQuads, blockSettings, blockVector);
        } else {
            update(state, displayContext, itemQuads, itemSettings, itemVector);
        }
    }

    private void update(
        ItemStackRenderState state,
        ItemDisplayContext displayContext,
        List<BakedQuad> quads,
        ModelRenderProperties settings,
        Supplier<Vector3f[]> vector
    ) {
        ItemStackRenderState.LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderType(layer);
        layerRenderState.setExtents(vector);
        settings.applyToLayer(layerRenderState, displayContext);
        layerRenderState.prepareQuadList().addAll(quads);
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.GogglesModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.GogglesModel.Unbaked::new);

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.GogglesModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(BLOCK_ID);
        }

        @Override
        public ItemModel bake(BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            return new GogglesModel(bake(baker, ITEM_ID), bake(baker, BLOCK_ID));
        }

        private static Tuple<List<BakedQuad>, ModelRenderProperties> bake(ModelBaker baker, ResourceLocation id) {
            ResolvedModel model = baker.getModel(id);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.X0_Y0).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new Tuple<>(quads, settings);
        }
    }
}
