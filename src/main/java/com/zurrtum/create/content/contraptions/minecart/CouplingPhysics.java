package com.zurrtum.create.content.contraptions.minecart;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import net.minecraft.block.AbstractRailBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.RailShape;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.tick.TickManager;

public class CouplingPhysics {

    public static void tick(ServerWorld world) {
        CouplingHandler.forEachLoadedCoupling(world, c -> tickCoupling(world, c));
    }

    public static void tickCoupling(ServerWorld world, Couple<MinecartController> c) {
        Couple<AbstractMinecartEntity> carts = c.map(MinecartController::cart);

        TickManager trm = world.getTickManager();
        if (trm.shouldSkipTick(carts.getFirst()) && trm.shouldSkipTick(carts.getSecond())) {
            return;
        }

        float couplingLength = c.getFirst().getCouplingLength(true);
        softCollisionStep(world, carts, couplingLength);
        if (!AbstractMinecartEntity.areMinecartImprovementsEnabled(world)) {
            hardCollisionStep(world, carts, couplingLength);
        }
    }

    public static void hardCollisionStep(ServerWorld world, Couple<AbstractMinecartEntity> carts, double couplingLength) {
        if (!MinecartSim2020.canAddMotion(carts.get(false)) && MinecartSim2020.canAddMotion(carts.get(true)))
            carts = carts.swap();

        Couple<Vec3d> corrections = Couple.create(null, null);
        Couple<Double> maxSpeed = carts.map(cart -> cart.getMaxSpeed(world));
        boolean firstLoop = true;
        for (boolean current : new boolean[]{true, false, true}) {
            AbstractMinecartEntity cart = carts.get(current);
            AbstractMinecartEntity otherCart = carts.get(!current);

            float stress = (float) (couplingLength - cart.getEntityPos().distanceTo(otherCart.getEntityPos()));

            if (Math.abs(stress) < 1 / 8f)
                continue;

            Vec3d pos = cart.getEntityPos();
            Vec3d link = otherCart.getEntityPos().subtract(pos);
            float correctionMagnitude = firstLoop ? -stress / 2f : -stress;

            if (!MinecartSim2020.canAddMotion(cart))
                correctionMagnitude /= 2;

            Vec3d correction = link.normalize().multiply(correctionMagnitude);

            float maxResolveSpeed = 1.75f;
            correction = VecHelper.clamp(correction, (float) Math.min(maxResolveSpeed, maxSpeed.get(current)));

            if (corrections.get(current) == null)
                corrections.set(current, correction);

            cart.move(MovementType.SELF, correction);
            cart.setVelocity(cart.getVelocity().multiply(0.95f));
            firstLoop = false;
        }
    }

    public static void softCollisionStep(ServerWorld world, Couple<AbstractMinecartEntity> carts, double couplingLength) {
        Couple<Float> maxSpeed = carts.map(cart -> (float) cart.getMaxSpeed(world));
        Couple<Boolean> canAddmotion = carts.map(MinecartSim2020::canAddMotion);

        // Assuming Minecarts will never move faster than 1 block/tick
        Couple<Vec3d> motions = carts.map(Entity::getVelocity);
        motions.replaceWithParams(VecHelper::clamp, Couple.create(1f, 1f));
        Couple<Vec3d> nextPositions = carts.map(MinecartSim2020::predictNextPositionOf);

        Couple<RailShape> shapes = carts.mapWithContext((minecart, current) -> {
            Vec3d vec = nextPositions.get(current);
            int x = MathHelper.floor(vec.getX());
            int y = MathHelper.floor(vec.getY());
            int z = MathHelper.floor(vec.getZ());
            BlockPos pos = new BlockPos(x, y - 1, z);
            BlockState railState = world.getBlockState(pos);
            if (!railState.isIn(BlockTags.RAILS)) {
                railState = world.getBlockState(pos.up());
            }
            if (!(railState.getBlock() instanceof AbstractRailBlock block))
                return null;
            return railState.get(block.getShapeProperty());
        });

        float futureStress = (float) (couplingLength - nextPositions.getFirst().distanceTo(nextPositions.getSecond()));
        if (MathHelper.approximatelyEquals(futureStress, 0D))
            return;

        for (boolean current : Iterate.trueAndFalse) {
            Vec3d correction;
            Vec3d pos = nextPositions.get(current);
            Vec3d link = nextPositions.get(!current).subtract(pos);
            float correctionMagnitude = -futureStress / 2f;

            if (canAddmotion.get(current) != canAddmotion.get(!current))
                correctionMagnitude = !canAddmotion.get(current) ? 0 : correctionMagnitude * 2;
            if (!canAddmotion.get(current))
                continue;

            RailShape shape = shapes.get(current);
            if (shape != null) {
                Vec3d railVec = MinecartSim2020.getRailVec(shape);
                correction = followLinkOnRail(link, pos, correctionMagnitude, railVec).subtract(pos);
            } else
                correction = link.normalize().multiply(correctionMagnitude);

            correction = VecHelper.clamp(correction, maxSpeed.get(current));

            motions.set(current, motions.get(current).add(correction));
        }

        motions.replaceWithParams(VecHelper::clamp, maxSpeed);
        carts.forEachWithParams(Entity::setVelocity, motions);
    }

    public static Vec3d followLinkOnRail(Vec3d link, Vec3d cart, float diffToReduce, Vec3d railAxis) {
        double dotProduct = railAxis.dotProduct(link);
        if (Double.isNaN(dotProduct) || dotProduct == 0 || diffToReduce == 0)
            return cart;

        Vec3d axis = railAxis.multiply(-Math.signum(dotProduct));
        Vec3d center = cart.add(link);
        double radius = link.length() - diffToReduce;
        Vec3d intersectSphere = VecHelper.intersectSphere(cart, axis, center, radius);

        // Cannot satisfy on current rail vector
        if (intersectSphere == null)
            return cart.add(VecHelper.project(link, axis));

        return intersectSphere;
    }

}
