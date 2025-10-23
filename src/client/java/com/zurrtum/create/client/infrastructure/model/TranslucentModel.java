package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.tint.TintSource;
import net.minecraft.client.render.item.tint.TintSourceTypes;
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

public class TranslucentModel implements ItemModel {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/translucent");
    private final RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final List<TintSource> tints;
    private final List<BakedQuad> quads;
    private final Supplier<Vector3f[]> vector;
    private final ModelSettings settings;
    private final boolean animated;

    public TranslucentModel(List<TintSource> tints, List<BakedQuad> quads, ModelSettings settings) {
        this.tints = tints;
        this.quads = quads;
        this.settings = settings;
        this.vector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(this.quads));
        boolean bl = false;

        for (BakedQuad bakedQuad : quads) {
            if (bakedQuad.sprite().isAnimated()) {
                bl = true;
                break;
            }
        }

        this.animated = bl;
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
        if (stack.hasGlint()) {
            layerRenderState.setGlint(ItemRenderState.Glint.STANDARD);
            state.markAnimated();
            state.addModelKey(ItemRenderState.Glint.STANDARD);
        }

        int i = tints.size();
        int[] is = layerRenderState.initTints(i);

        for (int j = 0; j < i; ++j) {
            int k = tints.get(j).getTint(stack, world, user);
            is[j] = k;
            state.addModelKey(k);
        }

        layerRenderState.setVertices(vector);
        layerRenderState.setRenderLayer(layer);
        settings.addSettings(layerRenderState, displayContext);
        layerRenderState.getQuads().addAll(quads);
        if (animated) {
            state.markAnimated();
        }
    }

    public record Unbaked(Identifier model, List<TintSource> tints) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
            TintSourceTypes.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints)
        ).apply(instance, Unbaked::new));

        @Override
        public void resolve(ResolvableModel.Resolver resolver) {
            resolver.markDependency(model);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            Baker baker = context.blockModelBaker();
            BakedSimpleModel bakedSimpleModel = baker.getModel(model);
            ModelTextures modelTextures = bakedSimpleModel.getTextures();
            List<BakedQuad> list = bakedSimpleModel.bakeGeometry(modelTextures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings modelSettings = ModelSettings.resolveSettings(baker, bakedSimpleModel, modelTextures);
            return new TranslucentModel(tints, list, modelSettings);
        }

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }
    }
}
