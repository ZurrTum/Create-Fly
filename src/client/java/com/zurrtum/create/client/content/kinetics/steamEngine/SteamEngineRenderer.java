package com.zurrtum.create.client.content.kinetics.steamEngine;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

public class SteamEngineRenderer extends SafeBlockEntityRenderer<SteamEngineBlockEntity> {

    public SteamEngineRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(SteamEngineBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        if (VisualizationManager.supportsVisualization(be.getWorld()))
            return;

        Float angle = getTargetAngle(be);
        if (angle == null)
            return;

        BlockState blockState = be.getCachedState();
        Direction facing = SteamEngineBlock.getFacing(blockState);
        Axis facingAxis = facing.getAxis();
        Axis axis = Axis.Y;

        PoweredShaftBlockEntity shaft = be.getShaft();
        if (shaft != null)
            axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);

        boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
        float piston = ((6 / 16f) * MathHelper.sin(angle) - MathHelper.sqrt(MathHelper.square(14 / 16f) - MathHelper.square(6 / 16f) * MathHelper.square(
            MathHelper.cos(angle))));
        float distance = MathHelper.sqrt(MathHelper.square(piston - 6 / 16f * MathHelper.sin(angle)));
        float angle2 = (float) Math.acos(distance / (14 / 16f)) * (MathHelper.cos(angle) >= 0 ? 1f : -1f);

        VertexConsumer vb = buffer.getBuffer(RenderLayer.getSolid());

        transformed(AllPartialModels.ENGINE_PISTON, blockState, facing, roll90).translate(0, piston + 20 / 16f, 0).light(light).renderInto(ms, vb);

        transformed(AllPartialModels.ENGINE_LINKAGE, blockState, facing, roll90).center().translate(0, 1, 0).uncenter()
            .translate(0, piston + 20 / 16f, 0).translate(0, 4 / 16f, 8 / 16f).rotateX(angle2).translate(0, -4 / 16f, -8 / 16f).light(light)
            .renderInto(ms, vb);

        transformed(AllPartialModels.ENGINE_CONNECTOR, blockState, facing, roll90).translate(0, 2, 0).center().rotateX(-(angle + MathHelper.HALF_PI))
            .uncenter().light(light).renderInto(ms, vb);
    }

    private SuperByteBuffer transformed(PartialModel model, BlockState blockState, Direction facing, boolean roll90) {
        return CachedBuffers.partial(model, blockState).center().rotateYDegrees(AngleHelper.horizontalAngle(facing))
            .rotateXDegrees(AngleHelper.verticalAngle(facing) + 90).rotateYDegrees(roll90 ? -90 : 0).uncenter();
    }

    @Override
    public int getRenderDistance() {
        return 128;
    }

    @Nullable
    public static Float getTargetAngle(SteamEngineBlockEntity be) {
        BlockState blockState = be.getCachedState();
        if (!blockState.isOf(AllBlocks.STEAM_ENGINE))
            return null;

        Direction facing = SteamEngineBlock.getFacing(blockState);
        PoweredShaftBlockEntity shaft = be.getShaft();
        Axis facingAxis = facing.getAxis();

        if (shaft == null)
            return null;

        Axis axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        float angle = KineticBlockEntityRenderer.getAngleForBe(shaft, shaft.getPos(), axis);

        if (axis == facingAxis)
            return null;
        if (axis.isHorizontal() && (facingAxis == Axis.X ^ facing.getDirection() == Direction.AxisDirection.POSITIVE))
            angle *= -1;
        if (axis == Axis.X && facing == Direction.DOWN)
            angle *= -1;
        return angle;
    }
}
