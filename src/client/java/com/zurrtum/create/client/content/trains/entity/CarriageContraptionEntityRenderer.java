package com.zurrtum.create.client.content.trains.entity;

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
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class CarriageContraptionEntityRenderer extends OrientedContraptionEntityRenderer<CarriageContraptionEntity, CarriageContraptionEntityRenderer.CarriageContraptionState> {
    public CarriageContraptionEntityRenderer(EntityRendererFactory.Context context) {
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
    public void updateRenderState(CarriageContraptionEntity entity, CarriageContraptionState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        Carriage carriage = entity.getCarriage();
        if (carriage == null) {
            return;
        }
        World world = entity.getEntityWorld();
        Couple<CarriageBogey> bogeys = carriage.bogeys;
        CarriageBogey first = bogeys.getFirst();
        CarriageBogey second = bogeys.getSecond();
        Vec3d position = entity.getLerpedPos(tickProgress);
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
        if (VisualizationManager.supportsVisualization(world)) {
            return;
        }
        int cameraLight = -1;
        if (!state.contraption.isHiddenInPortal(BlockPos.ORIGIN)) {
            Vec3d pos = first.getAnchorPosition();
            int light;
            if (pos != null) {
                light = getBogeyLightCoords(world, pos);
            } else {
                light = cameraLight = getBogeyLightCoords(world, entity.getClientCameraPosVec(tickProgress));
            }
            state.firstBogey = CarriageBogeyRenderState.create(first, viewXRot, viewYRot, bogeySpacing, firstYaw, firstPitch, light, tickProgress);
        }
        if (second != null) {
            BlockPos bogeyPos = BlockPos.ORIGIN.offset(entity.getInitialOrientation().rotateYCounterclockwise(), bogeySpacing);
            if (!state.contraption.isHiddenInPortal(bogeyPos)) {
                Vec3d pos = second.getAnchorPosition();
                int light;
                if (pos != null) {
                    light = getBogeyLightCoords(world, pos);
                } else if (cameraLight == -1) {
                    light = getBogeyLightCoords(world, entity.getClientCameraPosVec(tickProgress));
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
        }
    }

    @Override
    protected ClientContraption createClientContraption(Contraption contraption) {
        return new CarriageClientContraption((CarriageContraption) contraption);
    }

    @Override
    public void render(CarriageContraptionState state, MatrixStack ms, OrderedRenderCommandQueue queue, CameraRenderState cameraRenderState) {
        super.render(state, ms, queue, cameraRenderState);
        if (state.firstBogey != null) {
            state.firstBogey.render(ms, queue);
        }
        if (state.secondBogey != null) {
            state.secondBogey.render(ms, queue);
        }
    }

    public static void translateBogey(MatrixStack ms, CarriageBogey bogey, int bogeySpacing, float viewYRot, float viewXRot, float yaw, float pitch) {
        boolean selfUpsideDown = bogey.isUpsideDown();
        boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
        TransformStack.of(ms).rotateYDegrees(viewYRot + 90).rotateXDegrees(-viewXRot).rotateYDegrees(180)
            .translate(0, 0, bogey.isLeading ? 0 : -bogeySpacing).rotateYDegrees(-180).rotateXDegrees(viewXRot).rotateYDegrees(-viewYRot - 90)
            .rotateYDegrees(yaw).rotateXDegrees(pitch).translate(0, .5f, 0).rotateZDegrees(selfUpsideDown ? 180 : 0)
            .translateY(selfUpsideDown != leadingUpsideDown ? 2 : 0);
    }

    public static int getBogeyLightCoords(World world, Vec3d pos) {
        BlockPos lightPos = BlockPos.ofFloored(pos);
        return LightmapTextureManager.pack(world.getLightLevel(LightType.BLOCK, lightPos), world.getLightLevel(LightType.SKY, lightPos));
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

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            matrices.push();
            if (offsetZ != 0) {
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(viewYRot));
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(-viewXRot));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yRot));
                matrices.translate(0, 0, offsetZ);
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-yRot));
                matrices.multiply(RotationAxis.POSITIVE_X.rotation(viewXRot));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-viewYRot));
            }
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(yaw));
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(pitch));
            matrices.translate(0, 0.5f, 0);
            if (zRot != 0) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotation(zRot));
            }
            matrices.translate(0, offsetY, 0);
            data.render(matrices, queue);
            matrices.pop();
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
                state.viewYRot = MathHelper.RADIANS_PER_DEGREE * (viewYRot + 90);
                state.viewXRot = MathHelper.RADIANS_PER_DEGREE * viewXRot;
                state.yRot = MathHelper.RADIANS_PER_DEGREE * 180;
                state.offsetZ = -bogeySpacing;
            }
            state.yaw = MathHelper.RADIANS_PER_DEGREE * yaw;
            state.pitch = MathHelper.RADIANS_PER_DEGREE * pitch;
            boolean selfUpsideDown = bogey.isUpsideDown();
            if (selfUpsideDown) {
                state.zRot = MathHelper.RADIANS_PER_DEGREE * 180;
            }
            boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
            if (selfUpsideDown != leadingUpsideDown) {
                state.offsetY = 2;
            }
            return state;
        }
    }
}
