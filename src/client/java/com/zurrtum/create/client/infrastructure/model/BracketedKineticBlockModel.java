package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

public class BracketedKineticBlockModel extends WrapperBlockStateModel {
    public BracketedKineticBlockModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        BracketedBlockEntityBehaviour attachmentBehaviour = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (attachmentBehaviour == null) {
            return;
        }
        BlockState bracket = attachmentBehaviour.getBracket();
        if (bracket == null) {
            return;
        }
        BlockStateModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(bracket);
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            model.collectParts(random, parts);
        }
    }
}
