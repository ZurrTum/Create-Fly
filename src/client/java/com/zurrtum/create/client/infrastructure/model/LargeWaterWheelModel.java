package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class LargeWaterWheelModel extends WrapperBlockStateModel {
    public LargeWaterWheelModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked);
    }

    @Override
    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        if (world.getBlockEntity(pos) instanceof LargeWaterWheelBlockEntity blockEntity) {
            return MinecraftClient.getInstance().getBlockRenderManager().getModel(blockEntity.material).particleSprite();
        } else {
            return model.particleSprite();
        }
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
    }
}
