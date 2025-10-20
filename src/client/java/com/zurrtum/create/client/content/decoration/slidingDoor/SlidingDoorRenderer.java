package com.zurrtum.create.client.content.decoration.slidingDoor;

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
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.enums.DoorHinge;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.client.MinecraftClient;
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
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

public class SlidingDoorRenderer implements BlockEntityRenderer<SlidingDoorBlockEntity, SlidingDoorRenderer.DoorRenderState> {
    public SlidingDoorRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public DoorRenderState createRenderState() {
        return new DoorRenderState();
    }

    @Override
    public void updateRenderState(
        SlidingDoorBlockEntity be,
        DoorRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        state.blockState = be.getCachedState();
        if (!be.shouldRenderSpecial(state.blockState)) {
            return;
        }
        state.pos = be.getPos();
        state.type = be.getType();
        World world = be.getWorld();
        state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(world, state.pos) : 15728880;
        state.layer = RenderLayer.getCutoutMipped();
        Direction facing = state.blockState.get(DoorBlock.FACING);
        Direction movementDirection = facing.rotateYClockwise();
        boolean isLeft = state.blockState.get(DoorBlock.HINGE) == DoorHinge.LEFT;
        float value = be.animation.getValue(tickProgress);
        Vec3d offset = Vec3d.of(facing.getVector()).multiply(MathHelper.clamp(value * 10, 0, 1) * 1 / 32f);
        SlidingDoorBlock block = (SlidingDoorBlock) state.blockState.getBlock();
        if (block.isFoldingDoor()) {
            FoldingDoorRenderState renderState = new FoldingDoorRenderState();
            renderState.offsetY = -1 / 512f;
            renderState.offset = offset;
            renderState.angle = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(movementDirection);
            float f = isLeft ? 1 : -1;
            float v = f * value * value;
            renderState.yRot = MathHelper.RADIANS_PER_DEGREE * 91 * v;
            renderState.flip = !isLeft;
            Couple<PartialModel> partials = AllPartialModels.FOLDING_DOORS.get(Registries.BLOCK.getId(block));
            renderState.left = CachedBuffers.partial(partials.get(isLeft), state.blockState);
            renderState.right = CachedBuffers.partial(partials.get(renderState.flip), state.blockState);
            renderState.rightOffset = f / 2f;
            renderState.rightYRot = MathHelper.RADIANS_PER_DEGREE * -181 * v;
            renderState.light = state.lightmapCoordinates;
            state.renderer = renderState;
        } else {
            if (isLeft) {
                movementDirection = movementDirection.getOpposite();
            }
            SlidingDoorRenderState renderState = new SlidingDoorRenderState();
            BlockState blockState = state.blockState.with(DoorBlock.OPEN, false);
            renderState.upper = CachedBuffers.block(blockState.with(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            renderState.lower = CachedBuffers.block(blockState.with(DoorBlock.HALF, DoubleBlockHalf.LOWER));
            renderState.upperOffset = 1 - 1 / 512f;
            renderState.offset = Vec3d.of(movementDirection.getVector()).multiply(value * value * 13 / 16f).add(offset);
            renderState.light = state.lightmapCoordinates;
            state.renderer = renderState;
        }
    }

    @Override
    public void render(DoorRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.renderer != null) {
            queue.submitCustom(matrices, state.layer, state.renderer);
        }
    }

    public static Pair<ScrollInput, Label> createWidget(MinecraftClient mc, int x, int y, Consumer<DoorControl> callback, DoorControl initial) {
        Entity entity = mc.getCameraEntity();
        DoorControl playerFacing = entity != null ? switch (entity.getHorizontalFacing()) {
            case EAST -> DoorControl.EAST;
            case WEST -> DoorControl.WEST;
            case NORTH -> DoorControl.NORTH;
            case SOUTH -> DoorControl.SOUTH;
            default -> DoorControl.NONE;
        } : DoorControl.NONE;

        Label label = new Label(x + 4, y + 6, Text.empty()).withShadow();
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
        public OrderedRenderCommandQueue.Custom renderer;
        public RenderLayer layer;
    }

    public static class FoldingDoorRenderState implements OrderedRenderCommandQueue.Custom {
        public float offsetY;
        public Vec3d offset;
        public float angle;
        public float yRot;
        public boolean flip;
        public SuperByteBuffer left;
        public SuperByteBuffer right;
        public float rightOffset;
        public float rightYRot;
        public int light;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
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

    public static class SlidingDoorRenderState implements OrderedRenderCommandQueue.Custom {
        public SuperByteBuffer upper;
        public SuperByteBuffer lower;
        public float upperOffset;
        public Vec3d offset;
        public int light;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            upper.translate(0, upperOffset, 0).translate(offset).light(light).renderInto(matricesEntry, vertexConsumer);
            lower.translate(offset).light(light).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
