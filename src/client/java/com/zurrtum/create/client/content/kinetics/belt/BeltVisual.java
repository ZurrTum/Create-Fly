package com.zurrtum.create.client.content.kinetics.belt;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.content.processing.burner.ScrollInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class BeltVisual extends KineticBlockEntityVisual<BeltBlockEntity> {
    public static final float MAGIC_SCROLL_MULTIPLIER = 1f / (31.5f * 16f);
    public static final float SCROLL_FACTOR_DIAGONAL = 3f / 8f;
    public static final float SCROLL_FACTOR_OTHERWISE = 0.5f;
    public static final float SCROLL_OFFSET_BOTTOM = 0.5f;
    public static final float SCROLL_OFFSET_OTHERWISE = 0f;

    protected final ScrollInstance[] belts;
    @Nullable
    protected final RotatingInstance pulley;

    public BeltVisual(VisualizationContext context, BeltBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);


        BeltPart part = blockState.getValue(BeltBlock.PART);
        boolean start = part == BeltPart.START;
        boolean end = part == BeltPart.END;
        DyeColor color = blockEntity.color.orElse(null);

        boolean diagonal = blockState.getValue(BeltBlock.SLOPE).isDiagonal();
        belts = new ScrollInstance[diagonal ? 1 : 2];

        for (boolean bottom : Iterate.trueAndFalse) {
            PartialModel beltPartial = BeltRenderer.getBeltPartial(diagonal, start, end, bottom);
            SpriteShiftEntry spriteShift = BeltRenderer.getSpriteShiftEntry(color, diagonal, bottom);

            Instancer<ScrollInstance> beltModel = instancerProvider().instancer(AllInstanceTypes.SCROLLING, Models.partial(beltPartial));

            belts[bottom ? 0 : 1] = setup(beltModel.createInstance(), bottom, spriteShift);

            if (diagonal)
                break;
        }

        if (blockEntity.hasPulley()) {
            pulley = instancerProvider().instancer(AllInstanceTypes.ROTATING, getPulleyModel()).createInstance();

            pulley.setup(BeltVisual.this.blockEntity).setPosition(getVisualPosition()).setChanged();
        } else {
            pulley = null;
        }
    }

    @Override
    public void update(float pt) {
        DyeColor color = blockEntity.color.orElse(null);

        boolean diagonal = blockState.getValue(BeltBlock.SLOPE).isDiagonal();

        boolean bottom = true;
        for (ScrollInstance key : belts) {
            setup(key, bottom, BeltRenderer.getSpriteShiftEntry(color, diagonal, bottom));
            bottom = false;
        }

        if (pulley != null) {
            pulley.setup(blockEntity).setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(belts);

        if (pulley != null)
            relight(pulley);
    }

    @Override
    protected void _delete() {
        for (var key : belts) {
            key.delete();
        }
        if (pulley != null) {
            pulley.delete();
        }
    }

    private Model getPulleyModel() {
        Direction dir = getOrientation();

        return Models.partial(
            AllPartialModels.BELT_PULLEY, dir.getAxis(), (axis11, modelTransform1) -> {
                var msr = TransformStack.of(modelTransform1);
                msr.center();
                if (axis11 == Direction.Axis.X)
                    msr.rotateYDegrees(90);
                if (axis11 == Direction.Axis.Y)
                    msr.rotateXDegrees(90);
                msr.rotateXDegrees(90);
                msr.uncenter();
            }
        );
    }

    private Direction getOrientation() {
        Direction dir = blockState.getValue(BeltBlock.HORIZONTAL_FACING).getClockWise();

        if (blockState.getValue(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS)
            dir = Direction.UP;

        return dir;
    }

    private ScrollInstance setup(ScrollInstance key, boolean bottom, SpriteShiftEntry spriteShift) {
        BeltSlope beltSlope = blockState.getValue(BeltBlock.SLOPE);
        Direction facing = blockState.getValue(BeltBlock.HORIZONTAL_FACING);
        boolean diagonal = beltSlope.isDiagonal();
        boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
        boolean vertical = beltSlope == BeltSlope.VERTICAL;
        boolean upward = beltSlope == BeltSlope.UPWARD;
        boolean alongX = facing.getAxis() == Direction.Axis.X;
        boolean alongZ = facing.getAxis() == Direction.Axis.Z;
        boolean downward = beltSlope == BeltSlope.DOWNWARD;

        float speed = blockEntity.getSpeed();
        if (((facing.getAxisDirection() == Direction.AxisDirection.NEGATIVE) ^ upward) ^ ((alongX && !diagonal) || (alongZ && diagonal))) {
            speed = -speed;
        }
        if (sideways && (facing == Direction.SOUTH || facing == Direction.WEST) || (vertical && facing == Direction.EAST)) {
            speed = -speed;
        }

        float rotX = (!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0) + (downward ? 180 : 0) + (sideways ? 90 : 0) + (vertical && alongZ ? 180 : 0);
        float rotY = facing.toYRot() + ((diagonal ^ alongX) && !downward ? 180 : 0) + (sideways && alongZ ? 180 : 0) + (vertical && alongX ? 90 : 0);
        float rotZ = (sideways ? 90 : 0) + (vertical && alongX ? 90 : 0);

        Quaternionf q = new Quaternionf().rotationXYZ(rotX * Mth.DEG_TO_RAD, rotY * Mth.DEG_TO_RAD, rotZ * Mth.DEG_TO_RAD);

        key.setSpriteShift(spriteShift, 1f, (diagonal ? SCROLL_FACTOR_DIAGONAL : SCROLL_FACTOR_OTHERWISE)).position(getVisualPosition()).rotation(q)
            .speed(0, speed * MAGIC_SCROLL_MULTIPLIER).offset(0, bottom ? SCROLL_OFFSET_BOTTOM : SCROLL_OFFSET_OTHERWISE)
            .colorRgb(RotatingInstance.colorFromBE(blockEntity)).setChanged();

        return key;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        if (pulley != null) {
            consumer.accept(pulley);
        }
        for (var key : belts) {
            consumer.accept(key);
        }
    }
}
