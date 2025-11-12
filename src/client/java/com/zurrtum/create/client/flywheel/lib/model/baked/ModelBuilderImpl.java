package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class ModelBuilderImpl {
    private ModelBuilderImpl() {
    }

    public static SimpleModel buildBakedModelBuilder(BakedModelBuilder builder) {
        BlockState blockState = builder.level.getBlockState(builder.pos);
        var builder1 = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

        BakedModelBufferer.ResultConsumer resultConsumer = (renderType, shaded, data) -> {
            Material material = builder.materialFunc.apply(renderType, shaded);
            if (material != null) {
                Mesh mesh = MeshHelper.blockVerticesToMesh(
                    data,
                    "source=BakedModelBuilder," + "bakedModel=" + builder.bakedModel + ",renderType=" + renderType + ",shaded=" + shaded
                );
                builder1.add(renderType, new Model.ConfiguredMesh(material, mesh));
            }
        };
        if (builder.bakedModel != null) {
            BakedModelBufferer.bufferModel(builder.bakedModel, builder.pos, builder.level, blockState, builder.poseStack, resultConsumer);
        } else {
            BakedModelBufferer.bufferModel(builder.model, builder.pos, builder.level, blockState, builder.poseStack, resultConsumer);
        }

        return new SimpleModel(builder1.build());
    }

    public static SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
        var builder1 = ChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();

        BakedModelBufferer.bufferBlocks(
            builder.positions.iterator(), builder.level, builder.poseStack, builder.renderFluids, (renderType, shaded, data) -> {
                Material material = builder.materialFunc.apply(renderType, shaded);
                if (material != null) {
                    Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=BlockModelBuilder," + "renderType=" + renderType + ",shaded=" + shaded);
                    builder1.add(renderType, new Model.ConfiguredMesh(material, mesh));
                }
            }
        );

        return new SimpleModel(builder1.build());
    }
}
