package com.zurrtum.create.client.content.trains.entity;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.client.content.contraptions.render.OrientedContraptionEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageBogey;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

import java.util.Objects;
import java.util.function.Supplier;

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
        if (carriage != null)
            for (CarriageBogey bogey : carriage.bogeys)
                if (bogey != null)
                    bogey.couplingAnchors.replace(v -> null);
        return super.shouldRender(entity, clippingHelper, cameraX, cameraY, cameraZ);
    }

    @Override
    public void updateRenderState(CarriageContraptionEntity entity, CarriageContraptionState state, float tickProgress) {
        super.updateRenderState(entity, state, tickProgress);
        state.pass = !entity.validForRender || entity.firstPositionUpdate;
        if (state.pass) {
            return;
        }
        Carriage carriage = entity.getCarriage();
        if (carriage == null) {
            state.bogeys = null;
            return;
        }
        state.bogeySpacing = carriage.bogeySpacing;
        state.position = entity.getLerpedPos(tickProgress);
        state.viewYRot = entity.getViewYRot(tickProgress);
        state.viewXRot = entity.getViewXRot(tickProgress);
        state.bogeys = carriage.bogeys;
        state.leadingPos = BlockPos.ORIGIN.offset(entity.getInitialOrientation().rotateYCounterclockwise(), carriage.bogeySpacing);
        state.cameraPos = entity.getClientCameraPosVec(tickProgress);
        state.yaw.replaceWithParams((f, bogeys) -> bogeys == null ? null : bogeys.yaw.getValue(tickProgress), state.bogeys);
        state.pitch.replaceWithParams((f, bogeys) -> bogeys == null ? null : bogeys.pitch.getValue(tickProgress), state.bogeys);
        state.tickProgress = tickProgress;
    }

    @Override
    public void render(CarriageContraptionState state, MatrixStack ms, VertexConsumerProvider buffers, int overlay) {
        if (state.pass)
            return;

        super.render(state, ms, buffers, overlay);

        if (state.bogeys == null)
            return;

        state.bogeys.forEachWithContext((bogey, first) -> {
            if (bogey == null)
                return;

            BlockPos bogeyPos = bogey.isLeading ? BlockPos.ORIGIN : state.leadingPos;

            float yaw = state.yaw.get(first);
            float pitch = state.pitch.get(first);
            if (!VisualizationManager.supportsVisualization(state.world) && !state.contraption.isHiddenInPortal(bogeyPos)) {

                ms.push();
                translateBogey(ms, bogey, state.bogeySpacing, state.viewYRot, state.viewXRot, yaw, pitch);

                int light = getBogeyLightCoords(state.world, bogey, () -> state.cameraPos);

                AllBogeyStyleRenders.render(
                    bogey.getStyle(),
                    bogey.getSize(),
                    state.tickProgress,
                    ms,
                    buffers,
                    light,
                    overlay,
                    bogey.wheelAngle.getValue(state.tickProgress),
                    bogey.bogeyData,
                    true
                );

                ms.pop();
            }

            bogey.updateCouplingAnchor(state.position, state.viewXRot, state.viewYRot, state.bogeySpacing, yaw, pitch, bogey.isLeading);
            if (state.bogeys.getSecond() == null) {
                bogey.updateCouplingAnchor(state.position, state.viewXRot, state.viewYRot, state.bogeySpacing, yaw, pitch, !bogey.isLeading);
            }
        });
    }

    public static void translateBogey(MatrixStack ms, CarriageBogey bogey, int bogeySpacing, float viewYRot, float viewXRot, float yaw, float pitch) {
        boolean selfUpsideDown = bogey.isUpsideDown();
        boolean leadingUpsideDown = bogey.carriage.leadingBogey().isUpsideDown();
        TransformStack.of(ms).rotateYDegrees(viewYRot + 90).rotateXDegrees(-viewXRot).rotateYDegrees(180)
            .translate(0, 0, bogey.isLeading ? 0 : -bogeySpacing).rotateYDegrees(-180).rotateXDegrees(viewXRot).rotateYDegrees(-viewYRot - 90)
            .rotateYDegrees(yaw).rotateXDegrees(pitch).translate(0, .5f, 0).rotateZDegrees(selfUpsideDown ? 180 : 0)
            .translateY(selfUpsideDown != leadingUpsideDown ? 2 : 0);
    }

    public static int getBogeyLightCoords(World world, CarriageBogey bogey, Supplier<Vec3d> cameraPos) {
        var lightPos = BlockPos.ofFloored(Objects.requireNonNullElseGet(bogey.getAnchorPosition(), cameraPos));
        return LightmapTextureManager.pack(world.getLightLevel(LightType.BLOCK, lightPos), world.getLightLevel(LightType.SKY, lightPos));
    }

    public static class CarriageContraptionState extends OrientedContraptionState {
        boolean pass;
        Couple<CarriageBogey> bogeys;
        Couple<Float> yaw = Couple.create(null, null);
        Couple<Float> pitch = Couple.create(null, null);
        BlockPos leadingPos;
        Vec3d cameraPos;
        Vec3d position;
        float viewYRot;
        float viewXRot;
        int bogeySpacing;
        float tickProgress;
    }
}
