package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class LargeWaterWheelModel extends WrapperBlockStateModel {
    public LargeWaterWheelModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public TextureAtlasSprite particleSpriteWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof LargeWaterWheelBlockEntity blockEntity) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(blockEntity.material).particleIcon();
        } else {
            return model.particleIcon();
        }
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
    }
}
