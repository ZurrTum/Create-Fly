package com.zurrtum.create.content.redstone.displayLink;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.component.ClickToLinkData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ClickToLinkBlockItem extends BlockItem {
    public ClickToLinkBlockItem(Block pBlock, Properties pProperties) {
        super(pBlock, pProperties);
    }

    public static boolean linkableItemAlwaysPlacesWhenUsed(Level world, BlockPos pos, ItemStack stack) {
        if (stack.getItem() instanceof ClickToLinkBlockItem blockItem) {
            return !world.getBlockState(pos).is(blockItem.getBlock());
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext pContext) {
        Player player = pContext.getPlayer();
        if (player == null)
            return InteractionResult.FAIL;
        ItemStack stack = pContext.getItemInHand();
        BlockPos pos = pContext.getClickedPos();
        Level level = pContext.getLevel();
        BlockState state = level.getBlockState(pos);
        String msgKey = getMessageTranslationKey();
        int maxDistance = getMaxDistanceFromSelection();

        if (player.isShiftKeyDown() && stack.has(AllDataComponents.CLICK_TO_LINK_DATA)) {
            if (level.isClientSide())
                return InteractionResult.SUCCESS;
            player.displayClientMessage(Component.translatable("create." + msgKey + ".clear"), true);
            stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
            return InteractionResult.SUCCESS;
        }

        Identifier placedDim = level.dimension().identifier();

        if (!stack.has(AllDataComponents.CLICK_TO_LINK_DATA)) {
            if (!isValidTarget(level, pos)) {
                if (placeWhenInvalid()) {
                    InteractionResult useOn = super.useOn(pContext);
                    if (level.isClientSide() || useOn == InteractionResult.FAIL)
                        return useOn;

                    ItemStack itemInHand = player.getItemInHand(pContext.getHand());
                    if (!itemInHand.isEmpty()) {
                        stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
                        stack.remove(DataComponents.BLOCK_ENTITY_DATA);
                    }
                    return useOn;
                }

                if (level.isClientSide())
                    AllSoundEvents.DENY.playFrom(player);
                player.displayClientMessage(Component.translatable("create." + msgKey + ".invalid"), true);
                return InteractionResult.FAIL;
            }

            if (level.isClientSide())
                return InteractionResult.SUCCESS;

            player.displayClientMessage(Component.translatable("create." + msgKey + ".set"), true);
            stack.set(AllDataComponents.CLICK_TO_LINK_DATA, new ClickToLinkData(pos, placedDim));
            return InteractionResult.SUCCESS;
        }

        ClickToLinkData data = stack.get(AllDataComponents.CLICK_TO_LINK_DATA);
        //noinspection DataFlowIssue
        BlockPos selectedPos = data.selectedPos();
        Identifier selectedDim = data.selectedDim();
        BlockPos placedPos = pos.relative(pContext.getClickedFace(), state.canBeReplaced() ? 0 : 1);

        if (maxDistance != -1 && (!selectedPos.closerThan(placedPos, maxDistance) || !selectedDim.equals(placedDim))) {
            player.displayClientMessage(Component.translatable("create." + msgKey + ".too_far").withStyle(ChatFormatting.RED), true);
            return InteractionResult.FAIL;
        }

        CompoundTag beTag = new CompoundTag();
        beTag.store("TargetOffset", BlockPos.CODEC, selectedPos.subtract(placedPos));
        beTag.store("TargetDimension", Identifier.CODEC, selectedDim);
        stack.set(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(((IBE<?>) getBlock()).getBlockEntityType(), beTag));

        InteractionResult useOn = super.useOn(pContext);
        if (level.isClientSide() || useOn == InteractionResult.FAIL)
            return useOn;

        ItemStack itemInHand = player.getItemInHand(pContext.getHand());
        if (!itemInHand.isEmpty()) {
            stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
            stack.remove(DataComponents.BLOCK_ENTITY_DATA);
        }
        player.displayClientMessage(Component.translatable("create." + msgKey + ".success").withStyle(ChatFormatting.GREEN), true);
        return useOn;
    }

    public abstract int getMaxDistanceFromSelection();

    public abstract String getMessageTranslationKey();

    public boolean placeWhenInvalid() {
        return false;
    }

    public boolean isValidTarget(LevelAccessor level, BlockPos pos) {
        return true;
    }
}
