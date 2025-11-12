package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class OversizedModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/oversized");
    private final List<ItemTintSource> tints;
    private final List<BakedQuad> quads;
    private final Supplier<Vector3f[]> vector;
    private final ModelRenderProperties settings;
    private final AABB box;
    private final boolean animated;

    public OversizedModel(List<ItemTintSource> tints, List<BakedQuad> quads, ModelRenderProperties settings, AABB box) {
        this.tints = tints;
        this.quads = quads;
        this.settings = settings;
        this.vector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(this.quads));
        this.box = box;
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
        layerRenderState.setRenderType(ItemBlockRenderTypes.getRenderType(stack));
        settings.applyToLayer(layerRenderState, displayContext);
        layerRenderState.prepareQuadList().addAll(quads);
        if (animated) {
            state.setAnimated();
        }
        if (displayContext == ItemDisplayContext.GUI) {
            state.setOversizedInGui(true);
            state.cachedModelBoundingBox = box;
        }
    }

    public record Unbaked(Identifier model, List<ItemTintSource> tints, List<Double> min, List<Double> max) implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.OversizedModel.Unbaked> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                Identifier.CODEC.fieldOf("model").forGetter(com.zurrtum.create.client.infrastructure.model.OversizedModel.Unbaked::model),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of())
                    .forGetter(com.zurrtum.create.client.infrastructure.model.OversizedModel.Unbaked::tints),
                Codec.DOUBLE.listOf(3, 3).fieldOf("min").forGetter(com.zurrtum.create.client.infrastructure.model.OversizedModel.Unbaked::min),
                Codec.DOUBLE.listOf(3, 3).fieldOf("max").forGetter(com.zurrtum.create.client.infrastructure.model.OversizedModel.Unbaked::max)
            ).apply(instance, com.zurrtum.create.client.infrastructure.model.OversizedModel.Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(this.model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel bakedSimpleModel = baker.getModel(this.model);
            TextureSlots modelTextures = bakedSimpleModel.getTopTextureSlots();
            List<BakedQuad> list = bakedSimpleModel.bakeTopGeometry(modelTextures, baker, BlockModelRotation.X0_Y0).getAll();
            ModelRenderProperties modelSettings = ModelRenderProperties.fromResolvedModel(baker, bakedSimpleModel, modelTextures);
            return new OversizedModel(tints, list, modelSettings, new AABB(min.get(0), min.get(1), min.get(2), max.get(0), max.get(1), max.get(2)));
        }

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.OversizedModel.Unbaked> type() {
            return CODEC;
        }
    }
}
