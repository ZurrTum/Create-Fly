package com.zurrtum.create.content.logistics.packagerLink;

import com.mojang.serialization.Codec;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class LogisticsNetworkSavedData extends PersistentState {
    public static final Codec<LogisticsNetworkSavedData> CODEC = Codec.list(LogisticsNetwork.CODEC)
        .xmap(LogisticsNetworkSavedData::createMap, LogisticsNetworkSavedData::toList)
        .xmap(LogisticsNetworkSavedData::new, LogisticsNetworkSavedData::getLogisticsNetworks);
    private static final PersistentStateType<LogisticsNetworkSavedData> TYPE = new PersistentStateType<>(
        "create_logistics",
        LogisticsNetworkSavedData::new,
        CODEC,
        null
    );

    private final Map<UUID, LogisticsNetwork> logisticsNetworks;

    public Map<UUID, LogisticsNetwork> getLogisticsNetworks() {
        return logisticsNetworks;
    }

    private LogisticsNetworkSavedData() {
        logisticsNetworks = new HashMap<>();
    }

    private LogisticsNetworkSavedData(Map<UUID, LogisticsNetwork> logisticsNetworks) {
        this.logisticsNetworks = logisticsNetworks;
    }

    private static Map<UUID, LogisticsNetwork> createMap(List<LogisticsNetwork> list) {
        Map<UUID, LogisticsNetwork> logisticsNetworks = new HashMap<>();
        list.forEach(network -> logisticsNetworks.put(network.id, network));
        return logisticsNetworks;
    }

    private static List<LogisticsNetwork> toList(Map<UUID, LogisticsNetwork> logisticsNetworks) {
        return logisticsNetworks.values().stream().toList();
    }

    public static LogisticsNetworkSavedData load(MinecraftServer server) {
        return server.getOverworld().getPersistentStateManager().getOrCreate(TYPE);
    }
}
