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
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
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

    public static void tick(World world) {
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

    public static void addSpawnedContraptionsToCollisionList(Entity entity, World world) {
        if (entity instanceof AbstractContraptionEntity)
            queuedAdditions.get(world).add((AbstractContraptionEntity) entity);
    }

    public static void entitiesWhoJustDismountedGetSentToTheRightLocation(LivingEntity entityLiving, World world) {
        if (!world.isClient())
            return;

        AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.get(entityLiving).ifPresent(position -> {
            if (entityLiving.getVehicle() == null)
                entityLiving.updatePositionAndAngles(position.x, position.y, position.z, entityLiving.getYaw(), entityLiving.getPitch());
            AllSynchedDatas.CONTRAPTION_DISMOUNT_LOCATION.set(entityLiving, Optional.empty());
            entityLiving.setOnGround(false);
        });
    }

    public static void preventRemotePlayersWalkingAnimations(OtherClientPlayerEntity remotePlayer) {
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
        remotePlayer.lastX = remotePlayer.getX() - (limbSwing / 4);
        remotePlayer.lastZ = remotePlayer.getZ();
    }

    public static boolean rightClickingOnContraptionsGetsHandledLocally(MinecraftClient mc, Hand hand) {
        ClientPlayerEntity player = mc.player;

        if (player == null)
            return false;
        if (player.isSpectator())
            return false;
        if (mc.world == null)
            return false;

        Couple<Vec3d> rayInputs = getRayInputs(mc, player);
        Vec3d origin = rayInputs.getFirst();
        Vec3d target = rayInputs.getSecond();
        Box aabb = new Box(origin, target).expand(16);

        Collection<WeakReference<AbstractContraptionEntity>> contraptions = ContraptionHandlerClient.loadedContraptions.get(mc.world).values();

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

            double distance = contraptionEntity.toGlobalVector(rayTraceResult.getPos(), 1).distanceTo(origin);
            if (distance > bestDistance)
                continue;

            bestResult = rayTraceResult;
            bestDistance = distance;
            bestEntity = contraptionEntity;
        }

        if (bestResult == null)
            return false;

        Direction face = bestResult.getSide();
        BlockPos pos = bestResult.getBlockPos();

        if (bestEntity.handlePlayerInteraction(player, pos, face, hand)) {
            player.networkHandler.sendPacket(new ContraptionInteractionPacket(bestEntity, hand, pos, face));
        } else
            handleSpecialInteractions(bestEntity, player, pos, face, hand);
        return true;
    }

    private static boolean handleSpecialInteractions(
        AbstractContraptionEntity contraptionEntity,
        PlayerEntity player,
        BlockPos localPos,
        Direction side,
        Hand interactionHand
    ) {
        if (player.getStackInHand(interactionHand).isOf(AllItems.WRENCH) && contraptionEntity instanceof CarriageContraptionEntity car)
            return TrainRelocatorClient.carriageWrenched(car.toGlobalVector(VecHelper.getCenterOf(localPos), 1), car);
        return false;
    }

    public static Couple<Vec3d> getRayInputs(MinecraftClient mc, ClientPlayerEntity player) {
        Vec3d origin = RaycastHelper.getTraceOrigin(player);
        double reach = player.getBlockInteractionRange();
        if (mc.crosshairTarget != null && mc.crosshairTarget.getPos() != null)
            reach = Math.min(mc.crosshairTarget.getPos().distanceTo(origin), reach);
        Vec3d target = RaycastHelper.getTraceTarget(player, reach, origin);
        return Couple.create(origin, target);
    }

    @Nullable
    public static BlockHitResult rayTraceContraption(Vec3d origin, Vec3d target, AbstractContraptionEntity contraptionEntity) {
        Vec3d localOrigin = contraptionEntity.toLocalVector(origin, 1);
        Vec3d localTarget = contraptionEntity.toLocalVector(target, 1);
        Contraption contraption = contraptionEntity.getContraption();

        MutableObject<BlockHitResult> mutableResult = new MutableObject<>();
        PredicateTraceResult predicateResult = RaycastHelper.rayTraceUntil(
            localOrigin, localTarget, p -> {
                for (Direction d : Iterate.directions) {
                    if (d == Direction.UP)
                        continue;
                    BlockPos pos = d == Direction.DOWN ? p : p.offset(d);
                    StructureBlockInfo blockInfo = contraption.getBlocks().get(pos);
                    if (blockInfo == null)
                        continue;
                    BlockState state = blockInfo.state();
                    VoxelShape raytraceShape = state.getOutlineShape(contraption.getContraptionWorld(), BlockPos.ORIGIN.down());
                    if (raytraceShape.isEmpty())
                        continue;
                    if (contraption.isHiddenInPortal(pos))
                        continue;
                    BlockHitResult rayTrace = raytraceShape.raycast(localOrigin, localTarget, pos);
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
