package com.zurrtum.create.client.content.decoration.slidingDoor;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.OrientedInstance;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlock;
import com.zurrtum.create.content.decoration.slidingDoor.SlidingDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public abstract class SlidingDoorVisual extends AbstractBlockEntityVisual<SlidingDoorBlockEntity> implements SimpleDynamicVisual, ShaderLightVisual {
    private boolean needUpdate;

    public SlidingDoorVisual(VisualizationContext ctx, SlidingDoorBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        if (blockState.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT) {
            switch (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
                case NORTH -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 1, 0);
                case SOUTH -> setSectionCollector(sectionCollector, 0, 0, 0, 1, 1, 0);
                case WEST -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 1, 1);
                case EAST -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 1, 0);
            }
        } else {
            switch (blockState.getValue(HorizontalDirectionalBlock.FACING)) {
                case NORTH -> setSectionCollector(sectionCollector, 0, 0, 0, 1, 1, 0);
                case SOUTH -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 1, 0);
                case WEST -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 1, 0);
                case EAST -> setSectionCollector(sectionCollector, 0, 0, 0, 0, 1, 1);
            }
        }
    }

    @Override
    public void beginFrame(Context ctx) {
        if (!blockEntity.animation.settled()) {
            transformModels(ctx.partialTick());
            needUpdate = true;
        } else if (needUpdate) {
            transformModels(ctx.partialTick());
            needUpdate = false;
        }
    }

    protected abstract void transformModels(float partialTick);

    public static SlidingDoorVisual create(
        VisualizationContext ctx,
        SlidingDoorBlockEntity blockEntity,
        float partialTick
    ) {
        if (((SlidingDoorBlock) blockEntity.getBlockState().getBlock()).isFoldingDoor()) {
            return new FoldingVisual(ctx, blockEntity, partialTick);
        }
        return new SlidingVisual(ctx, blockEntity, partialTick);
    }

    public static class FoldingVisual extends SlidingDoorVisual {
        private final TransformedInstance left;
        private final TransformedInstance right;
        private final Vector3fc facingVec;
        private final boolean flip;
        private final float yRadians;

        public FoldingVisual(VisualizationContext ctx, SlidingDoorBlockEntity blockEntity, float partialTick) {
            super(ctx, blockEntity, partialTick);
            flip = blockState.getValue(DoorBlock.HINGE) != DoorHingeSide.LEFT;
            Direction facing = blockState.getValue(DoorBlock.FACING);
            facingVec = facing.getUnitVec3f();
            yRadians = Radians.horizontal(facing.getClockWise());
            InstancerProvider instancerProvider = instancerProvider();
            Couple<PartialModel> partials = AllPartialModels.FOLDING_DOORS.get(BuiltInRegistries.BLOCK.getKey(blockState.getBlock()));
            left = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(partials.get(!flip)))
                .createInstance();
            right = instancerProvider.instancer(InstanceTypes.TRANSFORMED, Models.chunkPartial(partials.get(flip)))
                .createInstance();
            transformModels(partialTick);
        }

        @Override
        protected void transformModels(float partialTick) {
            float value = blockEntity.animation.getValue(partialTick);
            BlockPos pos = getVisualPosition();
            if (value != 0) {
                float v = value * value;
                float scale = Mth.clamp(value * 10, 0, 1) * 0.03125f;
                left.setIdentityTransform().translate(
                    Math.fma(facingVec.x(), scale, pos.getX()),
                    Math.fma(facingVec.y(), scale, pos.getY() + SlidingDoorRenderer.DOOR_OFFSET),
                    Math.fma(facingVec.z(), scale, pos.getZ())
                ).rotateYCentered(yRadians);
                if (flip) {
                    left.translate(0, 0, 1).rotateYDegrees(-91 * v).translate(0, 0, -0.5f);
                    right.setTransform(left.pose).rotateYDegrees(181 * v).translate(0, 0, -0.5f);
                } else {
                    left.rotateYDegrees(91 * v);
                    right.setTransform(left.pose).translate(0, 0, 0.5f).rotateYDegrees(-181 * v);
                }
            } else {
                left.setIdentityTransform()
                    .translate(pos.getX(), pos.getY() + SlidingDoorRenderer.DOOR_OFFSET, pos.getZ())
                    .rotateYCentered(yRadians);
                if (flip) {
                    right.setTransform(left.pose);
                    left.translate(0, 0, 0.5f);
                } else {
                    right.setTransform(left.pose).translate(0, 0, 0.5f);
                }
            }
            left.setChanged();
            right.setChanged();
        }

        @Override
        public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
            consumer.accept(left);
            consumer.accept(right);
        }

        @Override
        public void updateLight(float partialTick) {
            relight(left, right);
        }

        @Override
        protected void _delete() {
            left.delete();
            right.delete();
        }
    }

    public static class SlidingVisual extends SlidingDoorVisual {
        private final OrientedInstance door;
        private final Vector3fc facingVec;
        private final Vector3fc movementVec;

        public SlidingVisual(VisualizationContext ctx, SlidingDoorBlockEntity blockEntity, float partialTick) {
            super(ctx, blockEntity, partialTick);
            Identifier key = BuiltInRegistries.BLOCK.getKey(blockState.getBlock());
            Model model = Models.chunkPartial(AllPartialModels.SLIDING_DOORS.get(key));
            door = instancerProvider().instancer(InstanceTypes.ORIENTED, model).createInstance();
            Direction facing = blockState.getValue(DoorBlock.FACING);
            movementVec = blockState.getValue(DoorBlock.HINGE) == DoorHingeSide.LEFT ?
                facing.getCounterClockWise().getUnitVec3f() : facing.getClockWise().getUnitVec3f();
            facingVec = facing.getUnitVec3f();
            switch (facing) {
                case NORTH -> door.rotation.rotationY(Radians.DEG_180);
                case WEST -> door.rotation.rotationY(Radians.DEG_270);
                case EAST -> door.rotation.rotationY(Radians.DEG_90);
                case UP -> door.rotation.rotationX(Radians.DEG_270);
                case DOWN -> door.rotation.rotationX(Radians.DEG_90);
            }
            transformModels(partialTick);
        }

        @Override
        protected void transformModels(float partialTick) {
            float value = blockEntity.animation.getValue(partialTick);
            if (value != 0) {
                float scale = Mth.clamp(value * 10, 0, 1) * 0.03125f;
                float scale2 = value * value * 0.8125f;
                BlockPos pos = getVisualPosition();
                door.position(
                    Math.fma(movementVec.x(), scale2, Math.fma(facingVec.x(), scale, pos.getX())),
                    Math.fma(
                        movementVec.y(),
                        scale2,
                        Math.fma(facingVec.y(), scale, pos.getY() + SlidingDoorRenderer.DOOR_OFFSET)
                    ),
                    Math.fma(movementVec.z(), scale2, Math.fma(facingVec.z(), scale, pos.getZ()))
                );
            } else {
                door.position(getVisualPosition());
            }
            door.setChanged();
        }

        @Override
        public void collectCrumblingInstances(Consumer<@Nullable Instance> consumer) {
            consumer.accept(door);
        }

        @Override
        public void updateLight(float partialTick) {
            relight(door);
        }

        @Override
        protected void _delete() {
            door.delete();
        }
    }
}
