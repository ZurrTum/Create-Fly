package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.content.equipment.armor.NetheriteBacktankFirstPersonRenderer;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.renderer.state.level.FirstPersonHandsAndItemsRenderState;
import net.minecraft.client.renderer.state.level.PlayerRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class FirstPersonHandsAndItemsRendererMixin {
    @Inject(method = "submitArmWithItem(Lnet/minecraft/client/renderer/state/level/PlayerRenderState;Lnet/minecraft/client/renderer/state/level/FirstPersonHandsAndItemsRenderState;FFLnet/minecraft/world/InteractionHand;FLnet/minecraft/world/item/ItemStack;FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;I)V", at = @At(value = "FIELD", target = "Lnet/minecraft/world/InteractionHand;MAIN_HAND:Lnet/minecraft/world/InteractionHand;", opcode = Opcodes.GETSTATIC), cancellable = true)
    private void renderItem(
        PlayerRenderState playerState,
        FirstPersonHandsAndItemsRenderState state,
        float partialTicks,
        float xRot,
        InteractionHand hand,
        float attack,
        ItemStack itemStack,
        float inverseArmHeight,
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        int lightCoords,
        CallbackInfo ci
    ) {
        FirstPersonHandsAndItemsRenderer renderer = (FirstPersonHandsAndItemsRenderer) (Object) this;
        if (Create.ZAPPER_RENDER_HANDLER.onRenderPlayerHand(
            itemStack,
            renderer,
            playerState,
            state,
            poseStack,
            submitNodeCollector,
            lightCoords,
            partialTicks,
            hand,
            inverseArmHeight,
            attack
        ) || Create.POTATO_CANNON_RENDER_HANDLER.onRenderPlayerHand(
            itemStack,
            renderer,
            playerState,
            state,
            poseStack,
            submitNodeCollector,
            lightCoords,
            partialTicks,
            hand,
            inverseArmHeight,
            attack
        ) || ExtendoGripRenderHandler.onRenderPlayerHand(
            itemStack,
            renderer,
            playerState,
            state,
            poseStack,
            submitNodeCollector,
            lightCoords,
            hand,
            inverseArmHeight,
            attack
        )) {
            ci.cancel();
        }
    }

    @WrapOperation(method = "renderPlayerHand(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/world/entity/HumanoidArm;Lnet/minecraft/client/renderer/state/level/PlayerRenderState;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/ClientAsset$Texture;texturePath()Lnet/minecraft/resources/Identifier;"))
    private Identifier getMapHandTexture(
        ClientAsset.Texture instance,
        Operation<Identifier> original,
        @Local AvatarRenderState avatarRenderState
    ) {
        Identifier id = NetheriteBacktankFirstPersonRenderer.getHandTexture(avatarRenderState);
        if (id != null) {
            return id;
        }
        return original.call(instance);
    }
}
