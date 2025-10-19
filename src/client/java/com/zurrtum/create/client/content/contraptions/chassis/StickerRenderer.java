package com.zurrtum.create.client.content.contraptions.chassis;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.content.contraptions.chassis.StickerBlock;
import com.zurrtum.create.content.contraptions.chassis.StickerBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class StickerRenderer implements BlockEntityRenderer<StickerBlockEntity, StickerRenderer.StickerRenderState> {
    public StickerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public StickerRenderState createRenderState() {
        return new StickerRenderState();
    }

    @Override
    public void updateRenderState(
        StickerBlockEntity be,
        StickerRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        World world = be.getWorld();
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        state.layer = RenderLayer.getSolid();
        state.head = CachedBuffers.partial(AllPartialModels.STICKER_HEAD, state.blockState);
        state.seed = be.hashCode();
        Direction facing = state.blockState.get(StickerBlock.FACING);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
        state.xRot = MathHelper.RADIANS_PER_DEGREE * (AngleHelper.verticalAngle(facing) + 90);
        float offset;
        if (!be.isVirtual() && world != MinecraftClient.getInstance().world) {
            offset = state.blockState.get(StickerBlock.EXTENDED) ? 1 : 0;
        } else {
            offset = be.piston.getValue(AnimationTickHolder.getPartialTicks(world));
        }
        state.offset = (offset * offset) * 4 / 16f;
    }

    @Override
    public void render(StickerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    public static class StickerRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer head;
        public int seed;
        public float yRot;
        public float xRot;
        public float offset;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            head.nudge(seed).center().rotateY(yRot).rotateX(xRot).uncenter().translate(0, offset, 0);
            head.light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
