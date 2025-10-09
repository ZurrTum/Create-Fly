package com.zurrtum.create.client.content.kinetics.waterwheel;

import com.zurrtum.create.catnip.registry.RegisteredObjectsHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.*;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer.KineticRenderState;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlock;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelBlock;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelBlockEntity;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class WaterWheelRenderer<T extends WaterWheelBlockEntity> extends KineticBlockEntityRenderer<T, KineticRenderState> {
    public static final SuperByteBufferCache.Compartment<ModelKey> WATER_WHEEL = new SuperByteBufferCache.Compartment<>();

    public static final StitchedSprite OAK_PLANKS_TEMPLATE = new StitchedSprite(Identifier.of("block/oak_planks"));
    public static final StitchedSprite OAK_LOG_TEMPLATE = new StitchedSprite(Identifier.of("block/oak_log"));
    public static final StitchedSprite OAK_LOG_TOP_TEMPLATE = new StitchedSprite(Identifier.of("block/oak_log_top"));

    protected final boolean large;

    public WaterWheelRenderer(BlockEntityRendererFactory.Context context, boolean large) {
        super(context);
        this.large = large;
    }

    public static <T extends WaterWheelBlockEntity> WaterWheelRenderer<T> standard(BlockEntityRendererFactory.Context context) {
        return new WaterWheelRenderer<>(context, false);
    }

    public static <T extends WaterWheelBlockEntity> WaterWheelRenderer<T> large(BlockEntityRendererFactory.Context context) {
        return new WaterWheelRenderer<>(context, true);
    }

    @Override
    protected SuperByteBuffer getRotatedModel(T be, KineticRenderState state) {
        ModelKey key = new ModelKey(large, state.blockState, be.material);
        return SuperByteBufferCache.getInstance().get(
            WATER_WHEEL, key, () -> {
                GeometryBakedModel model = generateModel(key);
                BlockState state1 = key.state();
                Direction dir;
                if (key.large()) {
                    dir = Direction.from(state1.get(LargeWaterWheelBlock.AXIS), AxisDirection.POSITIVE);
                } else {
                    dir = state1.get(WaterWheelBlock.FACING);
                }
                MatrixStack transform = CachedBuffers.rotateToFaceVertical(dir).get();
                return SuperBufferFactory.getInstance().createForBlock(model, Blocks.AIR.getDefaultState(), transform);
            }
        );
    }

    public static GeometryBakedModel generateModel(ModelKey key) {
        return generateModel(Variant.of(key.large(), key.state()), key.material());
    }

    public static GeometryBakedModel generateModel(Variant variant, BlockState material) {
        return generateModel(variant.model(), material);
    }

    public static GeometryBakedModel generateModel(GeometryBakedModel template, BlockState planksBlockState) {
        Block planksBlock = planksBlockState.getBlock();
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        String wood = plankStateToWoodName(planksBlockState);

        if (wood == null)
            return BakedModelHelper.generateModel(template, sprite -> null);

        String namespace = id.getNamespace();
        BlockState logBlockState = getLogBlockState(namespace, wood);

        Map<Sprite, Sprite> map = new Reference2ReferenceOpenHashMap<>();
        map.put(OAK_PLANKS_TEMPLATE.get(), getSpriteOnSide(planksBlockState, Direction.UP));
        map.put(OAK_LOG_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.SOUTH));
        map.put(OAK_LOG_TOP_TEMPLATE.get(), getSpriteOnSide(logBlockState, Direction.UP));

        return BakedModelHelper.generateModel(template, map::get);
    }

    @Nullable
    private static String plankStateToWoodName(BlockState planksBlockState) {
        Block planksBlock = planksBlockState.getBlock();
        Identifier id = RegisteredObjectsHelper.getKeyOrThrow(planksBlock);
        String path = id.getPath();

        if (path.endsWith("_planks")) // Covers most wood types
            return path.substring(0, path.length() - 7);

        if (path.contains("wood/planks/")) // TerraFirmaCraft
            return path.substring(12);

        return null;
    }

    private static final String[] LOG_LOCATIONS = new String[]{

        "x_log", "x_stem", "x_block", // Covers most wood types
        "wood/log/x" // TerraFirmaCraft

    };

    private static BlockState getLogBlockState(String namespace, String wood) {
        for (String location : LOG_LOCATIONS) {
            Optional<BlockState> state = Registries.BLOCK.getOptional(RegistryKey.of(
                RegistryKeys.BLOCK,
                Identifier.of(namespace, location.replace("x", wood))
            )).map(RegistryEntry::value).map(Block::getDefaultState);
            if (state.isPresent())
                return state.get();
        }
        return Blocks.OAK_LOG.getDefaultState();
    }

    private static Sprite getSpriteOnSide(BlockState state, Direction side) {
        BlockStateModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(state);
        if (model == null)
            return null;
        Random random = Random.create();
        random.setSeed(42L);
        List<BlockModelPart> parts = model.getParts(random);
        for (BlockModelPart part : parts) {
            List<BakedQuad> quads = part.getQuads(side);
            if (!quads.isEmpty()) {
                return quads.getFirst().sprite();
            }
        }
        random.setSeed(42L);
        for (BlockModelPart part : parts) {
            List<BakedQuad> quads = part.getQuads(null);
            if (!quads.isEmpty()) {
                for (BakedQuad quad : quads) {
                    if (quad.face() == side) {
                        return quad.sprite();
                    }
                }
            }
        }
        return model.particleSprite();
    }

    public enum Variant {
        SMALL(AllPartialModels.WATER_WHEEL),
        LARGE(AllPartialModels.LARGE_WATER_WHEEL),
        LARGE_EXTENSION(AllPartialModels.LARGE_WATER_WHEEL_EXTENSION),
        ;

        private final PartialModel partial;

        Variant(PartialModel partial) {
            this.partial = partial;
        }

        public GeometryBakedModel model() {
            return partial.get();
        }

        public static Variant of(boolean large, BlockState blockState) {
            if (large) {
                boolean extension = blockState.get(LargeWaterWheelBlock.EXTENSION);
                if (extension) {
                    return LARGE_EXTENSION;
                } else {
                    return LARGE;
                }
            } else {
                return SMALL;
            }
        }
    }

    public record ModelKey(boolean large, BlockState state, BlockState material) {
    }
}
