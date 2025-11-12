package com.zurrtum.create.client.content.contraptions;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.WorldAttached;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.trains.entity.TrainRelocatorClient;
import com.zurrtum.create.client.foundation.utility.RaycastHelper;
import com.zurrtum.create.client.foundation.utility.RaycastHelper.PredicateTraceResult;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.infrastructure.packet.c2s.ContraptionInteractionPacket;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectLists;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.StructureBlockInfo;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.*;

public class ContraptionHandlerClient {

    /* Global map of loaded contraptions */

    public static WorldAttached<Map<Integer, WeakReference<AbstractContraptionEntity>>> loadedContraptions;
    static WorldAttached<List<AbstractContraptionEntity>> queuedAdditions;

    static {
        loadedContraptions = new WorldAttached<>($ -> new HashMap<>());
        queuedAdditions = new WorldAttached<>($ -> ObjectLists.synchronize(new ObjectArrayList<>()));
    }

    public static void tick(Level world) {
        Map<Integer, WeakReference<AbstractContraptionEntity>> map = loadedContraptions.get(world);
        List<AbstractContraptionEntity> queued = queuedAdditions.get(world);

        for (AbstractContraptionEntity contraptionEntity : queued)
            map.put(contraptionEntity.getId(), new WeakReference<>(contraptionEntity));
        queued.clear();

        Collection<WeakReference<AbstractContraptionEntity>> values = map.values();
        for (Iterator<WeakReference<AbstractContraptionEntity>> iterator = values.iterator(); iterator.hasNext(); ) {
            WeakReference<AbstractContraptionEntity> weakReference = iterator.next();
            AbstractContraptionEntity contraptionEntity = weakReference.get();
            if (contraptionEntity == null || !contraptionEntity.isAliveOrStale()) {
                iterator.remove();
                continue;
            }
            if (!contraptionEntity.isAlive()) {
                contraptionEntity.staleTicks--;
                continue;
            }

            ContraptionColliderClient.collideEntities(contraptionEntity);
        }
    }

    public static void addSpawnedContraptionsToCollisionList(Entity entity, Level world) {
        if (entity instanceof AbstractContraptionEntity)
            queuedAdditions.get(world).add((AbstractContraptionEntity) entity);
    }

    public static void entitiesWhoJustDismountedGetSentToTheRightLocation(LivingEntity entityLiving, Level world) {
        if (!world.isClientSide())
            return;

        AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.get(entityLiving).ifPresent(position -> {
            if (entityLiving.getVehicle() == null)
                entityLiving.absSnapTo(position.x, position.y, position.z, entityLiving.getYRot(), entityLiving.getXRot());
            AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.set(entityLiving, Optional.empty());
            entityLiving.setOnGround(false);
        });
    }

    public static void preventRemotePlayersWalkingAnimations(RemotePlayer remotePlayer) {
        int lastOverride = AllSynchedDatas.LAST_OVERRIDE_LIMB_SWING_UPDATE.get(remotePlayer);
        if (lastOverride == -1)
            return;

        if (lastOverride > 5) {
            AllSynchedDatas.LAST_OVERRIDE_LIMB_SWING_UPDATE.set(remotePlayer, -1);
            AllSynchedDatas.OVERRIDE_LIMB_SWING.set(remotePlayer, 0F);
            return;
        }
        AllSynchedDatas.LAST_OVERRIDE_LIMB_SWING_UPDATE.set(remotePlayer, lastOverride + 1);

        float limbSwing = AllSynchedDatas.OVERRIDE_LIMB_SWING.get(remotePlayer);
        remotePlayer.xo = remotePlayer.getX() - (limbSwing / 4);
        remotePlayer.zo = remotePlayer.getZ();
    }

    public static boolean rightClickingOnContraptionsGetsHandledLocally(Minecraft mc, InteractionHand hand) {
        LocalPlayer player = mc.player;

        if (player == null)
            return false;
        if (player.isSpectator())
            return false;
        if (mc.level == null)
            return false;

        Couple<Vec3> rayInputs = getRayInputs(mc, player);
        Vec3 origin = rayInputs.getFirst();
        Vec3 target = rayInputs.getSecond();
        AABB aabb = new AABB(origin, target).inflate(16);

        Collection<WeakReference<AbstractContraptionEntity>> contraptions = ContraptionHandlerClient.loadedContraptions.get(mc.level).values();

        double bestDistance = Double.MAX_VALUE;
        BlockHitResult bestResult = null;
        AbstractContraptionEntity bestEntity = null;

        for (WeakReference<AbstractContraptionEntity> ref : contraptions) {
            AbstractContraptionEntity contraptionEntity = ref.get();
            if (contraptionEntity == null)
                continue;
            if (!contraptionEntity.getBoundingBox().intersects(aabb))
                continue;

            BlockHitResult rayTraceResult = rayTraceContraption(origin, target, contraptionEntity);
            if (rayTraceResult == null)
                continue;

            double distance = contraptionEntity.toGlobalVector(rayTraceResult.getLocation(), 1).distanceTo(origin);
            if (distance > bestDistance)
                continue;

            bestResult = rayTraceResult;
            bestDistance = distance;
            bestEntity = contraptionEntity;
        }

        if (bestResult == null)
            return false;

        Direction face = bestResult.getDirection();
        BlockPos pos = bestResult.getBlockPos();

        if (bestEntity.handlePlayerInteraction(player, pos, face, hand)) {
            player.connection.send(new ContraptionInteractionPacket(bestEntity, hand, pos, face));
        } else
            handleSpecialInteractions(bestEntity, player, pos, face, hand);
        return true;
    }

    private static boolean handleSpecialInteractions(
        AbstractContraptionEntity contraptionEntity,
        Player player,
        BlockPos localPos,
        Direction side,
        InteractionHand interactionHand
    ) {
        if (player.getItemInHand(interactionHand).is(AllItems.WRENCH) && contraptionEntity instanceof CarriageContraptionEntity car)
            return TrainRelocatorClient.carriageWrenched(car.toGlobalVector(VecHelper.getCenterOf(localPos), 1), car);
        return false;
    }

    public static Couple<Vec3> getRayInputs(Minecraft mc, LocalPlayer player) {
        Vec3 origin = player.getEyePosition();
        double reach = player.blockInteractionRange();
        if (mc.hitResult != null && mc.hitResult.getLocation() != null)
            reach = Math.min(mc.hitResult.getLocation().distanceTo(origin), reach);
        Vec3 target = RaycastHelper.getTraceTarget(player, reach, origin);
        return Couple.create(origin, target);
    }

    @Nullable
    public static BlockHitResult rayTraceContraption(Vec3 origin, Vec3 target, AbstractContraptionEntity contraptionEntity) {
        Vec3 localOrigin = contraptionEntity.toLocalVector(origin, 1);
        Vec3 localTarget = contraptionEntity.toLocalVector(target, 1);
        Contraption contraption = contraptionEntity.getContraption();

        MutableObject<BlockHitResult> mutableResult = new MutableObject<>();
        PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(
            localOrigin, localTarget, p -> {
                for (Direction d : Iterate.directions) {
                    if (d == Direction.UP)
                        continue;
                    BlockPos pos = d == Direction.DOWN ? p : p.relative(d);
                    StructureBlockInfo blockInfo = contraption.getBlocks().get(pos);
                    if (blockInfo == null)
                        continue;
                    BlockState state = blockInfo.state();
                    VoxelShape raytraceShape = state.getShape(contraption.getContraptionWorld(), BlockPos.ZERO.below());
                    if (raytraceShape.isEmpty())
                        continue;
                    if (contraption.isHiddenInPortal(pos))
                        continue;
                    BlockHitResult rayTrace = raytraceShape.clip(localOrigin, localTarget, pos);
                    if (rayTrace != null) {
                        mutableResult.setValue(rayTrace);
                        return true;
                    }
                }
                return false;
            }
        );

        if (predicateResult == null || predicateResult.missed())
            return null;

        BlockHitResult rayTraceResult = mutableResult.getValue();
        return rayTraceResult;
    }

}
