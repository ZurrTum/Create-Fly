package com.zurrtum.create.client.foundation.block;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BigOutlines {
    static BlockHitResult result = null;

    public static void pick(MinecraftClient mc) {
        if (!(mc.cameraEntity instanceof ClientPlayerEntity player))
            return;
        if (mc.world == null)
            return;

        result = null;

        Vec3d origin = player.getCameraPosVec(AnimationTickHolder.getPartialTicks(mc.world));

        double maxRange = mc.crosshairTarget == null ? Double.MAX_VALUE : mc.crosshairTarget.getPos().squaredDistanceTo(origin) + 0.5;

        double range = player.getAttributeValue(EntityAttributes.BLOCK_INTERACTION_RANGE);
        Vec3d target = RaycastHelper.getTraceTarget(player, Math.min(maxRange, range) + 1, origin);

        RaycastHelper.rayTraceUntil(
            origin, target, pos -> {
                BlockPos.Mutable p = BlockPos.ORIGIN.mutableCopy();

                for (int x = -1; x <= 1; x++) {
                    for (int y = -1; y <= 1; y++) {
                        for (int z = -1; z <= 1; z++) {
                            p.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                            BlockState blockState = mc.world.getBlockState(p);

                            if (!AllExtensions.BIG_OUTLINE.contains(blockState.getBlock()))
                                continue;

                            BlockHitResult hit = blockState.getRaycastShape(mc.world, p).raycast(origin, target, p.toImmutable());
                            if (hit == null)
                                continue;

                            if (result != null && Vec3d.ofCenter(p).squaredDistanceTo(origin) >= Vec3d.ofCenter(result.getBlockPos())
                                .squaredDistanceTo(origin))
                                continue;

                            Vec3d vec = hit.getPos();
                            double interactionDist = vec.squaredDistanceTo(origin);
                            if (interactionDist >= maxRange)
                                continue;

                            BlockPos hitPos = hit.getBlockPos();

                            // pacifies ServerGamePacketListenerImpl.handleUseItemOn
                            vec = vec.subtract(Vec3d.ofCenter(hitPos));
                            vec = VecHelper.clampComponentWise(vec, 1);
                            vec = vec.add(Vec3d.ofCenter(hitPos));

                            result = new BlockHitResult(vec, hit.getSide(), hitPos, hit.isInsideBlock());
                        }
                    }
                }

                return result != null;
            }
        );

        if (result != null)
            mc.crosshairTarget = result;
    }
}
