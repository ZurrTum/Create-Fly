package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.config.SyncConfigTask;
import net.minecraft.server.network.ServerConfigurationNetworkHandler;
import net.minecraft.server.network.ServerPlayerConfigurationTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;

@Mixin(ServerConfigurationNetworkHandler.class)
public abstract class ServerConfigurationNetworkHandlerMixin {
    @Shadow
    @Final
    private Queue<ServerPlayerConfigurationTask> tasks;

    @Shadow
    protected abstract void onTaskFinished(ServerPlayerConfigurationTask.Key key);

    @Inject(method = "queueSendResourcePackTask()V", at = @At("TAIL"))
    private void queueSendResourcePackTask(CallbackInfo ci) {
        tasks.add(new SyncConfigTask(this::onTaskFinished));
    }
}
