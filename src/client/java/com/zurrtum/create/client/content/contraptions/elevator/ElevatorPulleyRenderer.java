package com.zurrtum.create.client.content.contraptions.elevator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.pulley.AbstractPulleyRenderer;
import com.zurrtum.create.client.content.contraptions.pulley.PulleyRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlock;
import com.zurrtum.create.content.contraptions.elevator.ElevatorPulleyBlockEntity;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class ElevatorPulleyRenderer extends KineticBlockEntityRenderer<ElevatorPulleyBlockEntity, ElevatorPulleyRenderer.ElevatorPulleyRenderState> {
    public ElevatorPulleyRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ElevatorPulleyRenderState createRenderState() {
        return new ElevatorPulleyRenderState();
    }

    @Override
    public void extractRenderState(
        ElevatorPulleyBlockEntity be,
        ElevatorPulleyRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        Level world = be.getLevel();
        BlockState blockState = be.getBlockState();
        float offset = PulleyRenderer.getBlockEntityOffset(tickProgress, be);
        boolean running = PulleyRenderer.isPulleyRunning(be);
        state.yRot = Mth.DEG_TO_RAD * (180 + AngleHelper.horizontalAngle(blockState.getValue(ElevatorPulleyBlock.HORIZONTAL_FACING)));
        if (running || offset == 0) {
            state.magnet = CachedBuffers.partial(AllPartialModels.ELEVATOR_MAGNET, blockState);
            state.magnetOffset = -offset;
            state.magnetLight = LevelRenderer.getLightColor(world, state.blockPos.below((int) offset));
        }
        state.rotatedCoil = getRotatedCoil(be);
        if (offset == 0) {
            return;
        }
        state.coilShift = AllSpriteShifts.ELEVATOR_COIL;
        state.coilScroll = AbstractPulleyRenderer.getCoilVScroll(state.coilShift, offset, 2);
        float f = offset % 1;
        if (f < .25f || f > .75f) {
            state.halfRope = CachedBuffers.partial(AllPartialModels.ELEVATOR_BELT_HALF, blockState);
            updateHalfShift(state, offset);
            float down = f > .75f ? f - 1 : f;
            state.halfRopeOffset = -down;
            state.halfRopeLight = LevelRenderer.getLightColor(world, state.blockPos.below((int) down));
        }
        if (!running) {
            return;
        }
        if (state.halfRope == null) {
            updateHalfShift(state, offset);
        }
        state.rope = CachedBuffers.partial(AllPartialModels.ELEVATOR_BELT, blockState);
        int size = (int) Math.ceil(offset - .25f);
        float[] offsets = new float[size];
        int[] lights = new int[size];
        for (int i = 0; i < size; i++) {
            float down = offset - i;
            int light = LevelRenderer.getLightColor(world, state.blockPos.below((int) down));
            offsets[i] = -down;
            lights[i] = light;
        }
        state.offsets = offsets;
        state.lights = lights;
    }

    @Override
    public void submit(ElevatorPulleyRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        queue.submitCustomGeometry(matrices, state.layer, state);
    }

    private static void updateHalfShift(ElevatorPulleyRenderState state, float offset) {
        state.halfShift = AllSpriteShifts.ELEVATOR_BELT;
        double beltScroll = (-(offset + .5) - Math.floor(-(offset + .5))) / 2;
        TextureAtlasSprite target = state.halfShift.getTarget();
        float spriteSize = target.getV1() - target.getV0();
        state.halfScroll = (float) beltScroll * spriteSize;
    }

    @Override
    protected RenderType getRenderType(ElevatorPulleyBlockEntity be, BlockState state) {
        return RenderType.solid();
    }

    @Override
    protected BlockState getRenderedBlockState(ElevatorPulleyBlockEntity be) {
        return shaft(getRotationAxisOf(be));
    }

    protected SuperByteBuffer getRotatedCoil(KineticBlockEntity be) {
        BlockState blockState = be.getBlockState();
        return CachedBuffers.partialFacing(AllPartialModels.ELEVATOR_COIL, blockState, blockState.getValue(ElevatorPulleyBlock.HORIZONTAL_FACING));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    public static class ElevatorPulleyRenderState extends KineticRenderState {
        public float yRot;
        public SuperByteBuffer magnet;
        public float magnetOffset;
        public int magnetLight;
        public SuperByteBuffer rotatedCoil;
        public SpriteShiftEntry coilShift;
        public float coilScroll;
        public SuperByteBuffer halfRope;
        public SpriteShiftEntry halfShift;
        public float halfScroll;
        public float halfRopeOffset;
        public int halfRopeLight;
        public SuperByteBuffer rope;
        public float[] offsets;
        public int[] lights;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            if (model != null) {
                super.render(matricesEntry, vertexConsumer);
            }
            if (magnet != null) {
                magnet.center().rotateY(yRot).uncenter().translate(0, magnetOffset, 0).light(magnetLight).renderInto(matricesEntry, vertexConsumer);
            }
            if (coilScroll != 0) {
                rotatedCoil.shiftUVScrolling(coilShift, coilScroll);
            }
            rotatedCoil.light(lightCoords).renderInto(matricesEntry, vertexConsumer);
            if (halfRope != null) {
                halfRope.center().rotateY(yRot).uncenter().translate(0, halfRopeOffset, 0).shiftUVScrolling(halfShift, halfScroll)
                    .light(halfRopeLight).renderInto(matricesEntry, vertexConsumer);
            }
            if (rope != null) {
                for (int i = 0, size = offsets.length; i < size; i++) {
                    rope.center().rotateY(yRot).uncenter().translate(0, offsets[i], 0).shiftUVScrolling(halfShift, halfScroll).light(lights[i])
                        .renderInto(matricesEntry, vertexConsumer);
                }
            }
        }
    }
}
