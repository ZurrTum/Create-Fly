package com.zurrtum.create.client.content.decoration.slidingDoor;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SafeBlockEntityRenderer;
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
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Consumer;

public class SlidingDoorRenderer extends SafeBlockEntityRenderer<SlidingDoorBlockEntity> {
    public SlidingDoorRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(SlidingDoorBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        BlockState blockState = be.getCachedState();
        if (!be.shouldRenderSpecial(blockState))
            return;

        Direction facing = blockState.get(DoorBlock.FACING);
        Direction movementDirection = facing.rotateYClockwise();

        if (blockState.get(DoorBlock.HINGE) == DoorHinge.LEFT)
            movementDirection = movementDirection.getOpposite();

        float value = be.animation.getValue(partialTicks);
        float value2 = MathHelper.clamp(value * 10, 0, 1);

        VertexConsumer vb = buffer.getBuffer(RenderLayer.getCutoutMipped());
        Vec3d offset = Vec3d.of(movementDirection.getVector()).multiply(value * value * 13 / 16f)
            .add(Vec3d.of(facing.getVector()).multiply(value2 * 1 / 32f));

        if (((SlidingDoorBlock) blockState.getBlock()).isFoldingDoor()) {
            Couple<PartialModel> partials = AllPartialModels.FOLDING_DOORS.get(Registries.BLOCK.getId(blockState.getBlock()));

            boolean flip = blockState.get(DoorBlock.HINGE) == DoorHinge.RIGHT;
            for (boolean left : Iterate.trueAndFalse) {
                SuperByteBuffer partial = CachedBuffers.partial(partials.get(left ^ flip), blockState);
                float f = flip ? -1 : 1;

                partial.translate(0, -1 / 512f, 0).translate(Vec3d.of(facing.getVector()).multiply(value2 * 1 / 32f));
                partial.rotateCentered(MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing.rotateYClockwise()), Direction.UP);

                if (flip)
                    partial.translate(0, 0, 1);
                partial.rotateYDegrees(91 * f * value * value);

                if (!left)
                    partial.translate(0, 0, f / 2f).rotateYDegrees(-181 * f * value * value);

                if (flip)
                    partial.translate(0, 0, -1 / 2f);

                partial.light(light).renderInto(ms, vb);
            }

            return;
        }

        for (DoubleBlockHalf half : DoubleBlockHalf.values()) {
            CachedBuffers.block(blockState.with(DoorBlock.OPEN, false).with(DoorBlock.HALF, half))
                .translate(0, half == DoubleBlockHalf.UPPER ? 1 - 1 / 512f : 0, 0).translate(offset).light(light).renderInto(ms, vb);
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
}
