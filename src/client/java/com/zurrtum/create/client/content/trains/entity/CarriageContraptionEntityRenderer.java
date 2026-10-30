package com.zurrtum.create.client.content.trains.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.client.content.contraptions.render.OrientedContraptionEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import com.zurrtum.create.client.content.trains.entity.CarriageContraptionEntityRenderer.CarriageContraptionState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class CarriageContraptionEntityRenderer extends OrientedContraptionEntityRenderer<CarriageContraptionEntity, CarriageContraptionState> {
    public CarriageContraptionEntityRenderer(Context context) {
        super(context);
    }

    @Override
    public CarriageContraptionState createRenderState() {
        return new CarriageContraptionState();
    }

    @Override
    public boolean shouldRender(
        CarriageContraptionEntity entity,
        Frustum clippingHelper,
        double cameraX,
        double cameraY,
        double cameraZ,
        float partialTicks
    ) {
        Carriage carriage = entity.getCarriage();
        if (carriage != null) {
            for (CarriageBogey bogey : carriage.bogeys) {
                if (bogey != null) {
                    bogey.couplingAnchors.replace(v -> null);
                }
            }
        }
        if (!entity.validForRender || entity.firstPositionUpdate) {
            return false;
        }
        return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ, partialTicks);
    }

    @Override
    public void extractRenderState(
        CarriageContraptionEntity entity,
        CarriageContraptionState state,
        float tickProgress
    ) {
        super.extractRenderState(entity, state, tickProgress);
        Carriage carriage = entity.getCarriage();
        if (carriage == null) {
            return;
        }
        Level level = entity.level();
        Couple<@Nullable CarriageBogey> bogeys = carriage.bogeys;
        CarriageBogey first = bogeys.getFirst();
        CarriageBogey second = bogeys.getSecond();
        Vec3 position = entity.getPosition(tickProgress);
        float viewYRot = entity.getViewYRot(tickProgress);
        float viewXRot = entity.getViewXRot(tickProgress);
        int bogeySpacing = carriage.bogeySpacing;
        float firstYaw = first.yaw.getValue(tickProgress);
        float firstPitch = first.pitch.getValue(tickProgress);
        float secondYaw = 0;
        float secondPitch = 0;
        first.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, firstYaw, firstPitch, true);
        if (second == null) {
            first.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, firstYaw, firstPitch, false);
        } else {
            secondYaw = second.yaw.getValue(tickProgress);
            secondPitch = second.pitch.getValue(tickProgress);
            second.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, secondYaw, secondPitch, false);
        }
        if (VisualizationManager.supportsVisualization(level)) {
            return;
        }
        int cameraLight = -1;
        Contraption contraption = entity.getContraption();
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        if (!contraption.isHiddenInPortal(BlockPos.ZERO)) {
            Vec3 pos = first.getAnchorPosition();
            int light;
            if (pos != null) {
                light = getBogeyLightCoords(level, pos);
            } else {
                light = cameraLight = getBogeyLightCoords(level, entity.getLightProbePosition(tickProgress));
            }
            state.firstBogey = CarriageBogeyRenderState.create(
                first,
                viewXRot,
                viewYRot,
                bogeySpacing,
                firstYaw,
                firstPitch,
                light,
                cardinalLighting,
                tickProgress
            );
        }
        if (second != null) {
            BlockPos bogeyPos = BlockPos.ZERO.relative(
                entity.getInitialOrientation().getCounterClockWise(),
                bogeySpacing
            );
            if (!contraption.isHiddenInPortal(bogeyPos)) {
                Vec3 pos = second.getAnchorPosition();
                int light;
                if (pos != null) {
                    light = getBogeyLightCoords(level, pos);
                } else if (cameraLight == -1) {
                    light = getBogeyLightCoords(level, entity.getLightProbePosition(tickProgress));
                } else {
                    light = cameraLight;
                }
                state.secondBogey = CarriageBogeyRenderState.create(
                    second,
                    viewXRot,
                    viewYRot,
                    bogeySpacing,
                    secondYaw,
                    secondPitch,
                    light,
                    cardinalLighting,
                    tickProgress
                );
            }
        }
    }

    @Override
    protected ClientContraption createClientContraption(Contraption contraption) {
        return new CarriageClientContraption((CarriageContraption) contraption);
    }

    @Override
    public void submit(
        CarriageContraptionState state,
        PoseStack ms,
        SubmitNodeCollector queue,
        CameraRenderState cameraRenderState
    ) {
        super.submit(state, ms, queue, cameraRenderState);
        if (state.firstBogey != null) {
            state.firstBogey.submit(ms, queue);
        }
        if (state.secondBogey != null) {
            state.secondBogey.submit(ms, queue);
        }
    }

    public static void translateBogey(
        PoseStack ms,
        CarriageBogey bogey,
        int bogeySpacing,
        float viewYRot,
        float viewXRot,
        float yaw,
        float pitch
    ) {
        boolean selfUpsideDown = bogey.isUpsideDown();
        boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
        Pose pose = ms.last();
        if (!bogey.isLeading) {
            float viewYRotRadians = Mth.DEG_TO_RAD * (viewYRot + 90);
            float viewXRotRadians = Mth.DEG_TO_RAD * viewXRot;
            pose.rotate(Axis.YP, viewYRotRadians);
            pose.rotate(Axis.XP, -viewXRotRadians);
            pose.rotate(Axis.YP, Radians.DEG_180);
            pose.translate(0, 0, -bogeySpacing);
            pose.rotate(Axis.YP, Radians.DEG_180);
            pose.rotate(Axis.XP, viewXRotRadians);
            pose.rotate(Axis.YP, -viewYRotRadians);
        }
        pose.rotate(Axis.YP, Mth.DEG_TO_RAD * yaw);
        pose.rotate(Axis.XP, Mth.DEG_TO_RAD * pitch);
        pose.translate(0, 0.5f, 0);
        if (selfUpsideDown) {
            pose.rotate(Axis.ZP, Radians.DEG_180);
        }
        if (selfUpsideDown != leadingUpsideDown) {
            pose.translate(0, 2, 0);
        }
    }

    public static int getBogeyLightCoords(Level world, Vec3 pos) {
        BlockPos lightPos = BlockPos.containing(pos);
        return LightCoordsUtil.pack(
            world.getBrightness(LightLayer.BLOCK, lightPos),
            world.getBrightness(LightLayer.SKY, lightPos)
        );
    }

    public static class CarriageContraptionState extends AbstractContraptionState {
        public @Nullable CarriageBogeyRenderState firstBogey;
        public @Nullable CarriageBogeyRenderState secondBogey;
    }

    public static class CarriageBogeyRenderState {
        public BogeyRenderState data;
        public float viewYRadians;
        public float viewXRadians;
        public int offsetZ;
        public float yawRadians;
        public float pitchRadians;
        public boolean zRot;
        public int offsetY;

        public void submit(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            Pose pose = matrices.last();
            if (offsetZ != 0) {
                pose.rotate(Axis.YP, viewYRadians);
                pose.rotate(Axis.XP, -viewXRadians);
                pose.rotate(Axis.YP, Radians.DEG_180);
                pose.translate(0, 0, offsetZ);
                pose.rotate(Axis.YP, Radians.DEG_180);
                pose.rotate(Axis.XP, viewXRadians);
                pose.rotate(Axis.YP, -viewYRadians);
            }
            pose.rotate(Axis.YP, yawRadians);
            pose.rotate(Axis.XP, pitchRadians);
            pose.translate(0, 0.5f, 0);
            if (zRot) {
                pose.rotate(Axis.ZP, Radians.DEG_180);
            }
            pose.translate(0, offsetY, 0);
            data.submit(matrices, queue);
            matrices.popPose();
        }

        @Nullable
        public static CarriageBogeyRenderState create(
            CarriageBogey bogey,
            float viewXRot,
            float viewYRot,
            int bogeySpacing,
            float yaw,
            float pitch,
            int light,
            @Nullable CardinalLighting cardinalLighting,
            float tickProgress
        ) {
            float wheelAngle = bogey.wheelAngle.getValue(tickProgress);
            BogeyRenderState data = AllBogeyStyleRenders.getRenderData(
                bogey.getStyle(),
                bogey.getSize(),
                tickProgress,
                light,
                cardinalLighting,
                wheelAngle,
                bogey.bogeyData,
                true
            );
            if (data == null) {
                return null;
            }
            CarriageBogeyRenderState state = new CarriageBogeyRenderState();
            state.data = data;
            if (!bogey.isLeading) {
                state.viewYRadians = Mth.DEG_TO_RAD * (viewYRot + 90);
                state.viewXRadians = Mth.DEG_TO_RAD * viewXRot;
                state.offsetZ = -bogeySpacing;
            }
            state.yawRadians = Mth.DEG_TO_RAD * yaw;
            state.pitchRadians = Mth.DEG_TO_RAD * pitch;
            boolean selfUpsideDown = bogey.isUpsideDown();
            state.zRot = selfUpsideDown;
            boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
            if (selfUpsideDown != leadingUpsideDown) {
                state.offsetY = 2;
            }
            return state;
        }
    }
}
