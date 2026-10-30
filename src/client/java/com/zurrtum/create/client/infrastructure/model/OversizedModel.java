package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.math.Transformation;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

public class OversizedModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/oversized");
    private final List<ItemTintSource> tints;
    private final boolean animated;
    private final ItemQuads itemQuads;
    private final Supplier<Vector3fc[]> extents;
    private final ModelRenderProperties properties;
    private final Matrix4fc transformation;
    private final AABB box;

    public OversizedModel(
        List<ItemTintSource> tints,
        QuadCollection quads,
        ModelRenderProperties properties,
        Matrix4fc transformation,
        AABB box
    ) {
        this.tints = tints;
        animated = quads.hasMaterialFlag(2);
        itemQuads = ItemQuads.split(quads.getAll());
        this.properties = properties;
        this.transformation = transformation;
        extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(quads.getAll()));
        this.box = box;
    }

    @Override
    public void update(
        ItemStackRenderState output,
        ItemStack item,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel level,
        @Nullable ItemOwner owner,
        int seed
    ) {
        output.appendModelIdentityElement(this);
        LayerRenderState layerRenderState = submitQuads(output, properties, displayContext, itemQuads);
        if (item.hasFoil()) {
            layerRenderState.setFoilType(FoilType.STANDARD);
            output.setAnimated();
            output.appendModelIdentityElement(FoilType.STANDARD);
        } else if (animated) {
            output.setAnimated();
        }
        if (!tints.isEmpty()) {
            IntList tintLayers = layerRenderState.tintLayers();

            for (ItemTintSource tintSource : tints) {
                int tint = tintSource.calculate(item, level, owner == null ? null : owner.asLivingEntity());
                tintLayers.add(tint);
                output.appendModelIdentityElement(tint);
            }
        }
        layerRenderState.setExtents(extents);
        layerRenderState.setLocalTransform(transformation);
        if (displayContext == ItemDisplayContext.GUI) {
            output.setOversizedInGui(true);
            output.cachedModelBoundingBox = box;
        }
    }

    public record Unbaked(Identifier model, Optional<Transformation> transformation, List<ItemTintSource> tints,
                          List<Double> min, List<Double> max) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("model").forGetter(Unbaked::model),
            Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(Unbaked::transformation),
            ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints),
            Codec.DOUBLE.listOf(3, 3).fieldOf("min").forGetter(Unbaked::min),
            Codec.DOUBLE.listOf(3, 3).fieldOf("max").forGetter(Unbaked::max)
        ).apply(instance, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel bakedSimpleModel = baker.getModel(model);
            TextureSlots modelTextures = bakedSimpleModel.getTopTextureSlots();
            QuadCollection quads = bakedSimpleModel.bakeTopGeometry(modelTextures, baker, BlockModelRotation.IDENTITY);
            ModelRenderProperties modelSettings = ModelRenderProperties.fromResolvedModel(
                baker,
                bakedSimpleModel,
                modelTextures
            );
            Matrix4fc modelTransform = Transformation.compose(transformation, this.transformation);
            return new OversizedModel(
                tints,
                quads,
                modelSettings,
                modelTransform,
                new AABB(min.get(0), min.get(1), min.get(2), max.get(0), max.get(1), max.get(2))
            );
        }

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }
    }
}
