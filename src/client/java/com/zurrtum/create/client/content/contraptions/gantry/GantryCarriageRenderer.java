package com.zurrtum.create.client.content.contraptions.gantry;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlock;
import com.zurrtum.create.content.contraptions.gantry.GantryCarriageBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class GantryCarriageRenderer extends KineticBlockEntityRenderer<GantryCarriageBlockEntity, GantryCarriageRenderer.GantryCarriageRenderState> {
    public GantryCarriageRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public GantryCarriageRenderState createRenderState() {
        return new GantryCarriageRenderState();
    }

    @Override
    public void extractRenderState(
        GantryCarriageBlockEntity be,
        GantryCarriageRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        if (state.support) {
            return;
        }
        BlockState blockState = be.getBlockState();
        Direction facing = blockState.getValue(GantryCarriageBlock.FACING);
        Boolean alongFirst = blockState.getValue(GantryCarriageBlock.AXIS_ALONG_FIRST_COORDINATE);
        Axis rotationAxis = state.axis;
        BlockPos visualPos = facing.getAxisDirection() == AxisDirection.POSITIVE ? be.getBlockPos() : be.getBlockPos().relative(facing.getOpposite());
        float angleForBE = getAngleForBE(be, visualPos, rotationAxis);
        Axis gantryAxis = Axis.X;
        for (Axis axis : Iterate.axes) {
            if (axis != rotationAxis && axis != facing.getAxis()) {
                gantryAxis = axis;
            }
        }
        if (gantryAxis == Axis.X && facing == Direction.UP) {
            angleForBE *= -1;
        }
        if (gantryAxis == Axis.Y && (facing == Direction.NORTH || facing == Direction.EAST)) {
            angleForBE *= -1;
        }
        state.cogs = CachedBuffers.partial(AllPartialModels.GANTRY_COGS, blockState);
        state.yRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
        state.xRot = Mth.DEG_TO_RAD * (facing == Direction.UP ? 0 : facing == Direction.DOWN ? 180 : 90);
        state.yRot2 = Mth.DEG_TO_RAD * (alongFirst ^ facing.getAxis() == Axis.X ? 0 : 90);
        state.xRot2 = Mth.DEG_TO_RAD * -angleForBE;
    }

    @Override
    protected RenderType getRenderType(GantryCarriageBlockEntity be, BlockState state) {
        return RenderTypes.solidMovingBlock();
    }

    public static float getAngleForBE(KineticBlockEntity be, final BlockPos pos, Axis axis) {
        float time = AnimationTickHolder.getRenderTime(be.getLevel());
        float offset = getRotationOffsetForPosition(be, pos, axis);
        return (time * be.getSpeed() * 3f / 20 + offset) % 360;
    }

    @Override
    protected BlockState getRenderedBlockState(GantryCarriageBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    public static class GantryCarriageRenderState extends KineticRenderState {
        public SuperByteBuffer cogs;
        public float yRot;
        public float xRot;
        public float yRot2;
        public float xRot2;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            cogs.center().rotateY(yRot).rotateX(xRot).rotateY(yRot2);
            if (xRot2 != 0) {
                cogs.translate(0, -0.5625f, 0).rotateX(xRot2).translate(0, 0.5625f, 0);
            }
            cogs.uncenter().light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
