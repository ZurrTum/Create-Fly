package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.zurrtum.create.content.trains.schedule.ScheduleItemEntityInteraction;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net.minecraft.server.network.ServerPlayNetworkHandler$1")
public class PlayerInteractEntityC2SPacketMixin {
    @WrapOperation(method = "method_33898(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/entity/Entity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;interactAt(Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/ActionResult;"))
    private static ActionResult interactAt(Entity entity, PlayerEntity player, Vec3d hitPos, Hand hand, Operation<ActionResult> original) {
        ActionResult result = ScheduleItemEntityInteraction.interactWithConductor(entity, player, hand);
        if (result != null) {
            return result;
        }
        result = StockTickerInteractionHandler.interactWithLogisticsManager(entity, player, hand);
        if (result != null) {
            return result;
        }
        return original.call(entity, player, hitPos, hand);
    }
}
