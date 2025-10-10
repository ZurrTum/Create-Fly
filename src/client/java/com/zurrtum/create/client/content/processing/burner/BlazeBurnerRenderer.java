package com.zurrtum.create.client.content.processing.burner;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ContraptionMatrices;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlazeBurnerRenderer implements BlockEntityRenderer<BlazeBurnerBlockEntity, BlazeBurnerRenderer.BlazeBurnerRenderState> {
    public BlazeBurnerRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public BlazeBurnerRenderState createRenderState() {
        return new BlazeBurnerRenderState();
    }

    @Override
    public void updateRenderState(
        BlazeBurnerBlockEntity be,
        BlazeBurnerRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        HeatLevel heatLevel = be.getHeatLevelForRender();
        if (heatLevel == HeatLevel.NONE)
            return;
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        World level = be.getWorld();
        float animation = be.headAnimation.getValue(tickProgress) * .175f;
        state.horizontalAngle = AngleHelper.rad(be.headAngle.getValue(tickProgress));
        boolean canDrawFlame = heatLevel.isAtLeast(HeatLevel.FADING);
        boolean drawGoggles = be.goggles;
        PartialModel drawHat = be.hat ? AllPartialModels.TRAIN_HAT : be.stockKeeper ? AllPartialModels.LOGISTICS_HAT : null;
        boolean blockAbove = animation > 0.125f;
        float time = AnimationTickHolder.getRenderTime(level);
        float renderTick = time / 16f + (be.hashCode() % 13);
        float offsetMult = heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16;
        float offset = MathHelper.sin(renderTick % MathHelper.TAU) / offsetMult;
        float offset1 = MathHelper.sin((float) ((renderTick + Math.PI) % MathHelper.TAU)) / offsetMult;
        float offset2 = MathHelper.sin((renderTick + MathHelper.HALF_PI) % MathHelper.TAU) / offsetMult;
        state.layer = RenderLayer.getSolid();
        state.headY = offset - (animation * .75f);
        PartialModel blazeModel = getBlazeModel(heatLevel, blockAbove);
        state.blaze = CachedBuffers.partial(blazeModel, state.blockState);
        if (drawGoggles) {
            PartialModel gogglesModel = blazeModel == AllPartialModels.BLAZE_INERT ? AllPartialModels.BLAZE_GOGGLES_SMALL : AllPartialModels.BLAZE_GOGGLES;
            state.goggles = CachedBuffers.partial(gogglesModel, state.blockState);
            state.gogglesHeadY = state.headY + 0.5f;
        }
        if (drawHat != null) {
            state.hat = new HatRenderState();
            boolean scale = blazeModel == AllPartialModels.BLAZE_INERT;
            state.hat.scale = scale;
            state.hat.offset = state.headY + (scale ? 0.5f : 0.75f);
            state.hat.layer = RenderLayer.getCutoutMipped();
            state.hat.model = CachedBuffers.partial(drawHat, state.blockState);
            state.hat.angle = state.horizontalAngle + MathHelper.PI;
        }
        if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            PartialModel rodsModel = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS : AllPartialModels.BLAZE_BURNER_RODS;
            PartialModel rodsModel2 = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2;
            state.rods = CachedBuffers.partial(rodsModel, state.blockState);
            state.rodsY = offset1 + animation + .125f;
            state.rods2 = CachedBuffers.partial(rodsModel2, state.blockState);
            state.rods2Y = offset2 + animation - 3 / 16f;
        }
        if (canDrawFlame && blockAbove) {
            state.flame = new FlameRenderState();
            state.flame.layer = RenderLayer.getCutoutMipped();
            state.flame.model = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, state.blockState);
            state.flame.angle = state.horizontalAngle;
            state.flame.spriteShift = heatLevel == HeatLevel.SEETHING ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;
            Sprite target = state.flame.spriteShift.getTarget();
            float spriteWidth = target.getMaxU() - target.getMinU();
            float spriteHeight = target.getMaxV() - target.getMinV();
            float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();
            double vScroll = speed * time;
            vScroll = vScroll - Math.floor(vScroll);
            vScroll = vScroll * spriteHeight / 2;
            state.flame.vScroll = (float) vScroll;
            double uScroll = speed * time / 2;
            uScroll = uScroll - Math.floor(uScroll);
            uScroll = uScroll * spriteWidth / 2;
            state.flame.uScroll = (float) uScroll;
        }
    }

    @Override
    public void render(BlazeBurnerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        queue.submitCustom(matrices, state.layer, state);
        if (state.hat != null) {
            queue.submitCustom(matrices, state.hat.layer, state.hat);
        }
        if (state.flame != null) {
            queue.submitCustom(matrices, state.flame.layer, state.flame);
        }
    }

    public static void renderInContraption(
        MovementContext context,
        VirtualRenderWorld renderWorld,
        ContraptionMatrices matrices,
        VertexConsumerProvider bufferSource,
        LerpedFloat headAngle,
        boolean conductor
    ) {
        BlockState state = context.state;
        HeatLevel heatLevel = BlazeBurnerBlock.getHeatLevelOf(state);
        if (heatLevel == HeatLevel.NONE)
            return;

        if (!heatLevel.isAtLeast(HeatLevel.FADING))
            heatLevel = HeatLevel.FADING;

        World level = context.world;
        float horizontalAngle = AngleHelper.rad(headAngle.getValue(AnimationTickHolder.getPartialTicks(level)));
        boolean drawGoggles = context.blockEntityData.contains("Goggles");
        boolean drawHat = conductor || context.blockEntityData.contains("TrainHat");
        int hashCode = context.hashCode();

        renderShared(
            matrices.getViewProjection(),
            matrices.getModel(),
            bufferSource,
            level,
            state,
            heatLevel,
            0,
            horizontalAngle,
            false,
            drawGoggles,
            drawHat ? AllPartialModels.TRAIN_HAT : null,
            hashCode
        );
    }

    public static void renderShared(
        MatrixStack ms,
        @Nullable MatrixStack modelTransform,
        VertexConsumerProvider bufferSource,
        World level,
        BlockState blockState,
        HeatLevel heatLevel,
        float animation,
        float horizontalAngle,
        boolean canDrawFlame,
        boolean drawGoggles,
        PartialModel drawHat,
        int hashCode
    ) {

        boolean blockAbove = animation > 0.125f;
        float time = AnimationTickHolder.getRenderTime(level);
        float renderTick = time + (hashCode % 13) * 16f;
        float offsetMult = heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16;
        float offset = MathHelper.sin((float) ((renderTick / 16f) % (2 * Math.PI))) / offsetMult;
        float offset1 = MathHelper.sin((float) ((renderTick / 16f + Math.PI) % (2 * Math.PI))) / offsetMult;
        float offset2 = MathHelper.sin((float) ((renderTick / 16f + Math.PI / 2) % (2 * Math.PI))) / offsetMult;
        float headY = offset - (animation * .75f);

        ms.push();

        var blazeModel = getBlazeModel(heatLevel, blockAbove);

        SuperByteBuffer blazeBuffer = CachedBuffers.partial(blazeModel, blockState);
        if (modelTransform != null)
            blazeBuffer.transform(modelTransform);
        blazeBuffer.translate(0, headY, 0);
        draw(blazeBuffer, horizontalAngle, ms, bufferSource.getBuffer(RenderLayer.getSolid()));

        if (drawGoggles) {
            PartialModel gogglesModel = blazeModel == AllPartialModels.BLAZE_INERT ? AllPartialModels.BLAZE_GOGGLES_SMALL : AllPartialModels.BLAZE_GOGGLES;

            SuperByteBuffer gogglesBuffer = CachedBuffers.partial(gogglesModel, blockState);
            if (modelTransform != null)
                gogglesBuffer.transform(modelTransform);
            gogglesBuffer.translate(0, headY + 8 / 16f, 0);
            draw(gogglesBuffer, horizontalAngle, ms, bufferSource.getBuffer(RenderLayer.getSolid()));
        }

        if (drawHat != null) {
            SuperByteBuffer hatBuffer = CachedBuffers.partial(drawHat, blockState);
            if (modelTransform != null)
                hatBuffer.transform(modelTransform);
            hatBuffer.translate(0, headY, 0);
            if (blazeModel == AllPartialModels.BLAZE_INERT) {
                hatBuffer.translateY(0.5f).center().scale(0.75f).uncenter();
            } else {
                hatBuffer.translateY(0.75f);
            }
            VertexConsumer cutout = bufferSource.getBuffer(RenderLayer.getCutoutMipped());
            hatBuffer.rotateCentered(horizontalAngle + MathHelper.PI, Direction.UP).translate(0.5f, 0, 0.5f)
                .light(LightmapTextureManager.MAX_LIGHT_COORDINATE).renderInto(ms.peek(), cutout);
        }

        if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            PartialModel rodsModel = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS : AllPartialModels.BLAZE_BURNER_RODS;
            PartialModel rodsModel2 = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2;

            SuperByteBuffer rodsBuffer = CachedBuffers.partial(rodsModel, blockState);
            if (modelTransform != null)
                rodsBuffer.transform(modelTransform);
            rodsBuffer.translate(0, offset1 + animation + .125f, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .renderInto(ms.peek(), bufferSource.getBuffer(RenderLayer.getSolid()));

            SuperByteBuffer rodsBuffer2 = CachedBuffers.partial(rodsModel2, blockState);
            if (modelTransform != null)
                rodsBuffer2.transform(modelTransform);
            rodsBuffer2.translate(0, offset2 + animation - 3 / 16f, 0).light(LightmapTextureManager.MAX_LIGHT_COORDINATE)
                .renderInto(ms.peek(), bufferSource.getBuffer(RenderLayer.getSolid()));
        }

        if (canDrawFlame && blockAbove) {
            SpriteShiftEntry spriteShift = heatLevel == HeatLevel.SEETHING ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;

            float spriteWidth = spriteShift.getTarget().getMaxU() - spriteShift.getTarget().getMinU();

            float spriteHeight = spriteShift.getTarget().getMaxV() - spriteShift.getTarget().getMinV();

            float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();

            double vScroll = speed * time;
            vScroll = vScroll - Math.floor(vScroll);
            vScroll = vScroll * spriteHeight / 2;

            double uScroll = speed * time / 2;
            uScroll = uScroll - Math.floor(uScroll);
            uScroll = uScroll * spriteWidth / 2;

            SuperByteBuffer flameBuffer = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, blockState);
            if (modelTransform != null)
                flameBuffer.transform(modelTransform);
            flameBuffer.shiftUVScrolling(spriteShift, (float) uScroll, (float) vScroll);

            VertexConsumer cutout = bufferSource.getBuffer(RenderLayer.getCutoutMipped());
            draw(flameBuffer, horizontalAngle, ms, cutout);
        }

        ms.pop();
    }

    public static PartialModel getBlazeModel(HeatLevel heatLevel, boolean blockAbove) {
        if (heatLevel.isAtLeast(HeatLevel.SEETHING)) {
            return blockAbove ? AllPartialModels.BLAZE_SUPER_ACTIVE : AllPartialModels.BLAZE_SUPER;
        } else if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            return blockAbove && heatLevel.isAtLeast(HeatLevel.KINDLED) ? AllPartialModels.BLAZE_ACTIVE : AllPartialModels.BLAZE_IDLE;
        } else {
            return AllPartialModels.BLAZE_INERT;
        }
    }

    private static void draw(SuperByteBuffer buffer, float horizontalAngle, MatrixStack ms, VertexConsumer vc) {
        buffer.rotateCentered(horizontalAngle, Direction.UP).light(LightmapTextureManager.MAX_LIGHT_COORDINATE).renderInto(ms.peek(), vc);
    }

    public static void tickAnimation(BlazeBurnerBlockEntity be) {
        boolean active = be.getHeatLevelFromBlock().isAtLeast(HeatLevel.FADING) && be.isValidBlockAbove();

        if (!active) {
            float target = 0;
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null && !player.isInvisible()) {
                double x;
                double z;
                if (be.isVirtual()) {
                    x = -4;
                    z = -10;
                } else {
                    x = player.getX();
                    z = player.getZ();
                }
                double dx = x - (be.getPos().getX() + 0.5);
                double dz = z - (be.getPos().getZ() + 0.5);
                target = AngleHelper.deg(-MathHelper.atan2(dz, dx)) - 90;
            }
            target = be.headAngle.getValue() + AngleHelper.getShortestAngleDiff(be.headAngle.getValue(), target);
            be.headAngle.chase(target, .25f, Chaser.exp(5));
            be.headAngle.tickChaser();
        } else {
            be.headAngle.chase(
                (AngleHelper.horizontalAngle(be.getCachedState().getOrEmpty(BlazeBurnerBlock.FACING).orElse(Direction.SOUTH)) + 180) % 360,
                .125f,
                Chaser.EXP
            );
            be.headAngle.tickChaser();
        }

        be.headAnimation.chase(active ? 1 : 0, .25f, Chaser.exp(.25f));
        be.headAnimation.tickChaser();
    }

    public static class BlazeBurnerRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public float headY;
        public float horizontalAngle;
        public SuperByteBuffer blaze;
        public SuperByteBuffer goggles;
        public float gogglesHeadY;
        public HatRenderState hat;
        public SuperByteBuffer rods;
        public float rodsY;
        public SuperByteBuffer rods2;
        public float rods2Y;
        public FlameRenderState flame;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            blaze.translate(0, headY, 0);
            blaze.rotateCentered(horizontalAngle, Direction.UP);
            blaze.light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
            blaze.renderInto(matricesEntry, vertexConsumer);
            if (goggles != null) {
                goggles.translate(0, gogglesHeadY, 0);
                goggles.rotateCentered(horizontalAngle, Direction.UP);
                goggles.light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
                goggles.renderInto(matricesEntry, vertexConsumer);
            }
            if (rods != null) {
                rods.translate(0, rodsY, 0);
                rods.light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
                rods.renderInto(matricesEntry, vertexConsumer);
                rods2.translate(0, rods2Y, 0);
                rods2.light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
                rods2.renderInto(matricesEntry, vertexConsumer);
            }
        }
    }

    public static class HatRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public float angle;
        public boolean scale;
        public float offset;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.translate(0, offset, 0);
            if (scale) {
                model.scale(0.75f);
            }
            model.rotateCentered(angle, Direction.UP);
            model.translate(0.5f, 0, 0.5f);
            model.light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }

    public static class FlameRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer model;
        public SpriteShiftEntry spriteShift;
        public float uScroll;
        public float vScroll;
        public float angle;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            model.shiftUVScrolling(spriteShift, uScroll, vScroll);
            model.rotateCentered(angle, Direction.UP);
            model.light(LightmapTextureManager.MAX_LIGHT_COORDINATE);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
