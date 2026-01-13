package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.foundation.item.LayeredArmorItem;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedEntityRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<S extends BipedEntityRenderState, M extends BipedEntityModel<S>, A extends BipedEntityModel<S>> extends FeatureRenderer<S, M> {
    private ArmorFeatureRendererMixin(FeatureRendererContext<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;FF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/model/BipedEntityModel;)V"))
    private void renderArmorPiece(
        ArmorFeatureRenderer<S, M, A> instance,
        MatrixStack poseStack,
        VertexConsumerProvider vertexConsumers,
        ItemStack stack,
        EquipmentSlot slot,
        int light,
        M armorModel,
        Operation<Void> original,
        @Local(argsOnly = true) S state
    ) {
        original.call(instance, poseStack, vertexConsumers, stack, slot, light, armorModel);
        if (stack.getItem() instanceof LayeredArmorItem item) {
            RenderLayer renderType = RenderLayer.getArmorCutoutNoCull(item.getLayerTexture());
            M model = getContextModel();
            model.setAngles(state);
            model.render(poseStack, vertexConsumers.getBuffer(renderType), light, OverlayTexture.DEFAULT_UV, -1);
        }
    }
}
