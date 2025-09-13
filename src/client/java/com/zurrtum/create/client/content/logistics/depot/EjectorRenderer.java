package com.zurrtum.create.client.content.logistics.depot;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.kinetics.base.ShaftRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationManager;
import com.zurrtum.create.client.flywheel.lib.transform.Rotate;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.transform.Translate;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.logistics.depot.DepotBehaviour;
import com.zurrtum.create.content.logistics.depot.EjectorBlock;
import com.zurrtum.create.content.logistics.depot.EjectorBlockEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3d;

public class EjectorRenderer extends ShaftRenderer<EjectorBlockEntity> {

    static final Vec3d pivot = VecHelper.voxelSpace(0, 11.25, 0.75);

    public EjectorRenderer(BlockEntityRendererFactory.Context context) {
        super(context);
    }

    @Override
    public boolean rendersOutsideBoundingBox() {
        return true;
    }

    @Override
    protected void renderSafe(EjectorBlockEntity be, float partialTicks, MatrixStack ms, VertexConsumerProvider buffer, int light, int overlay) {
        super.renderSafe(be, partialTicks, ms, buffer, light, overlay);

        float lidProgress = be.getLidProgress(partialTicks);
        float angle = lidProgress * 70;

        if (!VisualizationManager.supportsVisualization(be.getWorld())) {
            SuperByteBuffer model = CachedBuffers.partial(AllPartialModels.EJECTOR_TOP, be.getCachedState());
            applyLidAngle(be, angle, model);
            model.light(light).renderInto(ms, buffer.getBuffer(RenderLayer.getSolid()));
        }

        var msr = TransformStack.of(ms);

        DepotBehaviour behaviour = be.getBehaviour(DepotBehaviour.TYPE);
        if (behaviour == null || behaviour.isEmpty())
            return;

        ms.push();
        applyLidAngle(be, angle, msr);
        msr.center().rotateYDegrees(-180 - AngleHelper.horizontalAngle(be.getCachedState().get(EjectorBlock.HORIZONTAL_FACING))).uncenter();
        DepotRenderer.renderItemsOf(be, partialTicks, ms, buffer, light, overlay, behaviour);
        ms.pop();
    }

    static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, float angle, T tr) {
        applyLidAngle(be, pivot, angle, tr);
    }

    static <T extends Translate<T> & Rotate<T>> void applyLidAngle(KineticBlockEntity be, Vec3d rotationOffset, float angle, T tr) {
        tr.center().rotateYDegrees(180 + AngleHelper.horizontalAngle(be.getCachedState().get(EjectorBlock.HORIZONTAL_FACING))).uncenter()
            .translate(rotationOffset).rotateXDegrees(-angle).translateBack(rotationOffset);
    }

}
