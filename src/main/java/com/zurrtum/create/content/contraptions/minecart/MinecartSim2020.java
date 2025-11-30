package com.zurrtum.create.content.contraptions.minecart;

import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartFurnace;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;

/**
 * Useful methods for dealing with Minecarts
 */
public class MinecartSim2020 {
    public static Vec3 predictNextPositionOf(AbstractMinecart cart) {
        Vec3 position = cart.position();
        Vec3 motion = VecHelper.clamp(cart.getDeltaMovement(), 1f);
        return position.add(motion);
    }

    public static boolean canAddMotion(AbstractMinecart c) {
        if (c instanceof MinecartFurnace furnace)
            return Mth.equal(furnace.push.x, 0) && Mth.equal(furnace.push.z, 0);

        return AllSynchedDatas.MINECART_CONTROLLER.get(c).map(controller -> !controller.isStalled()).orElse(true);
    }

    public static Vec3 getRailVec(RailShape shape) {
        return switch (shape) {
            case ASCENDING_NORTH, ASCENDING_SOUTH, NORTH_SOUTH -> new Vec3(0, 0, 1);
            case ASCENDING_EAST, ASCENDING_WEST, EAST_WEST -> new Vec3(1, 0, 0);
            case NORTH_EAST, SOUTH_WEST -> new Vec3(1, 0, 1).normalize();
            case NORTH_WEST, SOUTH_EAST -> new Vec3(1, 0, -1).normalize();
            default -> new Vec3(0, 1, 0);
        };
    }

}
