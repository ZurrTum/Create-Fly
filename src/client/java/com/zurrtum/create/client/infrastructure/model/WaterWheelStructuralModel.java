package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.kinetics.waterwheel.LargeWaterWheelBlockEntity;
import com.zurrtum.create.content.kinetics.waterwheel.WaterWheelStructuralBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class WaterWheelStructuralModel extends WrapperBlockStateModel {
    public static final WaterWheelStructuralModel INSTANCE = new WaterWheelStructuralModel();

    public static WaterWheelStructuralModel single(BlockState state, UnbakedGrouped unbaked) {
        return INSTANCE;
    }

    @Override
    public Sprite particleSpriteWithInfo(BlockRenderView world, BlockPos pos, BlockState state) {
        BlockPos master = WaterWheelStructuralBlock.getMaster(world, pos, state);
        if (world.getBlockEntity(master) instanceof LargeWaterWheelBlockEntity blockEntity) {
            return MinecraftClient.getInstance().getBlockRenderManager().getModel(blockEntity.material).particleSprite();
        }
        return particleSprite();
    }

    @Override
    public Sprite particleSprite() {
        return MinecraftClient.getInstance().getBlockRenderManager().getModels().getModelManager().getMissingModel().particleSprite();
    }

    @Override
    public void addParts(Random random, List<BlockModelPart> parts) {
    }
}
