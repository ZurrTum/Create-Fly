package com.zurrtum.create.client.content.redstone.displayLink;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.api.behaviour.display.ClickToLinkSelection;
import com.zurrtum.create.api.behaviour.display.DisplayTarget;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.redstone.displayLink.ClickToLinkBlockItem;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ClickToLinkHandler {
    private static @Nullable BlockPos lastShownPos = null;
    private static @Nullable AABB lastShownAABB = null;

    public static void clientTick(Minecraft mc) {
        Player player = mc.player;
        if (player == null)
            return;
        ItemStack heldItemMainhand = player.getMainHandItem();
        if (!(heldItemMainhand.getItem() instanceof ClickToLinkBlockItem blockItem))
            return;
        if (!heldItemMainhand.has(AllDataComponents.CLICK_TO_LINK_DATA))
            return;

        //noinspection DataFlowIssue
        BlockPos selectedPos = heldItemMainhand.get(AllDataComponents.CLICK_TO_LINK_DATA).selectedPos();

        if (!selectedPos.equals(lastShownPos)) {
            if (blockItem instanceof ClickToLinkSelection item) {
                lastShownAABB = item.getSelectionBounds(mc.level, selectedPos);
            } else {
                lastShownAABB = getSelectionBounds(mc.level, selectedPos);
            }
            lastShownPos = selectedPos;
        }

        Outliner.getInstance().showAABB("target", lastShownAABB).colored(0xffcb74).lineWidth(1 / 16f);
    }

    public static AABB getSelectionBounds(Level world, BlockPos pos) {
        DisplayTarget target = DisplayTarget.get(world, pos);
        if (target != null) {
            return target.getMultiblockBounds(world, pos);
        }
        VoxelShape shape = world.getBlockState(pos).getShape(world, pos);
        return shape.isEmpty() ? new AABB(BlockPos.ZERO) : shape.bounds().move(pos);
    }
}
