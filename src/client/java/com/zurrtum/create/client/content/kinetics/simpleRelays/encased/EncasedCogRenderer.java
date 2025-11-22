package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class EncasedCogRenderer extends KineticBlockEntityRenderer<SimpleKineticBlockEntity, EncasedCogRenderer.EncasedCogRenderState> {
    private final boolean large;

    public static EncasedCogRenderer small(BlockEntityRendererFactory.Context context) {
        return new EncasedCogRenderer(context, false);
    }

    public static EncasedCogRenderer large(BlockEntityRendererFactory.Context context) {
        return new EncasedCogRenderer(context, true);
    }

    public EncasedCogRenderer(BlockEntityRendererFactory.Context context, boolean large) {
        super(context);
        this.large = large;
    }

    @Override
    public EncasedCogRenderState createRenderState() {
        return new EncasedCogRenderState();
    }

    @Override
    public void updateRenderState(
        SimpleKineticBlockEntity be,
        EncasedCogRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.shaftAngle = large ? BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft(be, state.axis) : state.angle;
        if (state.blockState.get(EncasedCogwheelBlock.TOP_SHAFT, false)) {
            state.top = CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF, state.blockState, switch (state.axis) {
                    case Y -> Direction.UP;
                    case Z -> Direction.SOUTH;
                    case X -> Direction.EAST;
                }
            );
        }
        if (state.blockState.get(EncasedCogwheelBlock.BOTTOM_SHAFT, false)) {
            state.bottom = CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF, state.blockState, switch (state.axis) {
                    case Y -> Direction.DOWN;
                    case Z -> Direction.NORTH;
                    case X -> Direction.WEST;
                }
            );
        }
    }

    @Override
    protected RenderLayer getRenderType(SimpleKineticBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    protected SuperByteBuffer getRotatedModel(SimpleKineticBlockEntity be, EncasedCogRenderState state) {
        return CachedBuffers.partialFacingVertical(
            large ? AllPartialModels.SHAFTLESS_LARGE_COGWHEEL : AllPartialModels.SHAFTLESS_COGWHEEL,
            state.blockState,
            state.direction
        );
    }

    public static class EncasedCogRenderState extends KineticRenderState {
        public float shaftAngle;
        public SuperByteBuffer top;
        public SuperByteBuffer bottom;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            if (top != null) {
                top.light(lightmapCoordinates);
                top.rotateCentered(shaftAngle, direction);
                top.color(color);
                top.renderInto(matricesEntry, vertexConsumer);
            }
            if (bottom != null) {
                bottom.light(lightmapCoordinates);
                bottom.rotateCentered(shaftAngle, direction);
                bottom.color(color);
                bottom.renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
