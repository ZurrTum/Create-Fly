package com.zurrtum.create.client.foundation.blockEntity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer;
import com.zurrtum.create.client.content.redstone.link.LinkRenderer.LinkRenderState;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.filtering.FilteringRenderer.FilterRenderState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SmartBlockEntityRenderer<T extends SmartBlockEntity, S extends SmartBlockEntityRenderer.SmartRenderState> implements BlockEntityRenderer<T, S> {
    protected final ItemModelResolver itemModelManager;

    public SmartBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        itemModelManager = context.itemModelResolver();
    }

    @Override
    @SuppressWarnings("unchecked")
    public S createRenderState() {
        return (S) new SmartRenderState();
    }

    @Override
    public void extractRenderState(
        T be,
        S state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        BlockEntityRenderState.extractBase(be, state, crumblingOverlay);
        if (be.isRemoved()) {
            return;
        }
        double distance = be.isVirtual() ? -1 : cameraPos.distanceToSqr(VecHelper.getCenterOf(state.blockPos));
        state.filter = FilteringRenderer.getFilterRenderState(be, state.blockState, itemModelManager, distance);
        state.link = LinkRenderer.getLinkRenderState(be, itemModelManager, distance);
    }

    @Override
    public void submit(S state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        if (state.filter != null) {
            state.filter.render(state.blockState, queue, matrices, state.lightCoords);
        }
        if (state.link != null) {
            state.link.render(state.blockState, queue, matrices, state.lightCoords);
        }
    }

    public static NameplateRenderState getNameplateRenderState(
        SmartBlockEntity blockEntity,
        BlockPos pos,
        Vec3 cameraPos,
        Component tag,
        float yOffset,
        int light
    ) {
        if (blockEntity.isVirtual()) {
            return null;
        }
        double distance = cameraPos.distanceToSqr(Vec3.atCenterOf(pos));
        if (distance > 4096.0f) {
            return null;
        }
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (!(hitResult instanceof BlockHitResult bhr) || bhr.getType() == Type.MISS || !bhr.getBlockPos().equals(pos)) {
            return null;
        }
        Vec3 labelPos = new Vec3(0.5, yOffset - 0.25, 0.5);
        return new NameplateRenderState(labelPos, tag, light, distance);
    }

    public static class SmartRenderState extends BlockEntityRenderState {
        public FilterRenderState filter;
        public LinkRenderState link;
    }

    public record NameplateRenderState(Vec3 pos, Component label, int light, double distance) {
        public void render(PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
            queue.submitNameTag(matrices, pos, 0, label, true, light, distance, cameraState);
        }
    }
}
