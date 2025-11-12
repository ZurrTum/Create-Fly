package com.zurrtum.create.content.contraptions.gantry;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.ContraptionCollider;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlock;
import com.zurrtum.create.content.kinetics.gantry.GantryShaftBlockEntity;
import com.zurrtum.create.infrastructure.packet.s2c.GantryContraptionUpdatePacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class GantryContraptionEntity extends AbstractContraptionEntity {

    Direction movementAxis;
    public double clientOffsetDiff;
    public double axisMotion;

    public double sequencedOffsetLimit;

    public GantryContraptionEntity(EntityType<? extends GantryContraptionEntity> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);
        sequencedOffsetLimit = -1;
    }

    public static GantryContraptionEntity create(Level world, Contraption contraption, Direction movementAxis) {
        GantryContraptionEntity entity = new GantryContraptionEntity(AllEntityTypes.GANTRY_CONTRAPTION, world);
        entity.setContraption(contraption);
        entity.movementAxis = movementAxis;
        return entity;
    }

    public void limitMovement(double maxOffset) {
        sequencedOffsetLimit = maxOffset;
    }

    @Override
    protected void tickContraption() {
        if (!(contraption instanceof GantryContraption))
            return;

        double prevAxisMotion = axisMotion;
        Level world = level();
        if (world.isClientSide()) {
            clientOffsetDiff *= .75;
            updateClientMotion();
        }

        checkPinionShaft();
        tickActors();
        Vec3 movementVec = getDeltaMovement();

        if (ContraptionCollider.collideBlocks(this)) {
            if (!world.isClientSide())
                disassemble();
            return;
        }

        if (!isStalled() && tickCount > 2) {
            if (sequencedOffsetLimit >= 0)
                movementVec = VecHelper.clampComponentWise(movementVec, (float) sequencedOffsetLimit);
            move(movementVec.x, movementVec.y, movementVec.z);
            if (sequencedOffsetLimit > 0)
                sequencedOffsetLimit = Math.max(0, sequencedOffsetLimit - movementVec.length());
        }

        if (Math.signum(prevAxisMotion) != Math.signum(axisMotion) && prevAxisMotion != 0)
            contraption.stop(world);
        if (!world.isClientSide() && (prevAxisMotion != axisMotion || tickCount % 3 == 0))
            sendPacket();
    }

    @Override
    public void disassemble() {
        sequencedOffsetLimit = -1;
        super.disassemble();
    }

    protected void checkPinionShaft() {
        Vec3 movementVec;
        Direction facing = ((GantryContraption) contraption).getFacing();
        Vec3 currentPosition = getAnchorVec().add(.5, .5, .5);
        BlockPos gantryShaftPos = BlockPos.containing(currentPosition).relative(facing.getOpposite());

        Level world = level();
        BlockEntity be = world.getBlockEntity(gantryShaftPos);
        if (!(be instanceof GantryShaftBlockEntity gantryShaftBlockEntity) || !be.getBlockState().is(AllBlocks.GANTRY_SHAFT)) {
            if (!world.isClientSide()) {
                setContraptionMotion(Vec3.ZERO);
                disassemble();
            }
            return;
        }

        BlockState blockState = be.getBlockState();
        Direction direction = blockState.getValue(GantryShaftBlock.FACING);

        float pinionMovementSpeed = gantryShaftBlockEntity.getPinionMovementSpeed();
        if (blockState.getValue(GantryShaftBlock.POWERED) || pinionMovementSpeed == 0) {
            setContraptionMotion(Vec3.ZERO);
            if (!world.isClientSide())
                disassemble();
            return;
        }

        if (sequencedOffsetLimit >= 0)
            pinionMovementSpeed = (float) Mth.clamp(pinionMovementSpeed, -sequencedOffsetLimit, sequencedOffsetLimit);
        movementVec = Vec3.atLowerCornerOf(direction.getUnitVec3i()).scale(pinionMovementSpeed);

        Vec3 nextPosition = currentPosition.add(movementVec);
        double currentCoord = direction.getAxis().choose(currentPosition.x, currentPosition.y, currentPosition.z);
        double nextCoord = direction.getAxis().choose(nextPosition.x, nextPosition.y, nextPosition.z);

        if ((Mth.floor(currentCoord) + .5 < nextCoord != (pinionMovementSpeed * direction.getAxisDirection().getStep() < 0)))
            if (!gantryShaftBlockEntity.canAssembleOn()) {
                setContraptionMotion(Vec3.ZERO);
                if (!world.isClientSide())
                    disassemble();
                return;
            }

        if (world.isClientSide())
            return;

        axisMotion = pinionMovementSpeed;
        setContraptionMotion(movementVec);
    }

    @Override
    protected void writeAdditional(ValueOutput view, boolean spawnPacket) {
        view.store("GantryAxis", Direction.CODEC, movementAxis);
        if (sequencedOffsetLimit >= 0)
            view.putDouble("SequencedOffsetLimit", sequencedOffsetLimit);
        super.writeAdditional(view, spawnPacket);
    }

    @Override
    protected void readAdditional(ValueInput view, boolean spawnPacket) {
        movementAxis = view.read("GantryAxis", Direction.CODEC).orElse(Direction.DOWN);
        sequencedOffsetLimit = view.getDoubleOr("SequencedOffsetLimit", -1);
        super.readAdditional(view, spawnPacket);
    }

    @Override
    public Vec3 applyRotation(Vec3 localPos, float partialTicks) {
        return localPos;
    }

    @Override
    public Vec3 reverseRotation(Vec3 localPos, float partialTicks) {
        return localPos;
    }

    @Override
    protected StructureTransform makeStructureTransform() {
        return new StructureTransform(BlockPos.containing(getAnchorVec().add(.5, .5, .5)), 0, 0, 0);
    }

    @Override
    protected float getStalledAngle() {
        return 0;
    }

    @Override
    public void teleportTo(double destX, double destY, double destZ) {
    }

    @Override
    public void moveOrInterpolateTo(Vec3 pos, float yaw, float pitch) {
    }

    @Override
    public void handleStallInformation(double x, double y, double z, float angle) {
        setPosRaw(x, y, z);
        clientOffsetDiff = 0;
    }

    @Override
    public ContraptionRotationState getRotationState() {
        return ContraptionRotationState.NONE;
    }

    public void updateClientMotion() {
        float modifier = movementAxis.getAxisDirection().getStep();
        Vec3 motion = Vec3.atLowerCornerOf(movementAxis.getUnitVec3i())
            .scale((axisMotion + clientOffsetDiff * modifier / 2d) * AllClientHandle.INSTANCE.getServerSpeed());
        if (sequencedOffsetLimit >= 0)
            motion = VecHelper.clampComponentWise(motion, (float) sequencedOffsetLimit);
        setContraptionMotion(motion);
    }

    public double getAxisCoord() {
        Vec3 anchorVec = getAnchorVec();
        return movementAxis.getAxis().choose(anchorVec.x, anchorVec.y, anchorVec.z);
    }

    public void sendPacket() {
        ServerChunkCache chunkManager = ((ServerLevel) level()).getChunkSource();
        chunkManager.sendToTrackingPlayers(this, new GantryContraptionUpdatePacket(getId(), getAxisCoord(), axisMotion, sequencedOffsetLimit));
    }
}
