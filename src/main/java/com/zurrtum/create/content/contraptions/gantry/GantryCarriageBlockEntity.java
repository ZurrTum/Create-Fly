package com.zurrtum.create.content.contraptions.gantry;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.ContraptionCollider;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlockEntity;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencerInstructions;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;

import java.util.List;

public class GantryCarriageBlockEntity extends KineticBlockEntity {

    boolean assembleNextTick;
    protected AssemblyException lastException;

    public GantryCarriageBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.GANTRY_PINION, pos, state);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CONTRAPTION_ACTORS);
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
    }

    public void checkValidGantryShaft() {
        if (shouldAssemble())
            queueAssembly();
    }

    @Override
    public void initialize() {
        super.initialize();
        if (!getCachedState().canPlaceAt(world, pos))
            world.breakBlock(pos, true);
    }

    public void queueAssembly() {
        assembleNextTick = true;
    }

    @Override
    public void tick() {
        super.tick();

        if (world.isClient)
            return;

        if (assembleNextTick) {
            tryAssemble();
            assembleNextTick = false;
        }
    }

    public AssemblyException getLastAssemblyException() {
        return lastException;
    }

    private void tryAssemble() {
        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof GantryCarriageBlock))
            return;

        Direction direction = blockState.get(GantryCarriageBlock.FACING);
        GantryContraption contraption = new GantryContraption(direction);

        BlockEntity blockEntity = world.getBlockEntity(pos.offset(direction.getOpposite()));
        if (!(blockEntity instanceof GantryShaftBlockEntity shaftBE))
            return;
        BlockState shaftState = shaftBE.getCachedState();
        if (shaftState.getBlock() != AllBlocks.GANTRY_SHAFT)
            return;

        float pinionMovementSpeed = shaftBE.getPinionMovementSpeed();
        Direction shaftOrientation = shaftState.get(GantryShaftBlock.FACING);
        Direction movementDirection = shaftOrientation;
        if (pinionMovementSpeed < 0)
            movementDirection = movementDirection.getOpposite();

        try {
            lastException = null;
            if (!contraption.assemble(world, pos))
                return;

            sendData();
        } catch (AssemblyException e) {
            lastException = e;
            sendData();
            return;
        }
        if (ContraptionCollider.isCollidingWithWorld(world, contraption, pos.offset(movementDirection), movementDirection))
            return;

        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);

        contraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
        GantryContraptionEntity movedContraption = GantryContraptionEntity.create(world, contraption, shaftOrientation);
        BlockPos anchor = pos;
        movedContraption.setPos(anchor.getX(), anchor.getY(), anchor.getZ());
        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(world, pos);
        world.spawnEntity(movedContraption);

        if (shaftBE.sequenceContext != null && shaftBE.sequenceContext.instruction() == SequencerInstructions.TURN_DISTANCE)
            movedContraption.limitMovement(shaftBE.sequenceContext.getEffectiveValue(shaftBE.getTheoreticalSpeed()));
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        if (lastException != null) {
            view.put("LastException", AssemblyException.CODEC, lastException);
        }
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        lastException = view.read("LastException", AssemblyException.CODEC).orElse(null);
        super.read(view, clientPacket);
    }

    @Override
    public float propagateRotationTo(
        KineticBlockEntity target,
        BlockState stateFrom,
        BlockState stateTo,
        BlockPos diff,
        boolean connectedViaAxes,
        boolean connectedViaCogs
    ) {
        float defaultModifier = super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);

        if (connectedViaAxes)
            return defaultModifier;
        if (!stateTo.isOf(AllBlocks.GANTRY_SHAFT))
            return defaultModifier;
        if (!stateTo.get(GantryShaftBlock.POWERED))
            return defaultModifier;

        Direction direction = Direction.getFacing(diff.getX(), diff.getY(), diff.getZ());
        if (stateFrom.get(GantryCarriageBlock.FACING) != direction.getOpposite())
            return defaultModifier;
        return getGantryPinionModifier(stateTo.get(GantryShaftBlock.FACING), stateFrom.get(GantryCarriageBlock.FACING));
    }

    public static float getGantryPinionModifier(Direction shaft, Direction pinionDirection) {
        Axis shaftAxis = shaft.getAxis();
        float directionModifier = shaft.getDirection().offset();
        if (shaftAxis == Axis.Y)
            if (pinionDirection == Direction.NORTH || pinionDirection == Direction.EAST)
                return -directionModifier;
        if (shaftAxis == Axis.X)
            if (pinionDirection == Direction.DOWN || pinionDirection == Direction.SOUTH)
                return -directionModifier;
        if (shaftAxis == Axis.Z)
            if (pinionDirection == Direction.UP || pinionDirection == Direction.WEST)
                return -directionModifier;
        return directionModifier;
    }

    private boolean shouldAssemble() {
        BlockState blockState = getCachedState();
        if (!(blockState.getBlock() instanceof GantryCarriageBlock))
            return false;
        Direction facing = blockState.get(GantryCarriageBlock.FACING).getOpposite();
        BlockState shaftState = world.getBlockState(pos.offset(facing));
        if (!(shaftState.getBlock() instanceof GantryShaftBlock))
            return false;
        if (shaftState.get(GantryShaftBlock.POWERED))
            return false;
        BlockEntity be = world.getBlockEntity(pos.offset(facing));
        return be instanceof GantryShaftBlockEntity && ((GantryShaftBlockEntity) be).canAssembleOn();
    }
}