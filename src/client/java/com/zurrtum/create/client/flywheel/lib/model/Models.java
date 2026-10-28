package com.zurrtum.create.client.flywheel.lib.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedModelBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.BlockModelBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.model.baked.SinglePosVirtualBlockGetter;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * A collection of methods for creating models from various sources.
 * <br>
 * All Models returned from this class are cached, so calling the same
 * method with the same parameters will return the same object.
 */
public final class Models {
    private static final RendererReloadCache<BlockState, Model> BLOCK_STATE = new RendererReloadCache<>(it -> new BlockModelBuilder(SinglePosVirtualBlockGetter.createFullDark().blockState(it),
        List.of(BlockPos.ZERO)
    ).build());
    private static final RendererReloadCache<PartialModel, Model> PARTIAL = new RendererReloadCache<>(it -> new BakedModelBuilder(
        it.get()).build());
    private static final RendererReloadCache<PartialModel, Model> CHUNK_PARTIAL = new RendererReloadCache<>(it -> new BakedModelBuilder(
        it.get()).materialFunc(ModelUtil::getChunkMaterial).build());
    private static final RendererReloadCache<TransformedPartial<?>, Model> TRANSFORMED_PARTIAL = new RendererReloadCache<>(
        TransformedPartial::create);
    private static final RendererReloadCache<TransformedPartial<?>, Model> CHUNK_TRANSFORMED_PARTIAL = new RendererReloadCache<>(
        TransformedPartial::createChunk);

    private Models() {
    }

    /**
     * Get a usable model for a given block state.
     *
     * @param state The block state you wish to render.
     * @return A model corresponding to how the given block state would appear in the level.
     */
    public static Model block(BlockState state) {
        return BLOCK_STATE.get(state);
    }

    /**
     * Get a usable model for a given partial model.
     *
     * @param partial The partial model you wish to render.
     * @return A model built from the baked model the partial model represents.
     */
    public static Model partial(PartialModel partial) {
        return PARTIAL.get(partial);
    }

    /**
     * Get a usable model for a given partial model. World-space normal based darkening will be applied.
     * Need to implement ShaderLightVisual and override the setSectionCollector method.
     *
     * @param partial The partial model you wish to render.
     * @return A model built from the baked model the partial model represents.
     */
    public static Model chunkPartial(PartialModel partial) {
        return CHUNK_PARTIAL.get(partial);
    }

    /**
     * Get a usable model for a given partial model, transformed in some way.
     * <br>
     * In general, you should try to avoid captures in the transformer function,
     * i.e. prefer static method references over lambdas.
     *
     * @param partial     The partial model you wish to render.
     * @param key         A key that will be used to cache the transformed model.
     * @param transformer A function that will transform the model in some way.
     * @param <T>         The type of the key.
     * @return A model built from the baked model the partial model represents, transformed by the given function.
     */
    public static <T> Model partial(PartialModel partial, T key, BiConsumer<T, PoseStack> transformer) {
        return TRANSFORMED_PARTIAL.get(new TransformedPartial<>(partial, key, transformer));
    }

    /**
     * Get a usable model for a given partial model, transformed in some way. World-space normal based darkening will be applied.
     * Need to implement ShaderLightVisual and override the setSectionCollector method.
     * <br>
     * In general, you should try to avoid captures in the transformer function,
     * i.e. prefer static method references over lambdas.
     *
     * @param partial     The partial model you wish to render.
     * @param key         A key that will be used to cache the transformed model.
     * @param transformer A function that will transform the model in some way.
     * @param <T>         The type of the key.
     * @return A model built from the baked model the partial model represents, transformed by the given function.
     */
    public static <T> Model chunkPartial(PartialModel partial, T key, BiConsumer<T, PoseStack> transformer) {
        return CHUNK_TRANSFORMED_PARTIAL.get(new TransformedPartial<>(partial, key, transformer));
    }

    /**
     * Get a usable model for a given partial model, transformed to face a given direction.
     * <br>
     * {@link Direction#NORTH} is considered the default direction and the corresponding transform will be a no-op.
     *
     * @param partial The partial model you wish to render.
     * @param dir     The direction you wish the model to be rotated to.
     * @return A model built from the baked model the partial model represents, transformed to face the given direction.
     */
    public static Model partial(PartialModel partial, Direction dir) {
        return partial(partial, dir, Models::rotateAboutCenterToFace);
    }

    private static void rotateAboutCenterToFace(Direction facing, PoseStack stack) {
        switch (facing.getOpposite()) {
            case DOWN -> stack.rotateXCentered(Radians.DEG_270);
            case UP -> stack.rotateXCentered(Radians.DEG_90);
            case SOUTH -> stack.rotateYCentered(Radians.DEG_180);
            case WEST -> stack.rotateYCentered(Radians.DEG_90);
            case EAST -> stack.rotateYCentered(Radians.DEG_270);
        }
    }

    private record TransformedPartial<T>(PartialModel partial, T key, BiConsumer<T, PoseStack> transformer) {
        private Model create() {
            var stack = new PoseStack();
            transformer.accept(key, stack);
            return new BakedModelBuilder(partial.get()).poseStack(stack).build();
        }

        public Model createChunk() {
            var stack = new PoseStack();
            transformer.accept(key, stack);
            return new BakedModelBuilder(partial.get()).poseStack(stack).materialFunc(ModelUtil::getChunkMaterial)
                .build();
        }
    }
}
