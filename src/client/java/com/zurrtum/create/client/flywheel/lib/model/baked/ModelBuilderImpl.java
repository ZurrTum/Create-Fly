package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ModelBuilderImpl {
    private ModelBuilderImpl() {
    }

    public static SimpleModel buildBakedModelBuilder(BakedModelBuilder builder) {
        return BakedModelBufferer.bufferModel(
            builder.model,
            builder.pos,
            builder.level,
            builder.level.getBlockState(builder.pos),
            builder.poseStack,
            builder.materialFunc
        );
    }

    public static SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
        return BakedModelBufferer.bufferBlocks(
            builder.positions.iterator(),
            builder.level,
            builder.poseStack,
            builder.renderFluids,
            builder.materialFunc
        );
    }

    public static SimpleModel buildSingleBlockModelBuilder(BlockModelBuilder builder) {
        return BakedModelBufferer.bufferBlock(
            builder.level,
            builder.poseStack,
            builder.renderFluids,
            builder.materialFunc
        );
    }
}
