package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.ClipboardType;
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
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class ClipboardModel implements ItemModel {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/clipboard");
    public static final Identifier EMPTY_ID = Identifier.of(MOD_ID, "item/clipboard_0");
    public static final Identifier WRITTEN_ID = Identifier.of(MOD_ID, "item/clipboard_1");
    public static final Identifier EDITING_ID = Identifier.of(MOD_ID, "item/clipboard_2");
    private final RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final ModelData[] models;

    public ClipboardModel(ModelData[] models) {
        this.models = models;
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
        ClipboardType type = stack.getOrDefault(AllDataComponents.CLIPBOARD_TYPE, ClipboardType.EMPTY);
        int index = type.ordinal();
        state.addModelKey(this);
        state.addModelKey(index);
        models[index].update(state, layer, displayContext);

    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public void resolve(Resolver resolver) {
            resolver.markDependency(EMPTY_ID);
            resolver.markDependency(WRITTEN_ID);
            resolver.markDependency(EDITING_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            Baker baker = context.blockModelBaker();
            ModelData[] models = new ModelData[3];
            models[0] = ModelData.bake(baker, EMPTY_ID);
            models[1] = ModelData.bake(baker, WRITTEN_ID);
            models[2] = ModelData.bake(baker, EDITING_ID);
            return new ClipboardModel(models);
        }
    }

    public record ModelData(List<BakedQuad> quads, ModelSettings settings, Supplier<Vector3f[]> vector) {
        public static ModelData bake(Baker baker, Identifier id) {
            BakedSimpleModel model = baker.getModel(id);
            ModelTextures textures = model.getTextures();
            List<BakedQuad> quads = model.bakeGeometry(textures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, model, textures);
            return new ModelData(quads, settings, Suppliers.memoize(() -> BasicItemModel.bakeQuads(quads)));
        }

        public void update(ItemRenderState state, RenderLayer layer, ItemDisplayContext displayContext) {
            ItemRenderState.LayerRenderState layerRenderState = state.newLayer();
            layerRenderState.setRenderLayer(layer);
            layerRenderState.setVertices(vector);
            settings.addSettings(layerRenderState, displayContext);
            layerRenderState.getQuads().addAll(quads);
        }
    }
}
