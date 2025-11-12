package com.zurrtum.create.client.content.decoration.girder;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.decoration.girder.GirderWrenchBehavior;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Arrays;

public class GirderWrenchBehaviorHandler {
    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult result))
            return;

        ClientLevel world = mc.level;
        BlockPos pos = result.getBlockPos();
        Player player = mc.player;
        ItemStack heldItem = player.getMainHandItem();

        if (player.isShiftKeyDown())
            return;

        if (!world.getBlockState(pos).is(AllBlocks.METAL_GIRDER))
            return;

        if (!heldItem.is(AllItems.WRENCH))
            return;

        Pair<Direction, GirderWrenchBehavior.Action> dirPair = GirderWrenchBehavior.getDirectionAndAction(result, world, pos);
        if (dirPair == null)
            return;

        Vec3 center = VecHelper.getCenterOf(pos);
        Vec3 edge = center.add(Vec3.atLowerCornerOf(dirPair.getFirst().getUnitVec3i()).scale(0.4));
        Direction.Axis[] axes = Arrays.stream(Iterate.axes).filter(axis -> axis != dirPair.getFirst().getAxis()).toArray(Direction.Axis[]::new);

        double normalMultiplier = dirPair.getSecond() == GirderWrenchBehavior.Action.PAIR ? 4 : 1;
        Vec3 corner1 = edge.add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.POSITIVE).getUnitVec3i())
                .scale(0.3))
            .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.POSITIVE).getUnitVec3i()).scale(0.3))
            .add(Vec3.atLowerCornerOf(dirPair.getFirst().getUnitVec3i()).scale(0.1 * normalMultiplier));

        normalMultiplier = dirPair.getSecond() == GirderWrenchBehavior.Action.HORIZONTAL ? 9 : 2;
        Vec3 corner2 = edge.add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[0], Direction.AxisDirection.NEGATIVE).getUnitVec3i())
                .scale(0.3))
            .add(Vec3.atLowerCornerOf(Direction.fromAxisAndDirection(axes[1], Direction.AxisDirection.NEGATIVE).getUnitVec3i()).scale(0.3))
            .add(Vec3.atLowerCornerOf(dirPair.getFirst().getOpposite().getUnitVec3i()).scale(0.1 * normalMultiplier));

        Outliner.getInstance().showAABB("girderWrench", new AABB(corner1, corner2)).lineWidth(1 / 32f).colored(new Color(127, 127, 127));
    }
}
