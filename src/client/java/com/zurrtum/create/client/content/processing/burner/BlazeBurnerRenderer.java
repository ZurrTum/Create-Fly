package com.zurrtum.create.client.content.processing.burner;

import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
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
        float horizontalAngle = AngleHelper.rad(be.headAngle.getValue(tickProgress));
        boolean canDrawFlame = heatLevel.isAtLeast(HeatLevel.FADING);
        boolean drawGoggles = be.goggles;
        PartialModel drawHat = be.hat ? AllPartialModels.TRAIN_HAT : be.stockKeeper ? AllPartialModels.LOGISTICS_HAT : null;
        int hashCode = be.hashCode();
        state.data = getBlazeBurnerRenderData(
            level,
            state.blockState,
            heatLevel,
            animation,
            horizontalAngle,
            canDrawFlame,
            drawGoggles,
            drawHat,
            hashCode
        );
    }

    @Override
    public void render(BlazeBurnerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        state.data.render(matrices, queue);
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

    public static BlazeBurnerRenderData getBlazeBurnerRenderData(
        World level,
        BlockState blockState,
        HeatLevel heatLevel,
        float animation,
        float horizontalAngle,
        boolean canDrawFlame,
        boolean drawGoggles,
        @Nullable PartialModel drawHat,
        int hashCode
    ) {
        BlazeBurnerRenderData data = new BlazeBurnerRenderData();
        data.layer = RenderLayer.getSolid();
        data.horizontalAngle = horizontalAngle;
        boolean blockAbove = animation > 0.125f;
        float time = AnimationTickHolder.getRenderTime(level);
        float renderTick = time / 16f + (hashCode % 13);
        float offsetMult = heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16;
        float offset = MathHelper.sin(renderTick % MathHelper.TAU) / offsetMult;
        float offset1 = MathHelper.sin((float) ((renderTick + Math.PI) % MathHelper.TAU)) / offsetMult;
        float offset2 = MathHelper.sin((renderTick + MathHelper.HALF_PI) % MathHelper.TAU) / offsetMult;
        data.headY = offset - (animation * .75f);
        PartialModel blazeModel = getBlazeModel(heatLevel, blockAbove);
        data.blaze = CachedBuffers.partial(blazeModel, blockState);
        if (drawGoggles) {
            PartialModel gogglesModel = blazeModel == AllPartialModels.BLAZE_INERT ? AllPartialModels.BLAZE_GOGGLES_SMALL : AllPartialModels.BLAZE_GOGGLES;
            data.goggles = CachedBuffers.partial(gogglesModel, blockState);
            data.gogglesHeadY = data.headY + 0.5f;
        }
        if (drawHat != null) {
            data.hat = new HatRenderState();
            boolean scale = blazeModel == AllPartialModels.BLAZE_INERT;
            data.hat.scale = scale;
            data.hat.offset = data.headY + (scale ? 0.5f : 0.75f);
            data.hat.layer = RenderLayer.getCutoutMipped();
            data.hat.model = CachedBuffers.partial(drawHat, blockState);
            data.hat.angle = horizontalAngle + MathHelper.PI;
        }
        if (heatLevel.isAtLeast(HeatLevel.FADING)) {
            PartialModel rodsModel = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS : AllPartialModels.BLAZE_BURNER_RODS;
            PartialModel rodsModel2 = heatLevel == HeatLevel.SEETHING ? AllPartialModels.BLAZE_BURNER_SUPER_RODS_2 : AllPartialModels.BLAZE_BURNER_RODS_2;
            data.rods = CachedBuffers.partial(rodsModel, blockState);
            data.rodsY = offset1 + animation + .125f;
            data.rods2 = CachedBuffers.partial(rodsModel2, blockState);
            data.rods2Y = offset2 + animation - 3 / 16f;
        }
        if (canDrawFlame && blockAbove) {
            data.flame = new FlameRenderState();
            data.flame.layer = RenderLayer.getCutoutMipped();
            data.flame.model = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, blockState);
            data.flame.angle = horizontalAngle;
            data.flame.spriteShift = heatLevel == HeatLevel.SEETHING ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;
            Sprite target = data.flame.spriteShift.getTarget();
            float spriteWidth = target.getMaxU() - target.getMinU();
            float spriteHeight = target.getMaxV() - target.getMinV();
            float speed = 1 / 32f + 1 / 64f * heatLevel.ordinal();
            double vScroll = speed * time;
            vScroll = vScroll - Math.floor(vScroll);
            vScroll = vScroll * spriteHeight / 2;
            data.flame.vScroll = (float) vScroll;
            double uScroll = speed * time / 2;
            uScroll = uScroll - Math.floor(uScroll);
            uScroll = uScroll * spriteWidth / 2;
            data.flame.uScroll = (float) uScroll;
        }
        return data;
    }

    public static class BlazeBurnerRenderState extends BlockEntityRenderState {
        public BlazeBurnerRenderData data;
    }

    public static class BlazeBurnerRenderData implements OrderedRenderCommandQueue.Custom {
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

        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            queue.submitCustom(matrices, layer, this);
            if (hat != null) {
                queue.submitCustom(matrices, hat.layer, hat);
            }
            if (flame != null) {
                queue.submitCustom(matrices, flame.layer, flame);
            }
        }

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
