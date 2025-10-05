package com.zurrtum.create.content.equipment.wrench;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class WrenchItem extends Item {

    public WrenchItem(Settings properties) {
        super(properties);
    }

    @Override
    @NotNull
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null || !player.canModifyBlocks())
            return super.useOnBlock(context);

        BlockState state = context.getWorld().getBlockState(context.getBlockPos());
        Block block = state.getBlock();

        if (!(block instanceof IWrenchable actor)) {
            if (player.isSneaking() && canWrenchPickup(state))
                return onItemUseOnOther(context);
            return super.useOnBlock(context);
        }

        if (player.isSneaking())
            return actor.onSneakWrenched(state, context);
        return actor.onWrenched(state, context);
    }

    private boolean canWrenchPickup(BlockState state) {
        return state.isIn(AllBlockTags.WRENCH_PICKUP);
    }

    private ActionResult onItemUseOnOther(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (!(world instanceof ServerWorld serverWorld))
            return ActionResult.SUCCESS;
        if (player != null && !player.isCreative())
            Block.getDroppedStacks(state, serverWorld, pos, world.getBlockEntity(pos), player, context.getStack())
                .forEach(itemStack -> player.getInventory().offerOrDrop(itemStack));
        state.onStacksDropped(serverWorld, pos, ItemStack.EMPTY, true);
        world.breakBlock(pos, false);
        AllSoundEvents.WRENCH_REMOVE.playOnServer(world, pos, 1, world.random.nextFloat() * .5f + .5f);
        return ActionResult.SUCCESS;
    }

    public static boolean wrenchInstaKillsMinecarts(ServerPlayerEntity player, Entity target) {
        if (!(target instanceof AbstractMinecartEntity minecart))
            return false;
        ItemStack heldItem = player.getMainHandStack();
        if (!heldItem.isOf(AllItems.WRENCH))
            return false;
        if (player.isCreative())
            return false;
        minecart.damage(player.getEntityWorld(), minecart.getDamageSources().playerAttack(player), 100);
        return true;
    }
}
