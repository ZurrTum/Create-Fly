package com.zurrtum.create.content.logistics.packagerLink;

import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Consumer;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class LogisticallyLinkedBlockItem extends BlockItem {

    public LogisticallyLinkedBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    public boolean isFoil(ItemStack pStack) {
        return isTuned(pStack);
    }

    public static boolean isTuned(ItemStack pStack) {
        return pStack.has(DataComponents.BLOCK_ENTITY_DATA);
    }

    @Nullable
    public static UUID networkFromStack(ItemStack pStack) {
        TypedEntityData<BlockEntityType<?>> data = pStack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null || !data.contains("Freq")) {
            return null;
        }
        return data.copyTagWithoutId().read("Freq", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(
        @NotNull ItemStack stack,
        @NotNull TooltipContext tooltipContext,
        TooltipDisplay displayComponent,
        @NotNull Consumer<Component> textConsumer,
        TooltipFlag type
    ) {
        super.appendHoverText(stack, tooltipContext, displayComponent, textConsumer, type);

        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null || !data.contains("Freq"))
            return;

        textConsumer.accept(Component.translatable("create.logistically_linked.tooltip").withStyle(ChatFormatting.GOLD));

        textConsumer.accept(Component.translatable("create.logistically_linked.tooltip_clear").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public InteractionResult use(Level world, Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (isTuned(stack)) {
            if (world.isClientSide()) {
                world.playSound(player, player.blockPosition(), SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.75f, 1.0f);
            } else {
                player.displayClientMessage(Component.translatable("create.logistically_linked.cleared"), true);
                stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            }
            return InteractionResult.SUCCESS;
        } else {
            return super.use(world, player, usedHand);
        }
    }

    @Override
    public @NotNull InteractionResult useOn(UseOnContext pContext) {
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        Player player = pContext.getPlayer();

        if (player == null)
            return InteractionResult.FAIL;
        if (player.isShiftKeyDown())
            return super.useOn(pContext);

        LogisticallyLinkedBehaviour link = BlockEntityBehaviour.get(level, pos, LogisticallyLinkedBehaviour.TYPE);
        boolean tuned = isTuned(stack);

        if (link != null) {
            if (level.isClientSide())
                return InteractionResult.SUCCESS;
            if (!link.mayInteractMessage(player))
                return InteractionResult.SUCCESS;

            assignFrequency(stack, player, link.freqId);
            return InteractionResult.SUCCESS;
        }

        InteractionResult useOn = super.useOn(pContext);
        if (level.isClientSide() || useOn == InteractionResult.FAIL)
            return useOn;

        player.displayClientMessage(
            tuned ? Component.translatable("create.logistically_linked.connected") : Component.translatable(
                "create.logistically_linked.new_network_started"), true
        );
        return useOn;
    }

    public static void assignFrequency(ItemStack stack, Player player, UUID frequency) {
        TypedEntityData<BlockEntityType<?>> data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        CompoundTag tag = data == null ? new CompoundTag() : data.copyTagWithoutId();
        tag.store("Freq", UUIDUtil.CODEC, frequency);

        player.displayClientMessage(Component.translatable("create.logistically_linked.tuned"), true);

        stack.set(
            DataComponents.BLOCK_ENTITY_DATA,
            TypedEntityData.of(((IBE<?>) ((BlockItem) stack.getItem()).getBlock()).getBlockEntityType(), tag)
        );
    }

}