package com.zurrtum.create.client.content.logistics.tableCloth;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotOutputItemState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;

public class TableClothRenderer extends SmartBlockEntityRenderer<TableClothBlockEntity, TableClothRenderer.TableClothRenderState> {
    public TableClothRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
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
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.radians = Mth.DEG_TO_RAD * (180 - be.facing.toYRot());
        if (be.isShop()) {
            state.layer = RenderType.cutout();
            state.shop = CachedBuffers.partial(
                be.sideOccluded ? AllPartialModels.TABLE_CLOTH_PRICE_TOP : AllPartialModels.TABLE_CLOTH_PRICE_SIDE,
                state.blockState
            );
        }
        List<ItemStack> stacks = be.getItemsForRender();
        int size = stacks.size();
        if (size == 0) {
            return;
        }
        DepotOutputItemState[] items = state.items = new DepotOutputItemState[size];
        Level world = be.getLevel();
        for (int i = 0; i < size; i++) {
            items[i] = DepotOutputItemState.create(itemModelManager, stacks.get(i), world);
        }
        state.itemPosition = Vec3.atCenterOf(state.blockPos);
    }

    @Override
    public void submit(TableClothRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.shop != null) {
            queue.submitCustomGeometry(matrices, state.layer, state);
        }
        DepotOutputItemState[] items = state.items;
        if (items != null) {
            matrices.rotateAround(new Quaternionf().setAngleAxis(state.radians, 0, 1, 0), 0.5f, 0.5f, 0.5f);
            int size = items.length;
            boolean multiple = size > 1;
            for (int i = 0; i < size; i++) {
                matrices.translate(0.5f, 0.1875f, 0.5f);
                if (multiple) {
                    matrices.mulPose(Axis.YP.rotationDegrees(i * (360f / size) + 45f));
                    matrices.translate(0, i % 2 == 0 ? -0.005f : 0, 0.3125f);
                    matrices.mulPose(Axis.YP.rotationDegrees(-i * (360f / size) - 45f));
                }
                DepotOutputItemState item = items[i];
                DepotRenderer.renderItem(
                    queue,
                    matrices,
                    state.lightCoords,
                    item.state(),
                    0,
                    item.upright(),
                    item.box(),
                    item.count(),
                    null,
                    state.itemPosition,
                    cameraState.pos,
                    true,
                    (stack, blockItem) -> {
                        if (!blockItem) {
                            stack.rotate(-state.radians + Mth.PI, Direction.UP);
                        }
                    }
                );
            }
        }
    }

    public static class TableClothRenderState extends SmartRenderState implements SubmitNodeCollector.CustomGeometryRenderer {
        public RenderType layer;
        public SuperByteBuffer shop;
        public float radians;
        public DepotOutputItemState[] items;
        public Vec3 itemPosition;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            shop.rotateCentered(radians, Direction.UP);
            shop.light(lightCoords);
            shop.overlay(OverlayTexture.NO_OVERLAY);
            shop.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
