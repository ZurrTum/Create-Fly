package com.zurrtum.create.client.content.redstone.displayLink;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.api.behaviour.display.ClickToLinkSelection;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.redstone.displayLink.ClickToLinkBlockItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

public class ClickToLinkHandler {
    private static BlockPos lastShownPos = null;
    private static Box lastShownAABB = null;

    public static void clientTick(MinecraftClient mc) {
        PlayerEntity player = mc.player;
        if (player == null)
            return;
        ItemStack heldItemMainhand = player.getMainHandStack();
        if (!(heldItemMainhand.getItem() instanceof ClickToLinkBlockItem blockItem))
            return;
        if (!heldItemMainhand.contains(AllDataComponents.CLICK_TO_LINK_DATA))
            return;

        //noinspection DataFlowIssue
        BlockPos selectedPos = heldItemMainhand.get(AllDataComponents.CLICK_TO_LINK_DATA).selectedPos();

        if (!selectedPos.equals(lastShownPos)) {
            if (blockItem instanceof ClickToLinkSelection item) {
                lastShownAABB = item.getSelectionBounds(mc.world, selectedPos);
            } else {
                lastShownAABB = getSelectionBounds(mc.world, selectedPos);
            }
            lastShownPos = selectedPos;
        }

        Outliner.getInstance().showAABB("target", lastShownAABB).colored(0xffcb74).lineWidth(1 / 16f);
    }

    public static Box getSelectionBounds(World world, BlockPos pos) {
        DisplayTarget target = DisplayTarget.get(world, pos);
        if (target != null) {
            return target.getMultiblockBounds(world, pos);
        }
        VoxelShape shape = world.getBlockState(pos).getOutlineShape(world, pos);
        return shape.isEmpty() ? new Box(BlockPos.ORIGIN) : shape.getBoundingBox().offset(pos);
    }
}
