package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.flywheel.impl.visualization.VisualizationManagerImpl;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import net.minecraft.client.world.ClientChunkManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientChunkManager.class)
public class ClientChunkManagerMixin {
    @Shadow
    @Final
    ClientWorld world;

    @Inject(method = "onLightUpdate", at = @At("HEAD"))
    private void flywheel$onLightUpdate(LightType type, ChunkSectionPos pos, CallbackInfo ci) {
        var manager = VisualizationManagerImpl.get(world);

        if (manager != null) {
            manager.onLightUpdate(pos, type);
        }
    }

    @Inject(method = "unload(Lnet/minecraft/util/math/ChunkPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientChunkManager$ClientChunkMap;unloadChunk(ILnet/minecraft/world/chunk/WorldChunk;)V"))
    private void unload(ChunkPos pos, CallbackInfo ci, @Local WorldChunk chunk) {
        CapabilityMinecartController.onChunkUnloaded(chunk);
    }
}
