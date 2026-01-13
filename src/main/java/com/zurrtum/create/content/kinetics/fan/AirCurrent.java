package com.zurrtum.create.content.kinetics.fan;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.decoration.copycat.CopycatBlock;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessing;
import com.zurrtum.create.content.kinetics.fan.processing.FanProcessingType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AirCurrent {

    public final IAirCurrentSource source;
    public Box bounds = new Box(0, 0, 0, 0, 0, 0);
    public List<AirCurrentSegment> segments = new ArrayList<>();
    public Direction direction;
    public boolean pushing;
    public float maxDistance;

    protected List<Pair<TransportedItemStackHandlerBehaviour, FanProcessingType>> affectedItemHandlers = new ArrayList<>();
    protected List<Entity> caughtEntities = new ArrayList<>();

    public AirCurrent(IAirCurrentSource source) {
        this.source = source;
    }

    public void tick() {
        if (direction == null)
            rebuild();
        World world = source.getAirCurrentWorld();
        if (world != null && world.isClient) {
            float offset = pushing ? 0.5f : maxDistance + .5f;
            Vec3d pos = VecHelper.getCenterOf(source.getAirCurrentPos()).add(Vec3d.of(direction.getVector()).multiply(offset));
            AllClientHandle.INSTANCE.addAirFlowParticle(world, source.getAirCurrentPos(), pos.x, pos.y, pos.z);
        }

        tickAffectedEntities(world);
        tickAffectedHandlers();
    }

    protected void tickAffectedEntities(World world) {
        for (Iterator<Entity> iterator = caughtEntities.iterator(); iterator.hasNext(); ) {
            Entity entity = iterator.next();
            if (!entity.isAlive() || !entity.getBoundingBox().intersects(bounds) || isPlayerCreativeFlying(entity)) {
                iterator.remove();
                continue;
            }

            Vec3i flow = (pushing ? direction : direction.getOpposite()).getVector();
            float speed = Math.abs(source.getSpeed());
            float sneakModifier = entity.isSneaking() ? 4096f : 512f;
            double entityDistance = VecHelper.alignedDistanceToFace(entity.getPos(), source.getAirCurrentPos(), direction);
            // entityDistanceOld should be removed eventually. Remember that entityDistanceOld cannot be 0 while entityDistance can,
            // so division by 0 must be avoided.
            double entityDistanceOld = entity.getPos().distanceTo(VecHelper.getCenterOf(source.getAirCurrentPos()));
            float acceleration = (float) (speed / sneakModifier / (entityDistanceOld / maxDistance));
            Vec3d previousMotion = entity.getVelocity();
            float maxAcceleration = 5;

            double xIn = MathHelper.clamp(flow.getX() * acceleration - previousMotion.x, -maxAcceleration, maxAcceleration);
            double yIn = MathHelper.clamp(flow.getY() * acceleration - previousMotion.y, -maxAcceleration, maxAcceleration);
            double zIn = MathHelper.clamp(flow.getZ() * acceleration - previousMotion.z, -maxAcceleration, maxAcceleration);

            entity.setVelocity(previousMotion.add(new Vec3d(xIn, yIn, zIn).multiply(1 / 8f)));
            entity.fallDistance = 0;
            if (world != null && world.isClient) {
                AllClientHandle.INSTANCE.enableClientPlayerSound(entity, MathHelper.clamp(speed / 128f * .4f, 0.01f, .4f));
            }

            if (entity instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.networkHandler.floatingTicks = 0;

            FanProcessingType processingType = getTypeAt((float) entityDistance);

            if (processingType == null)
                continue;

            if (entity instanceof ItemEntity itemEntity) {
                if (world != null && world.isClient) {
                    processingType.spawnProcessingParticles(world, entity.getPos());
                    continue;
                }
                if (FanProcessing.canProcess(itemEntity, processingType))
                    if (FanProcessing.applyProcessing(itemEntity, processingType) && source instanceof EncasedFanBlockEntity fan)
                        fan.award(AllAdvancements.FAN_PROCESSING);
                continue;
            }

            if (world != null)
                processingType.affectEntity(entity, world);
        }
    }

    public static boolean isPlayerCreativeFlying(Entity entity) {
        if (entity instanceof PlayerEntity player) {
            return player.isCreative() && player.getAbilities().flying;
        }
        return false;
    }

    public void tickAffectedHandlers() {
        for (Pair<TransportedItemStackHandlerBehaviour, FanProcessingType> pair : affectedItemHandlers) {
            TransportedItemStackHandlerBehaviour handler = pair.getKey();
            World world = handler.getWorld();
            FanProcessingType processingType = pair.getRight();
            if (processingType == null)
                continue;

            handler.handleProcessingOnAllItems(transported -> {
                if (world.isClient) {
                    processingType.spawnProcessingParticles(world, handler.getWorldPositionOf(transported));
                    return TransportedResult.doNothing();
                }
                TransportedResult applyProcessing = FanProcessing.applyProcessing(transported, world, processingType);
                if (!applyProcessing.doesNothing() && source instanceof EncasedFanBlockEntity fan)
                    fan.award(AllAdvancements.FAN_PROCESSING);
                return applyProcessing;
            });
        }
    }

    public void rebuild() {
        if (source.getSpeed() == 0) {
            maxDistance = 0;
            segments.clear();
            bounds = new Box(0, 0, 0, 0, 0, 0);
            return;
        }

        direction = source.getAirflowOriginSide();
        pushing = source.getAirFlowDirection() == direction;
        maxDistance = source.getMaxDistance();

        World world = source.getAirCurrentWorld();
        BlockPos start = source.getAirCurrentPos();
        float max = this.maxDistance;
        Direction facing = direction;
        Vec3d directionVec = Vec3d.of(facing.getVector());
        maxDistance = getFlowLimit(world, start, max, facing);

        // Determine segments with transported fluids/gases
        segments.clear();
        AirCurrentSegment currentSegment = null;
        FanProcessingType type = null;

        int limit = getLimit();
        int searchStart = pushing ? 1 : limit;
        int searchEnd = pushing ? limit : 1;
        int searchStep = pushing ? 1 : -1;
        int toOffset = pushing ? -1 : 0;

        for (int i = searchStart; i * searchStep <= searchEnd * searchStep; i += searchStep) {
            BlockPos currentPos = start.offset(direction, i);
            FanProcessingType newType = FanProcessingType.getAt(world, currentPos);
            if (newType != null) {
                type = newType;
            }
            if (currentSegment == null) {
                currentSegment = new AirCurrentSegment();
                currentSegment.startOffset = i + toOffset;
                currentSegment.type = type;
            } else if (currentSegment.type != type) {
                currentSegment.endOffset = i + toOffset;
                segments.add(currentSegment);
                currentSegment = new AirCurrentSegment();
                currentSegment.startOffset = i + toOffset;
                currentSegment.type = type;
            }
        }
        if (currentSegment != null) {
            currentSegment.endOffset = searchEnd + searchStep + toOffset;
            segments.add(currentSegment);
        }

        // Build Bounding Box
        if (maxDistance < 0.25f)
            bounds = new Box(0, 0, 0, 0, 0, 0);
        else {
            float factor = maxDistance - 1;
            Vec3d scale = directionVec.multiply(factor);
            if (factor > 0)
                bounds = new Box(start.offset(direction)).stretch(scale);
            else {
                bounds = new Box(start.offset(direction)).shrink(scale.x, scale.y, scale.z).offset(scale);
            }
        }

        findAffectedHandlers();
    }

    public static float getFlowLimit(World world, BlockPos start, float max, Direction facing) {
        for (int i = 0; i < max; i++) {
            BlockPos currentPos = start.offset(facing, i + 1);
            if (!world.isPosLoaded(currentPos)) {
                return i;
            }

            BlockState state = world.getBlockState(currentPos);
            BlockState copycatState = CopycatBlock.getMaterial(world, currentPos);
            if (shouldAlwaysPass(copycatState.isAir() ? state : copycatState)) {
                continue;
            }

            VoxelShape shape = state.getCollisionShape(world, currentPos);
            if (shape.isEmpty()) {
                continue;
            }
            if (shape == VoxelShapes.fullCube()) {
                return i;
            }
            double shapeDepth = findMaxDepth(shape, facing);
            if (shapeDepth == Double.POSITIVE_INFINITY) {
                continue;
            }
            return Math.min((float) (i + shapeDepth + 1 / 32d), max);
        }

        return max;
    }

    private static final double[][] DEPTH_TEST_COORDINATES = {{0.25, 0.25}, {0.25, 0.75}, {0.5, 0.5}, {0.75, 0.25}, {0.75, 0.75}};

    // Finds the maximum depth of the shape when traveling in the given direction.
    // The result is always positive.
    // If there is a hole, the result will be Double.POSITIVE_INFINITY.
    private static double findMaxDepth(VoxelShape shape, Direction direction) {
        Direction.Axis axis = direction.getAxis();
        Direction.AxisDirection axisDirection = direction.getDirection();
        double maxDepth = 0;

        for (double[] coordinates : DEPTH_TEST_COORDINATES) {
            double depth;
            if (axisDirection == Direction.AxisDirection.POSITIVE) {
                double min = shape.getStartingCoord(axis, coordinates[0], coordinates[1]);
                if (min == Double.POSITIVE_INFINITY) {
                    return Double.POSITIVE_INFINITY;
                }
                depth = min;
            } else {
                double max = shape.getEndingCoord(axis, coordinates[0], coordinates[1]);
                if (max == Double.NEGATIVE_INFINITY) {
                    return Double.POSITIVE_INFINITY;
                }
                depth = 1 - max;
            }

            if (depth > maxDepth) {
                maxDepth = depth;
            }
        }

        return maxDepth;
    }

    private static boolean shouldAlwaysPass(BlockState state) {
        return state.isIn(AllBlockTags.FAN_TRANSPARENT);
    }

    private int getLimit() {
        if ((float) (int) maxDistance == maxDistance) {
            return (int) maxDistance;
        } else {
            return (int) maxDistance + 1;
        }
    }

    public void findAffectedHandlers() {
        World world = source.getAirCurrentWorld();
        BlockPos start = source.getAirCurrentPos();
        affectedItemHandlers.clear();
        int limit = getLimit();
        for (int i = 1; i <= limit; i++) {
            FanProcessingType segmentType = getTypeAt(i - 1);
            for (int offset : Iterate.zeroAndOne) {
                BlockPos pos = start.offset(direction, i).down(offset);
                TransportedItemStackHandlerBehaviour behaviour = BlockEntityBehaviour.get(world, pos, TransportedItemStackHandlerBehaviour.TYPE);
                if (behaviour != null) {
                    FanProcessingType type = FanProcessingType.getAt(world, pos);
                    if (type == null)
                        type = segmentType;
                    affectedItemHandlers.add(Pair.of(behaviour, type));
                }
                if (direction.getAxis().isVertical())
                    break;
            }
        }
    }

    public void findEntities() {
        caughtEntities.clear();
        caughtEntities = source.getAirCurrentWorld().getOtherEntities(null, bounds);
    }

    @Nullable
    public FanProcessingType getTypeAt(float offset) {
        if (offset >= 0 && offset <= maxDistance) {
            if (pushing) {
                for (AirCurrentSegment airCurrentSegment : segments) {
                    if (offset <= airCurrentSegment.endOffset) {
                        return airCurrentSegment.type;
                    }
                }
            } else {
                for (AirCurrentSegment airCurrentSegment : segments) {
                    if (offset >= airCurrentSegment.endOffset) {
                        return airCurrentSegment.type;
                    }
                }
            }
        }
        return null;
    }

    public static class AirCurrentSegment {
        @Nullable
        private FanProcessingType type;
        private int startOffset;
        private int endOffset;
    }
}