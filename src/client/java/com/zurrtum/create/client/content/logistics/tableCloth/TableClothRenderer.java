package com.zurrtum.create.client.content.logistics.tableCloth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferRenderState;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotOutputItemState;
import com.zurrtum.create.client.content.logistics.tableCloth.TableClothRenderer.TableClothRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider.Context;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class TableClothRenderer implements BlockEntityRenderer<TableClothBlockEntity, TableClothRenderState> {
    protected final ItemModelResolver itemModelManager;

    public TableClothRenderer(Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public boolean shouldRender(TableClothBlockEntity be, Vec3 cameraPosition) {
        return BlockEntityRenderer.super.shouldRender(
            be,
            cameraPosition
        ) && (be.isShop() || !be.manuallyAddedItems.isEmpty());
    }

    @Override
    public TableClothRenderState createRenderState() {
        return new TableClothRenderState();
    }

    @Override
    public void extractRenderState(
        TableClothBlockEntity be,
        TableClothRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable CrumblingOverlay crumblingOverlay
    ) {
        Level level = SmartBlockEntityRenderer.extractBase(be, state, crumblingOverlay);
        float yRot = be.facing.toYRot();
        state.rotation = 180 - yRot;
        List<ItemStack> stacks;
        if (be.isShop()) {
            state.shop = CachedBuffers.partial(
                be.sideOccluded ? AllPartialModels.TABLE_CLOTH_PRICE_TOP : AllPartialModels.TABLE_CLOTH_PRICE_SIDE,
                state.blockState
            ).cardinalLighting(level).light(state.lightCoords).extractRenderState();
            state.filter = FilteringRenderer.getFilterRenderState(
                be,
                state.blockState,
                itemModelManager,
                be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos))
            );
            stacks = be.getShopItemsForRender();
            if (stacks.isEmpty()) {
                return;
            }
        } else {
            stacks = be.manuallyAddedItems;
        }
        int size = stacks.size();
        DepotOutputItemState[] items = state.items = new DepotOutputItemState[size];
        for (int i = 0; i < size; i++) {
            items[i] = DepotOutputItemState.create(itemModelManager, stacks.get(i), level);
        }
        state.itemPosition = Vec3.atCenterOf(state.blockPos);
        state.yRadians = Mth.DEG_TO_RAD * yRot;
    }

    @Override
    public void submit(
        TableClothRenderState state,
        PoseStack matrices,
        SubmitNodeCollector queue,
        CameraRenderState cameraState
    ) {
        Pose pose = matrices.last();
        if (state.filter != null) {
            state.filter.submit(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.rotation != 0) {
            pose.rotateYCentered(Mth.DEG_TO_RAD * state.rotation);
        }
        if (state.shop != null) {
            state.shop.submit(matrices, queue);
        }
        DepotOutputItemState[] items = state.items;
        if (items != null) {
            int size = items.length;
            if (size == 1) {
                pose.translate(0.5f, 0.1875f, 0.5f);
                DepotOutputItemState item = items[0];
                ItemStackRenderState renderState = item.state();
                if (!renderState.usesBlockLight() && state.yRadians != 0) {
                    pose.rotate(Axis.YP, state.yRadians);
                }
                DepotRenderer.renderItem(
                    queue,
                    matrices,
                    state.lightCoords,
                    renderState,
                    0,
                    item.upright(),
                    item.box(),
                    item.count(),
                    null,
                    state.itemPosition,
                    cameraState.pos,
                    true
                );
                return;
            }
            for (int i = 0; i < size; i++) {
                matrices.pushPose();
                pose = matrices.last();
                pose.translate(0.5f, 0.1875f, 0.5f);
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * (i * (360.0f / size) + 45.0f));
                pose.translate(0, i % 2 == 0 ? -0.005f : 0, 0.3125f);
                pose.rotate(Axis.YP, Mth.DEG_TO_RAD * (-i * (360.0f / size) - 45.0f));
                DepotOutputItemState item = items[i];
                ItemStackRenderState renderState = item.state();
                if (!renderState.usesBlockLight() && state.yRadians != 0) {
                    pose.rotate(Axis.YP, state.yRadians);
                }
                DepotRenderer.renderItem(
                    queue,
                    matrices,
                    state.lightCoords,
                    renderState,
                    0,
                    item.upright(),
                    item.box(),
                    item.count(),
                    null,
                    state.itemPosition,
                    cameraState.pos,
                    true
                );
                matrices.popPose();
            }
        }
    }

    public static class TableClothRenderState extends BlockEntityRenderState {
        public @Nullable FilterRenderState filter;
        public @Nullable SuperByteBufferRenderState shop;
        public float rotation;
        public DepotOutputItemState @Nullable [] items;
        public @UnknownNullability Vec3 itemPosition;
        public float yRadians;
    }
}
