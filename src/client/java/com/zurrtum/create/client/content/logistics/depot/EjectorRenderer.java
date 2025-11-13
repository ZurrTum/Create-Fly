package com.zurrtum.create.client.content.logistics.depot;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotItemState;
import com.zurrtum.create.client.content.logistics.depot.DepotRenderer.DepotOutputItemState;
import com.zurrtum.create.client.flywheel.lib.transform.Rotate;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.depot.EjectorBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EjectorRenderer extends ShaftRenderer<EjectorBlockEntity, EjectorRenderer.EjectorRenderState> {
    static final Vec3 pivot = VecHelper.voxelSpace(0, 11.25, 0.75);
    protected final ItemModelResolver itemModelManager;

    public EjectorRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
        itemModelManager = context.itemModelResolver();
    }

    @Override
    public EjectorRenderState createRenderState() {
        return new EjectorRenderState();
    }

    @Override
    public void extractRenderState(
        EjectorBlockEntity be,
        EjectorRenderState state,
        float tickProgress,
        Vec3 cameraPos,
        @Nullable ModelFeatureRenderer.CrumblingOverlay crumblingOverlay
    ) {
        super.extractRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        BlockState blockState = be.getBlockState();
        if (!state.support) {
            state.top = CachedBuffers.partial(AllPartialModels.EJECTOR_TOP, blockState);
            state.lidAngle = Mth.DEG_TO_RAD * (be.getLidProgress(tickProgress) * -70);
            state.yRot = Mth.DEG_TO_RAD * (180 + AngleHelper.horizontalAngle(blockState.getValue(EjectorBlock.HORIZONTAL_FACING)));
        }
        DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
        if (behaviour == null || behaviour.isEmpty())
            return;
        Level world = be.getLevel();
        state.incoming = DepotRenderer.createIncomingStateList(behaviour, itemModelManager, tickProgress, world);
        state.outputs = DepotRenderer.createOutputStateList(behaviour, itemModelManager, world);
        if (state.support && (state.incoming != null || state.outputs != null)) {
            state.blockPos = be.getBlockPos();
            state.blockEntityType = be.getType();
            state.lightCoords = world != null ? LevelRenderer.getLightColor(world, state.blockPos) : LightTexture.FULL_BRIGHT;
            state.lidAngle = Mth.DEG_TO_RAD * (be.getLidProgress(tickProgress) * -70);
            state.yRot = Mth.DEG_TO_RAD * (180 + AngleHelper.horizontalAngle(blockState.getValue(EjectorBlock.HORIZONTAL_FACING)));
        }
    }

    @Override
    public void submit(EjectorRenderState state, PoseStack matrices, SubmitNodeCollector queue, CameraRenderState cameraState) {
        super.submit(state, matrices, queue, cameraState);
        if (state.incoming != null || state.outputs != null) {
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotation(state.yRot));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            matrices.translate(pivot.x, pivot.y, pivot.z);
            matrices.mulPose(Axis.XP.rotation(state.lidAngle));
            matrices.translate(-pivot.x, -pivot.y, -pivot.z);
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.mulPose(Axis.YP.rotation(-state.yRot));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            DepotRenderer.renderItemsOf(state.incoming, state.outputs, state.blockPos, cameraState.pos, queue, matrices, state.lightCoords);
        }
    }

    @Override
    protected RenderType getRenderType(EjectorBlockEntity be, BlockState state) {
        return RenderType.solid();
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, float angle, T tr) {
        tr.center().rotateYDegrees(180 + AngleHelper.horizontalAngle(be.getBlockState().getValue(EjectorBlock.HORIZONTAL_FACING))).uncenter()
            .translate(pivot).rotateXDegrees(-angle).translateBack(pivot);
    }

    public static class EjectorRenderState extends KineticRenderState {
        public SuperByteBuffer top;
        public float lidAngle;
        public float yRot;
        public DepotItemState[] incoming;
        public List<DepotOutputItemState> outputs;

        @Override
        public void render(PoseStack.Pose matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            top.center().rotateY(yRot).uncenter();
            top.translate(pivot).rotateX(lidAngle).translateBack(pivot);
            top.light(lightCoords);
            top.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
