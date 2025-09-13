package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.model.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class GogglesModel implements ItemModel {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/goggles");
    public static final Identifier ITEM_ID = Identifier.of(MOD_ID, "item/goggles");
    public static final Identifier BLOCK_ID = Identifier.of(MOD_ID, "block/goggles");

    private final RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final List<BakedQuad> itemQuads;
    private final ModelSettings itemSettings;
    private final Supplier<Vector3f[]> itemVector;
    private final List<BakedQuad> blockQuads;
    private final ModelSettings blockSettings;
    private final Supplier<Vector3f[]> blockVector;

    public GogglesModel(Pair<List<BakedQuad>, ModelSettings> item, Pair<List<BakedQuad>, ModelSettings> block) {
        itemQuads = item.getLeft();
        itemSettings = item.getRight();
        itemVector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(itemQuads));
        blockQuads = block.getLeft();
        blockSettings = block.getRight();
        blockVector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(blockQuads));
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
        if (displayContext == ItemDisplayContext.HEAD) {
            update(state, displayContext, blockQuads, blockSettings, blockVector);
        } else {
            update(state, displayContext, itemQuads, itemSettings, itemVector);
        }
    }

    private void update(
        ItemRenderState state,
        ItemDisplayContext displayContext,
        List<BakedQuad> quads,
        ModelSettings settings,
        Supplier<Vector3f[]> vector
    ) {
        ItemRenderState.LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderLayer(layer);
        layerRenderState.setVertices(vector);
        settings.addSettings(layerRenderState, displayContext);
        layerRenderState.getQuads().addAll(quads);
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
            return new GogglesModel(bake(baker, ITEM_ID), bake(baker, BLOCK_ID));
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
