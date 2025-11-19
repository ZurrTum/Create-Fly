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
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Optional;

public class PipeAttachmentModel extends WrapperBlockStateModel {
    public PipeAttachmentModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
    }

    public static UnbakedRoot encased(BlockState state, UnbakedRoot unbaked) {
        return new PipeAttachmentModel(state, new CTModel(state, unbaked, AllCTBehaviours.COPPER_CASING));
    }

    @Override
    public void addPartsWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state, RandomSource random, List<BlockModelPart> parts) {
        if (model instanceof WrapperBlockStateModel wrapper) {
            wrapper.addPartsWithInfo(world, pos, state, random, parts);
        } else {
            model.collectParts(random, parts);
        }
        Optional.ofNullable(BlockEntityBehaviour.get(world, pos, BracketedBlockEntityBehaviour.TYPE)).map(BracketedBlockEntityBehaviour::getBracket)
            .map(bracket -> Minecraft.getInstance().getBlockRenderer().getBlockModel(bracket)).ifPresent(model -> model.collectParts(random, parts));
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
