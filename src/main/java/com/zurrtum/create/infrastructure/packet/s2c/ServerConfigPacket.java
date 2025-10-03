package com.zurrtum.create.infrastructure.packet.s2c;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.zurrtum.create.AllPackets;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.PacketType;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public record ServerConfigPacket(byte[] data) implements Packet<ClientConfigurationPacketListener> {
    public static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final PacketCodec<ByteBuf, ServerConfigPacket> CODEC = PacketCodecs.byteArray(Integer.MAX_VALUE)
        .xmap(ServerConfigPacket::new, ServerConfigPacket::data);
    public static byte[] CACHE;

    @Nullable
    public static Packet<?> create() {
        if (CACHE == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (GZIPOutputStream gzos = new GZIPOutputStream(baos)) {
                String json = GSON.toJson(AllConfigs.server().builder.object);
                gzos.write(json.getBytes(StandardCharsets.UTF_8));
            } catch (Exception ignore) {
                return null;
            }
            CACHE = baos.toByteArray();
        }
        return new ServerConfigPacket(CACHE);
    }

    @Override
    public void apply(ClientConfigurationPacketListener listener) {
        try (GZIPInputStream gzis = new GZIPInputStream(new ByteArrayInputStream(data))) {
            String json = new String(gzis.readAllBytes(), StandardCharsets.UTF_8);
            AllConfigs.server().reload(GSON.fromJson(json, JsonObject.class));
        } catch (Exception ignore) {
        }
    }

    @Override
    public PacketType<ServerConfigPacket> getPacketType() {
        return AllPackets.SERVER_CONFIG;
    }
}
