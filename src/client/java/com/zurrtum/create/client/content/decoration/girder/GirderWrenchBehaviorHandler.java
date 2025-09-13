package com.zurrtum.create.client.content.decoration.girder;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.decoration.girder.GirderWrenchBehavior;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

public class GirderWrenchBehaviorHandler {
    public static void tick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null || !(mc.crosshairTarget instanceof BlockHitResult result))
            return;

        ClientWorld world = mc.world;
        BlockPos pos = result.getBlockPos();
        PlayerEntity player = mc.player;
        ItemStack heldItem = player.getMainHandStack();

        if (player.isSneaking())
            return;

        if (!world.getBlockState(pos).isOf(AllBlocks.METAL_GIRDER))
            return;

        if (!heldItem.isOf(AllItems.WRENCH))
            return;

        Pair<Direction, GirderWrenchBehavior.Action> dirPair = GirderWrenchBehavior.getDirectionAndAction(result, world, pos);
        if (dirPair == null)
            return;

        Vec3d center = VecHelper.getCenterOf(pos);
        Vec3d edge = center.add(Vec3d.of(dirPair.getFirst().getVector()).multiply(0.4));
        Direction.Axis[] axes = Arrays.stream(Iterate.axes).filter(axis -> axis != dirPair.getFirst().getAxis()).toArray(Direction.Axis[]::new);

        double normalMultiplier = dirPair.getSecond() == GirderWrenchBehavior.Action.PAIR ? 4 : 1;
        Vec3d corner1 = edge.add(Vec3d.of(Direction.from(axes[0], Direction.AxisDirection.POSITIVE).getVector()).multiply(0.3))
            .add(Vec3d.of(Direction.from(axes[1], Direction.AxisDirection.POSITIVE).getVector()).multiply(0.3))
            .add(Vec3d.of(dirPair.getFirst().getVector()).multiply(0.1 * normalMultiplier));

        normalMultiplier = dirPair.getSecond() == GirderWrenchBehavior.Action.HORIZONTAL ? 9 : 2;
        Vec3d corner2 = edge.add(Vec3d.of(Direction.from(axes[0], Direction.AxisDirection.NEGATIVE).getVector()).multiply(0.3))
            .add(Vec3d.of(Direction.from(axes[1], Direction.AxisDirection.NEGATIVE).getVector()).multiply(0.3))
            .add(Vec3d.of(dirPair.getFirst().getOpposite().getVector()).multiply(0.1 * normalMultiplier));

        Outliner.getInstance().showAABB("girderWrench", new Box(corner1, corner2)).lineWidth(1 / 32f).colored(new Color(127, 127, 127));
    }
}
