package com.zurrtum.create.client.foundation.gui.render;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.render.SpecialGuiElementRenderer;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Unit;

public class SandPaperRenderer extends SpecialGuiElementRenderer<SandPaperRenderState> {
    private final ItemRenderState renderState = new ItemRenderState();
    private final ItemStack stack;

    public SandPaperRenderer(VertexConsumerProvider.Immediate vertexConsumers) {
        super(vertexConsumers);
        stack = AllItems.SAND_PAPER.getDefaultStack();
        stack.set(AllDataComponents.SAND_PAPER_JEI, Unit.INSTANCE);
    }

    @Override
    protected void render(SandPaperRenderState state, MatrixStack matrices) {
        matrices.translate(0, -0.35f, 0);
        matrices.scale(1, -1, -1);
        DiffuseLighting lighting = MinecraftClient.getInstance().gameRenderer.getDiffuseLighting();
        lighting.setShaderLights(DiffuseLighting.Type.ITEMS_FLAT);
        stack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(state.stack()));
        MinecraftClient.getInstance().getItemRenderer().itemModelManager.clearAndUpdate(renderState, stack, ItemDisplayContext.GUI, null, null, 0);
        renderState.render(matrices, vertexConsumers, LightmapTextureManager.MAX_LIGHT_COORDINATE, OverlayTexture.DEFAULT_UV);
    }

    @Override
    protected String getName() {
        return "Sand Paper";
    }

    @Override
    public Class<SandPaperRenderState> getElementClass() {
        return SandPaperRenderState.class;
    }
}
