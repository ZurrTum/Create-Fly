package com.zurrtum.create.client.foundation.ponder;

import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.foundation.blockEntity.IMultiBlockEntityContainer;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class PonderWorldBlockEntityFix {

    public static void fixControllerBlockEntities(PonderLevel world) {
        for (BlockEntity blockEntity : world.getBlockEntities()) {

            if (blockEntity instanceof BeltBlockEntity beltBlockEntity) {
                if (!beltBlockEntity.isController())
                    continue;
                BlockPos controllerPos = blockEntity.getPos();
                for (BlockPos blockPos : BeltBlock.getBeltChain(world, controllerPos)) {
                    BlockEntity blockEntity2 = world.getBlockEntity(blockPos);
                    if (!(blockEntity2 instanceof BeltBlockEntity belt2))
                        continue;
                    belt2.setController(controllerPos);
                }
            }

            if (blockEntity instanceof IMultiBlockEntityContainer multiBlockEntity) {
                BlockPos lastKnown = multiBlockEntity.getLastKnownPos();
                BlockPos current = blockEntity.getPos();
                if (lastKnown == null || current == null)
                    continue;
                if (multiBlockEntity.isController())
                    continue;
                if (!lastKnown.equals(current)) {
                    BlockPos newControllerPos = multiBlockEntity.getController().add(current.subtract(lastKnown));
                    multiBlockEntity.setController(newControllerPos);
                }
            }

        }
    }

}