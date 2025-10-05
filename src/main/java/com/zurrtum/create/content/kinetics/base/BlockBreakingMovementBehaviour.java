package com.zurrtum.create.content.kinetics.base;

import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.OrientedContraptionEntity;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.mounted.MountedContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.BlockState;
import net.minecraft.block.FallingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class BlockBreakingMovementBehaviour extends MovementBehaviour {

    @Override
    public void startMoving(MovementContext context) {
        if (context.world.isClient())
            return;
        context.data.putInt("BreakerId", -BlockBreakingKineticBlockEntity.NEXT_BREAKER_ID.incrementAndGet());
    }

    @Override
    public void visitNewPosition(MovementContext context, BlockPos pos) {
        World world = context.world;
        BlockState stateVisited = world.getBlockState(pos);

        if (!stateVisited.isSolidBlock(world, pos))
            damageEntities(context, pos, world);
        if (world.isClient())
            return;

        if (!canBreak(world, pos, stateVisited))
            return;
        context.data.put("BreakingPos", BlockPos.CODEC, pos);
        context.stall = true;
    }

    public void damageEntities(MovementContext context, BlockPos pos, World world) {
        if (context.contraption.entity instanceof OrientedContraptionEntity oce && oce.nonDamageTicks > 0)
            return;
        DamageSource damageSource = getDamageSource(world);
        if (damageSource == null && !throwsEntities(world))
            return;
        Entities:
        for (Entity entity : world.getNonSpectatingEntities(Entity.class, new Box(pos))) {
            if (entity instanceof ItemEntity)
                continue;
            if (entity instanceof AbstractContraptionEntity)
                continue;
            if (entity.isConnectedThroughVehicle(context.contraption.entity))
                continue;
            if (entity instanceof AbstractMinecartEntity)
                for (Entity passenger : entity.getPassengersDeep())
                    if (passenger instanceof AbstractContraptionEntity && ((AbstractContraptionEntity) passenger).getContraption() == context.contraption)
                        continue Entities;

            if (damageSource != null && !world.isClient()) {
                float damage = (float) MathHelper.clamp(6 * Math.pow(context.relativeMotion.length(), 0.4) + 1, 2, 10);
                entity.damage((ServerWorld) world, damageSource, damage);
            }
            if (throwsEntities(world) && (world.isClient() == (entity instanceof PlayerEntity)))
                throwEntity(context, entity);
        }
    }

    protected void throwEntity(MovementContext context, Entity entity) {
        Vec3d motionBoost = context.motion.add(0, context.motion.length() / 4f, 0);
        int maxBoost = 4;
        if (motionBoost.length() > maxBoost) {
            motionBoost = motionBoost.subtract(motionBoost.normalize().multiply(motionBoost.length() - maxBoost));
        }
        entity.setVelocity(entity.getVelocity().add(motionBoost));
        entity.velocityModified = true;
    }

    protected DamageSource getDamageSource(World level) {
        return null;
    }

    protected boolean throwsEntities(World level) {
        return getDamageSource(level) != null;
    }

    @Override
    public void cancelStall(MovementContext context) {
        NbtCompound data = context.data;
        if (context.world.isClient())
            return;
        if (!data.contains("BreakingPos"))
            return;

        World world = context.world;
        int id = data.getInt("BreakerId", 0);
        BlockPos breakingPos = data.get("BreakingPos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);

        data.remove("Progress");
        data.remove("TicksUntilNextProgress");
        data.remove("BreakingPos");

        super.cancelStall(context);
        world.setBlockBreakingInfo(id, breakingPos, -1);
    }

    @Override
    public void stopMoving(MovementContext context) {
        cancelStall(context);
    }

    @Override
    public void tick(MovementContext context) {
        tickBreaker(context);

        NbtCompound data = context.data;
        if (!data.contains("WaitingTicks"))
            return;

        int waitingTicks = data.getInt("WaitingTicks", 0);
        if (waitingTicks-- > 0) {
            data.putInt("WaitingTicks", waitingTicks);
            context.stall = true;
            return;
        }

        BlockPos pos = data.get("LastPos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        data.remove("WaitingTicks");
        data.remove("LastPos");
        context.stall = false;
        visitNewPosition(context, pos);
    }

    public void tickBreaker(MovementContext context) {
        NbtCompound data = context.data;
        if (context.world.isClient())
            return;
        if (!data.contains("BreakingPos")) {
            context.stall = false;
            return;
        }
        if (context.relativeMotion.equals(Vec3d.ZERO)) {
            context.stall = false;
            return;
        }

        int ticksUntilNextProgress = data.getInt("TicksUntilNextProgress", 0);
        if (ticksUntilNextProgress-- > 0) {
            data.putInt("TicksUntilNextProgress", ticksUntilNextProgress);
            return;
        }

        World world = context.world;
        BlockPos breakingPos = data.get("BreakingPos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
        int destroyProgress = data.getInt("Progress", 0);
        int id = data.getInt("BreakerId", 0);
        BlockState stateToBreak = world.getBlockState(breakingPos);
        float blockHardness = stateToBreak.getHardness(world, breakingPos);

        if (!canBreak(world, breakingPos, stateToBreak)) {
            if (destroyProgress != 0) {
                data.remove("Progress");
                data.remove("TicksUntilNextProgress");
                data.remove("BreakingPos");
                world.setBlockBreakingInfo(id, breakingPos, -1);
            }
            context.stall = false;
            return;
        }

        float breakSpeed = getBlockBreakingSpeed(context);
        destroyProgress += MathHelper.clamp((int) (breakSpeed / blockHardness), 1, 10 - destroyProgress);
        world.playSound(null, breakingPos, stateToBreak.getSoundGroup().getHitSound(), SoundCategory.NEUTRAL, .25f, 1);

        if (destroyProgress >= 10) {
            world.setBlockBreakingInfo(id, breakingPos, -1);

            // break falling blocks from top to bottom
            BlockPos ogPos = breakingPos;
            BlockState stateAbove = world.getBlockState(breakingPos.up());
            while (stateAbove.getBlock() instanceof FallingBlock) {
                breakingPos = breakingPos.up();
                stateAbove = world.getBlockState(breakingPos.up());
            }
            stateToBreak = world.getBlockState(breakingPos);

            context.stall = false;
            if (shouldDestroyStartBlock(stateToBreak))
                destroyBlock(context, breakingPos);
            onBlockBroken(context, ogPos, stateToBreak);
            ticksUntilNextProgress = -1;
            data.remove("Progress");
            data.remove("TicksUntilNextProgress");
            data.remove("BreakingPos");
            return;
        }

        ticksUntilNextProgress = (int) (blockHardness / breakSpeed);
        world.setBlockBreakingInfo(id, breakingPos, destroyProgress);
        data.putInt("TicksUntilNextProgress", ticksUntilNextProgress);
        data.putInt("Progress", destroyProgress);
    }

    protected void destroyBlock(MovementContext context, BlockPos breakingPos) {
        BlockHelper.destroyBlock(context.world, breakingPos, 1f, stack -> this.dropItem(context, stack));
    }

    protected float getBlockBreakingSpeed(MovementContext context) {
        float lowerLimit = 1 / 128f;
        if (context.contraption instanceof MountedContraption)
            lowerLimit = 1f;
        if (context.contraption instanceof CarriageContraption)
            lowerLimit = 2f;
        return MathHelper.clamp(Math.abs(context.getAnimationSpeed()) / 500f, lowerLimit, 16f);
    }

    protected boolean shouldDestroyStartBlock(BlockState stateToBreak) {
        return true;
    }

    public boolean canBreak(World world, BlockPos breakingPos, BlockState state) {
        float blockHardness = state.getHardness(world, breakingPos);
        return BlockBreakingKineticBlockEntity.isBreakable(state, blockHardness);
    }

    protected void onBlockBroken(MovementContext context, BlockPos pos, BlockState brokenState) {
        // Check for falling blocks
        if (!(brokenState.getBlock() instanceof FallingBlock))
            return;

        NbtCompound data = context.data;
        data.putInt("WaitingTicks", 10);
        data.put("LastPos", BlockPos.CODEC, pos);
        context.stall = true;
    }

}
