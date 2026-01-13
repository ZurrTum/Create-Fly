package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;

public class BracketedKineticBlockModel extends WrapperBlockStateModel {
    public BracketedKineticBlockModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked);
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        BracketedBlockEntityBehaviour attachmentBehaviour = BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE);
        if (attachmentBehaviour == null) {
            return;
        }
        BlockState bracket = attachmentBehaviour.getBracket();
        if (bracket == null) {
            return;
        }
        BlockStateModel model = MinecraftClient.getInstance().getBlockRenderManager().getModel(bracket);
        if (WrapperBlockStateModel.unwrapCompat(model) instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            model.addParts(random, parts);
        }
    }
}
