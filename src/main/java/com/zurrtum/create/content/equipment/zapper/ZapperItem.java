package com.zurrtum.create.content.equipment.zapper;

import com.zurrtum.create.*;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.foundation.item.SwingControlItem;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.packet.s2c.ZapperBeamPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.storage.NbtReadView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class ZapperItem extends Item implements SwingControlItem {

    public ZapperItem(Settings properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendTooltip(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> tooltip,
        TooltipType flagIn
    ) {
        if (stack.contains(AllDataComponents.SHAPER_BLOCK_USED)) {
            MutableText usedBlock = stack.get(AllDataComponents.SHAPER_BLOCK_USED).getBlock().getName();
            tooltip.accept(Text.translatable("create.terrainzapper.usingBlock", usedBlock.formatted(Formatting.GRAY))
                .formatted(Formatting.DARK_GRAY));
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
    public ActionResult useOnBlock(ItemUsageContext context) {
        // Shift -> open GUI
        if (context.getPlayer() != null && context.getPlayer().isSneaking()) {
            if (context.getWorld().isClient()) {
                openHandgunGUI(context.getStack(), context.getHand());
                context.getPlayer().getItemCooldownManager().set(context.getStack(), 10);
            }
            return ActionResult.SUCCESS;
        }
        return super.useOnBlock(context);
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack item = player.getStackInHand(hand);
        boolean mainHand = hand == Hand.MAIN_HAND;

        // Shift -> Open GUI
        if (player.isSneaking()) {
            if (world.isClient()) {
                openHandgunGUI(item, hand);
                player.getItemCooldownManager().set(item, 10);
            }
            return ActionResult.SUCCESS;
        }

        if (ShootableGadgetItemMethods.shouldSwap(player, item, hand, this::isZapper))
            return ActionResult.FAIL;

        // Check if can be used
        Text msg = validateUsage(item);
        if (msg != null) {
            AllSoundEvents.DENY.play(world, player, player.getBlockPos());
            player.sendMessage(msg.copyContentOnly().formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        BlockState stateToUse = Blocks.AIR.getDefaultState();
        if (item.contains(AllDataComponents.SHAPER_BLOCK_USED))
            stateToUse = item.get(AllDataComponents.SHAPER_BLOCK_USED);
        stateToUse = BlockHelper.setZeroAge(stateToUse);
        NbtCompound data = null;
        if (stateToUse.isIn(AllBlockTags.SAFE_NBT) && item.contains(AllDataComponents.SHAPER_BLOCK_DATA)) {
            data = item.get(AllDataComponents.SHAPER_BLOCK_DATA);
        }

        // Raytrace - Find the target
        Vec3d start = player.getEntityPos().add(0, player.getStandingEyeHeight(), 0);
        Vec3d range = player.getRotationVector().multiply(getZappingRange(item));
        BlockHitResult raytrace = world.raycast(new RaycastContext(start, start.add(range), ShapeType.OUTLINE, FluidHandling.NONE, player));
        BlockPos pos = raytrace.getBlockPos();
        BlockState stateReplaced = world.getBlockState(pos);

        // No target
        if (pos == null || stateReplaced.getBlock() == Blocks.AIR) {
            ShootableGadgetItemMethods.applyCooldown(player, item, hand, this::isZapper, getCooldownDelay(item));
            player.clearActiveItem();
            return ActionResult.SUCCESS;
        }

        // Find exact position of gun barrel for VFX
        Vec3d barrelPos = ShootableGadgetItemMethods.getGunBarrelVec(player, mainHand, new Vec3d(.35f, -0.1f, 1));

        // Client side
        if (world.isClient()) {
            player.clearActiveItem();
            AllClientHandle.INSTANCE.zapperDontAnimateItem(hand);
            return ActionResult.SUCCESS;
        }

        // Server side
        if (activate(world, player, item, stateToUse, raytrace, data)) {
            ShootableGadgetItemMethods.applyCooldown(player, item, hand, this::isZapper, getCooldownDelay(item));
            ShootableGadgetItemMethods.sendPackets(player, b -> new ZapperBeamPacket(barrelPos, hand, b, raytrace.getPos()));
        }

        player.clearActiveItem();
        return ActionResult.SUCCESS;
    }

    public Text validateUsage(ItemStack item) {
        if (!canActivateWithoutSelectedBlock(item) && !item.contains(AllDataComponents.SHAPER_BLOCK_USED))
            return Text.translatable("create.terrainzapper.leftClickToSet");
        return null;
    }

    protected abstract boolean activate(
        World world,
        PlayerEntity player,
        ItemStack item,
        BlockState stateToUse,
        BlockHitResult raytrace,
        NbtCompound data
    );

    protected abstract void openHandgunGUI(ItemStack item, Hand hand);

    protected abstract int getCooldownDelay(ItemStack item);

    protected abstract int getZappingRange(ItemStack stack);

    protected boolean canActivateWithoutSelectedBlock(ItemStack stack) {
        return false;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, Hand hand) {
        return true;
    }

    @Override
    public boolean canMine(ItemStack stack, BlockState state, World world, BlockPos pos, LivingEntity player) {
        return false;
    }

    public static void setBlockEntityData(World world, BlockPos pos, BlockState state, NbtCompound data, PlayerEntity player) {
        if (data != null && state.isIn(AllBlockTags.SAFE_NBT)) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity != null) {
                data = NBTProcessors.process(state, blockEntity, data, !player.isCreative());
                if (data == null)
                    return;
                data.putInt("x", pos.getX());
                data.putInt("y", pos.getY());
                data.putInt("z", pos.getZ());
                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(blockEntity.getReporterContext(), Create.LOGGER)) {
                    blockEntity.read(NbtReadView.create(logging, world.getRegistryManager(), data));
                }
            }
        }
    }

}