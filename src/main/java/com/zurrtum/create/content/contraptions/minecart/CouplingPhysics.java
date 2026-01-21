package com.zurrtum.create.content.contraptions.minecart;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.minecart.capability.MinecartController;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.TickRateManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RailShape;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CouplingPhysics {

    public static void tick(ServerLevel world) {
        CouplingHandler.forEachLoadedCoupling(world, c -> tickCoupling(world, c));
    }

    public static void tickCoupling(ServerLevel world, Couple<MinecartController> c) {
        Couple<AbstractMinecart> carts = c.map(MinecartController::cart);

        TickRateManager trm = world.tickRateManager();
        if (trm.isEntityFrozen(carts.getFirst()) && trm.isEntityFrozen(carts.getSecond())) {
            return;
        }

        float couplingLength = c.getFirst().getCouplingLength(true);
        softCollisionStep(world, carts, couplingLength);
        if (!AbstractMinecart.useExperimentalMovement(world)) {
            hardCollisionStep(world, carts, couplingLength);
        }
    }

    public static void hardCollisionStep(ServerLevel world, Couple<AbstractMinecart> carts, double couplingLength) {
        if (!MinecartSim2020.canAddMotion(carts.get(false)) && MinecartSim2020.canAddMotion(carts.get(true)))
            carts = carts.swap();

        Couple<@Nullable Vec3> corrections = Couple.create(null, null);
        Couple<Double> maxSpeed = carts.map(cart -> cart.getMaxSpeed(world));
        boolean firstLoop = true;
        for (boolean current : new boolean[]{true, false, true}) {
            AbstractMinecart cart = carts.get(current);
            AbstractMinecart otherCart = carts.get(!current);

            float stress = (float) (couplingLength - cart.position().distanceTo(otherCart.position()));

            if (Math.abs(stress) < 1 / 8f)
                continue;

            Vec3 pos = cart.position();
            Vec3 link = otherCart.position().subtract(pos);
            float correctionMagnitude = firstLoop ? -stress / 2f : -stress;

            if (!MinecartSim2020.canAddMotion(cart))
                correctionMagnitude /= 2;

            Vec3 correction = link.normalize().scale(correctionMagnitude);

            float maxResolveSpeed = 1.75f;
            correction = VecHelper.clamp(correction, (float) Math.min(maxResolveSpeed, maxSpeed.get(current)));

            if (corrections.get(current) == null)
                corrections.set(current, correction);

            cart.move(MoverType.SELF, correction);
            cart.setDeltaMovement(cart.getDeltaMovement().scale(0.95f));
            firstLoop = false;
        }
    }

    public static void softCollisionStep(ServerLevel world, Couple<AbstractMinecart> carts, double couplingLength) {
        Couple<Float> maxSpeed = carts.map(cart -> (float) cart.getMaxSpeed(world));
        Couple<Boolean> canAddmotion = carts.map(MinecartSim2020::canAddMotion);

        // Assuming Minecarts will never move faster than 1 block/tick
        Couple<Vec3> motions = carts.map(Entity::getDeltaMovement);
        motions.replaceWithParams(VecHelper::clamp, Couple.create(1f, 1f));
        Couple<Vec3> nextPositions = carts.map(MinecartSim2020::predictNextPositionOf);

        Couple<@Nullable RailShape> shapes = carts.mapWithContext((minecart, current) -> {
            Vec3 vec = nextPositions.get(current);
            int x = Mth.floor(vec.x());
            int y = Mth.floor(vec.y());
            int z = Mth.floor(vec.z());
            BlockPos pos = new BlockPos(x, y - 1, z);
            BlockState railState = world.getBlockState(pos);
            if (!railState.is(BlockTags.RAILS)) {
                railState = world.getBlockState(pos.above());
            }
            if (!(railState.getBlock() instanceof BaseRailBlock block))
                return null;
            return railState.getValue(block.getShapeProperty());
        });

        float futureStress = (float) (couplingLength - nextPositions.getFirst().distanceTo(nextPositions.getSecond()));
        if (Mth.equal(futureStress, 0D))
            return;

        for (boolean current : Iterate.trueAndFalse) {
            Vec3 correction;
            Vec3 pos = nextPositions.get(current);
            Vec3 link = nextPositions.get(!current).subtract(pos);
            float correctionMagnitude = -futureStress / 2f;

            if (canAddmotion.get(current) != canAddmotion.get(!current))
                correctionMagnitude = !canAddmotion.get(current) ? 0 : correctionMagnitude * 2;
            if (!canAddmotion.get(current))
                continue;

            RailShape shape = shapes.get(current);
            if (shape != null) {
                Vec3 railVec = MinecartSim2020.getRailVec(shape);
                correction = followLinkOnRail(link, pos, correctionMagnitude, railVec).subtract(pos);
            } else
                correction = link.normalize().scale(correctionMagnitude);

            correction = VecHelper.clamp(correction, maxSpeed.get(current));

            motions.set(current, motions.get(current).add(correction));
        }

        motions.replaceWithParams(VecHelper::clamp, maxSpeed);
        carts.forEachWithParams(Entity::setDeltaMovement, motions);
    }

    public static Vec3 followLinkOnRail(Vec3 link, Vec3 cart, float diffToReduce, Vec3 railAxis) {
        double dotProduct = railAxis.dot(link);
        if (Double.isNaN(dotProduct) || dotProduct == 0 || diffToReduce == 0)
            return cart;

        Vec3 axis = railAxis.scale(-Math.signum(dotProduct));
        Vec3 center = cart.add(link);
        double radius = link.length() - diffToReduce;
        Vec3 intersectSphere = VecHelper.intersectSphere(cart, axis, center, radius);

        // Cannot satisfy on current rail vector
        if (intersectSphere == null)
            return cart.add(VecHelper.project(link, axis));

        return intersectSphere;
    }

}
