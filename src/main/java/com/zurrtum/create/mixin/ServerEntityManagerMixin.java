package com.zurrtum.create.mixin;

import com.zurrtum.create.content.contraptions.ContraptionHandler;
import com.zurrtum.create.content.contraptions.minecart.capability.CapabilityMinecartController;
import com.zurrtum.create.content.logistics.depot.EjectorItemEntity;
import com.zurrtum.create.foundation.item.EntityItem;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerEntityManager;
import net.minecraft.world.entity.EntityLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerEntityManager.class)
public class ServerEntityManagerMixin {
    @ModifyVariable(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At("HEAD"), ordinal = 0, argsOnly = true)
    private EntityLike addEntity(EntityLike entity) {
        if (entity instanceof ItemEntity itemEntity) {
            ItemStack stack = itemEntity.getStack();
            if (stack.getItem() instanceof EntityItem item) {
                if (entity instanceof EjectorItemEntity) {
                    return entity;
                }
                Entity newEntity = item.createEntity(itemEntity.getEntityWorld(), itemEntity, stack);
                if (newEntity != null) {
                    itemEntity.discard();
                    return newEntity;
                }
            }
        }
        return entity;
    }

    @Inject(method = "addEntity(Lnet/minecraft/world/entity/EntityLike;Z)Z", at = @At("HEAD"))
    private void addEntity(EntityLike entity, boolean existing, CallbackInfoReturnable<Boolean> cir) {
        ContraptionHandler.addSpawnedContraptionsToCollisionList(entity);
        if (!existing) {
            CapabilityMinecartController.attach(entity);
        }
    }
}
