package com.zurrtum.create.content.contraptions.piston;

import com.zurrtum.create.*;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.ContraptionCollider;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock.PistonState;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class MechanicalPistonBlockEntity extends LinearActuatorBlockEntity {
    protected int extensionLength;

    public MechanicalPistonBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_PISTON, pos, state);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        extensionLength = view.getInt("ExtensionLength", 0);
        super.read(view, clientPacket);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        view.putInt("ExtensionLength", extensionLength);
        super.write(view, clientPacket);
    }

    @Override
    public void assemble() throws AssemblyException {
        if (!(world.getBlockState(pos).getBlock() instanceof MechanicalPistonBlock))
            return;

        Direction direction = getCachedState().get(Properties.FACING);

        // Collect Construct
        PistonContraption contraption = new PistonContraption(direction, getMovementSpeed() < 0);
        if (!contraption.assemble(world, pos))
            return;

        Direction positive = Direction.get(AxisDirection.POSITIVE, direction.getAxis());
        Direction movementDirection = getSpeed() > 0 ^ direction.getAxis() != Axis.Z ? positive : positive.getOpposite();

        BlockPos anchor = contraption.anchor.offset(direction, contraption.initialExtensionProgress);
        if (ContraptionCollider.isCollidingWithWorld(world, contraption, anchor.offset(movementDirection), movementDirection))
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

        BlockPos startPos = BlockPos.ORIGIN.offset(direction, contraption.initialExtensionProgress);
        contraption.removeBlocksFromWorld(world, startPos);
        movedContraption = ControlledContraptionEntity.create(getWorld(), this, contraption);
        resetContraptionToOffset();
        forceMove = true;
        world.spawnEntity(movedContraption);

        AllSoundEvents.CONTRAPTION_ASSEMBLE.playOnServer(world, pos);

        if (contraption.containsBlockBreakers())
            award(AllAdvancements.CONTRAPTION_ACTORS);
    }

    @Override
    public void disassemble() {
        if (!running && movedContraption == null)
            return;
        if (!removed)
            getWorld().setBlockState(pos, getCachedState().with(MechanicalPistonBlock.STATE, PistonState.EXTENDED), 3 | 16);
        if (movedContraption != null) {
            resetContraptionToOffset();
            movedContraption.disassemble();
            AllSoundEvents.CONTRAPTION_DISASSEMBLE.playOnServer(world, pos);
        }
        running = false;
        movedContraption = null;
        sendData();

        if (removed)
            AllBlocks.MECHANICAL_PISTON.onBreak(world, pos, getCachedState(), null);
    }

    @Override
    protected void collided() {
        super.collided();
        if (!running && getMovementSpeed() > 0)
            assembleNextTick = true;
    }

    @Override
    public float getMovementSpeed() {
        float movementSpeed = MathHelper.clamp(convertToLinear(getSpeed()), -.49f, .49f);
        if (world.isClient())
            movementSpeed *= AllClientHandle.INSTANCE.getServerSpeed();
        Direction pistonDirection = getCachedState().get(Properties.FACING);
        int movementModifier = pistonDirection.getDirection().offset() * (pistonDirection.getAxis() == Axis.Z ? -1 : 1);
        movementSpeed = movementSpeed * -movementModifier + clientOffsetDiff / 2f;

        int extensionRange = getExtensionRange();
        movementSpeed = MathHelper.clamp(movementSpeed, 0 - offset, extensionRange - offset);
        if (sequencedOffsetLimit >= 0)
            movementSpeed = (float) MathHelper.clamp(movementSpeed, -sequencedOffsetLimit, sequencedOffsetLimit);
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
    protected Vec3d toMotionVector(float speed) {
        Direction pistonDirection = getCachedState().get(Properties.FACING);
        return Vec3d.of(pistonDirection.getVector()).multiply(speed);
    }

    @Override
    protected Vec3d toPosition(float offset) {
        Vec3d position = Vec3d.of(getCachedState().get(Properties.FACING).getVector()).multiply(offset);
        return position.add(Vec3d.of(movedContraption.getContraption().anchor));
    }

    @Override
    protected int getInitialOffset() {
        return movedContraption == null ? 0 : ((PistonContraption) movedContraption.getContraption()).initialExtensionProgress;
    }
}
