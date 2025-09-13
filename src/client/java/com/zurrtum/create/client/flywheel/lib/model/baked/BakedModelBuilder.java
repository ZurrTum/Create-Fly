package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.lib.internal.FlwLibXplat;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public final class BakedModelBuilder {
    final BlockStateModel model;
    final GeometryBakedModel bakedModel;
    @Nullable BlockRenderView level;
    @Nullable BlockPos pos;
    @Nullable MatrixStack poseStack;
    @Nullable BiFunction<BlockRenderLayer, Boolean, Material> materialFunc;

    public BakedModelBuilder(GeometryBakedModel bakedModel) {
        this.bakedModel = bakedModel;
        this.model = null;
    }

    public BakedModelBuilder(BlockStateModel model) {
        this.model = model;
        this.bakedModel = null;
    }

    public BakedModelBuilder level(@Nullable BlockRenderView level) {
        this.level = level;
        return this;
    }

    public BakedModelBuilder pos(@Nullable BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public BakedModelBuilder poseStack(@Nullable MatrixStack poseStack) {
        this.poseStack = poseStack;
        return this;
    }

    public BakedModelBuilder materialFunc(@Nullable BiFunction<BlockRenderLayer, Boolean, Material> materialFunc) {
        this.materialFunc = materialFunc;
        return this;
    }

    public SimpleModel build() {
        if (level == null) {
            level = EmptyVirtualBlockGetter.FULL_DARK;
        }
        if (pos == null) {
            pos = BlockPos.ORIGIN;
        }
        if (materialFunc == null) {
            materialFunc = ModelUtil::getMaterial;
        }

        return FlwLibXplat.INSTANCE.buildBakedModelBuilder(this);
    }
}