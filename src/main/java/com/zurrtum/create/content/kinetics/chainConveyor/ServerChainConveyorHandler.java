package com.zurrtum.create.content.kinetics.chainConveyor;

import com.zurrtum.create.infrastructure.packet.s2c.ClientboundChainConveyorRidingPacket;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;

import java.util.Map;
import java.util.UUID;

public class ServerChainConveyorHandler {


    public static Object2IntMap<UUID> hangingPlayers = new Object2IntOpenHashMap<>();

    public static int ticks;

    public static void handleTTLPacket(MinecraftServer server, PlayerEntity player) {
        int count = hangingPlayers.size();
        hangingPlayers.put(player.getUuid(), 20);

        if (hangingPlayers.size() != count)
            sync(server);
    }

    public static void handleStopRidingPacket(MinecraftServer server, PlayerEntity player) {
        if (hangingPlayers.removeInt(player.getUuid()) != 0)
            sync(server);
    }

    public static void tick(MinecraftServer server) {
        ticks++;

        int before = hangingPlayers.size();

        ObjectIterator<Object2IntMap.Entry<UUID>> iterator = hangingPlayers.object2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = iterator.next();
            int newTTL = entry.getValue() - 1;
            if (newTTL <= 0) {
                iterator.remove();
            } else {
                entry.setValue(newTTL);
            }
        }

        int after = hangingPlayers.size();

        if (ticks % 10 != 0 && before == after)
            return;

        sync(server);

    }

    public static void sync(MinecraftServer server) {
        server.getPlayerManager().sendToAll(new ClientboundChainConveyorRidingPacket(hangingPlayers.keySet()));
    }

}
