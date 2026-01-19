package com.zurrtum.create.content.equipment.wrench;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class WrenchItem extends Item {

    public WrenchItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.mayBuild())
            return super.useOn(context);

        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        Block block = state.getBlock();

        if (!(block instanceof IWrenchable actor)) {
            if (player.isShiftKeyDown() && canWrenchPickup(state))
                return onItemUseOnOther(context);
            return super.useOn(context);
        }

        if (player.isShiftKeyDown())
            return actor.onSneakWrenched(state, context);
        return actor.onWrenched(state, context);
    }

    private boolean canWrenchPickup(BlockState state) {
        return state.is(AllBlockTags.WRENCH_PICKUP);
    }

    private InteractionResult onItemUseOnOther(UseOnContext context) {
        Player player = context.getPlayer();
        Level world = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = world.getBlockState(pos);
        if (!(world instanceof ServerLevel serverWorld))
            return InteractionResult.SUCCESS;
        if (player != null && !player.isCreative())
            Block.getDrops(state, serverWorld, pos, world.getBlockEntity(pos), player, context.getItemInHand())
                .forEach(itemStack -> player.getInventory().placeItemBackInInventory(itemStack));
        state.spawnAfterBreak(serverWorld, pos, ItemStack.EMPTY, true);
        world.destroyBlock(pos, false);
        AllSoundEvents.WRENCH_REMOVE.playOnServer(world, pos, 1, world.getRandom().nextFloat() * .5f + .5f);
        return InteractionResult.SUCCESS;
    }

    public static boolean wrenchInstaKillsMinecarts(ServerPlayer player, Entity target) {
        if (!(target instanceof AbstractMinecart minecart))
            return false;
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.is(AllItems.WRENCH))
            return false;
        if (player.isCreative())
            return false;
        minecart.hurtServer(player.level(), minecart.damageSources().playerAttack(player), 100);
        return true;
    }
}
