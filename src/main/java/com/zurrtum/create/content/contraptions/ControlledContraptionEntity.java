package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.bearing.BearingContraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Ex: Pistons, bearings <br>
 * Controlled Contraption Entities can rotate around one axis and translate.
 * <br>
 * They are bound to an {@link IControlContraption}
 */
public class ControlledContraptionEntity extends AbstractContraptionEntity {

    protected @Nullable BlockPos controllerPos;
    protected @Nullable Axis rotationAxis;
    public float prevAngle;
    public float angle;
    protected float angleDelta;

    public ControlledContraptionEntity(EntityType<? extends ControlledContraptionEntity> type, Level world) {
        super(type, world);
    }

    public static ControlledContraptionEntity create(Level world, IControlContraption controller, Contraption contraption) {
        ControlledContraptionEntity entity = new ControlledContraptionEntity(AllEntityTypes.CONTROLLED_CONTRAPTION, world);
        entity.controllerPos = controller.getBlockPosition();
        entity.setContraption(contraption);
        return entity;
    }

    @Override
    public void setPos(double x, double y, double z) {
        super.setPos(x, y, z);
        if (!level().isClientSide())
            return;
        for (Entity entity : getPassengers())
            positionRider(entity);
    }

    @Override
    public Vec3 getContactPointMotion(Vec3 globalContactPoint) {
        if (contraption instanceof TranslatingContraption)
            return getDeltaMovement();
        return super.getContactPointMotion(globalContactPoint);
    }

    @Override
    protected void setContraption(@Nullable Contraption contraption) {
        super.setContraption(contraption);
        if (contraption instanceof BearingContraption)
            rotationAxis = ((BearingContraption) contraption).getFacing().getAxis();
    }

    @Override
    protected void readAdditional(ValueInput view, boolean spawnPacket) {
        super.readAdditional(view, spawnPacket);
        view.read("ControllerRelative", BlockPos.CODEC).ifPresent(pos -> controllerPos = pos.offset(blockPosition()));
        view.read("Axis", Axis.CODEC).ifPresent(axis -> rotationAxis = axis);
        angle = view.getFloatOr("Angle", 0);
    }

    @Override
    protected void writeAdditional(ValueOutput view, boolean spawnPacket) {
        super.writeAdditional(view, spawnPacket);
        view.store("ControllerRelative", BlockPos.CODEC, controllerPos.subtract(blockPosition()));
        if (rotationAxis != null)
            view.store("Axis", Axis.CODEC, rotationAxis);
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
    public Vec3 applyRotation(Vec3 localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, getAngle(partialTicks), rotationAxis);
        return localPos;
    }

    @Override
    public Vec3 reverseRotation(Vec3 localPos, float partialTicks) {
        localPos = VecHelper.rotate(localPos, -getAngle(partialTicks), rotationAxis);
        return localPos;
    }

    public void setAngle(float angle) {
        this.angle = angle;

        if (!level().isClientSide())
            return;
        for (Entity entity : getPassengers())
            positionRider(entity);
    }

    public float getAngle(float partialTicks) {
        return partialTicks == 1.0F ? angle : AngleHelper.angleLerp(partialTicks, prevAngle, angle);
    }

    public void setRotationAxis(Axis rotationAxis) {
        this.rotationAxis = rotationAxis;
    }

    @Nullable
    public Axis getRotationAxis() {
        return rotationAxis;
    }

    @Override
    public void teleportTo(double p_70634_1_, double p_70634_3_, double p_70634_5_) {
    }

    // Always noop this. Controlled Contraptions are given their position on the client from the BE
    @Override
    public void moveOrInterpolateTo(Vec3 pos, float yaw, float pitch) {
    }

    protected void tickContraption() {
        angleDelta = angle - prevAngle;
        prevAngle = angle;
        tickActors();

        if (controllerPos == null)
            return;
        if (!level().isLoaded(controllerPos))
            return;
        IControlContraption controller = getController();
        if (controller == null) {
            discard();
            return;
        }
        if (!controller.isAttachedTo(this)) {
            controller.attach(this);
            if (level().isClientSide())
                setPos(getX(), getY(), getZ());
        }
    }

    @Override
    protected boolean shouldActorTrigger(
        MovementContext context,
        StructureBlockInfo blockInfo,
        MovementBehaviour actor,
        Vec3 actorPosition,
        BlockPos gridPosition
    ) {
        if (super.shouldActorTrigger(context, blockInfo, actor, actorPosition, gridPosition))
            return true;

        // Special activation timer for actors in the center of a bearing contraption
        if (!(contraption instanceof BearingContraption bc))
            return false;
        Direction facing = bc.getFacing();
        Vec3 activeAreaOffset = actor.getActiveAreaOffset(context);
        if (!activeAreaOffset.multiply(VecHelper.axisAlingedPlaneOf(Vec3.atLowerCornerOf(facing.getUnitVec3i()))).equals(Vec3.ZERO))
            return false;
        if (!VecHelper.onSameAxis(blockInfo.pos(), BlockPos.ZERO, facing.getAxis()))
            return false;
        context.motion = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(angleDelta / 360.0);
        context.relativeMotion = context.motion;
        int timer = context.data.getIntOr("StationaryTimer", 0);
        if (timer > 0) {
            context.data.putInt("StationaryTimer", timer - 1);
            return false;
        }

        context.data.putInt("StationaryTimer", 20);
        return true;
    }

    @Nullable
    protected IControlContraption getController() {
        if (controllerPos == null)
            return null;
        if (!level().isLoaded(controllerPos))
            return null;
        BlockEntity be = level().getBlockEntity(controllerPos);
        if (!(be instanceof IControlContraption))
            return null;
        return (IControlContraption) be;
    }

    @Override
    protected StructureTransform makeStructureTransform() {
        BlockPos offset = BlockPos.containing(getAnchorVec().add(.5, .5, .5));
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
        setPosRaw(x, y, z);
        this.angle = this.prevAngle = angle;
    }
}
