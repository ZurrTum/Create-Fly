package com.zurrtum.create.content.decoration.slidingDoor;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn;
import com.zurrtum.create.content.contraptions.elevator.ElevatorColumn.ColumnCoords;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.lang.ref.WeakReference;

public class SlidingDoorMovementBehaviour extends MovementBehaviour {
    @Override
    public boolean mustTickWhileDisabled() {
        return true;
    }

    @Override
    public void tick(MovementContext context) {
        StructureBlockInfo structureBlockInfo = context.contraption.getBlocks().get(context.localPos);
        if (structureBlockInfo == null)
            return;
        boolean open = SlidingDoorBlockEntity.isOpen(structureBlockInfo.state());

        if (!context.world.isClient()) {
            tickOpen(context, open);
            return;
        }

        if (!(AllClientHandle.INSTANCE.getBlockEntityClientSide(context.contraption, context.localPos) instanceof SlidingDoorBlockEntity sdbe))
            return;
        boolean wasSettled = sdbe.animation.settled();
        sdbe.animation.chase(open ? 1 : 0, .15f, Chaser.LINEAR);
        sdbe.animation.tickChaser();

        if (!wasSettled && sdbe.animation.settled() && !open)
            context.world.playSoundClient(
                context.position.x,
                context.position.y,
                context.position.z,
                SoundEvents.BLOCK_IRON_DOOR_CLOSE,
                SoundCategory.BLOCKS,
                .125f,
                1,
                false
            );
    }

    protected void tickOpen(MovementContext context, boolean currentlyOpen) {
        boolean shouldOpen = shouldOpen(context);
        if (!shouldUpdate(context, shouldOpen))
            return;
        if (currentlyOpen == shouldOpen)
            return;

        BlockPos pos = context.localPos;
        Contraption contraption = context.contraption;

        StructureBlockInfo info = contraption.getBlocks().get(pos);
        if (info == null || !info.state().contains(DoorBlock.OPEN))
            return;

        toggleDoor(pos, contraption, info);

        Direction facing = getDoorFacing(context);
        BlockPos inWorldDoor = BlockPos.ofFloored(context.position).offset(facing);
        BlockState inWorldDoorState = context.world.getBlockState(inWorldDoor);
        if (inWorldDoorState.getBlock() instanceof DoorBlock db && inWorldDoorState.contains(DoorBlock.OPEN))
            if (inWorldDoorState.contains(DoorBlock.FACING) && inWorldDoorState.get(DoorBlock.FACING, Direction.UP).getAxis() == facing.getAxis())
                db.setOpen(null, context.world, inWorldDoorState, inWorldDoor, shouldOpen);

        if (shouldOpen)
            context.world.playSound(null, BlockPos.ofFloored(context.position), SoundEvents.BLOCK_IRON_DOOR_OPEN, SoundCategory.BLOCKS, .125f, 1);
    }

    private void toggleDoor(BlockPos pos, Contraption contraption, StructureBlockInfo info) {
        BlockState newState = info.state().cycle(DoorBlock.OPEN);
        contraption.entity.setBlock(pos, new StructureBlockInfo(info.pos(), newState, info.nbt()));

        BlockPos otherPos = newState.get(DoorBlock.HALF) == DoubleBlockHalf.LOWER ? pos.up() : pos.down();
        info = contraption.getBlocks().get(otherPos);
        if (info != null && info.state().contains(DoorBlock.OPEN)) {
            newState = info.state().cycle(DoorBlock.OPEN);
            contraption.entity.setBlock(otherPos, new StructureBlockInfo(info.pos(), newState, info.nbt()));
            contraption.invalidateColliders();
        }
    }

    protected boolean shouldUpdate(MovementContext context, boolean shouldOpen) {
        if (context.firstMovement && shouldOpen)
            return false;
        if (!context.data.contains("Open")) {
            context.data.putBoolean("Open", shouldOpen);
            return true;
        }
        boolean wasOpen = context.data.getBoolean("Open", false);
        context.data.putBoolean("Open", shouldOpen);
        return wasOpen != shouldOpen;
    }

    protected boolean shouldOpen(MovementContext context) {
        if (context.disabled)
            return false;
        Contraption contraption = context.contraption;
        boolean canOpen = context.motion.length() < 1 / 128f && !contraption.entity.isStalled() || contraption instanceof ElevatorContraption ec && ec.arrived;

        if (!canOpen) {
            context.temporaryData = null;
            return false;
        }

        if (context.temporaryData instanceof WeakReference<?> wr && wr.get() instanceof DoorControlBehaviour dcb)
            if (dcb.blockEntity != null && !dcb.blockEntity.isRemoved())
                return shouldOpenAt(dcb, context);

        context.temporaryData = null;
        DoorControlBehaviour doorControls = null;

        if (contraption instanceof ElevatorContraption ec)
            doorControls = getElevatorDoorControl(ec, context);
        if (context.contraption.entity instanceof CarriageContraptionEntity cce)
            doorControls = getTrainStationDoorControl(cce, context);

        if (doorControls == null)
            return false;

        context.temporaryData = new WeakReference<>(doorControls);
        return shouldOpenAt(doorControls, context);
    }

    protected boolean shouldOpenAt(DoorControlBehaviour controller, MovementContext context) {
        if (controller.mode == DoorControl.ALL)
            return true;
        if (controller.mode == DoorControl.NONE)
            return false;
        return controller.mode.matches(getDoorFacing(context));
    }

    protected DoorControlBehaviour getElevatorDoorControl(ElevatorContraption ec, MovementContext context) {
        Integer currentTargetY = ec.getCurrentTargetY(context.world);
        if (currentTargetY == null)
            return null;
        ColumnCoords columnCoords = ec.getGlobalColumn();
        if (columnCoords == null)
            return null;
        ElevatorColumn elevatorColumn = ElevatorColumn.get(context.world, columnCoords);
        if (elevatorColumn == null)
            return null;
        return BlockEntityBehaviour.get(context.world, elevatorColumn.contactAt(currentTargetY), DoorControlBehaviour.TYPE);
    }

    protected DoorControlBehaviour getTrainStationDoorControl(CarriageContraptionEntity cce, MovementContext context) {
        Carriage carriage = cce.getCarriage();
        if (carriage == null || carriage.train == null)
            return null;
        GlobalStation currentStation = carriage.train.getCurrentStation();
        if (currentStation == null)
            return null;

        BlockPos stationPos = currentStation.getBlockEntityPos();
        RegistryKey<World> stationDim = currentStation.getBlockEntityDimension();
        MinecraftServer server = context.world.getServer();
        if (server == null)
            return null;
        ServerWorld stationLevel = server.getWorld(stationDim);
        if (stationLevel == null || !stationLevel.isPosLoaded(stationPos))
            return null;
        return BlockEntityBehaviour.get(stationLevel, stationPos, DoorControlBehaviour.TYPE);
    }

    protected Direction getDoorFacing(MovementContext context) {
        Direction stateFacing = context.state.get(DoorBlock.FACING);
        Direction originalFacing = Direction.get(AxisDirection.POSITIVE, stateFacing.getAxis());
        Vec3d centerOfContraption = context.contraption.bounds.getCenter();
        Vec3d diff = Vec3d.ofCenter(context.localPos).add(Vec3d.of(stateFacing.getVector()).multiply(-.45f)).subtract(centerOfContraption);
        if (originalFacing.getAxis().choose(diff.x, diff.y, diff.z) < 0)
            originalFacing = originalFacing.getOpposite();

        Vec3d directionVec = Vec3d.of(originalFacing.getVector());
        directionVec = context.rotation.apply(directionVec);
        return Direction.getFacing(directionVec.x, directionVec.y, directionVec.z);
    }

}
