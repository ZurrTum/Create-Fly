package com.zurrtum.create.content.kinetics.steamEngine;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.content.contraptions.bearing.WindmillBearingBlockEntity.RotationDirection;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.base.IRotate;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;
import java.util.List;

public class SteamEngineBlockEntity extends SmartBlockEntity {

    protected ServerScrollOptionBehaviour<RotationDirection> movementDirection;

    public WeakReference<PoweredShaftBlockEntity> target;
    public WeakReference<FluidTankBlockEntity> source;

    public float prevAngle = 0;

    public SteamEngineBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.STEAM_ENGINE, pos, state);
        source = new WeakReference<>(null);
        target = new WeakReference<>(null);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        movementDirection = new ServerScrollOptionBehaviour<>(RotationDirection.class, this);
        movementDirection.withCallback($ -> onDirectionChanged());
        behaviours.add(movementDirection);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.STEAM_ENGINE);
    }

    private void onDirectionChanged() {
    }

    @Override
    public void tick() {
        super.tick();
        FluidTankBlockEntity tank = getTank();
        PoweredShaftBlockEntity shaft = getShaft();

        if (tank == null || shaft == null || !isValid()) {
            if (world.isClient())
                return;
            if (shaft == null)
                return;
            if (!shaft.getPos().subtract(pos).equals(shaft.enginePos))
                return;
            if (shaft.engineEfficiency == 0)
                return;
            Direction facing = SteamEngineBlock.getFacing(getCachedState());
            if (world.isPosLoaded(pos.offset(facing.getOpposite())))
                shaft.update(pos, 0, 0);
            return;
        }

        BlockState shaftState = shaft.getCachedState();
        Axis targetAxis = Axis.X;
        if (shaftState.getBlock() instanceof IRotate ir)
            targetAxis = ir.getRotationAxis(shaftState);
        boolean verticalTarget = targetAxis == Axis.Y;

        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.STEAM_ENGINE))
            return;
        Direction facing = SteamEngineBlock.getFacing(blockState);
        if (facing.getAxis() == Axis.Y)
            facing = blockState.get(SteamEngineBlock.FACING);

        float efficiency = MathHelper.clamp(tank.boiler.getEngineEfficiency(tank.getTotalTankSize()), 0, 1);
        if (efficiency > 0)
            award(AllAdvancements.STEAM_ENGINE);

        int conveyedSpeedLevel = efficiency == 0 ? 1 : verticalTarget ? 1 : (int) GeneratingKineticBlockEntity.convertToDirection(1, facing);
        if (targetAxis == Axis.Z)
            conveyedSpeedLevel *= -1;
        if (movementDirection.get() == RotationDirection.COUNTER_CLOCKWISE)
            conveyedSpeedLevel *= -1;

        float shaftSpeed = shaft.getTheoreticalSpeed();
        if (shaft.hasSource() && shaftSpeed != 0 && conveyedSpeedLevel != 0 && (shaftSpeed > 0) != (conveyedSpeedLevel > 0)) {
            movementDirection.setValue(1 - movementDirection.get().ordinal());
            conveyedSpeedLevel *= -1;
        }

        shaft.update(pos, conveyedSpeedLevel, efficiency);

        if (!world.isClient())
            return;

        AllClientHandle.INSTANCE.spawnSteamEngineParticles(this);
    }

    @Override
    public void remove() {
        PoweredShaftBlockEntity shaft = getShaft();
        if (shaft != null)
            shaft.remove(pos);
        super.remove();
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().expand(2);
    }

    public PoweredShaftBlockEntity getShaft() {
        PoweredShaftBlockEntity shaft = target.get();
        if (shaft == null || shaft.isRemoved() || !shaft.canBePoweredBy(pos)) {
            if (shaft != null)
                target = new WeakReference<>(null);
            Direction facing = SteamEngineBlock.getFacing(getCachedState());
            BlockEntity anyShaftAt = world.getBlockEntity(pos.offset(facing, 2));
            if (anyShaftAt instanceof PoweredShaftBlockEntity ps && ps.canBePoweredBy(pos))
                target = new WeakReference<>(shaft = ps);
        }
        return shaft;
    }

    public FluidTankBlockEntity getTank() {
        FluidTankBlockEntity tank = source.get();
        if (tank == null || tank.isRemoved()) {
            if (tank != null)
                source = new WeakReference<>(null);
            Direction facing = SteamEngineBlock.getFacing(getCachedState());
            BlockEntity be = world.getBlockEntity(pos.offset(facing.getOpposite()));
            if (be instanceof FluidTankBlockEntity tankBe)
                source = new WeakReference<>(tank = tankBe);
        }
        if (tank == null)
            return null;
        return tank.getControllerBE();
    }

    public boolean isValid() {
        Direction dir = SteamEngineBlock.getConnectedDirection(getCachedState()).getOpposite();

        World level = getWorld();
        if (level == null)
            return false;

        return level.getBlockState(getPos().offset(dir)).isOf(AllBlocks.FLUID_TANK);
    }
}
