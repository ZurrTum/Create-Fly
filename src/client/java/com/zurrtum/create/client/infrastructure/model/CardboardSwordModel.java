package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class CardboardSwordModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/cardboard_sword");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/cardboard_sword/item");
    public static final Identifier BLOCK_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/cardboard_sword/item_in_hand");

    private final RenderType layer = Sheets.translucentItemSheet();
    private final List<BakedQuad> itemQuads;
    private final Supplier<Vector3fc[]> itemVector;
    private final List<BakedQuad> blockQuads;
    private final Supplier<Vector3fc[]> blockVector;
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
        ItemStackRenderState.LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderType(layer);
        settings.applyToLayer(layerRenderState, displayContext);
        if (stack.hasFoil()) {
            layerRenderState.setFoilType(FoilType.STANDARD);
            state.setAnimated();
            state.appendModelIdentityElement(FoilType.STANDARD);
        }
        if (displayContext == ItemDisplayContext.GUI) {
            layerRenderState.setExtents(itemVector);
            layerRenderState.prepareQuadList().addAll(itemQuads);
        } else {
            layerRenderState.setExtents(blockVector);
            layerRenderState.prepareQuadList().addAll(blockQuads);
        }
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
            List<BakedQuad> itemQuads = itemModel.bakeTopGeometry(itemTextures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, itemModel, itemTextures);
            ResolvedModel blockModel = baker.getModel(BLOCK_ID);
            List<BakedQuad> blockQuads = blockModel.bakeTopGeometry(blockModel.getTopTextureSlots(), baker, BlockModelRotation.IDENTITY).getAll();
            return new CardboardSwordModel(itemQuads, blockQuads, settings);
        }
    }
}
