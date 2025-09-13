package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.zurrtum.create.client.content.redstone.link.LinkRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;

public class SmartBlockEntityRenderer<T extends SmartBlockEntity> extends SafeBlockEntityRenderer<T> {
    public SmartBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    protected void renderSafe(T blockEntity, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        FilteringRenderer.renderOnBlockEntity(blockEntity, partialTicks, ms, buffer, light, overlay);
        LinkRenderer.renderOnBlockEntity(blockEntity, partialTicks, ms, buffer, light, overlay);
    }

    protected void renderNameplateOnHover(T blockEntity, Text tag, float yOffset, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (blockEntity.isVirtual())
            return;
        if (mc.player.squaredDistanceTo(Vec3d.ofCenter(blockEntity.getPos())) > 4096.0f)
            return;
        HitResult hitResult = mc.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS || !bhr.getBlockPos().equals(blockEntity.getPos()))
            return;

        float f = yOffset + 0.25f;
        ms.push();
        ms.translate(0.5, f, 0.5);
        ms.multiply(mc.getEntityRenderDispatcher().getRotation());
        ms.scale(0.025F, -0.025F, 0.025F);
        Matrix4f matrix4f = ms.peek().getPositionMatrix();
        float f2 = mc.options.getTextBackgroundOpacity(0.25F);
        int j = (int) (f2 * 255.0F) << 24;
        TextRenderer font = mc.textRenderer;
        float f1 = (float) (-font.getWidth(tag) / 2);
        font.draw(tag, f1, (float) 0, 553648127, false, matrix4f, buffer, TextRenderer.TextLayerType.SEE_THROUGH, j, light);
        font.draw(tag, f1, (float) 0, -1, false, matrix4f, buffer, TextRenderer.TextLayerType.NORMAL, 0, light);
        ms.pop();
    }

}
