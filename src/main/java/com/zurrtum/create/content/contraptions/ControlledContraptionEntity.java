package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.bearing.BearingContraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

/**
 * Ex: Pistons, bearings <br>
 * Controlled Contraption Entities can rotate around one axis and translate.
 * <br>
 * They are bound to an {@link IControlContraption}
 */
public class ControlledContraptionEntity extends AbstractContraptionEntity {

    protected BlockPos controllerPos;
    protected Axis rotationAxis;
    public float prevAngle;
    public float angle;
    protected float angleDelta;

    public ControlledContraptionEntity(EntityType<? extends ControlledContraptionEntity> type, World world) {
        super(type, world);
    }

    public static ControlledContraptionEntity create(World world, IControlContraption controller, Contraption contraption) {
        ControlledContraptionEntity entity = new ControlledContraptionEntity(AllEntityTypes.CONTROLLED_CONTRAPTION, world);
        entity.controllerPos = controller.getBlockPosition();
        entity.setContraption(contraption);
        return entity;
    }

    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(x, y, z);
        if (!getWorld().isClient())
            return;
        for (Entity entity : getPassengerList())
            updatePassengerPosition(entity);
    }

    @Override
    public Vec3d getContactPointMotion(Vec3d globalContactPoint) {
        if (contraption instanceof TranslatingContraption)
            return getVelocity();
        return super.getContactPointMotion(globalContactPoint);
    }

    @Override
    protected void setContraption(Contraption contraption) {
        super.setContraption(contraption);
        if (contraption instanceof BearingContraption)
            rotationAxis = ((BearingContraption) contraption).getFacing().getAxis();
    }

    @Override
    protected void readAdditional(ReadView view, boolean spawnPacket) {
        super.readAdditional(view, spawnPacket);
        view.read("ControllerRelative", BlockPos.CODEC).ifPresent(pos -> controllerPos = pos.add(getBlockPos()));
        view.read("Axis", Axis.CODEC).ifPresent(axis -> rotationAxis = axis);
        angle = view.getFloat("Angle", 0);
    }

    @Override
    protected void writeAdditional(WriteView view, boolean spawnPacket) {
        super.writeAdditional(view, spawnPacket);
        view.put("ControllerRelative", BlockPos.CODEC, controllerPos.subtract(getBlockPos()));
        if (rotationAxis != null)
            view.put("Axis", Axis.CODEC, rotationAxis);
        view.putFloat("Angle", angle);
    }

    @Override
    public ContraptionRotationState getRotationState() {
        ContraptionRotationState crs = new ContraptionRotationState();
        if (rotationAxis == Axis.X)
            crs.xRotation = angle;
        if (rotationAxis == Axis.Y)
            crs.yRotation = angle;
        if (rotationAxis == Axis.Z)
            crs.zRotation = angle;
        return crs;
    }

    @Override
    public Vec3d applyRotation(Vec3d localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, getAngle(partialTicks), rotationAxis);
        return localPos;
    }

    @Override
    public Vec3d reverseRotation(Vec3d localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, -getAngle(partialTicks), rotationAxis);
        return localPos;
    }

    public void setAngle(float angle) {
        this.angle = angle;

        if (!getWorld().isClient())
            return;
        for (Entity entity : getPassengerList())
            updatePassengerPosition(entity);
    }

    public float getAngle(float partialTicks) {
        return partialTicks == 1.0F ? angle : AngleHelper.angleLerp(partialTicks, prevAngle, angle);
    }

    public void setRotationAxis(Axis rotationAxis) {
        this.rotationAxis = rotationAxis;
    }

    public Axis getRotationAxis() {
        return rotationAxis;
    }

    @Override
    public void requestTeleport(double p_70634_1_, double p_70634_3_, double p_70634_5_) {
    }

    // Always noop this. Controlled Contraptions are given their position on the client from the BE
    @Override
    public void updateTrackedPositionAndAngles(Vec3d pos, float yaw, float pitch) {
    }

    protected void tickContraption() {
        angleDelta = angle - prevAngle;
        prevAngle = angle;
        tickActors();

        if (controllerPos == null)
            return;
        if (!getWorld().isPosLoaded(controllerPos))
            return;
        IControlContraption controller = getController();
        if (controller == null) {
            discard();
            return;
        }
        if (!controller.isAttachedTo(this)) {
            controller.attach(this);
            if (getWorld().isClient)
                setPosition(getX(), getY(), getZ());
        }
    }

    @Override
    protected boolean shouldActorTrigger(
        MovementContext context,
        StructureBlockInfo blockInfo,
        MovementBehaviour actor,
        Vec3d actorPosition,
        BlockPos gridPosition
    ) {
        if (super.shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition))
            return true;

        // Special activation timer for actors in the center of a bearing contraption
        if (!(contraption instanceof BearingContraption bc))
            return false;
        Direction facing = bc.getFacing();
        Vec3d activeAreaOffset = actor.getActiveAreaOffset(context);
        if (!activeAreaOffset.multiply(VecHelper.axisAlingedPlaneOf(Vec3d.of(facing.getVector()))).equals(Vec3d.ZERO))
            return false;
        if (!VecHelper.onSameAxis(blockInfo.pos(), BlockPos.ORIGIN, facing.getAxis()))
            return false;
        context.motion = Vec3d.of(facing.getVector()).multiply(angleDelta / 360.0);
        context.relativeMotion = context.motion;
        int timer = context.data.getInt("StationaryTimer", 0);
        if (timer > 0) {
            context.data.putInt("StationaryTimer", timer - 1);
            return false;
        }

        context.data.putInt("StationaryTimer", 20);
        return true;
    }

    protected IControlContraption getController() {
        if (controllerPos == null)
            return null;
        if (!getWorld().isPosLoaded(controllerPos))
            return null;
        BlockEntity be = getWorld().getBlockEntity(controllerPos);
        if (!(be instanceof IControlContraption))
            return null;
        return (IControlContraption) be;
    }

    @Override
    protected StructureTransform makeStructureTransform() {
        BlockPos offset = BlockPos.ofFloored(getAnchorVec().add(.5, .5, .5));
        float xRot = rotationAxis == Axis.X ? angle : 0;
        float yRot = rotationAxis == Axis.Y ? angle : 0;
        float zRot = rotationAxis == Axis.Z ? angle : 0;
        return new StructureTransform(offset, xRot, yRot, zRot);
    }

    @Override
    protected void onContraptionStalled() {
        IControlContraption controller = getController();
        if (controller != null)
            controller.onStall();
        super.onContraptionStalled();
    }

    @Override
    protected float getStalledAngle() {
        return angle;
    }

    @Override
    public void handleStallInformation(double x, double y, double z, float angle) {
        setPos(x, y, z);
        this.angle = this.prevAngle = angle;
    }
}
