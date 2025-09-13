package com.zurrtum.create.foundation.utility;

import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.s2c.ServerSpeedPacket;
import net.minecraft.server.MinecraftServer;

public class ServerSpeedProvider {
    private static int serverTimer = 0;
    private static int syncInterval = getSyncInterval();
    private static ServerSpeedPacket speedPacket = new ServerSpeedPacket(syncInterval);

    public static void serverTick(MinecraftServer server) {
        serverTimer++;
        int syncInterval = getSyncInterval();
        if (serverTimer > syncInterval) {
            if (syncInterval != ServerSpeedProvider.syncInterval) {
                ServerSpeedProvider.syncInterval = syncInterval;
                speedPacket = new ServerSpeedPacket(syncInterval);
            }
            server.getPlayerManager().sendToAll(speedPacket);
            serverTimer = 0;
        }
    }

    public static Integer getSyncInterval() {
        return AllConfigs.server().tickrateSyncTimer.get();
    }
}
