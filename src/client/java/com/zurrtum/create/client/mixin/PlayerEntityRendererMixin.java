package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.AllExtensions;
import com.zurrtum.create.client.content.equipment.armor.CardboardRenderState;
import com.zurrtum.create.client.foundation.render.SkyhookRenderState;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.model.HumanoidModel.ArmPose;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AvatarRenderer.class)
public class PlayerEntityRendererMixin<AvatarlikeEntity extends Avatar & ClientAvatarEntity> {
    @Inject(method = "getArmPose(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/world/entity/HumanoidArm;)Lnet/minecraft/client/model/HumanoidModel$ArmPose;", at = @At(value = "HEAD"), cancellable = true)
    private static void getArmPose(Avatar player, HumanoidArm arm, CallbackInfoReturnable<ArmPose> cir) {
        InteractionHand hand = player.getMainArm() == arm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        Item item = player.getItemInHand(hand).getItem();
        ArmPose pose = AllExtensions.ARM_POSE.get(item);
        if (pose != null) {
            cir.setReturnValue(pose);
        }
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void updateRenderState(AvatarlikeEntity player, AvatarRenderState state, float tickProgress, CallbackInfo ci) {
        ((CardboardRenderState) state).create$update(player, tickProgress);
        SkyhookRenderState skyhookRenderState = (SkyhookRenderState) state;
        skyhookRenderState.create$setUuid(player.getUUID());
        skyhookRenderState.create$setMainStack(player.getMainHandItem());
    }
}
