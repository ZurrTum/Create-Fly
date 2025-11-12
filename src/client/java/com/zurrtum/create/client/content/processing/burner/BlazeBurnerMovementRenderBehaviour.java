package com.zurrtum.create.client.content.processing.burner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

public class BlazeBurnerMovementRenderBehaviour implements MovementRenderBehaviour {
    public void tick(MovementContext context) {
        if (!shouldRender(context))
            return;

        RandomSource r = context.world.getRandom();
        Vec3 c = context.position;
        Vec3 v = c.add(VecHelper.offsetRandomly(Vec3.ZERO, r, .125f).multiply(1, 0, 1));
        if (r.nextInt(3) == 0 && context.motion.length() < 1 / 64f)
            context.world.addParticle(ParticleTypes.LARGE_SMOKE, v.x, v.y, v.z, 0, 0, 0);

        LerpedFloat headAngle = getHeadAngle(context);
        boolean quickTurn = shouldRenderHat(context) && !Mth.equal(context.relativeMotion.length(), 0);
        headAngle.chase(
            headAngle.getValue() + AngleHelper.getShortestAngleDiff(headAngle.getValue(), getTargetAngle(context)),
            .5f,
            quickTurn ? LerpedFloat.Chaser.EXP : LerpedFloat.Chaser.exp(5)
        );
        headAngle.tickChaser();
    }

    private boolean shouldRender(MovementContext context) {
        return context.state.getValueOrElse(BlazeBurnerBlock.HEAT_LEVEL, BlazeBurnerBlock.HeatLevel.NONE) != BlazeBurnerBlock.HeatLevel.NONE;
    }

    private LerpedFloat getHeadAngle(MovementContext context) {
        if (!(context.temporaryData instanceof LerpedFloat))
            context.temporaryData = LerpedFloat.angular().startWithValue(getTargetAngle(context));
        return (LerpedFloat) context.temporaryData;
    }

    private float getTargetAngle(MovementContext context) {
        if (shouldRenderHat(context) && !Mth.equal(
            context.relativeMotion.length(),
            0
        ) && context.contraption.entity instanceof CarriageContraptionEntity cce) {

            float angle = AngleHelper.deg(-Mth.atan2(context.relativeMotion.x, context.relativeMotion.z));
            return cce.getInitialOrientation().getAxis() == Direction.Axis.X ? angle + 180 : angle;
        }

        Entity player = Minecraft.getInstance().getCameraEntity();
        if (player != null && !player.isInvisible() && context.position != null) {
            Vec3 applyRotation = context.contraption.entity.reverseRotation(player.position().subtract(context.position), 1);
            double dx = applyRotation.x;
            double dz = applyRotation.z;
            return AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
        }
        return 0;
    }

    private boolean shouldRenderHat(MovementContext context) {
        CompoundTag data = context.data;
        if (!data.contains("Conductor"))
            data.putBoolean("Conductor", determineIfConducting(context));
        return data.getBooleanOr("Conductor", false) && (context.contraption.entity instanceof CarriageContraptionEntity cce) && cce.hasSchedule();
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
    public MovementRenderState getRenderState(
        Vec3 camera,
        Font textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Matrix4f worldMatrix4f
    ) {
        if (!shouldRender(context)) {
            return null;
        }
        BlockState blockState = context.state;
        BlazeBurnerBlock.HeatLevel heatLevel = BlazeBurnerBlock.getHeatLevelOf(blockState);
        if (heatLevel == BlazeBurnerBlock.HeatLevel.NONE) {
            return null;
        }
        if (!heatLevel.isAtLeast(BlazeBurnerBlock.HeatLevel.FADING)) {
            heatLevel = BlazeBurnerBlock.HeatLevel.FADING;
        }
        BlazeBurnerMovementRenderState state = new BlazeBurnerMovementRenderState(context.localPos);
        Level level = context.world;
        float horizontalAngle = AngleHelper.rad(getHeadAngle(context).getValue(AnimationTickHolder.getPartialTicks(level)));
        boolean drawGoggles = context.blockEntityData.contains("Goggles");
        boolean drawHat = shouldRenderHat(context) || context.blockEntityData.contains("TrainHat");
        int hashCode = context.hashCode();
        state.data = BlazeBurnerRenderer.getBlazeBurnerRenderData(
            level,
            blockState,
            heatLevel,
            0,
            horizontalAngle,
            false,
            drawGoggles,
            drawHat ? AllPartialModels.TRAIN_HAT : null,
            hashCode
        );
        return state;
    }

    public static class BlazeBurnerMovementRenderState extends MovementRenderState {
        public BlazeBurnerRenderer.BlazeBurnerRenderData data;

        public BlazeBurnerMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            data.render(matrices, queue);
        }
    }
}
