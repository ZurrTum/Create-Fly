package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.lib.internal.FlwLibXplat;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;

public final class BakedModelBuilder {
    final BlockStateModel model;
    final SimpleModelWrapper bakedModel;
    @Nullable BlockAndTintGetter level;
    @Nullable BlockPos pos;
    @Nullable PoseStack poseStack;
    @Nullable BiFunction<ChunkSectionLayer, Boolean, Material> materialFunc;

    public BakedModelBuilder(SimpleModelWrapper bakedModel) {
        this.bakedModel = bakedModel;
        this.model = null;
    }

    public BakedModelBuilder(BlockStateModel model) {
        this.model = model;
        this.bakedModel = null;
    }

    public BakedModelBuilder level(@Nullable BlockAndTintGetter level) {
        this.level = level;
        return this;
    }

    public BakedModelBuilder pos(@Nullable BlockPos pos) {
        this.pos = pos;
        return this;
    }

    public BakedModelBuilder poseStack(@Nullable PoseStack poseStack) {
        this.poseStack = poseStack;
        return this;
    }

    public BakedModelBuilder materialFunc(@Nullable BiFunction<ChunkSectionLayer, Boolean, Material> materialFunc) {
        this.materialFunc = materialFunc;
        return this;
    }

    public SimpleModel build() {
        if (level == null) {
            level = EmptyVirtualBlockGetter.FULL_DARK;
        }
        if (pos == null) {
            pos = BlockPos.ZERO;
        }
        if (materialFunc == null) {
            materialFunc = ModelUtil::getMaterial;
        }

        return FlwLibXplat.INSTANCE.buildBakedModelBuilder(this);
    }
}