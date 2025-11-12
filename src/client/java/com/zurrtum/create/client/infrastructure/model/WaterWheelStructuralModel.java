package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class WaterWheelStructuralModel extends WrapperBlockStateModel {
    public static final WaterWheelStructuralModel INSTANCE = new WaterWheelStructuralModel();

    public static WaterWheelStructuralModel single(BlockState state, UnbakedRoot unbaked) {
        return INSTANCE;
    }

    @Override
    public TextureAtlasSprite particleSpriteWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        BlockPos master = WaterWheelStructuralBlock.getMaster(world, pos, state);
        if (world.getBlockEntity(master) instanceof LargeWaterWheelBlockEntity blockEntity) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(blockEntity.material).particleIcon();
        }
        return particleIcon();
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getModelManager().getMissingBlockStateModel().particleIcon();
    }

    @Override
    public void collectParts(RandomSource random, List<BlockModelPart> parts) {
    }
}
