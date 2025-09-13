package com.zurrtum.create.content.kinetics.mechanicalArm;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class ArmAngleTarget {

    static final ArmAngleTarget NO_TARGET = new ArmAngleTarget();

    float baseAngle;
    float lowerArmAngle;
    float upperArmAngle;
    float headAngle;

    private ArmAngleTarget() {
        lowerArmAngle = 135;
        upperArmAngle = 45;
        headAngle = 0;
    }

    public ArmAngleTarget(BlockPos armPos, Vec3d pointTarget, Direction clawFacing, boolean ceiling) {
        Vec3d target = pointTarget;
        Vec3d origin = VecHelper.getCenterOf(armPos).add(0, ceiling ? -6 / 16f : 6 / 16f, 0);
        Vec3d clawTarget = target;
        target = target.add(Vec3d.of(clawFacing.getOpposite().getVector()).multiply(.5f));

        Vec3d diff = target.subtract(origin);
        float horizontalDistance = (float) diff.multiply(1, 0, 1).length();

        float baseAngle = AngleHelper.deg(MathHelper.atan2(diff.x, diff.z)) + 180;
        if (ceiling) {
            diff = diff.multiply(1, -1, 1);
            baseAngle = 180 - baseAngle;
        }

        float alphaOffset = AngleHelper.deg(MathHelper.atan2(diff.y, horizontalDistance));

        float a = 14 / 16f; // lower arm length
        float a2 = a * a;
        float b = 15 / 16f; // upper arm length
        float b2 = b * b;
        float diffLength = MathHelper.clamp(MathHelper.sqrt((float) (diff.y * diff.y + horizontalDistance * horizontalDistance)), 1 / 8f, a + b);
        float diffLength2 = diffLength * diffLength;

        float alphaRatio = (-b2 + a2 + diffLength2) / (2 * a * diffLength);
        float alpha = AngleHelper.deg(Math.acos(alphaRatio)) + alphaOffset;
        float betaRatio = (-diffLength2 + a2 + b2) / (2 * b * a);
        float beta = AngleHelper.deg(Math.acos(betaRatio));

        if (Float.isNaN(alpha))
            alpha = 0;
        if (Float.isNaN(beta))
            beta = 0;

        Vec3d headPos = new Vec3d(0, 0, 0);
        headPos = VecHelper.rotate(headPos.add(0, b, 0), beta + 180, Axis.X);
        headPos = VecHelper.rotate(headPos.add(0, a, 0), alpha - 90, Axis.X);
        headPos = VecHelper.rotate(headPos, baseAngle, Axis.Y);
        headPos = VecHelper.rotate(headPos, ceiling ? 180 : 0, Axis.X);
        headPos = headPos.add(origin);
        Vec3d headDiff = clawTarget.subtract(headPos);

        if (ceiling)
            headDiff = headDiff.multiply(1, -1, 1);

        float horizontalHeadDistance = (float) headDiff.multiply(1, 0, 1).length();
        float headAngle = (float) (alpha + beta + 135 - AngleHelper.deg(MathHelper.atan2(headDiff.y, horizontalHeadDistance)));

        this.lowerArmAngle = alpha;
        this.upperArmAngle = beta;
        this.headAngle = -headAngle;
        this.baseAngle = baseAngle;
    }

}
