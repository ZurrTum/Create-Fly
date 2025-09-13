package com.zurrtum.create.content.logistics.tunnel;

import com.zurrtum.create.AllBlockEntityTypes;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.List;

public class BrassTunnelBlock extends BeltTunnelBlock {

    public BrassTunnelBlock(Settings properties) {
        super(properties);
    }

    @Override
    public Inventory getInventory(WorldAccess world, BlockPos pos, BlockState state, BeltTunnelBlockEntity blockEntity, Direction context) {
        if (blockEntity instanceof BrassTunnelBlockEntity brassTunnelBlockEntity) {
            return brassTunnelBlockEntity.tunnelCapability;
        }
        return super.getInventory(world, pos, state, blockEntity, context);
    }

    @Override
    protected ActionResult onUse(BlockState state, World level, BlockPos pos, PlayerEntity player, BlockHitResult hitResult) {
        return onBlockEntityUse(
            level, pos, be -> {
                if (!(be instanceof BrassTunnelBlockEntity bte))
                    return ActionResult.PASS;
                List<ItemStack> stacksOfGroup = bte.grabAllStacksOfGroup(level.isClient);
                if (stacksOfGroup.isEmpty())
                    return ActionResult.PASS;
                if (level.isClient)
                    return ActionResult.SUCCESS;
                for (ItemStack itemStack : stacksOfGroup)
                    player.getInventory().offerOrDrop(itemStack.copy());
                level.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, .2f, 1f + level.random.nextFloat());
                return ActionResult.SUCCESS;
            }
        );
    }

    @Override
    public BlockEntityType<? extends BeltTunnelBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BRASS_TUNNEL;
    }
}
