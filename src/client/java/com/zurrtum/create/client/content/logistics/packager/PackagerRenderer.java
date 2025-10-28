package com.zurrtum.create.client.content.logistics.packager;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.logistics.packager.PackagerBlock;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PackagerRenderer implements BlockEntityRenderer<PackagerBlockEntity, PackagerRenderer.PackagerRenderState> {
    protected final ItemModelManager itemModelManager;

    public PackagerRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    public PackagerRenderState createRenderState() {
        return new PackagerRenderState();
    }

    @Override
    public void updateRenderState(
        PackagerBlockEntity be,
        PackagerRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        World world = be.getWorld();
        boolean support = VisualizationManager.supportsVisualization(world);
        ItemStack renderedBox = be.getRenderedBox();
        boolean empty = renderedBox.isEmpty();
        if (support && empty) {
            return;
        }
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        Direction facing = state.blockState.get(PackagerBlock.FACING).getOpposite();
        float trayOffset = be.getTrayOffset(tickProgress);
        state.trayOffset = Vec3d.of(facing.getVector()).multiply(trayOffset);
        state.trayYRot = MathHelper.RADIANS_PER_DEGREE * facing.getPositiveHorizontalDegrees();
        if (!support) {
            state.layer = RenderLayer.getCutoutMipped();
            state.hatch = CachedBuffers.partial(getHatchModel(be), state.blockState);
            state.hatchOffset = Vec3d.of(facing.getVector()).multiply(.49999f);
            state.hatchYRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.horizontalAngle(facing);
            state.hatchXRot = MathHelper.RADIANS_PER_DEGREE * AngleHelper.verticalAngle(facing);
            state.tray = CachedBuffers.partial(getTrayModel(state.blockState), state.blockState);
        }
        if (!empty) {
            ItemRenderState item = new ItemRenderState();
            item.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.update(item, renderedBox, item.displayContext, world, null, 0);
            state.item = item;
        }
    }

    @Override
    public void render(PackagerRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.layer != null) {
            queue.submitCustom(matrices, state.layer, state);
        }
        if (state.item != null) {
            matrices.translate(state.trayOffset);
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.trayYRot));
            matrices.translate(0, 0.125f, 0);
            matrices.scale(1.49f, 1.49f, 1.49f);
            state.item.render(matrices, queue, state.lightmapCoordinates, OverlayTexture.DEFAULT_UV, 0);
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

    public static class PackagerRenderState extends BlockEntityRenderState implements OrderedRenderCommandQueue.Custom {
        public Vec3d trayOffset;
        public float trayYRot;
        public RenderLayer layer;
        public SuperByteBuffer hatch;
        public Vec3d hatchOffset;
        public float hatchYRot;
        public float hatchXRot;
        public SuperByteBuffer tray;
        public ItemRenderState item;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            hatch.translate(hatchOffset).rotateYCentered(hatchYRot).rotateXCentered(hatchXRot).light(lightmapCoordinates)
                .renderInto(matricesEntry, vertexConsumer);
            tray.translate(trayOffset).rotateYCentered(trayYRot).light(lightmapCoordinates).renderInto(matricesEntry, vertexConsumer);
        }
    }
}