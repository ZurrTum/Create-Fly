package com.zurrtum.create.content.contraptions;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity.ContraptionRotationState;
import com.zurrtum.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.zurrtum.create.content.kinetics.base.BlockBreakingMovementBehaviour;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.foundation.collision.CollisionList;
import com.zurrtum.create.foundation.collision.CollisionList.Populate;
import com.zurrtum.create.foundation.collision.ContinuousOBBCollider;
import com.zurrtum.create.foundation.collision.Matrix3d;
import com.zurrtum.create.foundation.collision.OrientedBB;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.block.CocoaBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes.BoxConsumer;
import net.minecraft.world.World;

import java.util.List;

public class ContraptionCollider {

    public enum PlayerType {
        NONE,
        CLIENT,
        REMOTE,
        SERVER
    }

    static void collideEntities(AbstractContraptionEntity contraptionEntity) {
        Contraption contraption = contraptionEntity.getContraption();
        if (contraption == null)
            return;
        Box bounds = contraptionEntity.getBoundingBox();
        if (bounds == null)
            return;

        Vec3d contraptionPosition = contraptionEntity.getPos();
        Vec3d contraptionMotion = contraptionPosition.subtract(contraptionEntity.getPrevPositionVec());
        Vec3d anchorVec = contraptionEntity.getAnchorVec();
        ContraptionRotationState rotation = null;

        CollisionList denseViableColliders = new CollisionList();

        World world = contraptionEntity.getWorld();
        List<Entity> entitiesWithinAABB = world.getEntitiesByClass(Entity.class, bounds.expand(2).stretch(0, 32, 0), contraptionEntity::collidesWith);
        for (Entity entity : entitiesWithinAABB) {
            if (!entity.isAlive() || world.getTickManager().shouldSkipTick(entity))
                continue;

            PlayerType playerType = getPlayerType(entity);

            entity.streamSelfAndPassengers().forEach(e -> {
                if (e instanceof ServerPlayerEntity playerEntity)
                    playerEntity.networkHandler.floatingTicks = 0;
            });

            if (playerType == PlayerType.SERVER)
                continue;

            // Init matrix
            if (rotation == null)
                rotation = contraptionEntity.getRotationState();
            Matrix3d rotationMatrix = rotation.asMatrix();

            // Transform entity position and motion to local space
            Vec3d entityPosition = entity.getPos();
            Box entityBounds = entity.getBoundingBox();
            Vec3d motion = entity.getVelocity();
            float yawOffset = rotation.getYawOffset();
            Vec3d position = getWorldToLocalTranslation(entity, anchorVec, rotationMatrix, yawOffset);

            motion = motion.subtract(contraptionMotion);
            motion = rotationMatrix.transform(motion);

            // Prepare entity bounds
            Box localBB = entityBounds.offset(position).expand(1.0E-7D);

            OrientedBB obb = new OrientedBB(localBB);
            obb.setRotation(rotationMatrix);

            // Use simplified bbs when present
            CollisionList collidableBBs = contraption.getSimplifiedEntityColliders();
            if (collidableBBs == null) {
                // Else find 'nearby' individual block shapes to collide with
                collidableBBs = new CollisionList();

                getPotentiallyCollidedShapes(world, contraption, localBB.stretch(motion), new Populate(collidableBBs));
            }

            var collisionResult = ContinuousOBBCollider.collideMany(
                collidableBBs,
                denseViableColliders,
                obb,
                motion,
                entity.getStepHeight(),
                !rotation.hasVerticalRotation()
            );

            // Resolve collision
            Vec3d entityMotion = entity.getVelocity();
            Vec3d entityMotionNoTemporal = entityMotion;
            Vec3d collisionNormal = collisionResult.normal;
            Vec3d collisionLocation = collisionResult.location;
            Vec3d totalResponse = collisionResult.collisionResponse;
            boolean surfaceCollision = collisionResult.surfaceCollision;
            boolean hardCollision = !totalResponse.equals(Vec3d.ZERO);
            boolean temporalCollision = collisionResult.temporalResponse != 1;
            Vec3d motionResponse = !temporalCollision ? motion : motion.normalize().multiply(motion.length() * collisionResult.temporalResponse);

            motionResponse = rotationMatrix.transformTransposed(motionResponse).add(contraptionMotion);
            totalResponse = rotationMatrix.transformTransposed(totalResponse);
            totalResponse = VecHelper.rotate(totalResponse, yawOffset, Axis.Y);
            collisionNormal = rotationMatrix.transformTransposed(collisionNormal);
            collisionNormal = VecHelper.rotate(collisionNormal, yawOffset, Axis.Y);
            collisionNormal = collisionNormal.normalize();
            collisionLocation = rotationMatrix.transformTransposed(collisionLocation);
            collisionLocation = VecHelper.rotate(collisionLocation, yawOffset, Axis.Y);

            double bounce = 0;
            double slide = 0;

            if (!collisionLocation.equals(Vec3d.ZERO)) {
                collisionLocation = collisionLocation.add(entity.getPos().add(entity.getBoundingBox().getCenter()).multiply(.5f));
                if (temporalCollision)
                    collisionLocation = collisionLocation.add(0, motionResponse.y, 0);

                BlockPos pos = BlockPos.ofFloored(contraptionEntity.toLocalVector(entity.getPos(), 0));
                if (contraption.getBlocks().containsKey(pos)) {
                    BlockState blockState = contraption.getBlocks().get(pos).state();
                    if (blockState.isIn(BlockTags.CLIMBABLE)) {
                        surfaceCollision = true;
                        totalResponse = totalResponse.add(0, .1f, 0);
                    }
                }

                pos = BlockPos.ofFloored(contraptionEntity.toLocalVector(collisionLocation, 0));
                if (contraption.getBlocks().containsKey(pos)) {
                    BlockState blockState = contraption.getBlocks().get(pos).state();

                    MovingInteractionBehaviour movingInteractionBehaviour = contraption.interactors.get(pos);
                    if (movingInteractionBehaviour != null)
                        movingInteractionBehaviour.handleEntityCollision(entity, pos, contraptionEntity);

                    bounce = BlockHelper.getBounceMultiplier(blockState.getBlock());
                    slide = Math.max(0, blockState.getBlock().getSlipperiness() - .6f);
                }
            }

            boolean hasNormal = !collisionNormal.equals(Vec3d.ZERO);
            boolean anyCollision = hardCollision || temporalCollision;

            if (bounce > 0 && hasNormal && anyCollision && bounceEntity(entity, collisionNormal, contraptionEntity, bounce)) {
                entity.getWorld()
                    .playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BLOCK_SLIME_BLOCK_FALL, SoundCategory.BLOCKS, .5f, 1);
                continue;
            }

            if (temporalCollision) {
                double idealVerticalMotion = motionResponse.y;
                if (idealVerticalMotion != entityMotion.y) {
                    entity.setVelocity(entityMotion.multiply(1, 0, 1).add(0, idealVerticalMotion, 0));
                    entityMotion = entity.getVelocity();
                }
            }

            if (hardCollision) {
                double motionX = entityMotion.getX();
                double motionY = entityMotion.getY();
                double motionZ = entityMotion.getZ();
                double intersectX = totalResponse.getX();
                double intersectY = totalResponse.getY();
                double intersectZ = totalResponse.getZ();

                double horizonalEpsilon = 1 / 128f;
                if (motionX != 0 && Math.abs(intersectX) > horizonalEpsilon && motionX > 0 == intersectX < 0)
                    entityMotion = entityMotion.multiply(0, 1, 1);
                if (motionY != 0 && intersectY != 0 && motionY > 0 == intersectY < 0)
                    entityMotion = entityMotion.multiply(1, 0, 1).add(0, contraptionMotion.y, 0);
                if (motionZ != 0 && Math.abs(intersectZ) > horizonalEpsilon && motionZ > 0 == intersectZ < 0)
                    entityMotion = entityMotion.multiply(1, 1, 0);

            }

            if (bounce == 0 && slide > 0 && hasNormal && anyCollision && rotation.hasVerticalRotation()) {
                double slideFactor = collisionNormal.multiply(1, 0, 1).length() * 1.25f;
                Vec3d motionIn = entityMotionNoTemporal.multiply(0, .9, 0).add(0, -.01f, 0);
                Vec3d slideNormal = collisionNormal.crossProduct(motionIn.crossProduct(collisionNormal)).normalize();
                Vec3d newMotion = entityMotion.multiply(.85, 0, .85)
                    .add(slideNormal.multiply((.2f + slide) * motionIn.length() * slideFactor).add(0, -.1f - collisionNormal.y * .125f, 0));
                entity.setVelocity(newMotion);
                entityMotion = entity.getVelocity();
            }

            if (!hardCollision && !surfaceCollision)
                continue;

            Vec3d allowedMovement = collide(totalResponse, entity);
            entity.setPosition(entityPosition.x + allowedMovement.x, entityPosition.y + allowedMovement.y, entityPosition.z + allowedMovement.z);
            entityPosition = entity.getPos();

            entityMotion = handleDamageFromTrain(world, contraptionEntity, contraptionMotion, entity, entityMotion, playerType);

            entity.velocityModified = true;
            Vec3d contactPointMotion;

            if (surfaceCollision) {
                contraptionEntity.registerColliding(entity);
                entity.fallDistance = 0;
                boolean canWalk = bounce != 0 || slide == 0;
                if (canWalk || !rotation.hasVerticalRotation()) {
                    if (canWalk)
                        entity.setOnGround(true);
                    if (entity instanceof ItemEntity)
                        entityMotion = entityMotion.multiply(.5f, 1, .5f);
                }
                contactPointMotion = contraptionEntity.getContactPointMotion(entityPosition);
                allowedMovement = collide(contactPointMotion, entity);
                entity.setPosition(entityPosition.x + allowedMovement.x, entityPosition.y, entityPosition.z + allowedMovement.z);
            }
            entity.setVelocity(entityMotion);
        }
    }

    private static Vec3d handleDamageFromTrain(
        World world,
        AbstractContraptionEntity contraptionEntity,
        Vec3d contraptionMotion,
        Entity entity,
        Vec3d entityMotion,
        PlayerType playerType
    ) {
        if (!(contraptionEntity instanceof CarriageContraptionEntity cce))
            return entityMotion;
        if (!entity.isOnGround())
            return entityMotion;

        if (AllSynchedDatas.CONTRAPTION_GROUNDED.get(entity)) {
            AllSynchedDatas.CONTRAPTION_GROUNDED.set(entity, false);
            return entityMotion;
        }

        if (cce.collidingEntities.containsKey(entity))
            return entityMotion;
        if (entity instanceof ItemEntity)
            return entityMotion;
        if (cce.nonDamageTicks != 0)
            return entityMotion;
        if (!AllConfigs.server().trains.trainsCauseDamage.get())
            return entityMotion;

        Vec3d diffMotion = contraptionMotion.subtract(entity.getVelocity());

        if (diffMotion.length() <= 0.35f || contraptionMotion.length() <= 0.35f)
            return entityMotion;

        DamageSource source = AllDamageSources.get(world).runOver(contraptionEntity);
        double damage = diffMotion.length();
        if (entity.getType().getSpawnGroup() == SpawnGroup.MONSTER)
            damage *= 2;

        if (entity instanceof PlayerEntity p && (p.isCreative() || p.isSpectator()))
            return entityMotion;

        if (playerType != PlayerType.CLIENT) {
            ServerWorld serverWorld = (ServerWorld) world;
            entity.damage(serverWorld, source, (int) (damage * 16));
            serverWorld.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.NEUTRAL, 1, .75f);
            if (!entity.isAlive())
                contraptionEntity.getControllingPlayer()
                    .ifPresent(uuid -> AllAdvancements.TRAIN_ROADKILL.trigger((ServerPlayerEntity) serverWorld.getPlayerByUuid(uuid)));
        }

        Vec3d added = entityMotion.add(contraptionMotion.multiply(1, 0, 1).normalize().add(0, .25, 0).multiply(damage * 4)).add(diffMotion);

        return VecHelper.clamp(added, 3);
    }

    public static boolean bounceEntity(Entity entity, Vec3d normal, AbstractContraptionEntity contraption, double factor) {
        if (factor == 0)
            return false;
        if (entity.bypassesLandingEffects())
            return false;

        Vec3d contactPointMotion = contraption.getContactPointMotion(entity.getPos());
        Vec3d motion = entity.getVelocity().subtract(contactPointMotion);
        Vec3d deltav = normal.multiply(factor * 2 * motion.dotProduct(normal));
        if (deltav.dotProduct(deltav) < 0.1f)
            return false;
        entity.setVelocity(entity.getVelocity().subtract(deltav));
        return true;
    }

    public static Vec3d getWorldToLocalTranslation(Entity entity, Vec3d anchorVec, Matrix3d rotationMatrix, float yawOffset) {
        Vec3d entityPosition = entity.getPos();
        Vec3d centerY = new Vec3d(0, entity.getBoundingBox().getLengthY() / 2, 0);
        Vec3d position = entityPosition;
        position = position.add(centerY);
        position = worldToLocalPos(position, anchorVec, rotationMatrix, yawOffset);
        position = position.subtract(centerY);
        position = position.subtract(entityPosition);
        return position;
    }

    public static Vec3d worldToLocalPos(Vec3d worldPos, AbstractContraptionEntity contraptionEntity) {
        return worldToLocalPos(worldPos, contraptionEntity.getAnchorVec(), contraptionEntity.getRotationState());
    }

    public static Vec3d worldToLocalPos(Vec3d worldPos, Vec3d anchorVec, ContraptionRotationState rotation) {
        return worldToLocalPos(worldPos, anchorVec, rotation.asMatrix(), rotation.getYawOffset());
    }

    public static Vec3d worldToLocalPos(Vec3d worldPos, Vec3d anchorVec, Matrix3d rotationMatrix, float yawOffset) {
        Vec3d localPos = worldPos;
        localPos = localPos.subtract(anchorVec);
        localPos = localPos.subtract(VecHelper.CENTER_OF_ORIGIN);
        localPos = VecHelper.rotate(localPos, -yawOffset, Axis.Y);
        localPos = rotationMatrix.transform(localPos);
        localPos = localPos.add(VecHelper.CENTER_OF_ORIGIN);
        return localPos;
    }

    /**
     * From Entity#collide
     **/
    public static Vec3d collide(Vec3d p_20273_, Entity e) {
        Box aabb = e.getBoundingBox();
        List<VoxelShape> list = e.getWorld().getEntityCollisions(e, aabb.stretch(p_20273_));
        Vec3d vec3 = p_20273_.lengthSquared() == 0.0D ? p_20273_ : Entity.adjustMovementForCollisions(e, p_20273_, aabb, e.getWorld(), list);
        boolean flag = p_20273_.x != vec3.x;
        boolean flag1 = p_20273_.y != vec3.y;
        boolean flag2 = p_20273_.z != vec3.z;
        boolean flag3 = flag1 && p_20273_.y < 0.0D;
        if (e.getStepHeight() > 0.0F && flag3 && (flag || flag2)) {
            Vec3d vec31 = Entity.adjustMovementForCollisions(e, new Vec3d(p_20273_.x, e.getStepHeight(), p_20273_.z), aabb, e.getWorld(), list);
            Vec3d vec32 = Entity.adjustMovementForCollisions(
                e,
                new Vec3d(0.0D, e.getStepHeight(), 0.0D),
                aabb.stretch(p_20273_.x, 0.0D, p_20273_.z),
                e.getWorld(),
                list
            );
            if (vec32.y < (double) e.getStepHeight()) {
                Vec3d vec33 = Entity.adjustMovementForCollisions(e, new Vec3d(p_20273_.x, 0.0D, p_20273_.z), aabb.offset(vec32), e.getWorld(), list)
                    .add(vec32);
                if (vec33.horizontalLengthSquared() > vec31.horizontalLengthSquared()) {
                    vec31 = vec33;
                }
            }

            if (vec31.horizontalLengthSquared() > vec3.horizontalLengthSquared()) {
                return vec31.add(Entity.adjustMovementForCollisions(
                    e,
                    new Vec3d(0.0D, -vec31.y + p_20273_.y, 0.0D),
                    aabb.offset(vec31),
                    e.getWorld(),
                    list
                ));
            }
        }

        return vec3;
    }

    private static PlayerType getPlayerType(Entity entity) {
        return entity instanceof PlayerEntity ? PlayerType.SERVER : PlayerType.NONE;
    }

    public static void getPotentiallyCollidedShapes(World world, Contraption contraption, Box localBB, BoxConsumer out) {
        double height = localBB.getLengthY();
        double width = localBB.getLengthX();
        double horizontalFactor = (height > width && width != 0) ? height / width : 1;
        double verticalFactor = (width > height && height != 0) ? width / height : 1;
        Box blockScanBB = localBB.expand(0.5f);
        blockScanBB = blockScanBB.expand(horizontalFactor, verticalFactor, horizontalFactor);

        BlockPos min = BlockPos.ofFloored(blockScanBB.minX, blockScanBB.minY, blockScanBB.minZ);
        BlockPos max = BlockPos.ofFloored(blockScanBB.maxX, blockScanBB.maxY, blockScanBB.maxZ);

        for (BlockPos p : BlockPos.iterate(min, max)) {
            if (contraption.blocks.containsKey(p) && !contraption.isHiddenInPortal(p)) {
                StructureBlockInfo info = contraption.getBlocks().get(p);

                BlockState blockState = info.state();
                BlockPos pos = info.pos();

                VoxelShape collisionShape = blockState.getCollisionShape(world, p).offset(pos.getX(), pos.getY(), pos.getZ());

                if (!collisionShape.isEmpty()) {
                    collisionShape.forEachBox(out);
                }
            }
        }
    }

    public static boolean collideBlocks(AbstractContraptionEntity contraptionEntity) {
        if (!contraptionEntity.supportsTerrainCollision())
            return false;

        World world = contraptionEntity.getWorld();
        Vec3d motion = contraptionEntity.getVelocity();
        TranslatingContraption contraption = (TranslatingContraption) contraptionEntity.getContraption();
        Box bounds = contraptionEntity.getBoundingBox();
        Vec3d position = contraptionEntity.getPos();
        BlockPos gridPos = BlockPos.ofFloored(position);

        if (contraption == null)
            return false;
        if (bounds == null)
            return false;
        if (motion.equals(Vec3d.ZERO))
            return false;

        Direction movementDirection = Direction.getFacing(motion.x, motion.y, motion.z);

        // Blocks in the world
        if (movementDirection.getDirection() == AxisDirection.POSITIVE)
            gridPos = gridPos.offset(movementDirection);
        if (isCollidingWithWorld(world, contraption, gridPos, movementDirection))
            return true;

        // Other moving Contraptions
        for (ControlledContraptionEntity otherContraptionEntity : world.getEntitiesByClass(
            ControlledContraptionEntity.class,
            bounds.expand(1),
            e -> !e.equals(contraptionEntity)
        )) {

            if (!otherContraptionEntity.supportsTerrainCollision())
                continue;

            Vec3d otherMotion = otherContraptionEntity.getVelocity();
            TranslatingContraption otherContraption = (TranslatingContraption) otherContraptionEntity.getContraption();
            Box otherBounds = otherContraptionEntity.getBoundingBox();
            Vec3d otherPosition = otherContraptionEntity.getPos();

            if (otherContraption == null)
                return false;
            if (otherBounds == null)
                return false;

            if (!bounds.offset(motion).intersects(otherBounds.offset(otherMotion)))
                continue;

            for (BlockPos colliderPos : contraption.getOrCreateColliders(world, movementDirection)) {
                colliderPos = colliderPos.add(gridPos).subtract(BlockPos.ofFloored(otherPosition));
                if (!otherContraption.getBlocks().containsKey(colliderPos))
                    continue;
                return true;
            }
        }

        return false;
    }

    public static boolean isCollidingWithWorld(World world, TranslatingContraption contraption, BlockPos anchor, Direction movementDirection) {
        for (BlockPos pos : contraption.getOrCreateColliders(world, movementDirection)) {
            BlockPos colliderPos = pos.add(anchor);

            if (!world.isPosLoaded(colliderPos))
                return true;

            BlockState collidedState = world.getBlockState(colliderPos);
            StructureBlockInfo blockInfo = contraption.getBlocks().get(pos);
            boolean emptyCollider = collidedState.getCollisionShape(world, pos).isEmpty();

            if (collidedState.getBlock() instanceof CocoaBlock)
                continue;

            MovementBehaviour movementBehaviour = MovementBehaviour.REGISTRY.get(blockInfo.state());
            if (movementBehaviour != null) {
                if (movementBehaviour instanceof BlockBreakingMovementBehaviour behaviour) {
                    if (!behaviour.canBreak(world, colliderPos, collidedState) && !emptyCollider)
                        return true;
                    continue;
                }
                if (movementBehaviour instanceof HarvesterMovementBehaviour harvesterMovementBehaviour) {
                    if (!harvesterMovementBehaviour.isValidCrop(world, colliderPos, collidedState) && !harvesterMovementBehaviour.isValidOther(
                        world,
                        colliderPos,
                        collidedState
                    ) && !emptyCollider)
                        return true;
                    continue;
                }
            }

            if (collidedState.isOf(AllBlocks.PULLEY_MAGNET) && pos.equals(BlockPos.ZERO) && movementDirection == Direction.UP)
                continue;
            if (!collidedState.isReplaceable() && !emptyCollider) {
                return true;
            }

        }
        return false;
    }

}
