package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WindmillBearingBlock extends BearingBlock implements IBE<WindmillBearingBlockEntity> {

    public WindmillBearingBlock(Settings properties) {
        super(properties);
    }

    @Override
    protected ActionResult onUseWithItem(
        ItemStack stack,
        BlockState state,
        World level,
        BlockPos pos,
        PlayerEntity player,
        Hand hand,
        BlockHitResult hitResult
    ) {
        if (!player.canModifyBlocks())
            return ActionResult.FAIL;
        if (player.isSneaking())
            return ActionResult.FAIL;
        if (stack.isEmpty()) {
            if (level.isClient())
                return ActionResult.SUCCESS;
            withBlockEntityDo(
                level, pos, be -> {
                    if (be.running) {
                        be.disassemble();
                        return;
                    }
                    be.assembleNextTick = true;
                }
            );
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public Class<WindmillBearingBlockEntity> getBlockEntityClass() {
        return WindmillBearingBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends WindmillBearingBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.WINDMILL_BEARING;
    }
}
