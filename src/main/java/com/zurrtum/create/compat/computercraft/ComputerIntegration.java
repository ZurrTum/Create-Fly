package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.api.contraption.BlockMovementChecks.CheckResult;
import dan200.computercraft.shared.peripheral.modem.wired.CableBlock;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessModemBlock;
import net.minecraft.world.level.block.Block;

public class ComputerIntegration {
    public static void register() {
        BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
            Block block = state.getBlock();
            if (block instanceof WirelessModemBlock) {
                return CheckResult.of(state.getValue(WirelessModemBlock.FACING) == direction);
            } else {
                return block instanceof CableBlock ?
                    CheckResult.of(state.getValue(CableBlock.MODEM).getFacing() == direction) : CheckResult.PASS;
            }
        });
    }
}
