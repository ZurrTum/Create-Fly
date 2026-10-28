package com.zurrtum.create.client.content.kinetics.waterwheel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.*;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache.Compartment;
import com.zurrtum.create.client.content.kinetics.base.SingleKineticRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelBlock;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockStateModelSet;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.*;

public class WaterWheelRenderer implements BlockEntityRenderer<WaterWheelBlockEntity, SingleKineticRenderState> {
    public static final Compartment<ModelKey> WATER_WHEEL = new Compartment<>();

    public static final StitchedSprite OAK_PLANKS_TEMPLATE = new StitchedSprite(Identifier.parse("block/oak_planks"));
    public static final StitchedSprite OAK_LOG_TEMPLATE = new StitchedSprite(Identifier.parse("block/oak_log"));
    public static final StitchedSprite OAK_LOG_TOP_TEMPLATE = new StitchedSprite(Identifier.parse("block/oak_log_top"));

    protected ModelKey key;

    public WaterWheelRenderer(boolean large) {
        key = new ModelKey(large);
    }

    public static WaterWheelRenderer standard(Context context) {
        return new WaterWheelRenderer(false);
    }

    public static WaterWheelRenderer large(Context context) {
        return new WaterWheelRenderer(true);
    }

    @Override
    public SingleKineticRenderState createRenderState() {
        return new SingleKineticRenderState();
    }

    @Override
    public void extractRenderState(
        WaterWheelBlockEntity be,
        SingleKineticRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        @Nullable CrumblingOverlay breakProgress
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, breakProgress);
        state.model = getRotatedModel(be.material, state.blockState).cardinalLighting(level).light(state.lightCoords)
            .color(getTintColor(be)).extractRenderState();
        Axis axis = getRotationAxisOf(state.blockState);
        state.axis = axis;
        state.angle = getDegreesWithoutBeOffset(axis, be, state, level);
    }

    @Override
    public void submit(
        SingleKineticRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState camera
    ) {
        state.submit(matrices, queue);
    }

    private SuperByteBuffer getRotatedModel(BlockState material, BlockState blockState) {
        key.update(blockState, material);
        return SuperByteBufferCache.getInstance().get(WATER_WHEEL, key, this::createRotatedModel);
    }

    private SuperByteBuffer createRotatedModel() {
        ModelKey current = key;
        key = new ModelKey(key.large);
        BlockStateModel model = generateModel(current);
        Direction dir;
        if (current.large) {
            dir = Direction.fromAxisAndDirection(
                current.state.getValue(LargeWaterWheelBlock.AXIS),
                AxisDirection.POSITIVE
            );
        } else {
            dir = current.state.getValue(WaterWheelBlock.FACING);
        }
        return SuperBufferFactory.getInstance()
            .createForBlock(model, Blocks.AIR.defaultBlockState(), CachedBuffers.rotateToFaceVertical(dir));
    }

    public static BlockStateModel generateModel(ModelKey key) {
        return generateModel(Variant.of(key.large, key.state), key.material);
    }

    public static BlockStateModel generateModel(Variant variant, BlockState material) {
        return generateModel(variant.model(), material);
    }

    public static BlockStateModel generateModel(BlockStateModel template, BlockState planksBlockState) {
        Block planksBlock = planksBlockState.getBlock();
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        String wood = plankStateToWoodName(planksBlockState);

        if (wood == null) {
            return BakedModelHelper.generateModel(template, sprite -> null);
        }

        String namespace = id.getNamespace();
        BlockState logBlockState = getLogBlockState(namespace, wood);

        Map<TextureAtlasSprite, TextureAtlasSprite> map = new Reference2ReferenceOpenHashMap<>();
        BlockStateModelSet blockStateModelSet = Minecraft.getInstance().getModelManager().getBlockStateModelSet();
        List<BlockStateModelPart> parts = new ObjectArrayList<>();
        map.put(OAK_PLANKS_TEMPLATE.get(), getSpriteOnSide(blockStateModelSet, planksBlockState, Direction.UP, parts));
        parts.clear();
        map.put(OAK_LOG_TEMPLATE.get(), getSpriteOnSide(blockStateModelSet, logBlockState, Direction.SOUTH, parts));
        parts.clear();
        map.put(OAK_LOG_TOP_TEMPLATE.get(), getSpriteOnSide(blockStateModelSet, logBlockState, Direction.UP, parts));

        return BakedModelHelper.generateModel(template, map::get);
    }

    @Nullable
    private static String plankStateToWoodName(BlockState planksBlockState) {
        Block planksBlock = planksBlockState.getBlock();
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        String path = id.getPath();

        if (path.endsWith("_planks")) // Covers most wood types
        {
            return (path.startsWith("archwood") ? "blue_" : "") + path.substring(0, path.length() - 7);
        }

        if (path.contains("wood/planks/")) // TerraFirmaCraft
        {
            return path.substring(12);
        }

        return null;
    }

    private static final String[] LOG_LOCATIONS = new String[]{

        "x_log", "x_stem", "x_block", // Covers most wood types
        "wood/log/x" // TerraFirmaCraft

    };

    private static BlockState getLogBlockState(String namespace, String wood) {
        for (String location : LOG_LOCATIONS) {
            Optional<BlockState> state = BuiltInRegistries.BLOCK.get(ResourceKey.create(
                Registries.BLOCK,
                Identifier.fromNamespaceAndPath(namespace, location.replace("x", wood))
            )).map(Holder::value).map(Block::defaultBlockState);
            if (state.isPresent()) {
                return state.get();
            }
        }
        return Blocks.OAK_LOG.defaultBlockState();
    }

    private static TextureAtlasSprite getSpriteOnSide(
        BlockStateModelSet blockStateModelSet,
        BlockState state,
        Direction side,
        List<BlockStateModelPart> parts
    ) {
        BlockStateModel model = blockStateModelSet.get(state);
        RandomSource random = RandomSource.create();
        random.setSeed(42L);
        model.collectParts(random, parts);
        for (BlockStateModelPart part : parts) {
            List<BakedQuad> quads = part.getQuads(side);
            if (!quads.isEmpty()) {
                return quads.getFirst().materialInfo().sprite();
            }
        }
        random.setSeed(42L);
        for (BlockStateModelPart part : parts) {
            List<BakedQuad> quads = part.getQuads(null);
            if (!quads.isEmpty()) {
                for (BakedQuad quad : quads) {
                    if (quad.direction() == side) {
                        return quad.materialInfo().sprite();
                    }
                }
            }
        }
        return model.particleMaterial().sprite();
    }

    public enum Variant {
        SMALL(AllPartialModels.WATER_WHEEL),
        LARGE(AllPartialModels.LARGE_WATER_WHEEL),
        LARGE_EXTENSION(AllPartialModels.LARGE_WATER_WHEEL_EXTENSION);

        private final PartialModel partial;

        Variant(PartialModel partial) {
            this.partial = partial;
        }

        public BlockStateModel model() {
            return partial.get();
        }

        public static Variant of(boolean large, BlockState blockState) {
            if (large) {
                boolean extension = blockState.getValue(LargeWaterWheelBlock.EXTENSION);
                if (extension) {
                    return LARGE_EXTENSION;
                }
                return LARGE;
            }
            return SMALL;
        }
    }

    public static class ModelKey {
        public final boolean large;
        public @UnknownNullability BlockState state;
        public @UnknownNullability BlockState material;

        public ModelKey(boolean large) {
            this.large = large;
        }

        public void update(BlockState state, BlockState material) {
            this.state = state;
            this.material = material;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ModelKey modelKey = (ModelKey) obj;
            return large == modelKey.large && state.equals(modelKey.state) && material.equals(modelKey.material);
        }

        @Override
        public int hashCode() {
            int result = Boolean.hashCode(large);
            result = 31 * result + state.hashCode();
            result = 31 * result + material.hashCode();
            return result;
        }
    }
}
