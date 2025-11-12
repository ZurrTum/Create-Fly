package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.infrastructure.model.CopycatModel;
import com.zurrtum.create.content.redstone.rail.ControllerRailBlock;
import net.minecraft.client.color.block.BlockColors;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockColors.class)
public class BlockColorsMixin {
    @ModifyReturnValue(method = "createDefault()Lnet/minecraft/client/color/block/BlockColors;", at = @At("TAIL"))
    private static BlockColors addColors(BlockColors blockColors) {
        blockColors.register(ControllerRailBlock::getWireColor, AllBlocks.CONTROLLER_RAIL);
        blockColors.register(CopycatModel::getColor, AllBlocks.COPYCAT_STEP);
        blockColors.register(CopycatModel::getColor, AllBlocks.COPYCAT_PANEL);
        return blockColors;
    }
}
