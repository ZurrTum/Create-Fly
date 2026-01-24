package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerInteractionHandler;
import com.zurrtum.create.content.trains.schedule.ScheduleItemEntityInteraction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerGamePacketListenerImpl.class)
public class ServerGamePacketListenerImplMixin {
    @WrapOperation(method = "handleInteract(Lnet/minecraft/network/protocol/game/ServerboundInteractPacket;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;interactOn(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/InteractionResult;"))
    private static InteractionResult interactAt(
        ServerPlayer player,
        Entity entity,
        InteractionHand hand,
        Vec3 location,
        Operation<InteractionResult> original
    ) {
        InteractionResult result = ScheduleItemEntityInteraction.interactWithConductor(entity, player, hand);
        if (result != null) {
            return result;
        }
        result = StockTickerInteractionHandler.interactWithLogisticsManager(entity, player, hand);
        if (result != null) {
            return result;
        }
        return original.call(player, entity, hand, location);
    }
}
