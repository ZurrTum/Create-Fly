package com.zurrtum.create.client.content.decoration.steamWhistle;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.AnimationBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.animation.WhistleAnimationBehaviour;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlock.WhistleSize;
import com.zurrtum.create.content.decoration.steamWhistle.WhistleBlockEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
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

public class WhistleRenderer implements BlockEntityRenderer<WhistleBlockEntity, WhistleRenderer.WhistleRenderState> {
    public WhistleRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public WhistleRenderState createRenderState() {
        return new WhistleRenderState();
    }

    @Override
    public void updateRenderState(
        WhistleBlockEntity be,
        WhistleRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        state.blockState = be.getCachedState();
        if (!(state.blockState.getBlock() instanceof WhistleBlock)) {
            return;
        }
        state.pos = be.getPos();
        state.type = be.getType();
        World world = be.getWorld();
        state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(
            world,
            state.pos
        ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        state.crumblingOverlay = crumblingOverlay;
        state.layer = RenderLayer.getSolid();
        Direction direction = state.blockState.get(WhistleBlock.FACING);
        WhistleSize size = state.blockState.get(WhistleBlock.SIZE);
        PartialModel mouth = size == WhistleSize.LARGE ? AllPartialModels.WHISTLE_MOUTH_LARGE : size == WhistleSize.MEDIUM ? AllPartialModels.WHISTLE_MOUTH_MEDIUM : AllPartialModels.WHISTLE_MOUTH_SMALL;
        WhistleAnimationBehaviour behaviour = (WhistleAnimationBehaviour) be.getBehaviour(AnimationBehaviour.TYPE);
        float offset = behaviour.animation.getValue(tickProgress);
        if (behaviour.animation.getChaseTarget() > 0 && behaviour.animation.getValue() > 0.5f) {
            float wiggleProgress = (AnimationTickHolder.getTicks(world) + tickProgress) / 8f;
            offset = (float) (offset - Math.sin(wiggleProgress * (2 * MathHelper.PI) * (4 - size.ordinal())) / 16f);
        }
        state.model = CachedBuffers.partial(mouth, state.blockState);
        state.yRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(direction);
        state.offset = offset * 4 / 16f;
    }

    @Override
    public void render(WhistleRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
    }

    public static class WhistleRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public float yRot;
        public float offset;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.center().rotateY(yRot).uncenter().translate(0, offset, 0).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
