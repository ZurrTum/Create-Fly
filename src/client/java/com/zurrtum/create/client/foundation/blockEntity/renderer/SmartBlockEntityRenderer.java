package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer.LinkRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
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

    public static NameplateRenderState getNameplateRenderState(
        SmartBlockEntity blockEntity,
        BlockPos pos,
        Vec3d cameraPos,
        Text tag,
        float yOffset,
        int light
    ) {
        if (blockEntity.isVirtual()) {
            return null;
        }
        double distance = cameraPos.squaredDistanceTo(Vec3d.ofCenter(pos));
        if (distance > 4096.0f) {
            return null;
        }
        HitResult hitResult = MinecraftClient.getInstance().crosshairTarget;
        if (!(hitResult instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS || !bhr.getBlockPos().equals(pos)) {
            return null;
        }
        Vec3d labelPos = new Vec3d(0.5, yOffset - 0.25, 0.5);
        return new NameplateRenderState(labelPos, tag, light, distance);
    }

    public static class SmartRenderState extends BlockEntityRenderState {
        public FilterRenderState filter;
        public LinkRenderState link;
    }

    public record NameplateRenderState(Vec3d pos, Text label, int light, double distance) {
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
            queue.submitLabel(matrices, pos, 0, label, true, light, distance, cameraState);
        }
    }
}
