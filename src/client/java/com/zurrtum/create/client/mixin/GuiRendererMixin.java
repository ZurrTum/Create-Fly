package com.zurrtum.create.client.mixin;

import com.google.common.collect.ImmutableMap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.catnip.gui.render.*;
import com.zurrtum.create.client.foundation.gui.render.*;
import net.minecraft.client.gui.render.GuiRenderer;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.gui.render.state.special.SpecialGuiElementRenderState;
import net.minecraft.client.render.VertexConsumerProvider.Immediate;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(GuiRenderer.class)
public class GuiRendererMixin {
    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableMap;builder()Lcom/google/common/collect/ImmutableMap$Builder;"), remap = false)
    private ImmutableMap.Builder<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> addRenderer(
        Operation<ImmutableMap.Builder<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>>> original,
        @Local(argsOnly = true) Immediate vertexConsumers
    ) {
        ImmutableMap.Builder<Class<? extends SpecialGuiElementRenderState>, SpecialGuiElementRenderer<?>> builder = original.call();
        builder.put(ItemTransformRenderState.class, new ItemTransformElementRenderer(vertexConsumers));
        builder.put(BlockTransformRenderState.class, new BlockTransformElementRenderer(vertexConsumers));
        builder.put(PartialRenderState.class, new PartialElementRenderer(vertexConsumers));
        builder.put(BlazeBurnerRenderState.class, new BlazeBurnerElementRenderer(vertexConsumers));
        builder.put(PressBasinRenderState.class, new PressBasinRenderer(vertexConsumers));
        builder.put(PressRenderState.class, new PressRenderer(vertexConsumers));
        builder.put(MixingBasinRenderState.class, new MixingBasinRenderer(vertexConsumers));
        builder.put(BasinBlazeBurnerRenderState.class, new BasinBlazeBurnerRenderer(vertexConsumers));
        builder.put(MillstoneRenderState.class, new MillstoneRenderer(vertexConsumers));
        builder.put(SawRenderState.class, new SawRenderer(vertexConsumers));
        builder.put(CrushWheelRenderState.class, new CrushWheelRenderer(vertexConsumers));
        builder.put(DeployerRenderState.class, new DeployerRenderer(vertexConsumers));
        builder.put(ManualBlockRenderState.class, new ManualBlockRenderer(vertexConsumers));
        builder.put(SpoutRenderState.class, new SpoutRenderer(vertexConsumers));
        return builder;
    }
}
