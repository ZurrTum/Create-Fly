package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3fc;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class TranslucentModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/translucent");
    private final RenderType layer = Sheets.translucentItemSheet();
    private final List<ItemTintSource> tints;
    private final List<BakedQuad> quads;
    private final Supplier<Vector3fc[]> vector;
    private final ModelRenderProperties settings;
    private final boolean animated;

    public TranslucentModel(List<ItemTintSource> tints, List<BakedQuad> quads, ModelRenderProperties settings) {
        this.tints = tints;
        this.quads = quads;
        this.settings = settings;
        this.vector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(quads));
        boolean bl = false;

        for (BakedQuad bakedQuad : quads) {
            if (bakedQuad.sprite().contents().isAnimated()) {
                bl = true;
                break;
            }
        }

        this.animated = bl;
    }

    @Override
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner heldItemContext,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        ItemStackRenderState.LayerRenderState layerRenderState = state.newLayer();
        if (stack.hasFoil()) {
            layerRenderState.setFoilType(ItemStackRenderState.FoilType.STANDARD);
            state.setAnimated();
            state.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
        }

        int i = tints.size();
        int[] is = layerRenderState.prepareTintLayers(i);

        for (int j = 0; j < i; j++) {
            int k = tints.get(j).calculate(stack, world, heldItemContext == null ? null : heldItemContext.asLivingEntity());
            is[j] = k;
            state.appendModelIdentityElement(k);
        }

        layerRenderState.setExtents(vector);
        layerRenderState.setRenderType(layer);
        settings.applyToLayer(layerRenderState, displayContext);
        layerRenderState.prepareQuadList().addAll(quads);
        if (animated) {
            state.setAnimated();
        }
    }

    public record Unbaked(Identifier model, List<ItemTintSource> tints) implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.TranslucentModel.Unbaked> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(com.zurrtum.create.client.infrastructure.model.TranslucentModel.Unbaked::model),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of())
                    .forGetter(com.zurrtum.create.client.infrastructure.model.TranslucentModel.Unbaked::tints)
            ).apply(instance, com.zurrtum.create.client.infrastructure.model.TranslucentModel.Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel bakedSimpleModel = baker.getModel(model);
            TextureSlots modelTextures = bakedSimpleModel.getTopTextureSlots();
            List<BakedQuad> list = bakedSimpleModel.bakeTopGeometry(modelTextures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties modelSettings = ModelRenderProperties.fromResolvedModel(baker, bakedSimpleModel, modelTextures);
            return new TranslucentModel(tints, list, modelSettings);
        }

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.TranslucentModel.Unbaked> type() {
            return CODEC;
        }
    }
}
