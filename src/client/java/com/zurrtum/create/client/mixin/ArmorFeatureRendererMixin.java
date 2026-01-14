package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.content.equipment.armor.LayerRenderState;
import com.zurrtum.create.foundation.item.LayeredArmorItem;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
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
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public abstract class ArmorFeatureRendererMixin<S extends BipedEntityRenderState, M extends BipedEntityModel<S>> extends FeatureRenderer<S, M> {
    private ArmorFeatureRendererMixin(FeatureRendererContext<S, M> renderLayerParent) {
        super(renderLayerParent);
    }

    @Inject(method = "renderArmor(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/command/OrderedRenderCommandQueue;Lnet/minecraft/item/ItemStack;Lnet/minecraft/entity/EquipmentSlot;ILnet/minecraft/client/render/entity/state/BipedEntityRenderState;)V", at = @At("TAIL"))
    private void renderArmorPiece(
        MatrixStack poseStack,
        OrderedRenderCommandQueue submitNodeCollector,
        ItemStack itemStack,
        EquipmentSlot equipmentSlot,
        int light,
        S humanoidRenderState,
        CallbackInfo ci
    ) {
        if (itemStack.getItem() instanceof LayeredArmorItem item) {
            M model = getContextModel();
            LayerRenderState<S, M> layer = new LayerRenderState<>();
            layer.model = model;
            layer.state = humanoidRenderState;
            layer.light = light;
            submitNodeCollector.getBatchingQueue(0).submitCustom(poseStack, RenderLayer.getArmorCutoutNoCull(item.getLayerTexture()), layer);
            if (itemStack.hasGlint()) {
                submitNodeCollector.getBatchingQueue(1).submitCustom(poseStack, RenderLayer.getArmorEntityGlint(), layer);
            }
        }
    }
}
