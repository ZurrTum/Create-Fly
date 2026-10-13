package com.zurrtum.create.client.content.kinetics.belt;

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
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class BeltVisual extends KineticBlockEntityVisual<BeltBlockEntity> {
    public static final float MAGIC_SCROLL_MULTIPLIER = 1.0f / (31.5f * 16.0f);
    public static final float SCROLL_FACTOR_DIAGONAL = 3.0f / 8.0f;
    public static final float SCROLL_FACTOR_OTHERWISE = 0.5f;
    public static final float SCROLL_OFFSET_OTHERWISE = 0.0f;

    protected final ScrollInstance belt;
    @Nullable
    protected final RotatingInstance pulley;

    public BeltVisual(VisualizationContext context, BeltBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);


        BeltPart part = blockState.getValue(BeltBlock.PART);
        DyeColor color = blockEntity.color.orElse(null);

        BeltSlope beltSlope = blockState.getValue(BeltBlock.SLOPE);
        boolean casing = blockEntity.casing != CasingType.NONE;

        PartialModel beltPartial = BeltRenderer.getBeltPartial(beltSlope, part, casing);
        SpriteShiftEntry spriteShift = BeltRenderer.getSpriteShiftEntry(color, beltSlope.isDiagonal());

        Instancer<ScrollInstance> beltModel = instancerProvider().instancer(
            AllInstanceTypes.SCROLLING,
            Models.chunkPartial(beltPartial)
        );

        belt = setup(beltModel.createInstance(), spriteShift);

        if (blockEntity.hasPulley()) {
            pulley = instancerProvider().instancer(AllInstanceTypes.ROTATING, getPulleyModel(casing)).createInstance();

            pulley.setup(BeltVisual.this.blockEntity).setPosition(getVisualPosition()).setChanged();
        } else {
            pulley = null;
        }
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BeltBlock.SLOPE)) {
            case DOWNWARD -> {
                switch (blockState.getValue(BeltBlock.PART)) {
                    case START -> setSectionCollector(sectionCollector, 0, -1, 0, 0, 0, 0);
                    case END -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 1, 0);
                    default -> setSectionCollector(sectionCollector, 0, -1, 0, 0, 1, 0);
                }
            }
            case UPWARD -> {
                switch (blockState.getValue(BeltBlock.PART)) {
                    case START -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 1, 0);
                    case END -> setSectionCollector(sectionCollector, 0, -1, 0, 0, 0, 0);
                    default -> setSectionCollector(sectionCollector, 0, -1, 0, 0, 1, 0);
                }
            }
            default -> super.setSectionCollector(sectionCollector);
        }
    }

    @Override
    public void update(float pt) {
        DyeColor color = blockEntity.color.orElse(null);

        boolean diagonal = blockState.getValue(BeltBlock.SLOPE).isDiagonal();

        setup(belt, BeltRenderer.getSpriteShiftEntry(color, diagonal));

        if (pulley != null) {
            pulley.setup(blockEntity).setChanged();
        }
    }

    @Override
    public void updateLight(float partialTick) {
        relight(belt);

        if (pulley != null) {
            relight(pulley);
        }
    }

    @Override
    protected void _delete() {
        belt.delete();
        if (pulley != null) {
            pulley.delete();
        }
    }

    private Model getPulleyModel(boolean casing) {
        Direction dir = getOrientation();

        return Models.chunkPartial(
            casing ? AllPartialModels.ENCASED_SHAFT : AllPartialModels.BELT_PULLEY,
            dir.getAxis(),
            (axis11, modelTransform1) -> {
                var msr = TransformStack.of(modelTransform1);
                msr.center();
                if (axis11 == Axis.X) {
                    msr.rotateYDegrees(90);
                }
                if (axis11 == Axis.Y) {
                    msr.rotateXDegrees(90);
                }
                msr.rotateXDegrees(90);
                msr.uncenter();
            }
        );
    }

    private Direction getOrientation() {
        Direction dir = blockState.getValue(BeltBlock.HORIZONTAL_FACING).getClockWise();

        if (blockState.getValue(BeltBlock.SLOPE) == BeltSlope.SIDEWAYS) {
            dir = Direction.UP;
        }

        return dir;
    }

    private ScrollInstance setup(ScrollInstance key, SpriteShiftEntry spriteShift) {
        BeltSlope beltSlope = blockState.getValue(BeltBlock.SLOPE);
        Direction facing = blockState.getValue(BeltBlock.HORIZONTAL_FACING);
        boolean diagonal = beltSlope.isDiagonal();
        boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
        boolean vertical = beltSlope == BeltSlope.VERTICAL;
        boolean upward = beltSlope == BeltSlope.UPWARD;
        boolean alongX = facing.getAxis() == Axis.X;
        boolean alongZ = facing.getAxis() == Axis.Z;

        float speed = blockEntity.getSpeed();
        if (facing.getAxisDirection() == AxisDirection.NEGATIVE ^ upward ^ (alongX && !diagonal || alongZ && diagonal)) {
            speed = -speed;
        }
        if (sideways && (facing == Direction.SOUTH || facing == Direction.WEST) || vertical && facing == Direction.EAST) {
            speed = -speed;
        }

        float rotX = (!diagonal && beltSlope != BeltSlope.HORIZONTAL ? 90 : 0) + (sideways ? 90 : 0) + (
            vertical && alongZ ? 180 : 0);
        float rotY = facing.toYRot() + (alongX ^ upward ? 180 : 0) + (sideways && alongZ ? 180 : 0) + (
            vertical && alongX ? 90 : 0);
        float rotZ = (sideways ? 90 : 0) + (vertical && alongX ? 90 : 0);

        Quaternionf q = new Quaternionf().rotationXYZ(
            rotX * Mth.DEG_TO_RAD,
            rotY * Mth.DEG_TO_RAD,
            rotZ * Mth.DEG_TO_RAD
        );

        key.setSpriteShift(spriteShift, 1.0f, diagonal ? SCROLL_FACTOR_DIAGONAL : SCROLL_FACTOR_OTHERWISE)
            .position(getVisualPosition()).rotation(q).speed(0, speed * MAGIC_SCROLL_MULTIPLIER)
            .offset(0, SCROLL_OFFSET_OTHERWISE).colorRgb(RotatingInstance.colorFromBE(blockEntity)).setChanged();

        return key;
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        if (pulley != null) {
            consumer.accept(pulley);
        }
        consumer.accept(belt);
    }

    public static boolean shouldSkipVanillaRender(BeltBlockEntity be) {
        if (be.hasLevel()) {
            BlockState state = be.getBlockState();
            return !state.hasProperty(BeltBlock.PART) || state.getValue(BeltBlock.PART) != BeltPart.START;
        }
        return !be.isController();
    }
}
