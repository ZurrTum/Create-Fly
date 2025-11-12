package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.config.SyncConfigTask;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Queue;
import net.minecraft.server.network.ConfigurationTask;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;

@Mixin(ServerConfigurationPacketListenerImpl.class)
public abstract class ServerConfigurationNetworkHandlerMixin {
    @Shadow
    @Final
    private Queue<ConfigurationTask> configurationTasks;

    @Shadow
    protected abstract void finishCurrentTask(ConfigurationTask.Type key);

    @Inject(method = "addOptionalTasks()V", at = @At("TAIL"))
    private void queueSendResourcePackTask(CallbackInfo ci) {
        configurationTasks.add(new SyncConfigTask(this::finishCurrentTask));
    }
}
