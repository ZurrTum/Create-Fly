package com.zurrtum.create.client.content.processing.burner;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;

public class BlazeBurnerMovementRenderBehaviour implements MovementRenderBehaviour {
    public void tick(MovementContext context) {
        if (!shouldRender(context))
            return;

        Random r = context.world.getRandom();
        Vec3d c = context.position;
        Vec3d v = c.add(VecHelper.offsetRandomly(Vec3d.ZERO, r, .125f).multiply(1, 0, 1));
        if (r.nextInt(3) == 0 && context.motion.length() < 1 / 64f)
            context.world.addParticleClient(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);

        LerpedFloat headAngle = getHeadAngle(context);
        boolean quickTurn = shouldRenderHat(context) && !MathHelper.approximatelyEquals(context.relativeMotion.length(), 0);
        headAngle.chase(
            headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), getTargetAngle(context)),
            .5f,
            quickTurn ? LerpedFloat.Chaser.EXP : LerpedFloat.Chaser.exp(5)
        );
        headAngle.tickChaser();
    }

    private boolean shouldRender(MovementContext context) {
        return context.state.get(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE) != BlazeBurnerBlock.HeatLevel.NONE;
    }

    private LerpedFloat getHeadAngle(MovementContext context) {
        if (!(context.temporaryData instanceof LerpedFloat))
            context.temporaryData = LerpedFloat.angular().startWithValue(getTargetAngle(context));
        return (LerpedFloat) context.temporaryData;
    }

    private float getTargetAngle(MovementContext context) {
        if (shouldRenderHat(context) && !MathHelper.approximatelyEquals(
            context.relativeMotion.length(),
            0
        ) && context.contraption.entity instanceof CarriageContraptionEntity cce) {

            float angle = AngleHelper.deg(-MathHelper.atan2(context.relativeMotion.x, context.relativeMotion.z));
            return cce.getInitialOrientation().getAxis() == Direction.Axis.X ? angle + 180 : angle;
        }

        Entity player = MinecraftClient.getInstance().cameraEntity;
        if (player != null && !player.isInvisible() && context.position != null) {
            Vec3d applyRotation = context.contraption.entity.reverseRotation(player.getPos().subtract(context.position), 1);
            double dx = applyRotation.x;
            double dz = applyRotation.z;
            return AngleHelper.deg(-MathHelper.atan2(dz, dx)) - 90;
        }
        return 0;
    }

    private boolean shouldRenderHat(MovementContext context) {
        NbtCompound data = context.data;
        if (!data.contains("Conductor"))
            data.putBoolean("Conductor", determineIfConducting(context));
        return data.getBoolean("Conductor", false) && (context.contraption.entity instanceof CarriageContraptionEntity cce) && cce.hasSchedule();
    }

    private boolean determineIfConducting(MovementContext context) {
        Contraption contraption = context.contraption;
        if (!(contraption instanceof CarriageContraption carriageContraption))
            return false;
        Direction assemblyDirection = carriageContraption.getAssemblyDirection();
        for (Direction direction : Iterate.directionsInAxis(assemblyDirection.getAxis()))
            if (carriageContraption.inControl(context.localPos, direction))
                return true;
        return false;
    }

    @Override
    public void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider buffer
    ) {
        if (!shouldRender(context))
            return;
        BlazeBurnerRenderer.renderInContraption(context, renderWorld, matrices, buffer, getHeadAngle(context), shouldRenderHat(context));
    }
}
