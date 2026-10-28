package com.zurrtum.create.client.content.decoration.slidingDoor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.decoration.slidingDoor.SlidingDoorRenderer.DoorRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.decoration.slidingDoor.DoorControl;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

public class SlidingDoorRenderer implements BlockEntityRenderer<SlidingDoorBlockEntity, DoorRenderState> {
    public static float DOOR_OFFSET = -1 / 512.0f;

    public SlidingDoorRenderer(Context context) {
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        Direction facing = state.blockState.getValue(DoorBlock.FACING);
        boolean isLeft = state.blockState.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT;
        float value = be.animation.getValue(tickProgress);
        Vec3i facingVec = facing.getUnitVec3i();
        float scale = Mth.clamp(value * 10, 0, 1) * 0.03125f;
        SlidingDoorBlock block = (SlidingDoorBlock) state.blockState.getBlock();
        if (block.isFoldingDoor()) {
            CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
            FoldingDoorRenderState renderState = new FoldingDoorRenderState();
            renderState.offsetX = facingVec.getX() * scale;
            renderState.offsetY = Math.fma(facingVec.getY(), scale, DOOR_OFFSET);
            renderState.offsetZ = facingVec.getZ() * scale;
            renderState.yRadians = Radians.horizontal(facing.getClockWise());
            float v = value * value * (isLeft ? 1 : -1);
            renderState.yRot = 91 * v;
            renderState.flip = !isLeft;
            Couple<PartialModel> partials = AllPartialModels.FOLDING_DOORS.get(BuiltInRegistries.BLOCK.getKey(block));
            renderState.left = CachedBuffers.partial(partials.get(isLeft), state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            renderState.right = CachedBuffers.partial(partials.get(renderState.flip), state.blockState)
                .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
            renderState.rightYRot = -181 * v;
            state.door = renderState;
        } else {
            SlidingDoorRenderState renderState = new SlidingDoorRenderState();
            renderState.model = CachedBuffers.partialFacing(
                    AllPartialModels.SLIDING_DOORS.get(BuiltInRegistries.BLOCK.getKey(block)), state.blockState, facing)
                .cardinalLighting(level).light(state.lightCoords).extractRenderState();
            Vector3fc movementVec =
                isLeft ? facing.getCounterClockWise().getUnitVec3f() : facing.getClockWise().getUnitVec3f();
            float movementScale = value * value * 0.8125f;
            renderState.offsetX = Math.fma(movementVec.x(), movementScale, facingVec.getX() * scale);
            renderState.offsetY = Math.fma(
                movementVec.y(),
                movementScale,
                Math.fma(facingVec.getY(), scale, DOOR_OFFSET)
            );
            renderState.offsetZ = Math.fma(movementVec.z(), movementScale, facingVec.getZ() * scale);
            state.door = renderState;
        }
    }

    @Override
    public void submit(
        DoorRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        state.door.submit(matrices, queue);
    }

    public static Pair<ScrollInput, Label> createWidget(
        Minecraft mc,
        int x,
        int y,
        Consumer<DoorControl> callback,
        DoorControl initial
    ) {
        Entity entity = mc.getCameraEntity();
        DoorControl playerFacing = entity != null ? switch (entity.getDirection()) {
            case EAST -> DoorControl.EAST;
            case WEST -> DoorControl.WEST;
            case NORTH -> DoorControl.NORTH;
            case SOUTH -> DoorControl.SOUTH;
            default -> DoorControl.NONE;
        } : DoorControl.NONE;

        Label label = new Label(x + 4, y + 6, Component.empty()).withShadow();
        ScrollInput input = new SelectionScrollInput(x, y, 53, 16).forOptions(CreateLang.translatedOptions("contraption.door_control",
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
        return Arrays.stream(values).map(dc -> dc.name().toLowerCase(Locale.ROOT)).toList()
            .toArray(new String[values.length]);
    }

    public static class DoorRenderState extends BlockEntityRenderState {
        public @UnknownNullability AbstractDoorRenderState door;
    }

    public interface AbstractDoorRenderState {
        void submit(PoseStack matrices, OrderedSubmitNodeCollector queue);
    }

    public static class FoldingDoorRenderState implements AbstractDoorRenderState {
        public @UnknownNullability SuperByteBufferRenderState left;
        public @UnknownNullability SuperByteBufferRenderState right;
        public float offsetX;
        public float offsetY;
        public float offsetZ;
        public float yRot;
        public float rightYRot;
        public float yRadians;
        public boolean flip;

        @Override
        public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
            Pose pose = matrices.last();
            pose.translate(offsetX, offsetY, offsetZ);
            if (yRadians != 0) {
                pose.rotateYCentered(yRadians);
            }
            if (flip) {
                if (yRot != 0) {
                    pose.translate(0, 0, 1);
                    pose.rotate(Axis.YP, Mth.DEG_TO_RAD * yRot);
                    pose.translate(0, 0, -0.5f);
                } else {
                    pose.translate(0, 0, 0.5f);
                }
                left.submit(matrices, queue);
                if (rightYRot != 0) {
                    pose.rotate(Axis.YP, Mth.DEG_TO_RAD * rightYRot);
                }
                pose.translate(0, 0, -0.5f);
                right.submit(matrices, queue);
            } else {
                if (yRot != 0) {
                    pose.rotate(Axis.YP, Mth.DEG_TO_RAD * yRot);
                }
                left.submit(matrices, queue);
                pose.translate(0, 0, 0.5f);
                if (rightYRot != 0) {
                    pose.rotate(Axis.YP, Mth.DEG_TO_RAD * rightYRot);
                }
                right.submit(matrices, queue);
            }
        }
    }

    public static class SlidingDoorRenderState implements AbstractDoorRenderState {
        public @UnknownNullability SuperByteBufferRenderState model;
        public float offsetX;
        public float offsetY;
        public float offsetZ;

        @Override
        public void submit(PoseStack matrices, OrderedSubmitNodeCollector queue) {
            matrices.translate(offsetX, offsetY, offsetZ);
            model.submit(matrices, queue);
        }
    }
}
