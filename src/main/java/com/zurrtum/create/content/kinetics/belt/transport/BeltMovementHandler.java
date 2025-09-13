package com.zurrtum.create.content.kinetics.belt.transport;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

import java.util.List;

import static net.minecraft.entity.MovementType.SELF;
import static net.minecraft.util.math.Direction.AxisDirection.NEGATIVE;
import static net.minecraft.util.math.Direction.AxisDirection.POSITIVE;

public class BeltMovementHandler {

    public static class TransportedEntityInfo {
        int ticksSinceLastCollision;
        BlockPos lastCollidedPos;
        BlockState lastCollidedState;

        public TransportedEntityInfo(BlockPos collision, BlockState belt) {
            refresh(collision, belt);
        }

        public void refresh(BlockPos collision, BlockState belt) {
            ticksSinceLastCollision = 0;
            lastCollidedPos = new BlockPos(collision).toImmutable();
            lastCollidedState = belt;
        }

        public TransportedEntityInfo tick() {
            ticksSinceLastCollision++;
            return this;
        }

        public int getTicksSinceLastCollision() {
            return ticksSinceLastCollision;
        }
    }

    public static boolean canBeTransported(Entity entity) {
        return entity.isAlive() && (!(entity instanceof PlayerEntity p) || !p.isSneaking() || CardboardArmorHandler.testForStealth(entity));
    }

    public static void transportEntity(BeltBlockEntity beltBE, Entity entityIn, TransportedEntityInfo info) {
        BlockPos pos = info.lastCollidedPos;
        World world = beltBE.getWorld();
        BlockEntity be = world.getBlockEntity(pos);
        BlockEntity blockEntityBelowPassenger = world.getBlockEntity(entityIn.getBlockPos());
        BlockState blockState = info.lastCollidedState;
        Direction movementFacing = Direction.from(
            blockState.get(Properties.HORIZONTAL_FACING).getAxis(),
            beltBE.getSpeed() < 0 ? POSITIVE : NEGATIVE
        );

        boolean collidedWithBelt = be instanceof BeltBlockEntity;
        boolean betweenBelts = blockEntityBelowPassenger instanceof BeltBlockEntity && blockEntityBelowPassenger != be;

        // Don't fight other Belts
        if (!collidedWithBelt || betweenBelts) {
            return;
        }

        // Too slow
        boolean notHorizontal = beltBE.getCachedState().get(BeltBlock.SLOPE) != BeltSlope.HORIZONTAL;
        if (Math.abs(beltBE.getSpeed()) < 1)
            return;

        // Not on top
        if (entityIn.getY() - .25f < pos.getY())
            return;

        // Lock entities in place
        boolean isPlayer = entityIn instanceof PlayerEntity;
        if (entityIn instanceof LivingEntity && !isPlayer)
            ((LivingEntity) entityIn).addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 10, 1, false, false));

        final Direction beltFacing = blockState.get(Properties.HORIZONTAL_FACING);
        final BeltSlope slope = blockState.get(BeltBlock.SLOPE);
        final Axis axis = beltFacing.getAxis();
        float movementSpeed = beltBE.getBeltMovementSpeed();
        final Direction movementDirection = Direction.get(axis == Axis.X ? NEGATIVE : POSITIVE, axis);

        Vec3i centeringDirection = Direction.get(POSITIVE, beltFacing.rotateYClockwise().getAxis()).getVector();
        Vec3d movement = Vec3d.of(movementDirection.getVector()).multiply(movementSpeed);

        double diffCenter = axis == Axis.Z ? (pos.getX() + .5f - entityIn.getX()) : (pos.getZ() + .5f - entityIn.getZ());
        if (Math.abs(diffCenter) > 48 / 64f)
            return;

        BeltPart part = blockState.get(BeltBlock.PART);
        float top = 13 / 16f;
        boolean onSlope = notHorizontal && (part == BeltPart.MIDDLE || part == BeltPart.PULLEY || part == (slope == BeltSlope.UPWARD ? BeltPart.END : BeltPart.START) && entityIn.getY() - pos.getY() < top || part == (slope == BeltSlope.UPWARD ? BeltPart.START : BeltPart.END) && entityIn.getY() - pos.getY() > top);

        boolean movingDown = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.DOWNWARD : BeltSlope.UPWARD);
        boolean movingUp = onSlope && slope == (movementFacing == beltFacing ? BeltSlope.UPWARD : BeltSlope.DOWNWARD);

        if (beltFacing.getAxis() == Axis.Z) {
            boolean b = movingDown;
            movingDown = movingUp;
            movingUp = b;
        }

        if (movingUp)
            movement = movement.add(0, Math.abs(axis.choose(movement.x, movement.y, movement.z)), 0);
        if (movingDown)
            movement = movement.add(0, -Math.abs(axis.choose(movement.x, movement.y, movement.z)), 0);

        Vec3d centering = Vec3d.of(centeringDirection).multiply(diffCenter * Math.min(Math.abs(movementSpeed), .1f) * 4);

        if (!(entityIn instanceof LivingEntity) || ((LivingEntity) entityIn).forwardSpeed == 0 && ((LivingEntity) entityIn).sidewaysSpeed == 0)
            movement = movement.add(centering);

        float step = entityIn.getStepHeight();
        if (!isPlayer && entityIn instanceof LivingEntity livingEntity) {
            step = (float) livingEntity.getAttributeBaseValue(EntityAttributes.STEP_HEIGHT);
            //noinspection DataFlowIssue
            livingEntity.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(1.0f);
        }

        // Entity Collisions
        if (Math.abs(movementSpeed) < .5f) {
            Vec3d checkDistance = movement.normalize().multiply(0.5);
            Box bb = entityIn.getBoundingBox();
            Box checkBB = new Box(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ);
            checkBB = checkBB.offset(checkDistance).expand(-Math.abs(checkDistance.x), -Math.abs(checkDistance.y), -Math.abs(checkDistance.z));
            List<Entity> list = world.getOtherEntities(entityIn, checkBB);
            list.removeIf(e -> shouldIgnoreBlocking(entityIn, e));
            if (!list.isEmpty()) {
                entityIn.setVelocity(0, 0, 0);
                info.ticksSinceLastCollision--;
                return;
            }
        }

        entityIn.fallDistance = 0;

        if (movingUp) {
            float minVelocity = .13f;
            float yMovement = (float) -(Math.max(Math.abs(movement.y), minVelocity));
            entityIn.move(SELF, new Vec3d(0, yMovement, 0));
            entityIn.move(SELF, movement.multiply(1, 0, 1));
        } else if (movingDown) {
            entityIn.move(SELF, movement.multiply(1, 0, 1));
            entityIn.move(SELF, movement.multiply(0, 1, 0));
        } else {
            entityIn.move(SELF, movement);
        }

        entityIn.setOnGround(true);

        if (!isPlayer && entityIn instanceof LivingEntity livingEntity) {
            livingEntity.getAttributeInstance(EntityAttributes.STEP_HEIGHT).setBaseValue(step);
        }

        boolean movedPastEndingSlope = onSlope && (world.getBlockState(entityIn.getBlockPos())
            .isOf(AllBlocks.BELT) || world.getBlockState(entityIn.getBlockPos().down()).isOf(AllBlocks.BELT));

        if (movedPastEndingSlope && !movingDown && Math.abs(movementSpeed) > 0)
            entityIn.setPosition(entityIn.getX(), entityIn.getY() + movement.y, entityIn.getZ());
        if (movedPastEndingSlope) {
            entityIn.setVelocity(movement);
            entityIn.velocityModified = true;
        }

    }

    public static boolean shouldIgnoreBlocking(Entity me, Entity other) {
        if (other instanceof AbstractDecorationEntity)
            return true;
        if (other.getPistonBehavior() == PistonBehavior.IGNORE)
            return true;
        return isRidingOrBeingRiddenBy(me, other);
    }

    public static boolean isRidingOrBeingRiddenBy(Entity me, Entity other) {
        for (Entity entity : me.getPassengerList()) {
            if (entity.equals(other))
                return true;
            if (isRidingOrBeingRiddenBy(entity, other))
                return true;
        }
        return false;
    }

}
