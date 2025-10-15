package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer.LinkRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class SmartBlockEntityRenderer<T extends SmartBlockEntity, S extends SmartBlockEntityRenderer.SmartRenderState> implements BlockEntityRenderer<T, S> {
    protected final ItemModelManager itemModelManager;

    public SmartBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        itemModelManager = context.itemModelManager();
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new SmartRenderState();
    }

    @Override
    public void updateRenderState(
        T be,
        S state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        BlockEntityRenderState.updateBlockEntityRenderState(be, state, crumblingOverlay);
        if (be.isRemoved()) {
            return;
        }
        double distance = be.isVirtual() ? -1 : cameraPos.squaredDistanceTo(VecHelper.getCenterOf(state.pos));
        state.filter = FilteringRenderer.getFilterRenderState(be, state.blockState, itemModelManager, distance);
        state.link = LinkRenderer.getLinkRenderState(be, itemModelManager, distance);
    }

    @Override
    public void render(S state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.filter != null) {
            state.filter.render(state.blockState, queue, matrices, state.lightmapCoordinates);
        }
        if (state.link != null) {
            state.link.render(state.blockState, queue, matrices, state.lightmapCoordinates);
        }
    }

    protected void renderNameplateOnHover(T blockEntity, Text tag, float yOffset, MatrixStack ms, VertexConsumerProvider buffer, int light) {
        //TODO
        //        MinecraftClient mc = MinecraftClient.getInstance();
        //        if (blockEntity.isVirtual())
        //            return;
        //        if (mc.player.squaredDistanceTo(Vec3d.ofCenter(blockEntity.getPos())) > 4096.0f)
        //            return;
        //        HitResult hitResult = mc.crosshairTarget;
        //        if (!(hitResult instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS || !bhr.getBlockPos().equals(blockEntity.getPos()))
        //            return;
        //
        //        float f = yOffset + 0.25f;
        //        ms.push();
        //        ms.translate(0.5, f, 0.5);
        //        ms.multiply(mc.getEntityRenderDispatcher().getRotation());
        //        ms.scale(0.025F, -0.025F, 0.025F);
        //        Matrix4f matrix4f = ms.peek().getPositionMatrix();
        //        float f2 = mc.options.getTextBackgroundOpacity(0.25F);
        //        int j = (int) (f2 * 255.0F) << 24;
        //        TextRenderer font = mc.textRenderer;
        //        float f1 = (float) (-font.getWidth(tag) / 2);
        //        font.draw(tag, f1, (float) 0, 553648127, false, matrix4f, buffer, TextRenderer.TextLayerType.SEE_THROUGH, j, light);
        //        font.draw(tag, f1, (float) 0, -1, false, matrix4f, buffer, TextRenderer.TextLayerType.NORMAL, 0, light);
        //        ms.pop();
    }

    public static class SmartRenderState extends BlockEntityRenderState {
        public FilterRenderState filter;
        public LinkRenderState link;
    }
}
