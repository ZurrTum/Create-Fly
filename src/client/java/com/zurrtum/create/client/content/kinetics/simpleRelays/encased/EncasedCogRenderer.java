package com.zurrtum.create.client.content.kinetics.simpleRelays.encased;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.simpleRelays.BracketedKineticBlockEntityRenderer;
import com.zurrtum.create.content.kinetics.simpleRelays.SimpleKineticBlockEntity;
import com.zurrtum.create.content.kinetics.simpleRelays.encased.EncasedCogwheelBlock;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EncasedCogRenderer extends KineticBlockEntityRenderer<SimpleKineticBlockEntity, EncasedCogRenderer.EncasedCogRenderState> {
    private final boolean large;

    public static EncasedCogRenderer small(BlockEntityRendererProvider.Context context) {
        return new EncasedCogRenderer(context, false);
    }

    public static EncasedCogRenderer large(BlockEntityRendererProvider.Context context) {
        return new EncasedCogRenderer(context, true);
    }

    public EncasedCogRenderer(BlockEntityRendererProvider.Context context, boolean large) {
        super(context);
        this.large = large;
    }

    @Override
    public EncasedCogRenderState createRenderState() {
        return new EncasedCogRenderState();
    }

    @Override
    public void extractRenderState(
        SimpleKineticBlockEntity be,
        EncasedCogRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.shaftAngle = large ? BracketedKineticBlockEntityRenderer.getAngleForLargeCogShaft(be, state.axis) : state.angle;
        if (state.blockState.getValueOrElse(EncasedCogwheelBlock.TOP_SHAFT, false)) {
            state.top = CachedBuffers.partialFacing(
                AllPartialModels.SHAFT_HALF, state.blockState, switch (state.axis) {
                    case Y -> Direction.UP;
                    case Z -> Direction.SOUTH;
                    case X -> Direction.EAST;
                }
            );
        }
        if (state.blockState.getValueOrElse(EncasedCogwheelBlock.BOTTOM_SHAFT, false)) {
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
    protected RenderType getRenderType(SimpleKineticBlockEntity be, BlockState state) {
        return RenderTypes.solidMovingBlock();
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
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            if (top != null) {
                top.light(lightCoords);
                top.rotateCentered(shaftAngle, direction);
                top.color(color);
                top.renderInto(matricesEntry, vertexConsumer);
            }
            if (bottom != null) {
                bottom.light(lightCoords);
                bottom.rotateCentered(shaftAngle, direction);
                bottom.color(color);
                bottom.renderInto(matricesEntry, vertexConsumer);
            }
        }
    }
}
