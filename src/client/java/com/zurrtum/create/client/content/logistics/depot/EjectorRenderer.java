package com.zurrtum.create.client.content.logistics.depot;

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
import net.minecraft.block.BlockState;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EjectorRenderer extends ShaftRenderer<EjectorBlockEntity, EjectorRenderer.EjectorRenderState> {
    static final Vec3d pivot = VecHelper.voxelSpace(0, 11.25, 0.75);
    protected final ItemModelManager itemModelManager;

    public EjectorRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
        itemModelManager = context.itemModelManager();
    }

    @Override
    public EjectorRenderState createRenderState() {
        return new EjectorRenderState();
    }

    @Override
    public void updateRenderState(
        EjectorBlockEntity be,
        EjectorRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        super.updateRenderState(be, state, tickProgress, cameraPos, crumblingOverlay);
        BlockState blockState = be.getCachedState();
        if (!state.support) {
            state.top = CachedBuffers.partial(AllPartialModels.EJECTOR_TOP, blockState);
            state.lidAngle = MathHelper.RADIANS_PER_DEGREE * (be.getLidProgress(tickProgress) * -70);
            state.yRot = MathHelper.RADIANS_PER_DEGREE * (180 + AngleHelper.horizontalAngle(blockState.get(EjectorBlock.HORIZONTAL_FACING)));
        }
        DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
        if (behaviour == null || behaviour.isEmpty())
            return;
        World world = be.getWorld();
        state.incoming = DepotRenderer.createIncomingStateList(behaviour, itemModelManager, tickProgress, world);
        state.outputs = DepotRenderer.createOutputStateList(behaviour, itemModelManager, world);
        if (state.support && (state.incoming != null || state.outputs != null)) {
            state.pos = be.getPos();
            state.type = be.getType();
            state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(
                world,
                state.pos
            ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
            state.lidAngle = MathHelper.RADIANS_PER_DEGREE * (be.getLidProgress(tickProgress) * -70);
            state.yRot = MathHelper.RADIANS_PER_DEGREE * (180 + AngleHelper.horizontalAngle(blockState.get(EjectorBlock.HORIZONTAL_FACING)));
        }
    }

    @Override
    public void render(EjectorRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        super.render(state, matrices, queue, cameraState);
        if (state.incoming != null || state.outputs != null) {
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.yRot));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            matrices.translate(pivot.x, pivot.y, pivot.z);
            matrices.multiply(RotationAxis.POSITIVE_X.rotation(state.lidAngle));
            matrices.translate(-pivot.x, -pivot.y, -pivot.z);
            matrices.translate(0.5f, 0.5f, 0.5f);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-state.yRot));
            matrices.translate(-0.5f, -0.5f, -0.5f);
            DepotRenderer.renderItemsOf(state.incoming, state.outputs, state.pos, cameraState.pos, queue, matrices, state.lightmapCoordinates);
        }
    }

    @Override
    protected RenderLayer getRenderType(EjectorBlockEntity be, BlockState state) {
        return RenderLayer.getSolid();
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, float angle, T tr) {
        tr.center().rotateYDegrees(180 + AngleHelper.horizontalAngle(be.getCachedState().get(EjectorBlock.HORIZONTAL_FACING))).uncenter()
            .translate(pivot).rotateXDegrees(-angle).translateBack(pivot);
    }

    public static class EjectorRenderState extends KineticRenderState {
        public SuperByteBuffer top;
        public float lidAngle;
        public float yRot;
        public DepotItemState[] incoming;
        public List<DepotOutputItemState> outputs;

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            super.render(matricesEntry, vertexConsumer);
            top.center().rotateY(yRot).uncenter();
            top.translate(pivot).rotateX(lidAngle).translateBack(pivot);
            top.light(lightmapCoordinates);
            top.renderInto(matricesEntry, vertexConsumer);
        }
    }
}
