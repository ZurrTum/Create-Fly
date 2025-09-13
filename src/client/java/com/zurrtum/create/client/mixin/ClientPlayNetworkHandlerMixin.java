package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.CommandDispatcher;
import com.zurrtum.create.client.flywheel.impl.FlwCommands;
import com.zurrtum.create.foundation.blockEntity.SyncedBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.command.CommandSource;
import net.minecraft.network.packet.s2c.play.CommandTreeS2CPacket;
import net.minecraft.storage.ReadView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Shadow
    private CommandDispatcher<CommandSource> commandDispatcher;

    @Inject(method = "onCommandTree(Lnet/minecraft/network/packet/s2c/play/CommandTreeS2CPacket;)V", at = @At("TAIL"))
    private void addCommand(CommandTreeS2CPacket packet, CallbackInfo ci) {
        FlwCommands.registerClientCommands(commandDispatcher);
    }

    @WrapOperation(method = "method_38542(Lnet/minecraft/network/packet/s2c/play/BlockEntityUpdateS2CPacket;Lnet/minecraft/block/entity/BlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/BlockEntity;read(Lnet/minecraft/storage/ReadView;)V"))
    private void onDataPacket(BlockEntity blockEntity, ReadView view, Operation<Void> original) {
        if (blockEntity instanceof SyncedBlockEntity syncedBlockEntity) {
            syncedBlockEntity.onDataPacket(view);
        } else {
            original.call(blockEntity, view);
        }
    }
}
