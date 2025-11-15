package com.zurrtum.create.client.content.logistics.packager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.content.logistics.packager.PackagerBlock;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class PackagerRenderer implements BlockEntityRenderer<PackagerBlockEntity, PackagerRenderer.PackagerRenderState> {
    protected final ItemModelResolver itemModelManager;

    public PackagerRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public PackagerRenderState createRenderState() {
        return new PackagerRenderState();
    }

    @Override
    public void extractRenderState(
        PackagerBlockEntity be,
        PackagerRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        Level world = be.getLevel();
        boolean support = VisualizationManager.supportsVisualization(world);
        ItemStack renderedBox = be.getRenderedBox();
        boolean empty = renderedBox.isEmpty();
        if (support && empty) {
            return;
        }
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        Direction facing = state.blockState.getValue(PackagerBlock.FACING).getOpposite();
        float trayOffset = be.getTrayOffset(tickProgress);
        state.trayOffset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(trayOffset);
        state.trayYRot = Mth.DEG_TO_RAD * facing.toYRot();
        if (!support) {
            state.layer = RenderTypes.cutoutMovingBlock();
            state.hatch = CachedBuffers.partial(getHatchModel(be), state.blockState);
            state.hatchOffset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(.49999f);
            state.hatchYRot = Mth.DEG_TO_RAD * AngleHelper.horizontalAngle(facing);
            state.hatchXRot = Mth.DEG_TO_RAD * AngleHelper.verticalAngle(facing);
            state.tray = CachedBuffers.partial(getTrayModel(state.blockState), state.blockState);
        }
        if (!empty) {
            ItemStackRenderState item = new ItemStackRenderState();
            item.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(item, renderedBox, item.displayContext, world, null, 0);
            state.item = item;
        }
    }

    @Override
    public void submit(PackagerRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.layer != null) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
        if (state.item != null) {
            matrices.translate(state.trayOffset);
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotation(state.trayYRot));
            matrices.translate(0, 0.125f, 0);
            matrices.scale(1.49f, 1.49f, 1.49f);
            state.item.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    public static PartialModel getTrayModel(BlockState blockState) {
        return blockState.is(AllBlocks.PACKAGER) ? AllPartialModels.PACKAGER_TRAY_REGULAR : AllPartialModels.PACKAGER_TRAY_DEFRAG;
    }

    public static PartialModel getHatchModel(PackagerBlockEntity be) {
        return isHatchOpen(be) ? AllPartialModels.PACKAGER_HATCH_OPEN : AllPartialModels.PACKAGER_HATCH_CLOSED;
    }

    public static boolean isHatchOpen(PackagerBlockEntity be) {
        return be.animationTicks > (be.animationInward ? 1 : 5) && be.animationTicks < PackagerBlockEntity.CYCLE - (be.animationInward ? 5 : 1);
    }

    public static class PackagerRenderState extends BlockEntityRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public Vec3 trayOffset;
        public float trayYRot;
        public RenderType layer;
        public SuperByteBuffer hatch;
        public Vec3 hatchOffset;
        public float hatchYRot;
        public float hatchXRot;
        public SuperByteBuffer tray;
        public ItemStackRenderState item;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            hatch.translate(hatchOffset).rotateYCentered(hatchYRot).rotateXCentered(hatchXRot).light(lightCoords)
                .renderInto(matricesEntry, vertexConsumer);
            tray.translate(trayOffset).rotateYCentered(trayYRot).light(lightCoords).renderInto(matricesEntry, vertexConsumer);
        }
    }
}