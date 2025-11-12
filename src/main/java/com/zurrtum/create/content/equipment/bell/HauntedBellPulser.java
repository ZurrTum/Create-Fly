package com.zurrtum.create.content.equipment.bell;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.infrastructure.packet.s2c.SoulPulseEffectPacket;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;

public class HauntedBellPulser {

    public static final int DISTANCE = 3;
    public static final int RECHARGE_TICKS = 8;
    public static final int WARMUP_TICKS = 10;

    public static final Cache<UUID, IntAttached<Entity>> WARMUP = CacheBuilder.newBuilder().expireAfterAccess(250, TimeUnit.MILLISECONDS).build();

    public static void hauntedBellCreatesPulse(ServerPlayer player) {
        if (player.isSpectator())
            return;
        if (!player.isHolding(item -> item.is(AllItems.HAUNTED_BELL)))
            return;

        boolean firstPulse = false;

        try {
            IntAttached<Entity> ticker = WARMUP.get(player.getUUID(), () -> IntAttached.with(WARMUP_TICKS, player));
            firstPulse = ticker.getFirst() == 1;
            ticker.decrement();
            if (!ticker.isOrBelowZero())
                return;
        } catch (ExecutionException ignored) {
        }

        long gameTime = player.level().getGameTime();
        if ((firstPulse || gameTime % RECHARGE_TICKS != 0) && player.level() instanceof ServerLevel serverLevel)
            sendPulse(serverLevel, player.blockPosition(), DISTANCE, false);
    }

    public static void sendPulse(ServerLevel world, BlockPos pos, int distance, boolean canOverlap) {
        ChunkPos chunk = world.getChunk(pos).getPos();
        SoulPulseEffectPacket packet = new SoulPulseEffectPacket(pos, distance, canOverlap);
        for (ServerPlayer player : world.getChunkSource().chunkMap.getPlayers(chunk, false)) {
            player.connection.send(packet);
        }
    }

}