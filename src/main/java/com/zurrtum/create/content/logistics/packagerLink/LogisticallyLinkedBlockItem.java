package com.zurrtum.create.content.logistics.packagerLink;

import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.block.Block;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

public class LogisticallyLinkedBlockItem extends BlockItem {

    public LogisticallyLinkedBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public boolean hasGlint(ItemStack pStack) {
        return isTuned(pStack);
    }

    public static boolean isTuned(ItemStack pStack) {
        return pStack.contains(DataComponentTypes.BLOCK_ENTITY_DATA);
    }

    @Nullable
    public static UUID networkFromStack(ItemStack pStack) {
        NbtCompound tag = pStack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
        return tag.get("Freq", Uuids.INT_STREAM_CODEC).orElse(null);
    }

    @Override
    public void appendTooltip(
        @NotNull ItemStack stack,
        @NotNull TooltipContext tooltipContext,
        TooltipDisplayComponent displayComponent,
        @NotNull Consumer<Text> textConsumer,
        TooltipType type
    ) {
        super.appendTooltip(stack, tooltipContext, displayComponent, textConsumer, type);

        NbtCompound tag = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
        if (!tag.contains("Freq"))
            return;

        textConsumer.accept(Text.translatable("create.logistically_linked.tooltip").formatted(Formatting.GOLD));

        textConsumer.accept(Text.translatable("create.logistically_linked.tooltip_clear").formatted(Formatting.GRAY));
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand usedHand) {
        ItemStack stack = player.getStackInHand(usedHand);
        if (isTuned(stack)) {
            if (world.isClient()) {
                world.playSound(player, player.getBlockPos(), SoundEvents.ENTITY_ITEM_FRAME_REMOVE_ITEM, SoundCategory.BLOCKS, 0.75f, 1.0f);
            } else {
                player.sendMessage(Text.translatable("create.logistically_linked.cleared"), true);
                stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);
            }
            return ActionResult.SUCCESS;
        } else {
            return super.use(world, player, usedHand);
        }
    }

    @Override
    public @NotNull ActionResult useOnBlock(ItemUsageContext pContext) {
        ItemStack stack = pContext.getStack();
        BlockPos pos = pContext.getBlockPos();
        World level = pContext.getWorld();
        PlayerEntity player = pContext.getPlayer();

        if (player == null)
            return ActionResult.FAIL;
        if (player.isSneaking())
            return super.useOnBlock(pContext);

        LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);
        boolean tuned = isTuned(stack);

        if (link != null) {
            if (level.isClient())
                return ActionResult.SUCCESS;
            if (!link.mayInteractMessage(player))
                return ActionResult.SUCCESS;

            assignFrequency(stack, player, link.freqId);
            return ActionResult.SUCCESS;
        }

        ActionResult useOn = super.useOnBlock(pContext);
        if (level.isClient() || useOn == ActionResult.FAIL)
            return useOn;

        player.sendMessage(
            tuned ? Text.translatable("create.logistically_linked.connected") : Text.translatable("create.logistically_linked.new_network_started"),
            true
        );
        return useOn;
    }

    public static void assignFrequency(ItemStack stack, PlayerEntity player, UUID frequency) {
        NbtCompound tag = stack.getOrDefault(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT).copyNbt();
        tag.put("Freq", Uuids.INT_STREAM_CODEC, frequency);

        player.sendMessage(Text.translatable("create.logistically_linked.tuned"), true);

        tag.put("id", CreateCodecs.BLOCK_ENTITY_TYPE_CODEC, ((IBE<?>) ((BlockItem) stack.getItem()).getBlock()).getBlockEntityType());
        stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.of(tag));
    }

}