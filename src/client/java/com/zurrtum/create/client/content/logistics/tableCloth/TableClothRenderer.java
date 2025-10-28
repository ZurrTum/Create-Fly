package com.zurrtum.create.client.content.logistics.tableCloth;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotOutputItemState;
import com.zurrtum.create.client.foundation.blockEntity.renderer.SmartBlockEntityRenderer;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlockEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;

public class TableClothRenderer extends SmartBlockEntityRenderer<TableClothBlockEntity, TableClothRenderer.TableClothRenderState> {
    public TableClothRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public TableClothRenderState createRenderState() {
        return new TableClothRenderState();
    }

    @Override
    public void updateRenderState(
        TableClothBlockEntity be,
        TableClothRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        state.radians = MathHelper.RADIANS_PER_DEGREE * (180 - be.facing.getPositiveHorizontalDegrees());
        if (be.isShop()) {
            state.layer = RenderLayer.getCutout();
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
        World world = be.getWorld();
        for (int i = 0; i < size; i++) {
            items[i] = DepotOutputItemState.create(itemModelManager, stacks.get(i), world);
        }
        state.itemPosition = Vec3d.ofCenter(state.pos);
    }

    @Override
    public void render(TableClothRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.shop != null) {
            queue.submitCustom(matrices, state.layer, state);
        }
        DepotOutputItemState[] items = state.items;
        if (items != null) {
            matrices.multiply(new Quaternionf().setAngleAxis(state.radians, 0, 1, 0), 0.5f, 0.5f, 0.5f);
            int size = items.length;
            boolean multiple = size > 1;
            for (int i = 0; i < size; i++) {
                matrices.translate(0.5f, 0.1875f, 0.5f);
                if (multiple) {
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(i * (360f / size) + 45f));
                    matrices.translate(0, i % 2 == 0 ? -0.005f : 0, 0.3125f);
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(-i * (360f / size) - 45f));
                }
                DepotOutputItemState item = items[i];
                DepotRenderer.renderItem(
                    queue,
                    matrices,
                    state.lightmapCoordinates,
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
                            stack.rotate(-state.radians + MathHelper.PI, Direction.UP);
                        }
                    }
                );
            }
        }
    }

    public static class TableClothRenderState extends SmartRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer shop;
        public float radians;
        public DepotOutputItemState[] items;
        public Vec3d itemPosition;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            shop.rotateCentered(radians, Direction.UP);
            shop.light(lightmapCoordinates);
            shop.overlay(OverlayTexture.DEFAULT_UV);
            shop.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
