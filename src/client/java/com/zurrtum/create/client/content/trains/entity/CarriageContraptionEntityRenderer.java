package com.zurrtum.create.client.content.trains.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.client.content.contraptions.render.OrientedContraptionEntityRenderer;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class CarriageContraptionEntityRenderer extends OrientedContraptionEntityRenderer<CarriageContraptionEntity, CarriageContraptionEntityRenderer.CarriageContraptionState> {
    public CarriageContraptionEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public CarriageContraptionState createRenderState() {
        return new CarriageContraptionState();
    }

    @Override
    public boolean shouldRender(CarriageContraptionEntity entity, Frustum clippingHelper, double cameraX, double cameraY, double cameraZ) {
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
        return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
    }

    @Override
    public void extractRenderState(CarriageContraptionEntity entity, CarriageContraptionState state, float tickProgress) {
        super.extractRenderState(entity, state, tickProgress);
        Carriage carriage = entity.getCarriage();
        if (carriage == null) {
            return;
        }
        Level level = entity.level();
        if (VisualizationManager.supportsVisualization(level)) {
            return;
        }
        Couple<CarriageBogey> bogeys = carriage.bogeys;
        CarriageBogey first = bogeys.getFirst();
        CarriageBogey second = bogeys.getSecond();
        Vec3 position = entity.getPosition(tickProgress);
        float viewYRot = entity.getViewYRot(tickProgress);
        float viewXRot = entity.getViewXRot(tickProgress);
        int bogeySpacing = carriage.bogeySpacing;
        int cameraLight = -1;
        float firstYaw = first.yaw.getValue(tickProgress);
        float firstPitch = first.pitch.getValue(tickProgress);
        if (!state.contraption.isHiddenInPortal(BlockPos.ZERO)) {
            Vec3 pos = first.getAnchorPosition();
            int light;
            if (pos != null) {
                light = getBogeyLightCoords(level, pos);
            } else {
                light = cameraLight = getBogeyLightCoords(level, entity.getLightProbePosition(tickProgress));
            }
            state.firstBogey = CarriageBogeyRenderState.create(first, viewXRot, viewYRot, bogeySpacing, firstYaw, firstPitch, light, tickProgress);
        }
        first.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, firstYaw, firstPitch, true);
        if (second == null) {
            first.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, firstYaw, firstPitch, false);
        } else {
            BlockPos bogeyPos = BlockPos.ZERO.relative(entity.getInitialOrientation().getCounterClockWise(), bogeySpacing);
            float secondYaw = second.yaw.getValue(tickProgress);
            float secondPitch = second.pitch.getValue(tickProgress);
            if (!state.contraption.isHiddenInPortal(bogeyPos)) {
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
                    tickProgress
                );
            }
            second.updateCouplingAnchor(position, viewXRot, viewYRot, bogeySpacing, secondYaw, secondPitch, false);
        }
    }

    @Override
    protected ClientContraption createClientContraption(Contraption contraption) {
        return new CarriageClientContraption((CarriageContraption) contraption);
    }

    @Override
    public void submit(CarriageContraptionState state, PoseStack ms, SubmitNodeCollector queue, CameraRenderState cameraRenderState) {
        super.submit(state, ms, queue, cameraRenderState);
        if (state.firstBogey != null) {
            state.firstBogey.render(ms, queue);
        }
        if (state.secondBogey != null) {
            state.secondBogey.render(ms, queue);
        }
    }

    public static void translateBogey(PoseStack ms, CarriageBogey bogey, int bogeySpacing, float viewYRot, float viewXRot, float yaw, float pitch) {
        boolean selfUpsideDown = bogey.isUpsideDown();
        boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
        TransformStack.of(ms).rotateYDegrees(viewYRot + 90).rotateXDegrees(-viewXRot).rotateYDegrees(180)
            .translate(0, 0, bogey.isLeading ? 0 : -bogeySpacing).rotateYDegrees(-180).rotateXDegrees(viewXRot).rotateYDegrees(-viewYRot - 90)
            .rotateYDegrees(yaw).rotateXDegrees(pitch).translate(0, .5f, 0).rotateZDegrees(selfUpsideDown ? 180 : 0)
            .translateY(selfUpsideDown != leadingUpsideDown ? 2 : 0);
    }

    public static int getBogeyLightCoords(Level world, Vec3 pos) {
        BlockPos lightPos = BlockPos.containing(pos);
        return LightTexture.pack(world.getBrightness(LightLayer.BLOCK, lightPos), world.getBrightness(LightLayer.SKY, lightPos));
    }

    public static class CarriageContraptionState extends OrientedContraptionState {
        public CarriageBogeyRenderState firstBogey;
        public CarriageBogeyRenderState secondBogey;
    }

    public static class CarriageBogeyRenderState {
        public BogeyRenderState data;
        public float viewYRot;
        public float viewXRot;
        public float yRot;
        public int offsetZ;
        public float yaw;
        public float pitch;
        public float zRot;
        public int offsetY;

        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            matrices.pushPose();
            if (offsetZ != 0) {
                matrices.mulPose(Axis.YP.rotation(viewYRot));
                matrices.mulPose(Axis.XP.rotation(-viewXRot));
                matrices.mulPose(Axis.YP.rotation(yRot));
                matrices.translate(0, 0, offsetZ);
                matrices.mulPose(Axis.YP.rotation(-yRot));
                matrices.mulPose(Axis.XP.rotation(viewXRot));
                matrices.mulPose(Axis.YP.rotation(-viewYRot));
            }
            matrices.mulPose(Axis.YP.rotation(yaw));
            matrices.mulPose(Axis.XP.rotation(pitch));
            matrices.translate(0, 0.5f, 0);
            if (zRot != 0) {
                matrices.mulPose(Axis.ZP.rotation(zRot));
            }
            matrices.translate(0, offsetY, 0);
            data.render(matrices, queue);
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
            float tickProgress
        ) {
            float wheelAngle = bogey.wheelAngle.getValue(tickProgress);
            BogeyRenderState data = AllBogeyStyleRenders.getRenderData(
                bogey.getStyle(),
                bogey.getSize(),
                tickProgress,
                light,
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
                state.viewYRot = Mth.DEG_TO_RAD * (viewYRot + 90);
                state.viewXRot = Mth.DEG_TO_RAD * viewXRot;
                state.yRot = Mth.DEG_TO_RAD * 180;
                state.offsetZ = -bogeySpacing;
            }
            state.yaw = Mth.DEG_TO_RAD * yaw;
            state.pitch = Mth.DEG_TO_RAD * pitch;
            boolean selfUpsideDown = bogey.isUpsideDown();
            if (selfUpsideDown) {
                state.zRot = Mth.DEG_TO_RAD * 180;
            }
            boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
            if (selfUpsideDown != leadingUpsideDown) {
                state.offsetY = 2;
            }
            return state;
        }
    }
}
