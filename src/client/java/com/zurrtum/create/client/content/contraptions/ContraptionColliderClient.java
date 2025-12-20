package com.zurrtum.create.client.content.contraptions;

import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.api.behaviour.interaction.MovingInteractionBehaviour;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.*;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity.ContraptionRotationState;
import com.zurrtum.create.content.contraptions.ContraptionCollider.PlayerType;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.foundation.collision.CollisionList;
import com.zurrtum.create.foundation.collision.ContinuousOBBCollider;
import com.zurrtum.create.foundation.collision.Matrix3d;
import com.zurrtum.create.foundation.collision.OrientedBB;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.ClientMotionPacket;
import com.zurrtum.create.infrastructure.packet.c2s.ContraptionColliderLockPacketRequest;
import com.zurrtum.create.infrastructure.packet.c2s.TrainCollisionPacket;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.MutablePair;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ContraptionColliderClient {
    private static MutablePair<WeakReference<AbstractContraptionEntity>, Double> safetyLock = new MutablePair<>();
    private static Map<AbstractContraptionEntity, Map<PlayerEntity, Double>> remoteSafetyLocks = new WeakHashMap<>();

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

        if (safetyLock.left != null && safetyLock.left.get() == contraptionEntity)
            saveClientPlayerFromClipping(contraptionEntity, contraptionMotion);

        // After death, multiple refs to the client player may show up in the area
        boolean skipClientPlayer = false;

        World world = contraptionEntity.getWorld();
        List<Entity> entitiesWithinAABB = world.getEntitiesByClass(Entity.class, bounds.expand(2).stretch(0, 32, 0), contraptionEntity::collidesWith);
        for (Entity entity : entitiesWithinAABB) {
            if (!entity.isAlive())
                continue;

            PlayerType playerType = getPlayerType(entity);
            if (playerType == PlayerType.REMOTE) {
                if (!(contraption instanceof TranslatingContraption))
                    continue;
                saveRemotePlayerFromClipping((PlayerEntity) entity, contraptionEntity, contraptionMotion);
                continue;
            }

            entity.streamSelfAndPassengers().forEach(e -> {
                if (e instanceof ServerPlayerEntity playerEntity)
                    playerEntity.networkHandler.floatingTicks = 0;
            });

            if (playerType == PlayerType.CLIENT) {
                if (skipClientPlayer)
                    continue;
                else
                    skipClientPlayer = true;
            }

            // Init matrix
            if (rotation == null)
                rotation = contraptionEntity.getRotationState();
            Matrix3d rotationMatrix = rotation.asMatrix();

            // Transform entity position and motion to local space
            Vec3d entityPosition = entity.getPos();
            Box entityBounds = entity.getBoundingBox();
            Vec3d motion = entity.getVelocity();
            float yawOffset = rotation.getYawOffset();
            Vec3d position = ContraptionCollider.getWorldToLocalTranslation(entity, anchorVec, rotationMatrix, yawOffset);

            // Make player 'shorter' to make it less likely to become stuck
            if (playerType == PlayerType.CLIENT && entityBounds.getLengthY() > 1)
                entityBounds = entityBounds.shrink(0, 2 / 16f, 0);

            motion = motion.subtract(contraptionMotion);
            motion = rotationMatrix.transform(motion);

            // Prepare entity bounds
            Box localBB = entityBounds.offset(position).expand(1.0E-7D);

            OrientedBB obb = new OrientedBB(localBB);
            obb.setRotation(rotationMatrix);

            // Use simplified bbs when present
            final Vec3d motionCopy = motion;
            CollisionList collidableBBs = contraption.getSimplifiedEntityColliders().orElseGet(() -> {

                // Else find 'nearby' individual block shapes to collide with
                CollisionList out = new CollisionList();
                var populate = new CollisionList.Populate(out);
                ContraptionCollider.getPotentiallyCollidedShapes(world, contraption, localBB.stretch(motionCopy), populate);
                return out;

            });

            MutableObject<Vec3d> collisionResponse = new MutableObject<>(Vec3d.ZERO);
            MutableObject<Vec3d> normal = new MutableObject<>(Vec3d.ZERO);
            MutableObject<Vec3d> location = new MutableObject<>(Vec3d.ZERO);
            MutableBoolean surfaceCollision = new MutableBoolean(false);
            MutableFloat temporalResponse = new MutableFloat(1);
            Vec3d obbCenter = obb.getCenter();

            // Apply separation maths
            boolean doHorizontalPass = !rotation.hasVerticalRotation();
            for (boolean horizontalPass : Iterate.trueAndFalse) {
                boolean verticalPass = !horizontalPass || !doHorizontalPass;

                for (int bbIdx = 0; bbIdx < collidableBBs.size; ++bbIdx) {
                    Vec3d currentResponse = collisionResponse.getValue();
                    Vec3d currentCenter = obbCenter.add(currentResponse);

                    if (Math.abs(currentCenter.x - collidableBBs.centerX[bbIdx]) - entityBounds.getLengthX() - 1 > collidableBBs.extentsX[bbIdx])
                        continue;
                    if (Math.abs((currentCenter.y + motion.y) - collidableBBs.centerY[bbIdx]) - entityBounds.getLengthY() - 1 > collidableBBs.extentsY[bbIdx])
                        continue;
                    if (Math.abs(currentCenter.z - collidableBBs.centerZ[bbIdx]) - entityBounds.getLengthZ() - 1 > collidableBBs.extentsZ[bbIdx])
                        continue;

                    obb.setCenter(currentCenter);
                    ContinuousOBBCollider.ContinuousSeparationManifold intersect = obb.intersect(collidableBBs, bbIdx, motion);

                    if (intersect == null)
                        continue;
                    if (verticalPass && surfaceCollision.isFalse())
                        surfaceCollision.setValue(intersect.isSurfaceCollision());

                    double timeOfImpact = intersect.getTimeOfImpact();
                    boolean isTemporal = timeOfImpact > 0 && timeOfImpact < 1;
                    Vec3d collidingNormal = intersect.getCollisionNormal();
                    Vec3d collisionPosition = intersect.getCollisionPosition();

                    if (!isTemporal) {
                        Vec3d separation = intersect.asSeparationVec(entity.getStepHeight());
                        if (separation != null && !separation.equals(Vec3d.ZERO)) {
                            collisionResponse.setValue(currentResponse.add(separation));
                            timeOfImpact = 0;
                        }
                    }

                    boolean nearest = timeOfImpact >= 0 && temporalResponse.getValue() > timeOfImpact;
                    if (collidingNormal != null && nearest)
                        normal.setValue(collidingNormal);
                    if (collisionPosition != null && nearest)
                        location.setValue(collisionPosition);

                    if (isTemporal) {
                        if (temporalResponse.getValue() > timeOfImpact)
                            temporalResponse.setValue(timeOfImpact);
                    }
                }

                if (verticalPass)
                    break;

                boolean noVerticalMotionResponse = temporalResponse.getValue() == 1;
                boolean noVerticalCollision = collisionResponse.getValue().y == 0;
                if (noVerticalCollision && noVerticalMotionResponse)
                    break;

                // Re-run collisions with horizontal offset
                collisionResponse.setValue(collisionResponse.getValue().multiply(129 / 128f, 0, 129 / 128f));
            }

            // Resolve collision
            Vec3d entityMotion = entity.getVelocity();
            Vec3d entityMotionNoTemporal = entityMotion;
            Vec3d collisionNormal = normal.getValue();
            Vec3d collisionLocation = location.getValue();
            Vec3d totalResponse = collisionResponse.getValue();
            boolean hardCollision = !totalResponse.equals(Vec3d.ZERO);
            boolean temporalCollision = temporalResponse.getValue() != 1;
            Vec3d motionResponse = !temporalCollision ? motion : motion.normalize().multiply(motion.length() * temporalResponse.getValue());

            rotationMatrix.transpose();
            motionResponse = rotationMatrix.transform(motionResponse).add(contraptionMotion);
            totalResponse = rotationMatrix.transform(totalResponse);
            totalResponse = VecHelper.rotate(totalResponse, yawOffset, Direction.Axis.Y);
            collisionNormal = rotationMatrix.transform(collisionNormal);
            collisionNormal = VecHelper.rotate(collisionNormal, yawOffset, Direction.Axis.Y);
            collisionNormal = collisionNormal.normalize();
            collisionLocation = rotationMatrix.transform(collisionLocation);
            collisionLocation = VecHelper.rotate(collisionLocation, yawOffset, Direction.Axis.Y);
            rotationMatrix.transpose();

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
                        surfaceCollision.setTrue();
                        totalResponse = totalResponse.add(0, .1f, 0);
                    }
                }

                pos = BlockPos.ofFloored(contraptionEntity.toLocalVector(collisionLocation, 0));
                if (contraption.getBlocks().containsKey(pos)) {
                    BlockState blockState = contraption.getBlocks().get(pos).state();

                    MovingInteractionBehaviour movingInteractionBehaviour = contraption.getInteractors().get(pos);
                    if (movingInteractionBehaviour != null)
                        movingInteractionBehaviour.handleEntityCollision(entity, pos, contraptionEntity);

                    bounce = BlockHelper.getBounceMultiplier(blockState.getBlock());
                    slide = Math.max(0, blockState.getBlock().getSlipperiness() - .6f);
                }
            }

            boolean hasNormal = !collisionNormal.equals(Vec3d.ZERO);
            boolean anyCollision = hardCollision || temporalCollision;

            if (bounce > 0 && hasNormal && anyCollision && ContraptionCollider.bounceEntity(entity, collisionNormal, contraptionEntity, bounce)) {
                entity.getWorld().playSound(
                    playerType == PlayerType.CLIENT ? entity : null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.BLOCK_SLIME_BLOCK_FALL,
                    SoundCategory.BLOCKS,
                    .5f,
                    1
                );
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

            if (!hardCollision && surfaceCollision.isFalse())
                continue;

            Vec3d allowedMovement = ContraptionCollider.collide(totalResponse, entity);
            entity.setPosition(entityPosition.x + allowedMovement.x, entityPosition.y + allowedMovement.y, entityPosition.z + allowedMovement.z);
            entityPosition = entity.getPos();

            entityMotion = handleDamageFromTrain(world, contraptionEntity, contraptionMotion, entity, entityMotion, playerType);

            entity.velocityModified = true;
            Vec3d contactPointMotion = Vec3d.ZERO;

            if (surfaceCollision.isTrue()) {
                contraptionEntity.registerColliding(entity);
                entity.fallDistance = 0;
                for (Entity rider : entity.getPassengersDeep())
                    if (getPlayerType(rider) == PlayerType.CLIENT)
                        MinecraftClient.getInstance().player.networkHandler.sendPacket(new ClientMotionPacket(rider.getVelocity(), true, 0));
                boolean canWalk = bounce != 0 || slide == 0;
                if (canWalk || !rotation.hasVerticalRotation()) {
                    if (canWalk)
                        entity.setOnGround(true);
                    if (entity instanceof ItemEntity)
                        entityMotion = entityMotion.multiply(.5f, 1, .5f);
                }
                contactPointMotion = contraptionEntity.getContactPointMotion(entityPosition);
                allowedMovement = ContraptionCollider.collide(contactPointMotion, entity);
                entity.setPosition(entityPosition.x + allowedMovement.x, entityPosition.y, entityPosition.z + allowedMovement.z);
            }

            entity.setVelocity(entityMotion);

            if (playerType != PlayerType.CLIENT)
                continue;

            double d0 = entity.getX() - entity.lastX - contactPointMotion.x;
            double d1 = entity.getZ() - entity.lastZ - contactPointMotion.z;
            float limbSwing = MathHelper.sqrt((float) (d0 * d0 + d1 * d1)) * 4.0F;
            if (limbSwing > 1.0F)
                limbSwing = 1.0F;
            MinecraftClient.getInstance().player.networkHandler.sendPacket(new ClientMotionPacket(entityMotion, true, limbSwing));

            if (entity.isOnGround() && contraption instanceof TranslatingContraption) {
                safetyLock.setLeft(new WeakReference<>(contraptionEntity));
                safetyLock.setRight(entity.getY() - contraptionEntity.getY());
            }
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

        if (playerType == PlayerType.CLIENT) {
            ((ClientPlayerEntity) entity).networkHandler.sendPacket(new TrainCollisionPacket((int) (damage * 16), contraptionEntity.getId()));
            world.playSound(entity, entity.getBlockPos(), SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.NEUTRAL, 1, .75f);
        }

        Vec3d added = entityMotion.add(contraptionMotion.multiply(1, 0, 1).normalize().add(0, .25, 0).multiply(damage * 4)).add(diffMotion);

        return VecHelper.clamp(added, 3);
    }

    private static int packetCooldown = 0;

    private static void saveClientPlayerFromClipping(AbstractContraptionEntity contraptionEntity, Vec3d contraptionMotion) {
        ClientPlayerEntity entity = MinecraftClient.getInstance().player;
        if (entity.hasVehicle())
            return;

        double prevDiff = safetyLock.right;
        double currentDiff = entity.getY() - contraptionEntity.getY();
        double motion = contraptionMotion.subtract(entity.getVelocity()).y;
        double trend = Math.signum(currentDiff - prevDiff);

        ClientPlayNetworkHandler handler = entity.networkHandler;
        if (handler.getPlayerList().size() > 1) {
            if (packetCooldown > 0)
                packetCooldown--;
            if (packetCooldown == 0) {
                handler.sendPacket(new ContraptionColliderLockPacketRequest(contraptionEntity.getId(), currentDiff));
                packetCooldown = 3;
            }
        }

        if (trend == 0)
            return;
        if (trend == Math.signum(motion))
            return;

        double speed = contraptionMotion.multiply(0, 1, 0).lengthSquared();
        if (trend > 0 && speed < 0.1)
            return;
        if (speed < 0.05)
            return;

        if (!savePlayerFromClipping(entity, contraptionEntity, contraptionMotion, prevDiff))
            safetyLock.setLeft(null);
    }

    public static void lockPacketReceived(int contraptionId, int remotePlayerId, double suggestedOffset) {
        ClientWorld level = MinecraftClient.getInstance().world;
        if (!(level.getEntityById(contraptionId) instanceof ControlledContraptionEntity contraptionEntity))
            return;
        if (!(level.getEntityById(remotePlayerId) instanceof OtherClientPlayerEntity player))
            return;
        remoteSafetyLocks.computeIfAbsent(contraptionEntity, $ -> new WeakHashMap<>()).put(player, suggestedOffset);
    }

    private static void saveRemotePlayerFromClipping(PlayerEntity entity, AbstractContraptionEntity contraptionEntity, Vec3d contraptionMotion) {
        if (entity.hasVehicle())
            return;

        Map<PlayerEntity, Double> locksOnThisContraption = remoteSafetyLocks.getOrDefault(contraptionEntity, Collections.emptyMap());
        double prevDiff = locksOnThisContraption.getOrDefault(entity, entity.getY() - contraptionEntity.getY());
        if (!savePlayerFromClipping(entity, contraptionEntity, contraptionMotion, prevDiff))
            if (locksOnThisContraption.containsKey(entity))
                locksOnThisContraption.remove(entity);
    }

    private static boolean savePlayerFromClipping(
        PlayerEntity entity,
        AbstractContraptionEntity contraptionEntity,
        Vec3d contraptionMotion,
        double yStartOffset
    ) {
        Box bb = entity.getBoundingBox().contract(1 / 4f, 0, 1 / 4f);
        double shortestDistance = Double.MAX_VALUE;
        double yStart = entity.getStepHeight() + contraptionEntity.getY() + yStartOffset;
        double rayLength = Math.max(5, Math.abs(entity.getY() - yStart));

        for (int rayIndex = 0; rayIndex < 4; rayIndex++) {
            Vec3d start = new Vec3d(rayIndex / 2 == 0 ? bb.minX : bb.maxX, yStart, rayIndex % 2 == 0 ? bb.minZ : bb.maxZ);
            Vec3d end = start.add(0, -rayLength, 0);

            BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(start, end, contraptionEntity);
            if (hitResult == null)
                continue;

            Vec3d hit = contraptionEntity.toGlobalVector(hitResult.getPos(), 1);
            double hitDiff = start.y - hit.y;
            if (shortestDistance > hitDiff)
                shortestDistance = hitDiff;
        }

        if (shortestDistance > rayLength)
            return false;
        entity.setPosition(entity.getX(), yStart - shortestDistance, entity.getZ());
        return true;
    }

    private static PlayerType getPlayerType(Entity entity) {
        if (!(entity instanceof PlayerEntity))
            return PlayerType.NONE;
        return entity instanceof ClientPlayerEntity ? PlayerType.CLIENT : PlayerType.REMOTE;
    }
}
