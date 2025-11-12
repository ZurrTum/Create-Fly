package com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip;

import com.zurrtum.create.client.content.contraptions.IDisplayAssemblyExceptions;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.bearing.BearingBlock;
import com.zurrtum.create.content.contraptions.bearing.MechanicalBearingBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class MechanicalBearingTooltipBehaviour extends GeneratingKineticTooltipBehaviour<MechanicalBearingBlockEntity> implements IDisplayAssemblyExceptions {
    public MechanicalBearingTooltipBehaviour(MechanicalBearingBlockEntity be) {
        super(be);
    }

    @Override
    public boolean addToTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        if (super.addToTooltip(tooltip, isPlayerSneaking))
            return true;
        if (isPlayerSneaking)
            return false;
        if (!blockEntity.isWindmill() && blockEntity.getSpeed() == 0)
            return false;
        if (blockEntity.isRunning())
            return false;
        BlockState state = blockEntity.getBlockState();
        if (!(state.getBlock() instanceof BearingBlock))
            return false;

        BlockState attachedState = blockEntity.getLevel().getBlockState(blockEntity.getBlockPos().relative(state.getValue(BearingBlock.FACING)));
        if (attachedState.canBeReplaced())
            return false;
        TooltipHelper.addHint(tooltip, "hint.empty_bearing");
        return true;
    }

    @Override
    public AssemblyException getLastAssemblyException() {
        return blockEntity.getLastAssemblyException();
    }
}
