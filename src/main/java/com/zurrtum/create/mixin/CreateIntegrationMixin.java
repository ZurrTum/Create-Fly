package com.zurrtum.create.mixin;

import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.api.contraption.BlockMovementChecks.CheckResult;
import dan200.computercraft.shared.integration.CreateIntegration;
import dan200.computercraft.shared.peripheral.modem.wired.CableBlock;
import dan200.computercraft.shared.peripheral.modem.wireless.WirelessModemBlock;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateIntegration.class)
public class CreateIntegrationMixin {
    @Inject(method = "setup()V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void setup(CallbackInfo ci) {
        BlockMovementChecks.registerAttachedCheck((state, world, pos, direction) -> {
            Block block = state.getBlock();
            if (block instanceof WirelessModemBlock) {
                return CheckResult.of(state.getValue(WirelessModemBlock.FACING) == direction);
            } else {
                return block instanceof CableBlock ? CheckResult.of(state.getValue(CableBlock.MODEM).getFacing() == direction) : CheckResult.PASS;
            }
        });
        ci.cancel();
    }
}
