package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
public class ChunkMapMixin {
    @WrapOperation(method = "lambda$scheduleUnload$0(Lnet/minecraft/server/level/ChunkHolder;Ljava/util/concurrent/CompletableFuture;J)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/chunk/LevelChunk;setLoaded(Z)V"))
    private void tryUnloadChunk(LevelChunk chunk, boolean loaded, Operation<Void> original) {
        original.call(chunk, loaded);
        CapabilityMinecartController.onChunkUnloaded(chunk);
    }
}
