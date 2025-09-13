package com.zurrtum.create.client.content.logistics.packager;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.packager.PackagerBlock;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PackagerRenderer extends SmartBlockEntityRenderer<PackagerBlockEntity> {

    public PackagerRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    protected void renderSafe(PackagerBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        ItemStack renderedBox = be.getRenderedBox();
        float trayOffset = be.getTrayOffset(partialTicks);
        BlockState blockState = be.getCachedState();
        Direction facing = blockState.get(PackagerBlock.FACING).getOpposite();

        if (!VisualizationManager.supportsVisualization(be.getWorld())) {
            var hatchModel = getHatchModel(be);

            SuperByteBuffer sbb = CachedBuffers.partial(hatchModel, blockState);
            sbb.translate(Vec3d.of(facing.getVector()).multiply(.49999f)).rotateYCenteredDegrees(AngleHelper.horizontalAngle(facing))
                .rotateXCenteredDegrees(AngleHelper.verticalAngle(facing)).light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));

            sbb = CachedBuffers.partial(getTrayModel(blockState), blockState);
            sbb.translate(Vec3d.of(facing.getVector()).multiply(trayOffset)).rotateYCenteredDegrees(facing.getPositiveHorizontalDegrees())
                .light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getCutoutMipped()));
        }

        if (!renderedBox.isEmpty()) {
            ms.push();
            var msr = TransformStack.of(ms);
            msr.translate(Vec3d.of(facing.getVector()).multiply(trayOffset)).translate(.5f, .5f, .5f)
                .rotateYDegrees(facing.getPositiveHorizontalDegrees()).translate(0, 2 / 16f, 0).scale(1.49f, 1.49f, 1.49f);
            MinecraftClient.getInstance().getItemRenderer()
                .renderItem(null, renderedBox, ItemDisplayContext.FIXED, ms, buffer, be.getWorld(), light, overlay, 0);
            ms.pop();
        }
    }

    public static PartialModel getTrayModel(BlockState blockState) {
        return blockState.isOf(AllBlocks.PACKAGER) ? AllPartialModels.PACKAGER_TRAY_REGULAR : AllPartialModels.PACKAGER_TRAY_DEFRAG;
    }

    public static PartialModel getHatchModel(PackagerBlockEntity be) {
        return isHatchOpen(be) ? AllPartialModels.PACKAGER_HATCH_OPEN : AllPartialModels.PACKAGER_HATCH_CLOSED;
    }

    public static boolean isHatchOpen(PackagerBlockEntity be) {
        return be.animationTicks > (be.animationInward ? 1 : 5) && be.animationTicks < PackagerBlockEntity.CYCLE - (be.animationInward ? 5 : 1);
    }

}