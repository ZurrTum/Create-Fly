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
import com.zurrtum.create.foundation.collision.ContinuousOBBCollider;
import com.zurrtum.create.foundation.collision.Matrix3d;
import com.zurrtum.create.foundation.collision.OrientedBB;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.ClientMotionPacket;
import com.zurrtum.create.infrastructure.packet.c2s.ContraptionColliderLockPacketRequest;
import com.zurrtum.create.infrastructure.packet.c2s.TrainCollisionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.commons.lang3.tuple.MutablePair;

import java.lang.ref.WeakReference;
import java.util.*;

public class ContraptionColliderClient {
    private static MutablePair<WeakReference<AbstractContraptionEntity>, Double> safetyLock = new MutablePair<>();
    private static Map<AbstractContraptionEntity, Map<Player, Double>> remoteSafetyLocks = new WeakHashMap<>();

    static void collideEntities(AbstractContraptionEntity contraptionEntity) {
        Contraption contraption = contraptionEntity.getContraption();
        if (contraption == null)
            return;
        AABB bounds = contraptionEntity.getBoundingBox();
        if (bounds == null)
            return;

        Vec3 contraptionPosition = contraptionEntity.position();
        Vec3 contraptionMotion = contraptionPosition.subtract(contraptionEntity.getPrevPositionVec());
        Vec3 anchorVec = contraptionEntity.getAnchorVec();
        ContraptionRotationState rotation = null;

        if (safetyLock.left != null && safetyLock.left.get() == contraptionEntity)
            saveClientPlayerFromClipping(contraptionEntity, contraptionMotion);

        // After death, multiple refs to the client player may show up in the area
        boolean skipClientPlayer = false;

        Level world = contraptionEntity.level();
        List<Entity> entitiesWithinAABB = world.getEntitiesOfClass(
            Entity.class,
            bounds.inflate(2).expandTowards(0, 32, 0),
            contraptionEntity::canCollideWith
        );
        for (Entity entity : entitiesWithinAABB) {
            if (!entity.isAlive())
                continue;

            PlayerType playerType = getPlayerType(entity);
            if (playerType == PlayerType.REMOTE) {
                if (!(contraption instanceof TranslatingContraption))
                    continue;
                saveRemotePlayerFromClipping((Player) entity, contraptionEntity, contraptionMotion);
                continue;
            }

            entity.getSelfAndPassengers().forEach(e -> {
                if (e instanceof ServerPlayer playerEntity)
                    playerEntity.connection.aboveGroundTickCount = 0;
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
            Vec3 entityPosition = entity.position();
            AABB entityBounds = entity.getBoundingBox();
            Vec3 motion = entity.getDeltaMovement();
            float yawOffset = rotation.getYawOffset();
            Vec3 position = ContraptionCollider.getWorldToLocalTranslation(entity, anchorVec, rotationMatrix, yawOffset);

            // Make player 'shorter' to make it less likely to become stuck
            if (playerType == PlayerType.CLIENT && entityBounds.getYsize() > 1)
                entityBounds = entityBounds.contract(0, 2 / 16f, 0);

            motion = motion.subtract(contraptionMotion);
            motion = rotationMatrix.transform(motion);

            // Prepare entity bounds
            AABB localBB = entityBounds.move(position).inflate(1.0E-7D);

            OrientedBB obb = new OrientedBB(localBB);
            obb.setRotation(rotationMatrix);

            // Use simplified bbs when present
            final Vec3 motionCopy = motion;
            List<AABB> collidableBBs = contraption.getSimplifiedEntityColliders().orElseGet(() -> {

                // Else find 'nearby' individual block shapes to collide with
                List<AABB> bbs = new ArrayList<>();
                List<VoxelShape> potentialHits = ContraptionCollider.getPotentiallyCollidedShapes(
                    world,
                    contraption,
                    localBB.expandTowards(motionCopy)
                );
                potentialHits.forEach(shape -> bbs.addAll(shape.toAabbs()));
                return bbs;

            });

            MutableObject<Vec3> collisionResponse = new MutableObject<>(Vec3.ZERO);
            MutableObject<Vec3> normal = new MutableObject<>(Vec3.ZERO);
            MutableObject<Vec3> location = new MutableObject<>(Vec3.ZERO);
            MutableBoolean surfaceCollision = new MutableBoolean(false);
            MutableFloat temporalResponse = new MutableFloat(1);
            Vec3 obbCenter = obb.getCenter();

            // Apply separation maths
            boolean doHorizontalPass = !rotation.hasVerticalRotation();
            for (boolean horizontalPass : Iterate.trueAndFalse) {
                boolean verticalPass = !horizontalPass || !doHorizontalPass;

                for (AABB bb : collidableBBs) {
                    Vec3 currentResponse = collisionResponse.getValue();
                    Vec3 currentCenter = obbCenter.add(currentResponse);

                    if (Math.abs(currentCenter.x - bb.getCenter().x) - entityBounds.getXsize() - 1 > bb.getXsize() / 2)
                        continue;
                    if (Math.abs((currentCenter.y + motion.y) - bb.getCenter().y) - entityBounds.getYsize() - 1 > bb.getYsize() / 2)
                        continue;
                    if (Math.abs(currentCenter.z - bb.getCenter().z) - entityBounds.getZsize() - 1 > bb.getZsize() / 2)
                        continue;

                    obb.setCenter(currentCenter);
                    ContinuousOBBCollider.ContinuousSeparationManifold intersect = obb.intersect(bb, motion);

                    if (intersect == null)
                        continue;
                    if (verticalPass && surfaceCollision.isFalse())
                        surfaceCollision.setValue(intersect.isSurfaceCollision());

                    double timeOfImpact = intersect.getTimeOfImpact();
                    boolean isTemporal = timeOfImpact > 0 && timeOfImpact < 1;
                    Vec3 collidingNormal = intersect.getCollisionNormal();
                    Vec3 collisionPosition = intersect.getCollisionPosition();

                    if (!isTemporal) {
                        Vec3 separation = intersect.asSeparationVec(entity.maxUpStep());
                        if (separation != null && !separation.equals(Vec3.ZERO)) {
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
            Vec3 entityMotion = entity.getDeltaMovement();
            Vec3 entityMotionNoTemporal = entityMotion;
            Vec3 collisionNormal = normal.getValue();
            Vec3 collisionLocation = location.getValue();
            Vec3 totalResponse = collisionResponse.getValue();
            boolean hardCollision = !totalResponse.equals(Vec3.ZERO);
            boolean temporalCollision = temporalResponse.getValue() != 1;
            Vec3 motionResponse = !temporalCollision ? motion : motion.normalize().scale(motion.length() * temporalResponse.getValue());

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

            if (!collisionLocation.equals(Vec3.ZERO)) {
                collisionLocation = collisionLocation.add(entity.position().add(entity.getBoundingBox().getCenter()).scale(.5f));
                if (temporalCollision)
                    collisionLocation = collisionLocation.add(0, motionResponse.y, 0);

                BlockPos pos = BlockPos.containing(contraptionEntity.toLocalVector(entity.position(), 0));
                if (contraption.getBlocks().containsKey(pos)) {
                    BlockState blockState = contraption.getBlocks().get(pos).state();
                    if (blockState.is(BlockTags.CLIMBABLE)) {
                        surfaceCollision.setTrue();
                        totalResponse = totalResponse.add(0, .1f, 0);
                    }
                }

                pos = BlockPos.containing(contraptionEntity.toLocalVector(collisionLocation, 0));
                if (contraption.getBlocks().containsKey(pos)) {
                    BlockState blockState = contraption.getBlocks().get(pos).state();

                    MovingInteractionBehaviour movingInteractionBehaviour = contraption.getInteractors().get(pos);
                    if (movingInteractionBehaviour != null)
                        movingInteractionBehaviour.handleEntityCollision(entity, pos, contraptionEntity);

                    bounce = BlockHelper.getBounceMultiplier(blockState.getBlock());
                    slide = Math.max(0, blockState.getBlock().getFriction() - .6f);
                }
            }

            boolean hasNormal = !collisionNormal.equals(Vec3.ZERO);
            boolean anyCollision = hardCollision || temporalCollision;

            if (bounce > 0 && hasNormal && anyCollision && ContraptionCollider.bounceEntity(entity, collisionNormal, contraptionEntity, bounce)) {
                entity.level().playSound(
                    playerType == PlayerType.CLIENT ? entity : null,
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    SoundEvents.SLIME_BLOCK_FALL,
                    SoundSource.BLOCKS,
                    .5f,
                    1
                );
                continue;
            }

            if (temporalCollision) {
                double idealVerticalMotion = motionResponse.y;
                if (idealVerticalMotion != entityMotion.y) {
                    entity.setDeltaMovement(entityMotion.multiply(1, 0, 1).add(0, idealVerticalMotion, 0));
                    entityMotion = entity.getDeltaMovement();
                }
            }

            if (hardCollision) {
                double motionX = entityMotion.x();
                double motionY = entityMotion.y();
                double motionZ = entityMotion.z();
                double intersectX = totalResponse.x();
                double intersectY = totalResponse.y();
                double intersectZ = totalResponse.z();

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
                Vec3 motionIn = entityMotionNoTemporal.multiply(0, .9, 0).add(0, -.01f, 0);
                Vec3 slideNormal = collisionNormal.cross(motionIn.cross(collisionNormal)).normalize();
                Vec3 newMotion = entityMotion.multiply(.85, 0, .85)
                    .add(slideNormal.scale((.2f + slide) * motionIn.length() * slideFactor).add(0, -.1f - collisionNormal.y * .125f, 0));
                entity.setDeltaMovement(newMotion);
                entityMotion = entity.getDeltaMovement();
            }

            if (!hardCollision && surfaceCollision.isFalse())
                continue;

            Vec3 allowedMovement = ContraptionCollider.collide(totalResponse, entity);
            entity.setPos(entityPosition.x + allowedMovement.x, entityPosition.y + allowedMovement.y, entityPosition.z + allowedMovement.z);
            entityPosition = entity.position();

            entityMotion = handleDamageFromTrain(world, contraptionEntity, contraptionMotion, entity, entityMotion, playerType);

            entity.hurtMarked = true;
            Vec3 contactPointMotion = Vec3.ZERO;

            if (surfaceCollision.isTrue()) {
                contraptionEntity.registerColliding(entity);
                entity.fallDistance = 0;
                for (Entity rider : entity.getIndirectPassengers())
                    if (getPlayerType(rider) == PlayerType.CLIENT)
                        Minecraft.getInstance().player.connection.send(new ClientMotionPacket(rider.getDeltaMovement(), true, 0));
                boolean canWalk = bounce != 0 || slide == 0;
                if (canWalk || !rotation.hasVerticalRotation()) {
                    if (canWalk)
                        entity.setOnGround(true);
                    if (entity instanceof ItemEntity)
                        entityMotion = entityMotion.multiply(.5f, 1, .5f);
                }
                contactPointMotion = contraptionEntity.getContactPointMotion(entityPosition);
                allowedMovement = ContraptionCollider.collide(contactPointMotion, entity);
                entity.setPos(entityPosition.x + allowedMovement.x, entityPosition.y, entityPosition.z + allowedMovement.z);
            }

            entity.setDeltaMovement(entityMotion);

            if (playerType != PlayerType.CLIENT)
                continue;

            double d0 = entity.getX() - entity.xo - contactPointMotion.x;
            double d1 = entity.getZ() - entity.zo - contactPointMotion.z;
            float limbSwing = Mth.sqrt((float) (d0 * d0 + d1 * d1)) * 4.0F;
            if (limbSwing > 1.0F)
                limbSwing = 1.0F;
            Minecraft.getInstance().player.connection.send(new ClientMotionPacket(entityMotion, true, limbSwing));

            if (entity.onGround() && contraption instanceof TranslatingContraption) {
                safetyLock.setLeft(new WeakReference<>(contraptionEntity));
                safetyLock.setRight(entity.getY() - contraptionEntity.getY());
            }
        }

    }

    private static Vec3 handleDamageFromTrain(
        Level world,
        AbstractContraptionEntity contraptionEntity,
        Vec3 contraptionMotion,
        Entity entity,
        Vec3 entityMotion,
        PlayerType playerType
    ) {
        if (!(contraptionEntity instanceof CarriageContraptionEntity cce))
            return entityMotion;
        if (!entity.onGround())
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

        Vec3 diffMotion = contraptionMotion.subtract(entity.getDeltaMovement());

        if (diffMotion.length() <= 0.35f || contraptionMotion.length() <= 0.35f)
            return entityMotion;

        DamageSource source = AllDamageSources.get(world).runOver(contraptionEntity);
        double damage = diffMotion.length();
        if (entity.getType().getCategory() == MobCategory.MONSTER)
            damage *= 2;

        if (entity instanceof Player p && (p.isCreative() || p.isSpectator()))
            return entityMotion;

        if (playerType == PlayerType.CLIENT) {
            ((LocalPlayer) entity).connection.send(new TrainCollisionPacket((int) (damage * 16), contraptionEntity.getId()));
            world.playSound(entity, entity.blockPosition(), SoundEvents.PLAYER_ATTACK_CRIT, SoundSource.NEUTRAL, 1, .75f);
        }

        Vec3 added = entityMotion.add(contraptionMotion.multiply(1, 0, 1).normalize().add(0, .25, 0).scale(damage * 4)).add(diffMotion);

        return VecHelper.clamp(added, 3);
    }

    private static int packetCooldown = 0;

    private static void saveClientPlayerFromClipping(AbstractContraptionEntity contraptionEntity, Vec3 contraptionMotion) {
        LocalPlayer entity = Minecraft.getInstance().player;
        if (entity.isPassenger())
            return;

        double prevDiff = safetyLock.right;
        double currentDiff = entity.getY() - contraptionEntity.getY();
        double motion = contraptionMotion.subtract(entity.getDeltaMovement()).y;
        double trend = Math.signum(currentDiff - prevDiff);

        ClientPacketListener handler = entity.connection;
        if (handler.getOnlinePlayers().size() > 1) {
            if (packetCooldown > 0)
                packetCooldown--;
            if (packetCooldown == 0) {
                handler.send(new ContraptionColliderLockPacketRequest(contraptionEntity.getId(), currentDiff));
                packetCooldown = 3;
            }
        }

        if (trend == 0)
            return;
        if (trend == Math.signum(motion))
            return;

        double speed = contraptionMotion.multiply(0, 1, 0).lengthSqr();
        if (trend > 0 && speed < 0.1)
            return;
        if (speed < 0.05)
            return;

        if (!savePlayerFromClipping(entity, contraptionEntity, contraptionMotion, prevDiff))
            safetyLock.setLeft(null);
    }

    public static void lockPacketReceived(int contraptionId, int remotePlayerId, double suggestedOffset) {
        ClientLevel level = Minecraft.getInstance().level;
        if (!(level.getEntity(contraptionId) instanceof ControlledContraptionEntity contraptionEntity))
            return;
        if (!(level.getEntity(remotePlayerId) instanceof RemotePlayer player))
            return;
        remoteSafetyLocks.computeIfAbsent(contraptionEntity, $ -> new WeakHashMap<>()).put(player, suggestedOffset);
    }

    private static void saveRemotePlayerFromClipping(Player entity, AbstractContraptionEntity contraptionEntity, Vec3 contraptionMotion) {
        if (entity.isPassenger())
            return;

        Map<Player, Double> locksOnThisContraption = remoteSafetyLocks.getOrDefault(contraptionEntity, Collections.emptyMap());
        double prevDiff = locksOnThisContraption.getOrDefault(entity, entity.getY() - contraptionEntity.getY());
        if (!savePlayerFromClipping(entity, contraptionEntity, contraptionMotion, prevDiff))
            if (locksOnThisContraption.containsKey(entity))
                locksOnThisContraption.remove(entity);
    }

    private static boolean savePlayerFromClipping(
        Player entity,
        AbstractContraptionEntity contraptionEntity,
        Vec3 contraptionMotion,
        double yStartOffset
    ) {
        AABB bb = entity.getBoundingBox().deflate(1 / 4f, 0, 1 / 4f);
        double shortestDistance = Double.MAX_VALUE;
        double yStart = entity.maxUpStep() + contraptionEntity.getY() + yStartOffset;
        double rayLength = Math.max(5, Math.abs(entity.getY() - yStart));

        for (int rayIndex = 0; rayIndex < 4; rayIndex++) {
            Vec3 start = new Vec3(rayIndex / 2 == 0 ? bb.minX : bb.maxX, yStart, rayIndex % 2 == 0 ? bb.minZ : bb.maxZ);
            Vec3 end = start.add(0, -rayLength, 0);

            BlockHitResult hitResult = ContraptionHandlerClient.rayTraceContraption(start, end, contraptionEntity);
            if (hitResult == null)
                continue;

            Vec3 hit = contraptionEntity.toGlobalVector(hitResult.getLocation(), 1);
            double hitDiff = start.y - hit.y;
            if (shortestDistance > hitDiff)
                shortestDistance = hitDiff;
        }

        if (shortestDistance > rayLength)
            return false;
        entity.setPos(entity.getX(), yStart - shortestDistance, entity.getZ());
        return true;
    }

    private static PlayerType getPlayerType(Entity entity) {
        if (!(entity instanceof Player))
            return PlayerType.NONE;
        return entity instanceof LocalPlayer ? PlayerType.CLIENT : PlayerType.REMOTE;
    }
}
