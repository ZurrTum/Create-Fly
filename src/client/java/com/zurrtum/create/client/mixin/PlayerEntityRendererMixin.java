package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.content.equipment.armor.CardboardRenderState;
import com.zurrtum.create.client.foundation.render.SkyhookRenderState;
import net.minecraft.client.network.ClientPlayerLikeEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel.ArmPose;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.entity.PlayerLikeEntity;
import net.minecraft.item.Item;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntityRenderer.class)
public class PlayerEntityRendererMixin<AvatarlikeEntity extends PlayerLikeEntity & ClientPlayerLikeEntity> {
    @Inject(method = "getArmPose(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/util/Arm;)Lnet/minecraft/client/render/entity/model/BipedEntityModel$ArmPose;", at = @At(value = "HEAD"), cancellable = true)
    private static void getArmPose(PlayerLikeEntity player, Arm arm, CallbackInfoReturnable<ArmPose> cir) {
        Hand hand = player.getMainArm() == arm ? Hand.MAIN_HAND : Hand.OFF_HAND;
        Item item = player.getStackInHand(hand).getItem();
        ArmPose pose = AllExtensions.ARM_POSE.get(item);
        if (pose != null) {
            cir.setReturnValue(pose);
        }
    }

    @Inject(method = "updateRenderState(Lnet/minecraft/entity/PlayerLikeEntity;Lnet/minecraft/client/render/entity/state/PlayerEntityRenderState;F)V", at = @At("TAIL"))
    private void updateRenderState(AvatarlikeEntity player, PlayerEntityRenderState state, float tickProgress, CallbackInfo ci) {
        ((CardboardRenderState) state).create$update(player, tickProgress);
        SkyhookRenderState skyhookRenderState = (SkyhookRenderState) state;
        skyhookRenderState.create$setUuid(player.getUuid());
        skyhookRenderState.create$setMainStack(player.getMainHandStack());
    }
}
