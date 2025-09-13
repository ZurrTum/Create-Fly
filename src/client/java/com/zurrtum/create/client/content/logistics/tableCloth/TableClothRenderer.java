package com.zurrtum.create.client.content.logistics.tableCloth;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class TableClothRenderer extends SmartBlockEntityRenderer<TableClothBlockEntity> {
    public TableClothRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(
        TableClothBlockEntity blockEntity,
        float partialTicks,
        MatrixStack ms,
        VertexConsumerProvider buffer,
        int light,
        int overlay
    ) {
        super.renderSafe(blockEntity, partialTicks, ms, buffer, light, overlay);
        List<ItemStack> stacks = blockEntity.getItemsForRender();
        float rotationInRadians = MathHelper.RADIANS_PER_DEGREE * (180 - blockEntity.facing.getPositiveHorizontalDegrees());

        if (blockEntity.isShop()) {
            CachedBuffers.partial(
                blockEntity.sideOccluded ? AllPartialModels.TABLE_CLOTH_PRICE_TOP : AllPartialModels.TABLE_CLOTH_PRICE_SIDE,
                blockEntity.getCachedState()
            ).rotateCentered(rotationInRadians, Direction.UP).light(light).overlay(overlay).renderInto(ms, buffer.getBuffer(RenderLayer.getCutout()));
        }

        ms.push();
        TransformStack.of(ms).rotateCentered(rotationInRadians, Direction.UP);
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack entry = stacks.get(i);
            ms.push();
            ms.translate(0.5f, 3 / 16f, 0.5f);

            if (stacks.size() > 1) {
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * (360f / stacks.size()) + 45f));
                ms.translate(0, i % 2 == 0 ? -0.005 : 0, 5 / 16f);
                ms.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-i * (360f / stacks.size()) - 45f));
            }

            DepotRenderer.renderItem(
                ms, buffer, light, OverlayTexture.DEFAULT_UV, entry, 0, null, Vec3d.ofCenter(blockEntity.getPos()), true, (stack, blockItem) -> {
                    if (!blockItem) {
                        stack.rotate(-rotationInRadians + MathHelper.PI, Direction.UP);
                    }
                }
            );
            ms.pop();
        }

        ms.pop();
    }
}
