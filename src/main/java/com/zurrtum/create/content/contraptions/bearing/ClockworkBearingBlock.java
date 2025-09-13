package com.zurrtum.create.content.contraptions.bearing;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.block.IBE;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ClockworkBearingBlock extends BearingBlock implements IBE<ClockworkBearingBlockEntity> {

    public ClockworkBearingBlock(Settings properties) {
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
            if (!level.isClient) {
                withBlockEntityDo(
                    level, pos, be -> {
                        if (be.running) {
                            be.disassemble();
                            return;
                        }
                        be.assembleNextTick = true;
                    }
                );
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS_TO_DEFAULT_BLOCK_ACTION;
    }

    @Override
    public Class<ClockworkBearingBlockEntity> getBlockEntityClass() {
        return ClockworkBearingBlockEntity.class;
    }

    @Override
    public ActionResult onWrenched(BlockState state, ItemUsageContext context) {
        ActionResult resultType = super.onWrenched(state, context);
        if (!context.getWorld().isClient && resultType.isAccepted())
            withBlockEntityDo(context.getWorld(), context.getBlockPos(), ClockworkBearingBlockEntity::disassemble);
        return resultType;
    }

    @Override
    public BlockEntityType<? extends ClockworkBearingBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.CLOCKWORK_BEARING;
    }

}
