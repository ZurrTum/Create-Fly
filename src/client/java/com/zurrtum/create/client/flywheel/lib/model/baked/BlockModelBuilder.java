package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.lib.internal.FlwLibXplat;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockRenderView;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

public final class BlockModelBuilder {
    final BlockRenderView level;
    final Iterable<BlockPos> positions;
    @Nullable MatrixStack poseStack;
    boolean renderFluids = false;
    @Nullable BlockMaterialFunction materialFunc;

    public BlockModelBuilder(BlockRenderView level, Iterable<BlockPos> positions) {
        this.level = level;
        this.positions = positions;
    }

    public BlockModelBuilder poseStack(@Nullable MatrixStack poseStack) {
        this.poseStack = poseStack;
        return this;
    }

    public BlockModelBuilder renderFluids(boolean renderFluids) {
        this.renderFluids = renderFluids;
        return this;
    }

    @Deprecated(forRemoval = true)
    public BlockModelBuilder materialFunc(@Nullable BiFunction<BlockRenderLayer, Boolean, Material> materialFunc) {
        if (materialFunc != null) {
            this.materialFunc = (chunkRenderType, shaded, ambientOcclusion) -> materialFunc.apply(chunkRenderType, shaded);
        } else {
            this.materialFunc = null;
        }
        return this;
    }

    public BlockModelBuilder materialFunc(@Nullable BlockMaterialFunction materialFunc) {
        this.materialFunc = materialFunc;
        return this;
    }

    public SimpleModel build() {
        if (materialFunc == null) {
            materialFunc = ModelUtil::getMaterial;
        }

        return FlwLibXplat.INSTANCE.buildBlockModelBuilder(this);
    }
}