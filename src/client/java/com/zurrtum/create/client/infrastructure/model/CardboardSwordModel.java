package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.Glint;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class CardboardSwordModel implements ItemModel {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/cardboard_sword");
    public static final Identifier ITEM_ID = Identifier.of(MOD_ID, "item/cardboard_sword/item");
    public static final Identifier BLOCK_ID = Identifier.of(MOD_ID, "item/cardboard_sword/item_in_hand");

    private final RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final List<BakedQuad> itemQuads;
    private final Supplier<Vector3f[]> itemVector;
    private final List<BakedQuad> blockQuads;
    private final Supplier<Vector3f[]> blockVector;
    private final ModelSettings settings;

    public CardboardSwordModel(List<BakedQuad> item, List<BakedQuad> block, ModelSettings settings) {
        itemQuads = item;
        itemVector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(itemQuads));
        blockQuads = block;
        blockVector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(blockQuads));
        this.settings = settings;
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
        ItemRenderState.LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderLayer(layer);
        settings.addSettings(layerRenderState, displayContext);
        if (stack.hasGlint()) {
            layerRenderState.setGlint(Glint.STANDARD);
            state.markAnimated();
            state.addModelKey(Glint.STANDARD);
        }
        if (displayContext == ItemDisplayContext.GUI) {
            layerRenderState.setVertices(itemVector);
            layerRenderState.getQuads().addAll(itemQuads);
        } else {
            layerRenderState.setVertices(blockVector);
            layerRenderState.getQuads().addAll(blockQuads);
        }
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
            resolver.markDependency(BLOCK_ID);
        }

        @Override
        public ItemModel bake(BakeContext context) {
            Baker baker = context.blockModelBaker();
            BakedSimpleModel itemModel = baker.getModel(ITEM_ID);
            ModelTextures itemTextures = itemModel.getTextures();
            List<BakedQuad> itemQuads = itemModel.bakeGeometry(itemTextures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, itemModel, itemTextures);
            BakedSimpleModel blockModel = baker.getModel(BLOCK_ID);
            List<BakedQuad> blockQuads = blockModel.bakeGeometry(blockModel.getTextures(), baker, ModelRotation.X0_Y0).getAllQuads();
            return new CardboardSwordModel(itemQuads, blockQuads, settings);
        }
    }
}
