package com.zurrtum.create.client.content.processing.burner;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BlazeBurnerRenderer implements BlockEntityRenderer<BlazeBurnerBlockEntity, BlazeBurnerRenderer.BlazeBurnerRenderState> {
    public BlazeBurnerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public BlazeBurnerRenderState createRenderState() {
        return new BlazeBurnerRenderState();
    }

    @Override
    public void extractRenderState(
        BlazeBurnerBlockEntity be,
        BlazeBurnerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        HeatLevel heatLevel = be.getHeatLevelForRender();
        if (heatLevel == HeatLevel.NONE)
            return;
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        Level level = be.getLevel();
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
    public void submit(BlazeBurnerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
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
            LocalPlayer player = Minecraft.getInstance().player;
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
                double dx = x - (be.getBlockPos().getX() + 0.5);
                double dz = z - (be.getBlockPos().getZ() + 0.5);
                target = AngleHelper.deg(-Mth.atan2(dz, dx)) - 90;
            }
            target = be.headAngle.getValue() + AngleHelper.getShortestAngleDiff(be.headAngle.getValue(), target);
            be.headAngle.chase(target, .25f, Chaser.exp(5));
            be.headAngle.tickChaser();
        } else {
            be.headAngle.chase(
                (AngleHelper.horizontalAngle(be.getBlockState().getOptionalValue(BlazeBurnerBlock.FACING).orElse(Direction.SOUTH)) + 180) % 360,
                .125f,
                Chaser.EXP
            );
            be.headAngle.tickChaser();
        }

        be.headAnimation.chase(active ? 1 : 0, .25f, Chaser.exp(.25f));
        be.headAnimation.tickChaser();
    }

    public static BlazeBurnerRenderData getBlazeBurnerRenderData(
        Level level,
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
        data.layer = RenderTypes.solidMovingBlock();
        data.horizontalAngle = horizontalAngle;
        boolean blockAbove = animation > 0.125f;
        float time = AnimationTickHolder.getRenderTime(level);
        float renderTick = time / 16f + (hashCode % 13);
        float offsetMult = heatLevel.isAtLeast(HeatLevel.FADING) ? 64 : 16;
        float offset = Mth.sin(renderTick % Mth.TWO_PI) / offsetMult;
        float offset1 = Mth.sin((float) ((renderTick + Math.PI) % Mth.TWO_PI)) / offsetMult;
        float offset2 = Mth.sin((renderTick + Mth.HALF_PI) % Mth.TWO_PI) / offsetMult;
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
            data.hat.layer = RenderTypes.cutoutMovingBlock();
            data.hat.model = CachedBuffers.partial(drawHat, blockState);
            data.hat.angle = horizontalAngle + Mth.PI;
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
            data.flame.layer = RenderTypes.cutoutMovingBlock();
            data.flame.model = CachedBuffers.partial(AllPartialModels.BLAZE_BURNER_FLAME, blockState);
            data.flame.angle = horizontalAngle;
            data.flame.spriteShift = heatLevel == HeatLevel.SEETHING ? AllSpriteShifts.SUPER_BURNER_FLAME : AllSpriteShifts.BURNER_FLAME;
            TextureAtlasSprite target = data.flame.spriteShift.getTarget();
            float spriteWidth = target.getU1() - target.getU0();
            float spriteHeight = target.getV1() - target.getV0();
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

    public static class BlazeBurnerRenderData implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public float headY;
        public float horizontalAngle;
        public SuperByteBuffer blaze;
        public @Nullable SuperByteBuffer goggles;
        public float gogglesHeadY;
        public @Nullable HatRenderState hat;
        public @Nullable SuperByteBuffer rods;
        public float rodsY;
        public SuperByteBuffer rods2;
        public float rods2Y;
        public @Nullable FlameRenderState flame;

        public void render(PoseStack matrices, SubmitNodeCollector queue) {
            queue.submitCustomGeometry(matrices, layer, this);
            if (hat != null) {
                queue.submitCustomGeometry(matrices, hat.layer, hat);
            }
            if (flame != null) {
                queue.submitCustomGeometry(matrices, flame.layer, flame);
            }
        }

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            blaze.translate(0, headY, 0);
            blaze.rotateCentered(horizontalAngle, Direction.UP);
            blaze.light(LightCoordsUtil.FULL_BRIGHT);
            blaze.renderInto(matricesEntry, vertexConsumer);
            if (goggles != null) {
                goggles.translate(0, gogglesHeadY, 0);
                goggles.rotateCentered(horizontalAngle, Direction.UP);
                goggles.light(LightCoordsUtil.FULL_BRIGHT);
                goggles.renderInto(matricesEntry, vertexConsumer);
            }
            if (rods != null) {
                rods.translate(0, rodsY, 0);
                rods.light(LightCoordsUtil.FULL_BRIGHT);
                rods.renderInto(matricesEntry, vertexConsumer);
                rods2.translate(0, rods2Y, 0);
                rods2.light(LightCoordsUtil.FULL_BRIGHT);
                rods2.renderInto(matricesEntry, vertexConsumer);
            }
        }
    }

    public static class HatRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        public float angle;
        public boolean scale;
        public float offset;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.translate(0, offset, 0);
            if (scale) {
                model.scale(0.75f);
            }
            model.rotateCentered(angle, Direction.UP);
            model.translate(0.5f, 0, 0.5f);
            model.light(LightCoordsUtil.FULL_BRIGHT);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }

    public static class FlameRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer model;
        public SpriteShiftEntry spriteShift;
        public float uScroll;
        public float vScroll;
        public float angle;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            model.shiftUVScrolling(spriteShift, uScroll, vScroll);
            model.rotateCentered(angle, Direction.UP);
            model.light(LightCoordsUtil.FULL_BRIGHT);
            model.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
