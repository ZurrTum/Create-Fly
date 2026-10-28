package com.zurrtum.create.client.content.kinetics.belt;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.SpriteShiftEntry;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.content.processing.burner.ScrollInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
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
                Pose pose = modelTransform1.last();
                pose.translate(0.5f, 0.5f, 0.5f);
                if (axis11 == Direction.Axis.X) {
                    pose.rotate(Axis.YP, Radians.DEG_90);
                    pose.rotate(Axis.XP, Radians.DEG_90);
                } else if (axis11 == Direction.Axis.Y) {
                    pose.rotate(Axis.XP, Radians.DEG_180);
                } else {
                    pose.rotate(Axis.XP, Radians.DEG_90);
                }
                pose.translate(-0.5f, -0.5f, -0.5f);
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
        boolean upward = beltSlope == BeltSlope.UPWARD;
        boolean downward = beltSlope == BeltSlope.DOWNWARD;
        boolean diagonal = upward || downward;
        boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
        boolean vertical = beltSlope == BeltSlope.VERTICAL;
        float speed = blockEntity.getSpeed();
        if (sideways || (downward ? facing == Direction.SOUTH || facing == Direction.WEST :
            facing == Direction.NORTH || !vertical && facing == Direction.EAST)) {
            speed = -speed;
        }
        float xRadians, zRadians, yRadians;
        if (sideways) {
            xRadians = Radians.DEG_180;
            zRadians = Radians.DEG_90;
            yRadians = facing == Direction.NORTH ? 0 : Math.fma(facing.toYRot(), Mth.DEG_TO_RAD, Radians.DEG_180);
        } else if (vertical) {
            if (facing.getAxis() == Direction.Axis.X) {
                xRadians = Radians.DEG_90;
                zRadians = Radians.DEG_90;
                yRadians = facing == Direction.WEST ? 0 : Math.fma(facing.toYRot(), Mth.DEG_TO_RAD, Radians.DEG_270);
            } else {
                xRadians = Radians.DEG_270;
                zRadians = 0;
                yRadians = Mth.DEG_TO_RAD * facing.toYRot();
            }
        } else {
            xRadians = 0;
            zRadians = 0;
            if (facing.getAxis() == Direction.Axis.X ^ upward) {
                yRadians = facing == Direction.NORTH ? 0 : Math.fma(facing.toYRot(), Mth.DEG_TO_RAD, Radians.DEG_180);
            } else {
                yRadians = Mth.DEG_TO_RAD * facing.toYRot();
            }
        }
        if (xRadians != 0 || yRadians != 0) {
            key.rotationXYZ(xRadians, yRadians, zRadians);
        }
        key.setSpriteShift(spriteShift, 1.0f, diagonal ? SCROLL_FACTOR_DIAGONAL : SCROLL_FACTOR_OTHERWISE)
            .position(getVisualPosition()).speed(0, speed * MAGIC_SCROLL_MULTIPLIER).offset(0, SCROLL_OFFSET_OTHERWISE)
            .colorRgb(RotatingInstance.colorFromBE(blockEntity)).setChanged();

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
