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
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class CardboardSwordModel implements ItemModel {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "model/cardboard_sword");
    public static final ResourceLocation ITEM_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "item/cardboard_sword/item");
    public static final ResourceLocation BLOCK_ID = ResourceLocation.fromNamespaceAndPath(MOD_ID, "item/cardboard_sword/item_in_hand");

    private final RenderType layer = Sheets.translucentItemSheet();
    private final List<BakedQuad> itemQuads;
    private final Supplier<Vector3f[]> itemVector;
    private final List<BakedQuad> blockQuads;
    private final Supplier<Vector3f[]> blockVector;
    private final ModelRenderProperties settings;

    public CardboardSwordModel(List<BakedQuad> item, List<BakedQuad> block, ModelRenderProperties settings) {
        itemQuads = item;
        itemVector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(itemQuads));
        blockQuads = block;
        blockVector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(blockQuads));
        this.settings = settings;
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
        if (displayContext == ItemDisplayContext.GUI) {
            update(state, displayContext, itemQuads, settings, itemVector);
        } else {
            update(state, displayContext, blockQuads, settings, blockVector);
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
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.CardboardSwordModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.CardboardSwordModel.Unbaked::new);

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.CardboardSwordModel.Unbaked> type() {
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
            ResolvedModel itemModel = baker.getModel(ITEM_ID);
            TextureSlots itemTextures = itemModel.getTopTextureSlots();
            List<BakedQuad> itemQuads = itemModel.bakeTopGeometry(itemTextures, baker, BlockModelRotation.X0_Y0).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, itemModel, itemTextures);
            ResolvedModel blockModel = baker.getModel(BLOCK_ID);
            List<BakedQuad> blockQuads = blockModel.bakeTopGeometry(blockModel.getTopTextureSlots(), baker, BlockModelRotation.X0_Y0).getAll();
            return new CardboardSwordModel(itemQuads, blockQuads, settings);
        }
    }
}
