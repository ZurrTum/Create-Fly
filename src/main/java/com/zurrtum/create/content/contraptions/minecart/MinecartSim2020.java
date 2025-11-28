package com.zurrtum.create.content.contraptions.minecart;

import com.mojang.datafixers.util.Pair;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.vehicle.minecart.*;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockState;
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

    public static void moveCartAlongTrack(ServerLevel world, AbstractMinecart cart, Vec3 forcedMovement, BlockPos cartPos, BlockState trackState) {
        if (forcedMovement.equals(Vec3.ZERO))
            return;
        Vec3 previousMotion = cart.getDeltaMovement();
        MinecartBehavior behavior = cart.getBehavior();
        if (behavior instanceof OldMinecartBehavior controller) {
            moveCartAlongTrack(world, cart, controller, forcedMovement, cartPos, trackState);
        } else {
            moveCartAlongTrack(world, cart, (NewMinecartBehavior) behavior, forcedMovement, cartPos, trackState);
        }
        cart.setDeltaMovement(previousMotion);
    }

    public static void moveCartAlongTrack(
        ServerLevel world,
        AbstractMinecart cart,
        OldMinecartBehavior controller,
        Vec3 forcedMovement,
        BlockPos cartPos,
        BlockState trackState
    ) {
        cart.fallDistance = 0.0F;

        double x = cart.getX();
        double y = cart.getY();
        double z = cart.getZ();

        double actualX = x;
        double actualY = y;
        double actualZ = z;

        Vec3 actualVec = controller.getPos(actualX, actualY, actualZ);
        actualY = cartPos.getY();

        BaseRailBlock abstractrailblock = (BaseRailBlock) trackState.getBlock();
        RailShape railshape = trackState.getValue(abstractrailblock.getShapeProperty());
        switch (railshape) {
            case ASCENDING_EAST:
                forcedMovement = forcedMovement.add(-1 * 0.0078125D, 0.0D, 0.0D);
                actualY++;
                break;
            case ASCENDING_WEST:
                forcedMovement = forcedMovement.add(0.0078125D, 0.0D, 0.0D);
                actualY++;
                break;
            case ASCENDING_NORTH:
                forcedMovement = forcedMovement.add(0.0D, 0.0D, 0.0078125D);
                actualY++;
                break;
            case ASCENDING_SOUTH:
                forcedMovement = forcedMovement.add(0.0D, 0.0D, -1 * 0.0078125D);
                actualY++;
            default:
                break;
        }

        Pair<Vec3i, Vec3i> pair = AbstractMinecart.exits(railshape);
        Vec3i Vector3i = pair.getFirst();
        Vec3i Vector3i1 = pair.getSecond();
        double d4 = Vector3i1.getX() - Vector3i.getX();
        double d5 = Vector3i1.getZ() - Vector3i.getZ();
        //		double d6 = Math.sqrt(d4 * d4 + d5 * d5);
        double d7 = forcedMovement.x * d4 + forcedMovement.z * d5;
        if (d7 < 0.0D) {
            d4 = -d4;
            d5 = -d5;
        }

        double d23 = (double) cartPos.getX() + 0.5D + (double) Vector3i.getX() * 0.5D;
        double d10 = (double) cartPos.getZ() + 0.5D + (double) Vector3i.getZ() * 0.5D;
        double d12 = (double) cartPos.getX() + 0.5D + (double) Vector3i1.getX() * 0.5D;
        double d13 = (double) cartPos.getZ() + 0.5D + (double) Vector3i1.getZ() * 0.5D;
        d4 = d12 - d23;
        d5 = d13 - d10;
        double d14;
        if (d4 == 0.0D) {
            d14 = actualZ - (double) cartPos.getZ();
        } else if (d5 == 0.0D) {
            d14 = actualX - (double) cartPos.getX();
        } else {
            double d15 = actualX - d23;
            double d16 = actualZ - d10;
            d14 = (d15 * d4 + d16 * d5) * 2.0D;
        }

        actualX = d23 + d4 * d14;
        actualZ = d10 + d5 * d14;

        cart.setPos(actualX, actualY, actualZ);
        cart.setDeltaMovement(forcedMovement);

        double s = cart.isVehicle() ? 0.75 : 1.0;
        double t = controller.getMaxSpeed(world);
        Vec3 vec3d2 = cart.getDeltaMovement();
        cart.move(MoverType.SELF, new Vec3(Mth.clamp(s * vec3d2.x, -t, t), 0.0, Mth.clamp(s * vec3d2.z, -t, t)));

        x = cart.getX();
        y = cart.getY();
        z = cart.getZ();

        if (Vector3i.getY() != 0 && Mth.floor(x) - cartPos.getX() == Vector3i.getX() && Mth.floor(z) - cartPos.getZ() == Vector3i.getZ()) {
            cart.setPos(x, y + (double) Vector3i.getY(), z);
        } else if (Vector3i1.getY() != 0 && Mth.floor(x) - cartPos.getX() == Vector3i1.getX() && Mth.floor(z) - cartPos.getZ() == Vector3i1.getZ()) {
            cart.setPos(x, y + (double) Vector3i1.getY(), z);
        }

        x = cart.getX();
        y = cart.getY();
        z = cart.getZ();

        Vec3 Vector3d3 = controller.getPos(x, y, z);
        if (Vector3d3 != null && actualVec != null) {
            double d17 = (actualVec.y - Vector3d3.y) * 0.05D;
            Vec3 Vector3d4 = cart.getDeltaMovement();
            double d18 = Vector3d4.horizontalDistance();
            if (d18 > 0.0D) {
                cart.setDeltaMovement(Vector3d4.multiply((d18 + d17) / d18, 1.0D, (d18 + d17) / d18));
            }

            cart.setPos(x, Vector3d3.y, z);
        }

        x = cart.getX();
        y = cart.getY();
        z = cart.getZ();

        int j = Mth.floor(x);
        int i = Mth.floor(z);
        if (j != cartPos.getX() || i != cartPos.getZ()) {
            Vec3 Vector3d5 = cart.getDeltaMovement();
            double d26 = Vector3d5.horizontalDistance();
            cart.setDeltaMovement(d26 * (double) (j - cartPos.getX()), Vector3d5.y, d26 * (double) (i - cartPos.getZ()));
        }
    }

    public static void moveCartAlongTrack(
        ServerLevel world,
        AbstractMinecart cart,
        NewMinecartBehavior controller,
        Vec3 forcedMovement,
        BlockPos cartPos,
        BlockState trackState
    ) {
        Vec3 initialStepDeltaMovement = forcedMovement;
        BlockPos currentPos = cartPos;
        BlockState currentState = trackState;
        for (NewMinecartBehavior.TrackIteration trackIteration = new NewMinecartBehavior.TrackIteration(); trackIteration.shouldIterate() && cart.isAlive(); trackIteration.firstIteration = false) {
            if (!trackIteration.firstIteration) {
                initialStepDeltaMovement = cart.getDeltaMovement();
                currentPos = cart.getCurrentBlockPosOrRailBelow();
                currentState = world.getBlockState(currentPos);
            }
            boolean onRails = BaseRailBlock.isRail(currentState);
            if (cart.isOnRails() != onRails) {
                cart.setOnRails(onRails);
                controller.adjustToRails(currentPos, currentState, false);
            }

            if (onRails) {
                cart.resetFallDistance();
                cart.setOldPosAndRot();
                if (currentState.is(Blocks.ACTIVATOR_RAIL)) {
                    cart.activateMinecart(
                        world,
                        currentPos.getX(),
                        currentPos.getY(),
                        currentPos.getZ(),
                        currentState.getValue(PoweredRailBlock.POWERED)
                    );
                }

                RailShape shape = currentState.getValue(((BaseRailBlock) currentState.getBlock()).getShapeProperty());
                Vec3 newDeltaMovement = controller.calculateTrackSpeed(
                    world,
                    initialStepDeltaMovement.horizontal(),
                    trackIteration,
                    currentPos,
                    currentState,
                    shape
                );
                if (trackIteration.firstIteration) {
                    trackIteration.movementLeft = newDeltaMovement.horizontalDistance();
                } else {
                    trackIteration.movementLeft = trackIteration.movementLeft + (newDeltaMovement.horizontalDistance() - initialStepDeltaMovement.horizontalDistance());
                }

                cart.setDeltaMovement(newDeltaMovement);
                trackIteration.movementLeft = controller.stepAlongTrack(currentPos, shape, trackIteration.movementLeft);
            } else {
                cart.comeOffTrack(world);
                trackIteration.movementLeft = 0.0;
            }

            Vec3 stepPosition = cart.position();
            Vec3 stepDelta = stepPosition.subtract(cart.oldPosition());
            double stepLength = stepDelta.length();
            if (stepLength > 1.0E-5F) {
                if (!(stepDelta.horizontalDistanceSqr() > 1.0E-5F)) {
                    if (!cart.isOnRails()) {
                        cart.setXRot(cart.onGround() ? 0.0F : Mth.rotLerp(0.2F, cart.getXRot(), 0.0F));
                    }
                } else {
                    float yRot = 180.0F - (float) (Math.atan2(stepDelta.z, stepDelta.x) * 180.0 / Math.PI);
                    float xRot = cart.onGround() && !cart.isOnRails() ? 0.0F : 90.0F - (float) (Math.atan2(
                        stepDelta.horizontalDistance(),
                        stepDelta.y
                    ) * 180.0 / Math.PI);
                    yRot += cart.isFlipped() ? 180.0F : 0.0F;
                    xRot *= cart.isFlipped() ? -1.0F : 1.0F;
                    controller.setRotation(yRot, xRot);
                }

                controller.lerpSteps.add(new NewMinecartBehavior.MinecartStep(
                    stepPosition,
                    cart.getDeltaMovement(),
                    cart.getYRot(),
                    cart.getXRot(),
                    (float) Math.min(stepLength, cart.getMaxSpeed(world))
                ));
            } else if (initialStepDeltaMovement.horizontalDistanceSqr() > 0.0) {
                controller.lerpSteps.add(new NewMinecartBehavior.MinecartStep(
                    stepPosition,
                    cart.getDeltaMovement(),
                    cart.getYRot(),
                    cart.getXRot(),
                    1.0F
                ));
            }

            if (stepLength > 1.0E-5F || trackIteration.firstIteration) {
                cart.applyEffectsFromBlocks();
                cart.applyEffectsFromBlocks();
            }
        }
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
