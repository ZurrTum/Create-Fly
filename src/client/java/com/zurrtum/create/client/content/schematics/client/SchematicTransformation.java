package com.zurrtum.create.client.content.schematics.client;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;

import static java.lang.Math.abs;

public class SchematicTransformation {

    private Vec3d chasingPos;
    private Vec3d prevChasingPos;
    private BlockPos target;

    private LerpedFloat scaleFrontBack, scaleLeftRight;
    private LerpedFloat rotation;
    private double xOrigin;
    private double zOrigin;

    public SchematicTransformation() {
        chasingPos = Vec3d.ZERO;
        prevChasingPos = Vec3d.ZERO;
        target = BlockPos.ORIGIN;
        scaleFrontBack = LerpedFloat.linear();
        scaleLeftRight = LerpedFloat.linear();
        rotation = LerpedFloat.angular();
    }

    public void init(BlockPos anchor, StructurePlacementData settings, Box bounds) {
        int leftRight = settings.getMirror() == BlockMirror.LEFT_RIGHT ? -1 : 1;
        int frontBack = settings.getMirror() == BlockMirror.FRONT_BACK ? -1 : 1;
        getScaleFB().chase(0, 0.45f, Chaser.EXP).startWithValue(frontBack);
        getScaleLR().chase(0, 0.45f, Chaser.EXP).startWithValue(leftRight);
        xOrigin = bounds.getLengthX() / 2f;
        zOrigin = bounds.getLengthZ() / 2f;

        int r = -(settings.getRotation().ordinal() * 90);
        rotation.chase(0, 0.45f, Chaser.EXP).startWithValue(r);

        target = fromAnchor(anchor);
        chasingPos = Vec3d.of(target);
        prevChasingPos = chasingPos;
    }

    public void applyTransformations(MatrixStack ms, Vec3d camera) {
        float pt = AnimationTickHolder.getPartialTicks();

        // Translation
        TransformStack.of(ms).translate(VecHelper.lerp(pt, prevChasingPos, chasingPos).subtract(camera));
        Vec3d rotationOffset = getRotationOffset(true);

        // Rotation & Mirror
        float fb = getScaleFB().getValue(pt);
        float lr = getScaleLR().getValue(pt);
        float rot = rotation.getValue(pt) + ((fb < 0 && lr < 0) ? 180 : 0);
        ms.translate(xOrigin, 0, zOrigin);
        TransformStack.of(ms).translate(rotationOffset).rotateYDegrees(rot).translateBack(rotationOffset);
        ms.scale(abs(fb), 1, abs(lr));
        ms.translate(-xOrigin, 0, -zOrigin);

    }

    public boolean isFlipped() {
        return getMirrorModifier(Axis.X) < 0 != getMirrorModifier(Axis.Z) < 0;
    }

    public Vec3d getRotationOffset(boolean ignoreMirrors) {
        Vec3d rotationOffset = Vec3d.ZERO;
        if ((int) (zOrigin * 2) % 2 != (int) (xOrigin * 2) % 2) {
            boolean xGreaterZ = xOrigin > zOrigin;
            float xIn = (xGreaterZ ? 0 : .5f);
            float zIn = (!xGreaterZ ? 0 : .5f);
            if (!ignoreMirrors) {
                xIn *= getMirrorModifier(Axis.X);
                zIn *= getMirrorModifier(Axis.Z);
            }
            rotationOffset = new Vec3d(xIn, 0, zIn);
        }
        return rotationOffset;
    }

    public Vec3d toLocalSpace(Vec3d vec) {
        float pt = AnimationTickHolder.getPartialTicks();
        Vec3d rotationOffset = getRotationOffset(true);

        vec = vec.subtract(VecHelper.lerp(pt, prevChasingPos, chasingPos));
        vec = vec.subtract(xOrigin + rotationOffset.x, 0, zOrigin + rotationOffset.z);
        vec = VecHelper.rotate(vec, -rotation.getValue(pt), Axis.Y);
        vec = vec.add(rotationOffset.x, 0, rotationOffset.z);
        vec = vec.multiply(getScaleFB().getValue(pt), 1, getScaleLR().getValue(pt));
        vec = vec.add(xOrigin, 0, zOrigin);

        return vec;
    }

    public StructurePlacementData toSettings() {
        StructurePlacementData settings = new StructurePlacementData();

        int i = (int) rotation.getChaseTarget();

        boolean mirrorlr = getScaleLR().getChaseTarget() < 0;
        boolean mirrorfb = getScaleFB().getChaseTarget() < 0;
        if (mirrorlr && mirrorfb) {
            mirrorlr = mirrorfb = false;
            i += 180;
        }
        i = i % 360;
        if (i < 0)
            i += 360;

        BlockRotation rotation = BlockRotation.NONE;
        switch (i) {
            case 90:
                rotation = BlockRotation.COUNTERCLOCKWISE_90;
                break;
            case 180:
                rotation = BlockRotation.CLOCKWISE_180;
                break;
            case 270:
                rotation = BlockRotation.CLOCKWISE_90;
                break;
            default:
        }

        settings.setRotation(rotation);
        if (mirrorfb)
            settings.setMirror(BlockMirror.FRONT_BACK);
        if (mirrorlr)
            settings.setMirror(BlockMirror.LEFT_RIGHT);

        return settings;
    }

    public BlockPos getAnchor() {
        Vec3d vec = Vec3d.ZERO.add(.5, 0, .5);
        Vec3d rotationOffset = getRotationOffset(false);
        vec = vec.subtract(xOrigin, 0, zOrigin);
        vec = vec.subtract(rotationOffset.x, 0, rotationOffset.z);
        vec = vec.multiply(getScaleFB().getChaseTarget(), 1, getScaleLR().getChaseTarget());
        vec = VecHelper.rotate(vec, rotation.getChaseTarget(), Axis.Y);
        vec = vec.add(xOrigin, 0, zOrigin);
        vec = vec.add(target.getX(), target.getY(), target.getZ());
        return BlockPos.ofFloored(vec.x, vec.y, vec.z);
    }

    public BlockPos fromAnchor(BlockPos pos) {
        Vec3d vec = Vec3d.ZERO.add(.5, 0, .5);
        Vec3d rotationOffset = getRotationOffset(false);
        vec = vec.subtract(xOrigin, 0, zOrigin);
        vec = vec.subtract(rotationOffset.x, 0, rotationOffset.z);
        vec = vec.multiply(getScaleFB().getChaseTarget(), 1, getScaleLR().getChaseTarget());
        vec = VecHelper.rotate(vec, rotation.getChaseTarget(), Axis.Y);
        vec = vec.add(xOrigin, 0, zOrigin);
        return pos.subtract(BlockPos.ofFloored(vec.x, vec.y, vec.z));
    }

    public int getRotationTarget() {
        return (int) rotation.getChaseTarget();
    }

    public int getMirrorModifier(Axis axis) {
        if (axis == Axis.Z)
            return (int) getScaleLR().getChaseTarget();
        return (int) getScaleFB().getChaseTarget();
    }

    public float getCurrentRotation() {
        float pt = AnimationTickHolder.getPartialTicks();
        return rotation.getValue(pt);
    }

    public void tick() {
        prevChasingPos = chasingPos;
        chasingPos = VecHelper.lerp(0.45f, chasingPos, Vec3d.of(target));
        getScaleLR().tickChaser();
        getScaleFB().tickChaser();
        rotation.tickChaser();
    }

    public void flip(Axis axis) {
        if (axis == Axis.X)
            getScaleLR().updateChaseTarget(getScaleLR().getChaseTarget() * -1);
        if (axis == Axis.Z)
            getScaleFB().updateChaseTarget(getScaleFB().getChaseTarget() * -1);
    }

    public void rotate90(boolean clockwise) {
        rotation.updateChaseTarget(rotation.getChaseTarget() + (clockwise ? -90 : 90));
    }

    public void move(int xIn, int yIn, int zIn) {
        moveTo(target.add(xIn, yIn, zIn));
    }

    public void startAt(BlockPos pos) {
        chasingPos = Vec3d.of(pos);
        prevChasingPos = chasingPos;
        moveTo(pos);
    }

    public void moveTo(BlockPos pos) {
        target = pos;
    }

    public void moveTo(int xIn, int yIn, int zIn) {
        moveTo(new BlockPos(xIn, yIn, zIn));
    }

    public LerpedFloat getScaleFB() {
        return scaleFrontBack;
    }

    public LerpedFloat getScaleLR() {
        return scaleLeftRight;
    }

}