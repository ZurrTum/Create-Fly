package com.zurrtum.create.content.contraptions.piston;

import com.zurrtum.create.*;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.ContraptionCollider;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.PistonState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class MechanicalPistonBlockEntity extends LinearActuatorBlockEntity {
    protected int extensionLength;

    public MechanicalPistonBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_PISTON, pos, state);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        extensionLength = view.getIntOr("ExtensionLength", 0);
        super.read(view, clientPacket);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        view.putInt("ExtensionLength", extensionLength);
        super.write(view, clientPacket);
    }

    @Override
    public void assemble() throws AssemblyException {
        if (!(level.getBlockState(worldPosition).getBlock() instanceof MechanicalPistonBlock))
            return;

        Direction direction = getBlockState().getValue(BlockStateProperties.FACING);

        // Collect Construct
        PistonContraption contraption = new PistonContraption(direction, getMovementSpeed() < 0);
        if (!contraption.assemble(level, worldPosition))
            return;

        Direction positive = Direction.get(AxisDirection.POSITIVE, direction.getAxis());
        Direction movementDirection = getSpeed() > 0 ^ direction.getAxis() != Axis.Z ? positive : positive.getOpposite();

        BlockPos anchor = contraption.anchor.relative(direction, contraption.initialExtensionProgress);
        if (ContraptionCollider.isCollidingWithWorld(level, contraption, anchor.relative(movementDirection), movementDirection))
            return;

        // Check if not at limit already
        extensionLength = contraption.extensionLength;
        float resultingOffset = contraption.initialExtensionProgress + Math.signum(getMovementSpeed()) * .5f;
        if (resultingOffset <= 0 || resultingOffset >= extensionLength) {
            return;
        }

        // Run
        running = true;
        offset = contraption.initialExtensionProgress;
        sendData();
        clientOffsetDiff = 0;

        BlockPos startPos = BlockPos.ZERO.relative(direction, contraption.initialExtensionProgress);
        contraption.removeBlocksFromWorld(level, startPos);
        movedContraption = ControlledContraptionEntity.create(getLevel(), this, contraption);
        resetContraptionToOffset();
        forceMove = true;
        level.addFreshEntity(movedContraption);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(level, worldPosition);

        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);
    }

    @Override
    public void disassemble() {
        if (!running && movedContraption == null)
            return;
        if (!remove)
            getLevel().setBlock(worldPosition, getBlockState().setValue(MechanicalPistonBlock.STATE, PistonState.EXTENDED), 3 | 16);
        if (movedContraption != null) {
            resetContraptionToOffset();
            movedContraption.disassemble();
            AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(level, worldPosition);
        }
        running = false;
        movedContraption = null;
        sendData();

        if (remove)
            AllBlocks.MECHANICAL_PISTON.playerWillDestroy(level, worldPosition, getBlockState(), null);
    }

    @Override
    protected void collided() {
        super.collided();
        if (!running && getMovementSpeed() > 0)
            assembleNextTick = true;
    }

    @Override
    public float getMovementSpeed() {
        float movementSpeed = Mth.clamp(convertToLinear(getSpeed()), -.49f, .49f);
        if (level.isClientSide())
            movementSpeed *= AllClientHandle.INSTANCE.getServerSpeed();
        Direction pistonDirection = getBlockState().getValue(BlockStateProperties.FACING);
        int movementModifier = pistonDirection.getAxisDirection().getStep() * (pistonDirection.getAxis() == Axis.Z ? -1 : 1);
        movementSpeed = movementSpeed * -movementModifier + clientOffsetDiff / 2f;

        int extensionRange = getExtensionRange();
        movementSpeed = Mth.clamp(movementSpeed, 0 - offset, extensionRange - offset);
        if (sequencedOffsetLimit >= 0)
            movementSpeed = (float) Mth.clamp(movementSpeed, -sequencedOffsetLimit, sequencedOffsetLimit);
        return movementSpeed;
    }

    @Override
    protected int getExtensionRange() {
        return extensionLength;
    }

    @Override
    protected void visitNewPosition() {
    }

    @Override
    protected Vec3 toMotionVector(float speed) {
        Direction pistonDirection = getBlockState().getValue(BlockStateProperties.FACING);
        return Vec3.atLowerCornerOf(pistonDirection.getUnitVec3i()).scale(speed);
    }

    @Override
    protected Vec3 toPosition(float offset) {
        Vec3 position = Vec3.atLowerCornerOf(getBlockState().getValue(BlockStateProperties.FACING).getUnitVec3i()).scale(offset);
        return position.add(Vec3.atLowerCornerOf(movedContraption.getContraption().anchor));
    }

    @Override
    protected int getInitialOffset() {
        return movedContraption == null ? 0 : ((PistonContraption) movedContraption.getContraption()).initialExtensionProgress;
    }
}
