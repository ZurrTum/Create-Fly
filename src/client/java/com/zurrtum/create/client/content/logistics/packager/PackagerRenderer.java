package com.zurrtum.create.client.content.logistics.packager;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.content.logistics.packager.PackagerRenderer.PackagerRenderState;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.packager.PackagerBlock;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

public class PackagerRenderer implements BlockEntityRenderer<PackagerBlockEntity, PackagerRenderState> {
    protected final ItemModelResolver itemModelManager;

    public PackagerRenderer(Context context) {
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
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = be.getLevel();
        ItemStack renderedBox = be.getRenderedBox();
        if (VisualizationManager.supportsVisualization(level)) {
            if (renderedBox.isEmpty()) {
                return;
            }
            state.blockPos = be.getBlockPos();
            state.blockEntityType = be.getType();
            state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, state.blockPos);
            Direction facing = be.getBlockState().getValue(PackagerBlock.FACING).getOpposite();
            state.trayOffset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(be.getTrayOffset(tickProgress));
            state.trayYRot = facing.toYRot();
            ItemStackRenderState item = state.item = new ItemStackRenderState();
            item.displayContext = ItemDisplayContext.FIXED;
            itemModelManager.appendItemLayers(item, renderedBox, item.displayContext, level, null, 0);
            return;
        }
        state.blockPos = be.getBlockPos();
        state.blockState = be.getBlockState();
        state.blockEntityType = be.getType();
        state.lightCoords = SmartBlockEntityRenderer.getLightCoords(level, state.blockPos);
        Direction facing = state.blockState.getValue(PackagerBlock.FACING).getOpposite();
        state.trayOffset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(be.getTrayOffset(tickProgress));
        state.trayYRot = facing.toYRot();
        CardinalLighting cardinalLighting = SmartBlockEntityRenderer.getCardinalLighting(level);
        state.hatch = CachedBuffers.partial(getHatchModel(be), state.blockState).cardinalLighting(cardinalLighting)
            .light(state.lightCoords).extractRenderState();
        state.hatchOffset = Vec3.atLowerCornerOf(facing.getUnitVec3i()).scale(0.49999f);
        state.hatchYRadians = Radians.horizontal(facing);
        state.hatchXRadians = Radians.vertical(facing);
        state.tray = CachedBuffers.partial(getTrayModel(state.blockState), state.blockState)
            .cardinalLighting(cardinalLighting).light(state.lightCoords).extractRenderState();
        if (renderedBox.isEmpty()) {
            return;
        }
        ItemStackRenderState item = state.item = new ItemStackRenderState();
        item.displayContext = ItemDisplayContext.FIXED;
        itemModelManager.appendItemLayers(item, renderedBox, item.displayContext, level, null, 0);
    }

    @Override
    public void submit(
        PackagerRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        if (state.hatch != null) {
            matrices.pushPose();
            Pose pose = matrices.last();
            Vec3 offset = state.hatchOffset;
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            if (state.hatchYRadians != 0) {
                pose.rotateYCentered(state.hatchYRadians);
            }
            if (state.hatchXRadians != 0) {
                pose.rotateXCentered(state.hatchXRadians);
            }
            state.hatch.submit(matrices, queue);
            matrices.popPose();
            matrices.pushPose();
            offset = state.trayOffset;
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            if (state.trayYRot != 0) {
                pose.rotateYCentered(Mth.DEG_TO_RAD * state.trayYRot);
            }
            state.tray.submit(matrices, queue);
            matrices.popPose();
        }
        if (state.item != null) {
            Pose pose = matrices.last();
            Vec3 offset = state.trayOffset;
            pose.translate((float) offset.x, (float) offset.y, (float) offset.z);
            pose.translate(0.5f, 0.5f, 0.5f);
            if (state.trayYRot != 0) {
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * state.trayYRot);
            }
            pose.translate(0, 0.125f, 0);
            pose.scale(1.49f, 1.49f, 1.49f);
            state.item.submit(matrices, queue, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }
    }

    public static PartialModel getTrayModel(BlockState blockState) {
        return blockState.is(AllBlocks.PACKAGER) ? AllPartialModels.PACKAGER_TRAY_REGULAR :
            AllPartialModels.PACKAGER_TRAY_DEFRAG;
    }

    public static PartialModel getHatchModel(PackagerBlockEntity be) {
        return isHatchOpen(be) ? AllPartialModels.PACKAGER_HATCH_OPEN : AllPartialModels.PACKAGER_HATCH_CLOSED;
    }

    public static boolean isHatchOpen(PackagerBlockEntity be) {
        return be.animationTicks > (be.animationInward ? 1 : 5) && be.animationTicks < PackagerBlockEntity.CYCLE - (
            be.animationInward ? 5 : 1);
    }

    public static class PackagerRenderState extends BlockEntityRenderState {
        public @UnknownNullability Vec3 trayOffset;
        public float trayYRot;
        public @Nullable SuperByteBufferRenderState hatch;
        public @UnknownNullability Vec3 hatchOffset;
        public float hatchYRadians;
        public float hatchXRadians;
        public @UnknownNullability SuperByteBufferRenderState tray;
        public @Nullable ItemStackRenderState item;
    }
}
