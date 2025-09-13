package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.contraptions.ContraptionHandlerClient;
import com.zurrtum.create.client.content.contraptions.render.ContraptionRenderInfoManager;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.impl.visualization.VisualizationEventHandler;
import com.zurrtum.create.client.flywheel.lib.visualization.VisualizationHelper;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.equipment.armor.CardboardArmorHandler;
import com.zurrtum.create.content.equipment.armor.DivingBootsItem;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.zurrtum.create.Create.REDSTONE_LINK_NETWORK_HANDLER;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {
    @Inject(method = "loadBlockEntity(Lnet/minecraft/block/entity/BlockEntity;)V", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"), cancellable = true)
    private void flywheel$decideNotToRenderEntity(BlockEntity entity, CallbackInfo ci) {
        if (VisualizationManager.supportsVisualization(entity.getWorld()) && VisualizationHelper.skipVanillaRender(entity)) {
            ci.cancel();
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onLoadWorld(CallbackInfo ci) {
        Create.SCHEMATIC_HANDLER.updateRenderers();
        ContraptionRenderInfoManager.resetAll();
        REDSTONE_LINK_NETWORK_HANDLER.onLoadWorld((World) (Object) this);
    }

    @Inject(method = "addEntity(Lnet/minecraft/entity/Entity;)V", at = @At("HEAD"))
    private void addEntity(CallbackInfo ci, @Local(argsOnly = true) Entity entity) {
        ClientWorld world = (ClientWorld) (Object) this;
        VisualizationEventHandler.onEntityJoinLevel(world, entity);
        ContraptionHandlerClient.addSpawnedContraptionsToCollisionList(entity, world);
        CapabilityMinecartController.attach(entity);
    }

    @Inject(method = "tickEntity(Lnet/minecraft/entity/Entity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;tick()V"))
    private void tickEntity(Entity entity, CallbackInfo ci) {
        CapabilityMinecartController.entityTick(entity);
        DivingBootsItem.accelerateDescentUnderwater(entity);
        CardboardArmorHandler.mobsMayLoseTargetWhenItIsWearingCardboard(entity);
    }
}
