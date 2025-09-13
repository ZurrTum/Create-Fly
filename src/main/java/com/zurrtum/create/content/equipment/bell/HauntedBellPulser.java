package com.zurrtum.create.content.equipment.bell;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.infrastructure.packet.s2c.SoulPulseEffectPacket;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class HauntedBellPulser {

    public static final int DISTANCE = 3;
    public static final int RECHARGE_TICKS = 8;
    public static final int WARMUP_TICKS = 10;

    public static final Cache<UUID, IntAttached<Entity>> WARMUP = CacheBuilder.newBuilder().expireAfterAccess(250, TimeUnit.MILLISECONDS).build();

    public static void hauntedBellCreatesPulse(ServerPlayerEntity player) {
        if (player.isSpectator())
            return;
        if (!player.isHolding(item -> item.isOf(AllItems.HAUNTED_BELL)))
            return;

        boolean firstPulse = false;

        try {
            IntAttached<Entity> ticker = WARMUP.get(player.getUuid(), () -> IntAttached.with(WARMUP_TICKS, player));
            firstPulse = ticker.getFirst() == 1;
            ticker.decrement();
            if (!ticker.isOrBelowZero())
                return;
        } catch (ExecutionException ignored) {
        }

        long gameTime = player.getWorld().getTime();
        if ((firstPulse || gameTime % RECHARGE_TICKS != 0) && player.getWorld() instanceof ServerWorld serverLevel)
            sendPulse(serverLevel, player.getBlockPos(), DISTANCE, false);
    }

    public static void sendPulse(ServerWorld world, BlockPos pos, int distance, boolean canOverlap) {
        ChunkPos chunk = world.getChunk(pos).getPos();
        SoulPulseEffectPacket packet = new SoulPulseEffectPacket(pos, distance, canOverlap);
        for (ServerPlayerEntity player : world.getChunkManager().chunkLoadingManager.getPlayersWatchingChunk(chunk, false)) {
            player.networkHandler.sendPacket(packet);
        }
    }

}