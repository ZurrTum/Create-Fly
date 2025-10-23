package com.zurrtum.create.content.equipment.clipboard;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.foundation.recipe.ItemCopyingRecipe.SupportsItemCopying;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ClipboardBlockItem extends BlockItem implements SupportsItemCopying {

    public ClipboardBlockItem(Block pBlock, Settings pProperties) {
        super(pBlock, pProperties);
    }

    @Override
    @NotNull
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        if (player == null)
            return ActionResult.PASS;
        if (player.isSneaking())
            return super.useOnBlock(context);
        return use(context.getWorld(), player, context.getHand());
    }

    @Override
    protected boolean postPlacement(BlockPos pPos, World pLevel, PlayerEntity pPlayer, ItemStack pStack, BlockState pState) {
        if (pLevel.isClient())
            return false;
        if (!(pLevel.getBlockEntity(pPos) instanceof ClipboardBlockEntity cbe))
            return false;
        cbe.notifyUpdate();
        return true;
    }

    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        ItemStack heldItem = player.getStackInHand(hand);
        if (hand == Hand.OFF_HAND)
            return ActionResult.PASS;

        player.getItemCooldownManager().set(heldItem, 10);
        if (world.isClient)
            AllClientHandle.INSTANCE.openClipboardScreen(player, heldItem.getComponents(), null);
        ClipboardContent content = heldItem.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY);
        heldItem.set(AllDataComponents.CLIPBOARD_CONTENT, content.setType(ClipboardType.EDITING));

        return ActionResult.SUCCESS.withNewHandStack(heldItem);
    }

    @Override
    public ComponentType<?> getComponentType() {
        return AllDataComponents.CLIPBOARD_CONTENT;
    }

}