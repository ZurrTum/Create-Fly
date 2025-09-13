package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.listener.ServerPlayPacketListener;
import net.minecraft.network.state.ContextAwareNetworkStateFactory;
import net.minecraft.network.state.NetworkStateBuilder;
import net.minecraft.network.state.NetworkStateFactory;
import net.minecraft.network.state.PlayStateFactories;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(PlayStateFactories.class)
public class PlayStateFactoriesMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/state/NetworkStateBuilder;s2c(Lnet/minecraft/network/NetworkPhase;Ljava/util/function/Consumer;)Lnet/minecraft/network/state/NetworkStateFactory;"))
    private static NetworkStateFactory<ClientPlayPacketListener, RegistryByteBuf> addS2CPacket(
        NetworkPhase type,
        Consumer<NetworkStateBuilder<ClientPlayPacketListener, RegistryByteBuf, Unit>> registrar,
        Operation<NetworkStateFactory<ClientPlayPacketListener, RegistryByteBuf>> original
    ) {
        return original.call(
            type, (Consumer<NetworkStateBuilder<ClientPlayPacketListener, RegistryByteBuf, Unit>>) (builder -> {
                registrar.accept(builder);
                AllPackets.S2C.forEach(builder::add);
            })
        );
    }

    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/state/NetworkStateBuilder;contextAwareC2S(Lnet/minecraft/network/NetworkPhase;Ljava/util/function/Consumer;)Lnet/minecraft/network/state/ContextAwareNetworkStateFactory;"))
    private static ContextAwareNetworkStateFactory<ServerPlayPacketListener, RegistryByteBuf, PlayStateFactories.PacketCodecModifierContext> addC2SPacket(
        NetworkPhase type,
        Consumer<NetworkStateBuilder<ServerPlayPacketListener, RegistryByteBuf, PlayStateFactories.PacketCodecModifierContext>> registrar,
        Operation<ContextAwareNetworkStateFactory<ServerPlayPacketListener, RegistryByteBuf, PlayStateFactories.PacketCodecModifierContext>> original
    ) {
        return original.call(
            type,
            (Consumer<NetworkStateBuilder<ServerPlayPacketListener, RegistryByteBuf, PlayStateFactories.PacketCodecModifierContext>>) (builder -> {
                registrar.accept(builder);
                AllPackets.C2S.forEach(builder::add);
            })
        );
    }
}
