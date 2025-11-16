package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.ProtocolInfoBuilder;
import net.minecraft.network.protocol.SimpleUnboundProtocol;
import net.minecraft.network.protocol.UnboundProtocol;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.GameProtocols;
import net.minecraft.network.protocol.game.ServerGamePacketListener;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(GameProtocols.class)
public class GameProtocolsMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/ProtocolInfoBuilder;clientboundProtocol(Lnet/minecraft/network/ConnectionProtocol;Ljava/util/function/Consumer;)Lnet/minecraft/network/protocol/SimpleUnboundProtocol;"))
    private static SimpleUnboundProtocol<ClientGamePacketListener, RegistryFriendlyByteBuf> addS2CPacket(
        ConnectionProtocol type,
        Consumer<ProtocolInfoBuilder<ClientGamePacketListener, RegistryFriendlyByteBuf, Unit>> registrar,
        Operation<SimpleUnboundProtocol<ClientGamePacketListener, RegistryFriendlyByteBuf>> original
    ) {
        return original.call(
            type, (Consumer<ProtocolInfoBuilder<ClientGamePacketListener, RegistryFriendlyByteBuf, Unit>>) (builder -> {
                registrar.accept(builder);
                AllPackets.S2C.forEach(builder::addPacket);
            })
        );
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/protocol/ProtocolInfoBuilder;contextServerboundProtocol(Lnet/minecraft/network/ConnectionProtocol;Ljava/util/function/Consumer;)Lnet/minecraft/network/protocol/UnboundProtocol;"))
    private static UnboundProtocol<ServerGamePacketListener, RegistryFriendlyByteBuf, GameProtocols.Context> addC2SPacket(
        ConnectionProtocol type,
        Consumer<ProtocolInfoBuilder<ServerGamePacketListener, RegistryFriendlyByteBuf, GameProtocols.Context>> registrar,
        Operation<UnboundProtocol<ServerGamePacketListener, RegistryFriendlyByteBuf, GameProtocols.Context>> original
    ) {
        return original.call(
            type, (Consumer<ProtocolInfoBuilder<ServerGamePacketListener, RegistryFriendlyByteBuf, GameProtocols.Context>>) (builder -> {
                registrar.accept(builder);
                AllPackets.C2S.forEach(builder::addPacket);
            })
        );
    }
}
