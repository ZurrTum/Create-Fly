package com.zurrtum.create.client.content.kinetics.belt.item;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.content.kinetics.belt.item.BeltConnectorItem;
import com.zurrtum.create.content.kinetics.simpleRelays.ShaftBlock;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;

import java.util.LinkedList;
import java.util.List;

public class BeltConnectorHandler {
    private static final int CONNECT_COLOR = ColorHelper.fromFloats(1, .3f, .9f, .5f);
    private static final int NO_CONNECT_COLOR = ColorHelper.fromFloats(1, .9f, .3f, .5f);

    public static void tick(MinecraftClient mc) {
        PlayerEntity player = mc.player;
        World world = mc.world;

        if (player == null || world == null)
            return;
        if (mc.currentScreen != null)
            return;

        Random random = world.random;
        for (Hand hand : Hand.values()) {
            ItemStack heldItem = player.getStackInHand(hand);

            if (!heldItem.isOf(AllItems.BELT_CONNECTOR))
                continue;

            if (!heldItem.contains(AllDataComponents.BELT_FIRST_SHAFT))
                continue;

            BlockPos first = heldItem.get(AllDataComponents.BELT_FIRST_SHAFT);

            if (!world.getBlockState(first).contains(Properties.AXIS))
                continue;
            Axis axis = world.getBlockState(first).get(Properties.AXIS);

            HitResult rayTrace = mc.crosshairTarget;
            if (rayTrace == null || !(rayTrace instanceof BlockHitResult)) {
                if (random.nextInt(50) == 0) {
                    world.addParticleClient(
                        new DustParticleEffect(CONNECT_COLOR, 1),
                        first.getX() + .5f + randomOffset(random, .25f),
                        first.getY() + .5f + randomOffset(random, .25f),
                        first.getZ() + .5f + randomOffset(random, .25f),
                        0,
                        0,
                        0
                    );
                }
                return;
            }

            BlockPos selected = ((BlockHitResult) rayTrace).getBlockPos();

            if (world.getBlockState(selected).isReplaceable())
                return;
            if (!ShaftBlock.isShaft(world.getBlockState(selected)))
                selected = selected.offset(((BlockHitResult) rayTrace).getSide());
            if (!selected.isWithinDistance(first, AllConfigs.server().kinetics.maxBeltLength.get()))
                return;

            boolean canConnect = BeltConnectorItem.validateAxis(world, selected) && BeltConnectorItem.canConnect(world, first, selected);

            Vec3d start = Vec3d.of(first);
            Vec3d end = Vec3d.of(selected);
            Vec3d actualDiff = end.subtract(start);
            end = end.subtract(axis.choose(actualDiff.x, 0, 0), axis.choose(0, actualDiff.y, 0), axis.choose(0, 0, actualDiff.z));
            Vec3d diff = end.subtract(start);

            double x = Math.abs(diff.x);
            double y = Math.abs(diff.y);
            double z = Math.abs(diff.z);
            float length = (float) Math.max(x, Math.max(y, z));
            Vec3d step = diff.normalize();

            int sames = ((x == y) ? 1 : 0) + ((y == z) ? 1 : 0) + ((z == x) ? 1 : 0);
            if (sames == 0) {
                List<Vec3d> validDiffs = new LinkedList<>();
                for (int i = -1; i <= 1; i++)
                    for (int j = -1; j <= 1; j++)
                        for (int k = -1; k <= 1; k++) {
                            if (axis.choose(i, j, k) != 0)
                                continue;
                            if (axis == Axis.Y && i != 0 && k != 0)
                                continue;
                            if (i == 0 && j == 0 && k == 0)
                                continue;
                            validDiffs.add(new Vec3d(i, j, k));
                        }
                int closestIndex = 0;
                float closest = Float.MAX_VALUE;
                for (Vec3d validDiff : validDiffs) {
                    double distanceTo = step.distanceTo(validDiff);
                    if (distanceTo < closest) {
                        closest = (float) distanceTo;
                        closestIndex = validDiffs.indexOf(validDiff);
                    }
                }
                step = validDiffs.get(closestIndex);
            }

            if (axis == Axis.Y && step.x != 0 && step.z != 0)
                return;

            step = new Vec3d(Math.signum(step.x), Math.signum(step.y), Math.signum(step.z));
            int color = canConnect ? CONNECT_COLOR : NO_CONNECT_COLOR;
            for (float f = 0; f < length; f += .0625f) {
                Vec3d position = start.add(step.multiply(f));
                if (random.nextInt(10) == 0) {
                    world.addParticleClient(new DustParticleEffect(color, 1), position.x + .5f, position.y + .5f, position.z + .5f, 0, 0, 0);
                }
            }

            return;
        }
    }

    private static float randomOffset(Random random, float range) {
        return (random.nextFloat() - .5f) * 2 * range;
    }

}
