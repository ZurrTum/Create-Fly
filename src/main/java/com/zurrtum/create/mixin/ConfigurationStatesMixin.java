package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.AllPackets;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.listener.ClientConfigurationPacketListener;
import net.minecraft.network.state.ConfigurationStates;
import net.minecraft.network.state.NetworkStateBuilder;
import net.minecraft.network.state.NetworkStateFactory;
import net.minecraft.util.Unit;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(ConfigurationStates.class)
public class ConfigurationStatesMixin {
    @WrapOperation(method = "<clinit>", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/state/NetworkStateBuilder;s2c(Lnet/minecraft/network/NetworkPhase;Ljava/util/function/Consumer;)Lnet/minecraft/network/state/NetworkStateFactory;"))
    private static NetworkStateFactory<ClientConfigurationPacketListener, RegistryByteBuf> addS2CPacket(
        NetworkPhase type,
        Consumer<NetworkStateBuilder<ClientConfigurationPacketListener, RegistryByteBuf, Unit>> registrar,
        Operation<NetworkStateFactory<ClientConfigurationPacketListener, RegistryByteBuf>> original
    ) {
        return original.call(
            type, (Consumer<NetworkStateBuilder<ClientConfigurationPacketListener, RegistryByteBuf, Unit>>) (builder -> {
                registrar.accept(builder);
                AllPackets.S2C_CONFIG.forEach(builder::add);
            })
        );
    }
}
