package com.zurrtum.create.content.redstone.displayLink;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.infrastructure.component.ClickToLinkData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

public abstract class ClickToLinkBlockItem extends BlockItem {
    public ClickToLinkBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    public static boolean linkableItemAlwaysPlacesWhenUsed(World world, BlockPos pos, ItemStack stack) {
        if (stack.getItem() instanceof ClickToLinkBlockItem blockItem) {
            return !world.getBlockState(pos).isOf(blockItem.getBlock());
        }
        return false;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext pContext) {
        PlayerEntity player = pContext.getPlayer();
        if (player == null)
            return ActionResult.FAIL;
        ItemStack stack = pContext.getStack();
        BlockPos pos = pContext.getBlockPos();
        World level = pContext.getWorld();
        BlockState state = level.getBlockState(pos);
        String msgKey = getMessageTranslationKey();
        int maxDistance = getMaxDistanceFromSelection();

        if (player.isSneaking() && stack.contains(AllDataComponents.CLICK_TO_LINK_DATA)) {
            if (level.isClient())
                return ActionResult.SUCCESS;
            player.sendMessage(Text.translatable("create." + msgKey + ".clear"), true);
            stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
            stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);
            return ActionResult.SUCCESS;
        }

        Identifier placedDim = level.getRegistryKey().getValue();

        if (!stack.contains(AllDataComponents.CLICK_TO_LINK_DATA)) {
            if (!isValidTarget(level, pos)) {
                if (placeWhenInvalid()) {
                    ActionResult useOn = super.useOnBlock(pContext);
                    if (level.isClient() || useOn == ActionResult.FAIL)
                        return useOn;

                    ItemStack itemInHand = player.getStackInHand(pContext.getHand());
                    if (!itemInHand.isEmpty()) {
                        stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
                        stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);
                    }
                    return useOn;
                }

                if (level.isClient())
                    AllSoundEvents.DENY.playFrom(player);
                player.sendMessage(Text.translatable("create." + msgKey + ".invalid"), true);
                return ActionResult.FAIL;
            }

            if (level.isClient())
                return ActionResult.SUCCESS;

            player.sendMessage(Text.translatable("create." + msgKey + ".set"), true);
            stack.set(AllDataComponents.CLICK_TO_LINK_DATA, new ClickToLinkData(pos, placedDim));
            return ActionResult.SUCCESS;
        }

        ClickToLinkData data = stack.get(AllDataComponents.CLICK_TO_LINK_DATA);
        //noinspection DataFlowIssue
        BlockPos selectedPos = data.selectedPos();
        Identifier selectedDim = data.selectedDim();
        BlockPos placedPos = pos.offset(pContext.getSide(), state.isReplaceable() ? 0 : 1);

        if (maxDistance != -1 && (!selectedPos.isWithinDistance(placedPos, maxDistance) || !selectedDim.equals(placedDim))) {
            player.sendMessage(Text.translatable("create." + msgKey + ".too_far").formatted(Formatting.RED), true);
            return ActionResult.FAIL;
        }

        NbtCompound beTag = new NbtCompound();
        beTag.put("TargetOffset", BlockPos.CODEC, selectedPos.subtract(placedPos));
        beTag.put("TargetDimension", Identifier.CODEC, selectedDim);
        stack.set(DataComponentTypes.BLOCK_ENTITY_DATA, TypedEntityData.create(((IBE<?>) getBlock()).getBlockEntityType(), beTag));

        ActionResult useOn = super.useOnBlock(pContext);
        if (level.isClient() || useOn == ActionResult.FAIL)
            return useOn;

        ItemStack itemInHand = player.getStackInHand(pContext.getHand());
        if (!itemInHand.isEmpty()) {
            stack.remove(AllDataComponents.CLICK_TO_LINK_DATA);
            stack.remove(DataComponentTypes.BLOCK_ENTITY_DATA);
        }
        player.sendMessage(Text.translatable("create." + msgKey + ".success").formatted(Formatting.GREEN), true);
        return useOn;
    }

    public abstract int getMaxDistanceFromSelection();

    public abstract String getMessageTranslationKey();

    public boolean placeWhenInvalid() {
        return false;
    }

    public boolean isValidTarget(WorldAccess level, BlockPos pos) {
        return true;
    }
}
