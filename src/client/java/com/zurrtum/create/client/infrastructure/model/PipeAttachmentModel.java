package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllCTBehaviours;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.content.decoration.bracket.BracketedBlockEntityBehaviour;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour.AttachmentTypes;
import com.zurrtum.create.content.fluids.FluidTransportBehaviour.AttachmentTypes.ComponentPartials;
import com.zurrtum.create.content.fluids.pipes.FluidPipeBlock;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.BlockRenderView;

import java.util.List;
import java.util.Optional;

public class PipeAttachmentModel extends WrapperBlockStateModel {
    public PipeAttachmentModel(BlockState state, UnbakedGrouped unbaked) {
        super(state, unbaked);
    }

    public static UnbakedGrouped encased(BlockState state, UnbakedGrouped unbaked) {
        return new PipeAttachmentModel(state, new CTModel(state, unbaked, AllCTBehaviours.COPPER_CASING));
    }

    @Override
    public void addPartsWithInfo(BlockRenderView world, BlockPos pos, BlockState state, Random random, List<BlockModelPart> parts) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            model.addParts(random, parts);
        }
        Optional.ofNullable(BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE)).map(BracketedBlockEntityBehaviour::getBracket)
            .map(bracket -> MinecraftClient.getInstance().getBlockRenderManager().getModel(bracket))
            .ifPresent(model -> model.addParts(random, parts));
        FluidTransportBehaviour transport = BlockEntityBehaviour.get(world, pos, FluidTransportBehaviour.TYPE);
        if (transport != null) {
            for (Direction direction : Iterate.directions) {
                AttachmentTypes type = transport.getRenderedRimAttachment(world, pos, state, direction);
                for (ComponentPartials partial : type.partials) {
                    parts.add(AllPartialModels.PIPE_ATTACHMENTS.get(partial).get(direction).get());
                }
            }
        }
        if (FluidPipeBlock.shouldDrawCasing(world, pos, state)) {
            parts.add(AllPartialModels.FLUID_PIPE_CASING.get());
        }
    }
}
