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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class GantryContraptionEntity extends AbstractContraptionEntity {

    Direction movementAxis;
    public double clientOffsetDiff;
    public double axisMotion;

    public double sequencedOffsetLimit;

    public GantryContraptionEntity(EntityType<? extends GantryContraptionEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        sequencedOffsetLimit = -1;
    }

    public static GantryContraptionEntity create(World world, Contraption contraption, Direction movementAxis) {
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
        World world = getWorld();
        if (world.isClient) {
            clientOffsetDiff *= .75;
            updateClientMotion();
        }

        checkPinionShaft();
        tickActors();
        Vec3d movementVec = getVelocity();

        if (ContraptionCollider.collideBlocks(this)) {
            if (!world.isClient)
                disassemble();
            return;
        }

        if (!isStalled() && age > 2) {
            if (sequencedOffsetLimit >= 0)
                movementVec = VecHelper.clampComponentWise(movementVec, (float) sequencedOffsetLimit);
            move(movementVec.x, movementVec.y, movementVec.z);
            if (sequencedOffsetLimit > 0)
                sequencedOffsetLimit = Math.max(0, sequencedOffsetLimit - movementVec.length());
        }

        if (Math.signum(prevAxisMotion) != Math.signum(axisMotion) && prevAxisMotion != 0)
            contraption.stop(world);
        if (!world.isClient && (prevAxisMotion != axisMotion || age % 3 == 0))
            sendPacket();
    }

    @Override
    public void disassemble() {
        sequencedOffsetLimit = -1;
        super.disassemble();
    }

    protected void checkPinionShaft() {
        Vec3d movementVec;
        Direction facing = ((GantryContraption) contraption).getFacing();
        Vec3d currentPosition = getAnchorVec().add(.5, .5, .5);
        BlockPos gantryShaftPos = BlockPos.ofFloored(currentPosition).offset(facing.getOpposite());

        World world = getWorld();
        BlockEntity be = world.getBlockEntity(gantryShaftPos);
        if (!(be instanceof GantryShaftBlockEntity gantryShaftBlockEntity) || !be.getCachedState().isOf(AllBlocks.GANTRY_SHAFT)) {
            if (!world.isClient) {
                setContraptionMotion(Vec3d.ZERO);
                disassemble();
            }
            return;
        }

        BlockState blockState = be.getCachedState();
        Direction direction = blockState.get(GantryShaftBlock.FACING);

        float pinionMovementSpeed = gantryShaftBlockEntity.getPinionMovementSpeed();
        if (blockState.get(GantryShaftBlock.POWERED) || pinionMovementSpeed == 0) {
            setContraptionMotion(Vec3d.ZERO);
            if (!world.isClient)
                disassemble();
            return;
        }

        if (sequencedOffsetLimit >= 0)
            pinionMovementSpeed = (float) MathHelper.clamp(pinionMovementSpeed, -sequencedOffsetLimit, sequencedOffsetLimit);
        movementVec = Vec3d.of(direction.getVector()).multiply(pinionMovementSpeed);

        Vec3d nextPosition = currentPosition.add(movementVec);
        double currentCoord = direction.getAxis().choose(currentPosition.x, currentPosition.y, currentPosition.z);
        double nextCoord = direction.getAxis().choose(nextPosition.x, nextPosition.y, nextPosition.z);

        if ((MathHelper.floor(currentCoord) + .5 < nextCoord != (pinionMovementSpeed * direction.getDirection().offset() < 0)))
            if (!gantryShaftBlockEntity.canAssembleOn()) {
                setContraptionMotion(Vec3d.ZERO);
                if (!world.isClient)
                    disassemble();
                return;
            }

        if (world.isClient)
            return;

        axisMotion = pinionMovementSpeed;
        setContraptionMotion(movementVec);
    }

    @Override
    protected void writeAdditional(WriteView view, boolean spawnPacket) {
        view.put("GantryAxis", Direction.CODEC, movementAxis);
        if (sequencedOffsetLimit >= 0)
            view.putDouble("SequencedOffsetLimit", sequencedOffsetLimit);
        super.writeAdditional(view, spawnPacket);
    }

    @Override
    protected void readAdditional(ReadView view, boolean spawnPacket) {
        movementAxis = view.read("GantryAxis", Direction.CODEC).orElse(Direction.DOWN);
        sequencedOffsetLimit = view.getDouble("SequencedOffsetLimit", -1);
        super.readAdditional(view, spawnPacket);
    }

    @Override
    public Vec3d applyRotation(Vec3d localPos, float partialTicks) {
        return localPos;
    }

    @Override
    public Vec3d reverseRotation(Vec3d localPos, float partialTicks) {
        return localPos;
    }

    @Override
    protected StructureTransform makeStructureTransform() {
        return new StructureTransform(BlockPos.ofFloored(getAnchorVec().add(.5, .5, .5)), 0, 0, 0);
    }

    @Override
    protected float getStalledAngle() {
        return 0;
    }

    @Override
    public void requestTeleport(double destX, double destY, double destZ) {
    }

    @Override
    public void updateTrackedPositionAndAngles(Vec3d pos, float yaw, float pitch) {
    }

    @Override
    public void handleStallInformation(double x, double y, double z, float angle) {
        setPos(x, y, z);
        clientOffsetDiff = 0;
    }

    @Override
    public ContraptionRotationState getRotationState() {
        return ContraptionRotationState.NONE;
    }

    public void updateClientMotion() {
        float modifier = movementAxis.getDirection().offset();
        Vec3d motion = Vec3d.of(movementAxis.getVector())
            .multiply((axisMotion + clientOffsetDiff * modifier / 2d) * AllClientHandle.INSTANCE.getServerSpeed());
        if (sequencedOffsetLimit >= 0)
            motion = VecHelper.clampComponentWise(motion, (float) sequencedOffsetLimit);
        setContraptionMotion(motion);
    }

    public double getAxisCoord() {
        Vec3d anchorVec = getAnchorVec();
        return movementAxis.getAxis().choose(anchorVec.x, anchorVec.y, anchorVec.z);
    }

    public void sendPacket() {
        ServerChunkManager chunkManager = ((ServerWorld) getWorld()).getChunkManager();
        chunkManager.sendToOtherNearbyPlayers(this, new GantryContraptionUpdatePacket(getId(), getAxisCoord(), axisMotion, sequencedOffsetLimit));
    }
}
