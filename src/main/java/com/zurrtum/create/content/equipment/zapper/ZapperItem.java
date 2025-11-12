package com.zurrtum.create.content.equipment.zapper;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.foundation.item.SwingControlItem;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.packet.s2c.ZapperBeamPacket;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public abstract class ZapperItem extends Item implements SwingControlItem {

    public ZapperItem(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplay displayComponent,
        Consumer<Component> tooltip,
        TooltipFlag flagIn
    ) {
        if (stack.has(AllDataComponents.SHAPER_BLOCK_USED)) {
            MutableComponent usedBlock = stack.get(AllDataComponents.SHAPER_BLOCK_USED).getBlock().getName();
            tooltip.accept(Component.translatable("create.terrainzapper.usingBlock", usedBlock.withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    //TODO
    //    @Override
    //    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
    //        boolean differentBlock = false;
    //        if (oldStack.contains(AllDataComponents.SHAPER_BLOCK_USED) && newStack.contains(AllDataComponents.SHAPER_BLOCK_USED))
    //            differentBlock = oldStack.get(AllDataComponents.SHAPER_BLOCK_USED) != newStack.get(AllDataComponents.SHAPER_BLOCK_USED);
    //        return slotChanged || !isZapper(newStack) || differentBlock;
    //    }

    public boolean isZapper(ItemStack newStack) {
        return newStack.getItem() instanceof ZapperItem;
    }

    @Override
    @NotNull
    public InteractionResult useOn(UseOnContext context) {
        // Shift -> open GUI
        if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
            if (context.getLevel().isClientSide()) {
                openHandgunGUI(context.getItemInHand(), context.getHand());
                context.getPlayer().getCooldowns().addCooldown(context.getItemInHand(), 10);
            }
            return InteractionResult.SUCCESS;
        }
        return super.useOn(context);
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand hand) {
        ItemStack item = player.getItemInHand(hand);
        boolean mainHand = hand == InteractionHand.MAIN_HAND;

        // Shift -> Open GUI
        if (player.isShiftKeyDown()) {
            if (world.isClientSide()) {
                openHandgunGUI(item, hand);
                player.getCooldowns().addCooldown(item, 10);
            }
            return InteractionResult.SUCCESS;
        }

        if (ShootableGadgetItemMethods.shouldSwap(player, item, hand, this::isZapper))
            return InteractionResult.FAIL;

        // Check if can be used
        Component msg = validateUsage(item);
        if (msg != null) {
            AllSoundEvents.DENY.play(world, player, player.blockPosition());
            player.displayClientMessage(msg.plainCopy().withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        BlockState stateToUse = Blocks.AIR.defaultBlockState();
        if (item.has(AllDataComponents.SHAPER_BLOCK_USED))
            stateToUse = item.get(AllDataComponents.SHAPER_BLOCK_USED);
        stateToUse = BlockHelper.setZeroAge(stateToUse);
        CompoundTag data = null;
        if (stateToUse.is(AllBlockTags.SAFE_NBT) && item.has(AllDataComponents.SHAPER_BLOCK_DATA)) {
            data = item.get(AllDataComponents.SHAPER_BLOCK_DATA);
        }

        // Raytrace - Find the target
        Vec3 start = player.position().add(0, player.getEyeHeight(), 0);
        Vec3 range = player.getLookAngle().scale(getZappingRange(item));
        BlockHitResult raytrace = world.clip(new ClipContext(start, start.add(range), Block.OUTLINE, Fluid.NONE, player));
        BlockPos pos = raytrace.getBlockPos();
        BlockState stateReplaced = world.getBlockState(pos);

        // No target
        if (pos == null || stateReplaced.getBlock() == Blocks.AIR) {
            ShootableGadgetItemMethods.applyCooldown(player, item, hand, this::isZapper, getCooldownDelay(item));
            player.stopUsingItem();
            return InteractionResult.SUCCESS;
        }

        // Find exact position of gun barrel for VFX
        Vec3 barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, mainHand, new Vec3(.35f, -0.1f, 1));

        // Client side
        if (world.isClientSide()) {
            player.stopUsingItem();
            AllClientHandle.INSTANCE.zapperDontAnimateItem(hand);
            return InteractionResult.SUCCESS;
        }

        // Server side
        if (activate(world, player, item, stateToUse, raytrace, data)) {
            ShootableGadgetItemMethods.applyCooldown(player, item, hand, this::isZapper, getCooldownDelay(item));
            ShootableGadgetItemMethods.sendPackets(player, b -> new ZapperBeamPacket(barrelPos, hand, b, raytrace.getLocation()));
        }

        player.stopUsingItem();
        return InteractionResult.SUCCESS;
    }

    public Component validateUsage(ItemStack item) {
        if (!canActivateWithoutSelectedBlock(item) && !item.has(AllDataComponents.SHAPER_BLOCK_USED))
            return Component.translatable("create.terrainzapper.leftClickToSet");
        return null;
    }

    protected abstract boolean activate(
        Level world,
        Player player,
        ItemStack item,
        BlockState stateToUse,
        BlockHitResult raytrace,
        CompoundTag data
    );

    protected abstract void openHandgunGUI(ItemStack item, InteractionHand hand);

    protected abstract int getCooldownDelay(ItemStack item);

    protected abstract int getZappingRange(ItemStack stack);

    protected boolean canActivateWithoutSelectedBlock(ItemStack stack) {
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        return true;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level world, BlockPos pos, LivingEntity player) {
        return false;
    }

    public static void setBlockEntityData(Level world, BlockPos pos, BlockState state, CompoundTag data, Player player) {
        if (data != null && state.is(AllBlockTags.SAFE_NBT)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null) {
                data = NBTProcessors.process(state, blockEntity, data, !player.isCreative());
                if (data == null)
                    return;
                data.putInt("x", pos.getX());
                data.putInt("y", pos.getY());
                data.putInt("z", pos.getZ());
                try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), Create.LOGGER)) {
                    blockEntity.loadWithComponents(TagValueInput.create(logging, world.registryAccess(), data));
                }
            }
        }
    }

}