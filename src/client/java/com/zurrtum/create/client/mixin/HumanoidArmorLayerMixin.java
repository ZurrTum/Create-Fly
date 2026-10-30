package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.foundation.item.LayeredArmorItem;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public abstract class HumanoidArmorLayerMixin<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    private HumanoidArmorLayerMixin(RenderLayerParent<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void renderArmorPiece(
        PoseStack poseStack,
        SubmitNodeCollector submitNodeCollector,
        ItemStack itemStack,
        EquipmentSlot slot,
        int lightCoords,
        S state,
        CallbackInfo ci
    ) {
        if (itemStack.getItem() instanceof LayeredArmorItem item) {
            M model = getParentModel();
            Identifier layerTexture = item.getLayerTexture();
            RenderType renderType = itemStack.hasFoil() ? RenderTypes.armorCutoutNoCullGlint(layerTexture) :
                RenderTypes.armorCutoutNoCull(layerTexture);
            submitNodeCollector.submitModel(
                model,
                state,
                poseStack,
                renderType,
                lightCoords,
                OverlayTexture.NO_OVERLAY,
                -1,
                null,
                0
            );
        }
    }
}
