package com.zurrtum.create.infrastructure.config;

import com.zurrtum.create.infrastructure.packet.s2c.ServerConfigPacket;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerConfigurationTask;

import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public record SyncConfigTask(Consumer<Key> callback) implements ServerPlayerConfigurationTask {
    public static Key KEY = new Key(MOD_ID);

    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        Packet<?> packet = ServerConfigPacket.create();
        if (packet != null) {
            sender.accept(packet);
        }
        callback.accept(KEY);
    }

    @Override
    public Key getKey() {
        return KEY;
    }
}
