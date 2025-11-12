package com.zurrtum.create.client.content.decoration.slidingDoor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControl;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

public class SlidingDoorRenderer implements BlockEntityRenderer<SlidingDoorBlockEntity, SlidingDoorRenderer.DoorRenderState> {
    public SlidingDoorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public DoorRenderState createRenderState() {
        return new DoorRenderState();
    }

    @Override
    public void extractRenderState(
        SlidingDoorBlockEntity be,
        DoorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        state.blockState = be.getBlockState();
        if (!be.shouldRenderSpecial(state.blockState)) {
            return;
        }
        state.blockPos = be.getBlockPos();
        state.blockEntityType = be.getType();
        Level world = be.getLevel();
        state.lightCoords = world != null ? LevelRenderer.getLightColor(world, state.blockPos) : 15728880;
        state.layer = RenderType.cutoutMipped();
        Direction facing = state.blockState.getValue(DoorBlock.FACING);
        Direction movementDirection = facing.getClockWise();
        boolean isLeft = state.blockState.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT;
        float value = be.animation.getValue(tickProgress);
        Vec3 offset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(Mth.clamp(value * 10, 0, 1) * 1 / 32f);
        SlidingDoorBlock block = (SlidingDoorBlock) state.blockState.getBlock();
        if (block.isFoldingDoor()) {
            FoldingDoorRenderState renderState = new FoldingDoorRenderState();
            renderState.offsetY = -1 / 512f;
            renderState.offset = offset;
            renderState.angle = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(movementDirection);
            float f = isLeft ? 1 : -1;
            float v = f * value * value;
            renderState.yRot = Mth.DEG_TO_RAD * 91 * v;
            renderState.flip = !isLeft;
            Couple<PartialModel> partials = AllPartialModels.FOLDING_DOORS.get(BuiltInRegistries.BLOCK.getKey(block));
            renderState.left = CachedBuffers.partial(partials.get(isLeft), state.blockState);
            renderState.right = CachedBuffers.partial(partials.get(renderState.flip), state.blockState);
            renderState.rightOffset = f / 2f;
            renderState.rightYRot = Mth.DEG_TO_RAD * -181 * v;
            renderState.light = state.lightCoords;
            state.renderer = renderState;
        } else {
            if (isLeft) {
                movementDirection = movementDirection.getOpposite();
            }
            SlidingDoorRenderState renderState = new SlidingDoorRenderState();
            BlockState blockState = state.blockState.setValue(DoorBlock.OPEN, false);
            renderState.upper = CachedBuffers.block(blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            renderState.lower = CachedBuffers.block(blockState.setValue(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            renderState.upperOffset = 1 - 1 / 512f;
            renderState.offset = Vec3.atLowerCornerOf(movementDirection.getUnitVec3i()).scale(value * value * 13 / 16f).add(offset);
            renderState.light = state.lightCoords;
            state.renderer = renderState;
        }
    }

    @Override
    public void submit(DoorRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.renderer != null) {
            queue.submitCustomGeometry(matrices, state.layer, state.renderer);
        }
    }

    public static Pair<ScrollInput, Label> createWidget(Minecraft mc, int x, int y, Consumer<DoorControl> callback, DoorControl initial) {
        Entity entity = mc.getCameraEntity();
        DoorControl playerFacing = entity != null ? switch (entity.getDirection()) {
            case EAST -> DoorControl.EAST;
            case WEST -> DoorControl.WEST;
            case NORTH -> DoorControl.NORTH;
            case SOUTH -> DoorControl.SOUTH;
            default -> DoorControl.NONE;
        } : DoorControl.NONE;

        Label label = new Label(x + 4, y + 6, Component.empty()).withShadow();
        ScrollInput input = new SelectionScrollInput(x, y, 53, 16).forOptions(CreateLang.translatedOptions(
            "contraption.door_control",
            valuesAsString()
        )).titled(CreateLang.translateDirect("contraption.door_control")).calling(s -> {
            DoorControl mode = DoorControl.values()[s];
            label.text = CreateLang.translateDirect("contraption.door_control." + Lang.asId(mode.name()) + ".short");
            callback.accept(mode);
        }).addHint(CreateLang.translateDirect(
            "contraption.door_control.player_facing",
            CreateLang.translateDirect("contraption.door_control." + Lang.asId(playerFacing.name()) + ".short")
        )).setState(initial.ordinal());
        input.onChanged();
        return Pair.of(input, label);
    }

    public static String[] valuesAsString() {
        DoorControl[] values = DoorControl.values();
        return Arrays.stream(values).map(dc -> dc.name().toLowerCase(Locale.ROOT)).toList().toArray(new String[values.length]);
    }

    public static class DoorRenderState extends BlockEntityRenderState {
        public SubmitNodeCollector.CustomGeometryRenderer renderer;
        public RenderType layer;
    }

    public static class FoldingDoorRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public float offsetY;
        public Vec3 offset;
        public float angle;
        public float yRot;
        public boolean flip;
        public SuperByteBuffer left;
        public SuperByteBuffer right;
        public float rightOffset;
        public float rightYRot;
        public int light;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            left.translate(0, offsetY, 0).translate(offset).rotateCentered(angle, Direction.UP);
            if (flip) {
                left.translate(0, 0, 1);
            }
            left.rotateY(yRot);
            if (flip) {
                left.translate(0, 0, -0.5f);
            }
            left.light(light).renderInto(matricesEntry, vertexConsumer);
            right.translate(0, offsetY, 0).translate(offset).rotateCentered(angle, Direction.UP);
            if (flip) {
                right.translate(0, 0, 1);
            }
            right.rotateY(yRot).translate(0, 0, rightOffset).rotateY(rightYRot);
            if (flip) {
                right.translate(0, 0, -0.5f);
            }
            right.light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }

    public static class SlidingDoorRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public SuperByteBuffer upper;
        public SuperByteBuffer lower;
        public float upperOffset;
        public Vec3 offset;
        public int light;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            upper.translate(0, upperOffset, 0).translate(offset).light(light).renderInto(matricesEntry, vertexConsumer);
            lower.translate(offset).light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
