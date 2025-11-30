package com.zurrtum.create.content.contraptions.minecart;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Useful methods for dealing with Minecarts
 */
public class MinecartSim2020 {
    public static Vec3d predictNextPositionOf(AbstractMinecartEntity cart) {
        Vec3d position = cart.getPos();
        Vec3d motion = VecHelper.clamp(cart.getVelocity(), 1f);
        return position.add(motion);
    }

    public static boolean canAddMotion(AbstractMinecartEntity c) {
        if (c instanceof FurnaceMinecartEntity furnace)
            return MathHelper.approximatelyEquals(furnace.pushVec.x, 0) && MathHelper.approximatelyEquals(furnace.pushVec.z, 0);

        return AllSynchedDatas.MINECART_CONTROLLER.get(c).map(controller -> !controller.isStalled()).orElse(true);
    }

    public static Vec3d getRailVec(RailShape shape) {
        return switch (shape) {
            case ASCENDING_NORTH, ASCENDING_SOUTH, NORTH_SOUTH -> new Vec3d(0, 0, 1);
            case ASCENDING_EAST, ASCENDING_WEST, EAST_WEST -> new Vec3d(1, 0, 0);
            case NORTH_EAST, SOUTH_WEST -> new Vec3d(1, 0, 1).normalize();
            case NORTH_WEST, SOUTH_EAST -> new Vec3d(1, 0, -1).normalize();
            default -> new Vec3d(0, 1, 0);
        };
    }

}
