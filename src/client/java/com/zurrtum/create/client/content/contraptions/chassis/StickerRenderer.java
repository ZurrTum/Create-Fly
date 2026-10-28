package com.zurrtum.create.client.content.contraptions.chassis;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.contraptions.chassis.StickerRenderer.StickerRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class StickerRenderer implements BlockEntityRenderer<StickerBlockEntity, StickerRenderState> {
    public StickerRenderer(Context context) {
    }

    @Override
    public StickerRenderState createRenderState() {
        return new StickerRenderState();
    }

    @Override
    public void extractRenderState(
        StickerBlockEntity be,
        StickerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        state.head = CachedBuffers.partial(AllPartialModels.STICKER_HEAD, state.blockState).cardinalLighting(level)
            .light(state.lightCoords).extractRenderState();
        Direction facing = state.blockState.getValue(StickerBlock.FACING);
        state.yRadians = Radians.horizontal(facing);
        state.xRadians = Radians.verticalPlus90(facing);
        float offset;
        if (!be.isVirtual() && level != Minecraft.getInstance().level) {
            offset = state.blockState.getValue(StickerBlock.EXTENDED) ? 1 : 0;
        } else {
            offset = be.piston.getValue(AnimationTickHolder.getPartialTicks(level));
        }
        state.offset = offset * offset * 0.25f;
        state.id = be.hashCode();
    }

    @Override
    public void submit(
        StickerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        Pose pose = matrices.last();
        SuperByteBuffer.nudge(pose, state.id);
        if (state.yRadians != 0 || state.xRadians != 0) {
            pose.translate(0.5f, 0.5f, 0.5f);
            if (state.yRadians != 0) {
                pose.rotate(Axis.YP, state.yRadians);
            }
            if (state.xRadians != 0) {
                pose.rotate(Axis.XP, state.xRadians);
            }
            pose.translate(-0.5f, -0.5f, -0.5f);
        }
        pose.translate(0, state.offset, 0);
        state.head.submit(matrices, queue);
    }

    public static class StickerRenderState extends BlockEntityRenderState {
        public @UnknownNullability SuperByteBufferRenderState head;
        public int id;
        public float yRadians;
        public float xRadians;
        public float offset;
    }
}
