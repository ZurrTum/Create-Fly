package com.zurrtum.create.client.foundation.block;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BigOutlines {
    static @Nullable BlockHitResult result = null;

    public static void pick(Minecraft mc) {
        if (!(mc.getCameraEntity() instanceof LocalPlayer player))
            return;
        if (mc.level == null)
            return;

        result = null;

        Vec3 origin = player.getEyePosition(AnimationTickHolder.getPartialTicks(mc.level));

        double maxRange = mc.hitResult == null ? Double.MAX_VALUE : mc.hitResult.getLocation().distanceToSqr(origin) + 0.5;

        double range = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        Vec3 target = RaycastHelper.getTraceTarget(player, Math.min(maxRange, range) + 1, origin);

        RaycastHelper.rayTraceUntil(
            origin, target, pos -> {
                BlockPos.MutableBlockPos p = BlockPos.ZERO.mutable();

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            p.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                            BlockState blockState = mc.level.getBlockState(p);

                            if (!AllExtensions.BIG_OUTLINE.contains(blockState.getBlock()))
                                continue;

                            BlockHitResult hit = blockState.getInteractionShape(mc.level, p).clip(origin, target, p.immutable());
                            if (hit == null)
                                continue;

                            if (result != null && Vec3.atCenterOf(p).distanceToSqr(origin) >= Vec3.atCenterOf(result.getBlockPos())
                                .distanceToSqr(origin))
                                continue;

                            Vec3 vec = hit.getLocation();
                            double interactionDist = vec.distanceToSqr(origin);
                            if (interactionDist >= maxRange)
                                continue;

                            BlockPos hitPos = hit.getBlockPos();

                            // pacifies ServerGamePacketListenerImpl.handleUseItemOn
                            vec = vec.subtract(Vec3.atCenterOf(hitPos));
                            vec = VecHelper.clampComponentWise(vec, 1);
                            vec = vec.add(Vec3.atCenterOf(hitPos));

                            result = new BlockHitResult(vec, hit.getDirection(), hitPos, hit.isInside());
                        }
                    }
                }

                return result != null;
            }
        );

        if (result != null)
            mc.hitResult = result;
    }
}
