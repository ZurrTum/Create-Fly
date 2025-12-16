package com.zurrtum.create.content.logistics.tunnel;

import com.zurrtum.create.AllBlockEntityTypes;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BrassTunnelBlock extends BeltTunnelBlock {

    public BrassTunnelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Container getInventory(LevelAccessor world, BlockPos pos, BlockState state, BeltTunnelBlockEntity blockEntity, Direction context) {
        if (blockEntity instanceof BrassTunnelBlockEntity brassTunnelBlockEntity) {
            return brassTunnelBlockEntity.tunnelCapability;
        }
        return super.getInventory(world, pos, state, blockEntity, context);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return onBlockEntityUse(
            level, pos, be -> {
                if (!(be instanceof BrassTunnelBlockEntity bte))
                    return InteractionResult.PASS;
                List<ItemStack> stacksOfGroup = bte.grabAllStacksOfGroup(level.isClientSide());
                if (stacksOfGroup.isEmpty())
                    return InteractionResult.PASS;
                if (level.isClientSide())
                    return InteractionResult.SUCCESS;
                for (ItemStack itemStack : stacksOfGroup)
                    player.getInventory().placeItemBackInInventory(itemStack.copy());
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, .2f, 1f + level.getRandom().nextFloat());
                return InteractionResult.SUCCESS;
            }
        );
    }

    @Override
    public BlockEntityType<? extends BeltTunnelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BRASS_TUNNEL;
    }
}
