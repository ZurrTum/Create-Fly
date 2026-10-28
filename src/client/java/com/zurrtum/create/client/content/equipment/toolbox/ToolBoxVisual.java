package com.zurrtum.create.client.content.equipment.toolbox;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.Instancer;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlock;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.function.Consumer;

public class ToolBoxVisual extends AbstractBlockEntityVisual<ToolboxBlockEntity> implements SimpleDynamicVisual, ShaderLightVisual {

    private final float yRadians;
    private final TransformedInstance lid;
    private final TransformedInstance drawer1;
    private final TransformedInstance drawer2;

    private float lastLidAngle = Float.NaN;
    private float lastDrawerOffset = Float.NaN;

    public ToolBoxVisual(VisualizationContext context, ToolboxBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        yRadians = Mth.DEG_TO_RAD * -blockState.getValue(ToolboxBlock.FACING).getOpposite().toYRot();

        Instancer<TransformedInstance> drawerModel = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.TOOLBOX_DRAWER)
        );

        drawer1 = drawerModel.createInstance();
        drawer2 = drawerModel.createInstance();
        lid = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.TOOLBOX_LIDS.get(blockEntity.getColor()))
        ).createInstance();

        animate(partialTick);
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
            case NORTH, SOUTH -> setSectionCollector(sectionCollector, 0, 0, -1, 0, 0, 1);
            case WEST, EAST -> setSectionCollector(sectionCollector, -1, 0, 0, 0, 1, 0);
        }
    }

    @Override
    protected void _delete() {
        lid.delete();
        drawer1.delete();
        drawer2.delete();
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate(ctx.partialTick());
    }

    private void animate(float partialTicks) {
        float lidAngle = blockEntity.lid.getValue(partialTicks);
        float drawerOffset = blockEntity.drawers.getValue(partialTicks);

        if (lidAngle != lastLidAngle) {
            lid.setIdentityTransform().translate(getVisualPosition()).rotateYCentered(yRadians)
                .translate(0, 6 / 16.0f, 12 / 16.0f).rotateX(Radians.DEG_135 * lidAngle)
                .translateBack(0, 6 / 16.0f, 12 / 16.0f).setChanged();
        }

        if (drawerOffset != lastDrawerOffset) {
            drawer1.setIdentityTransform().translate(getVisualPosition()).rotateYCentered(yRadians)
                .translate(0, 0, -drawerOffset * 0.35f).setChanged();
            drawer2.setIdentityTransform().translate(getVisualPosition()).rotateYCentered(yRadians)
                .translate(0, 0.125f, -drawerOffset * 0.175f).setChanged();
        }

        lastLidAngle = lidAngle;
        lastDrawerOffset = drawerOffset;
    }

    @Override
    public void updateLight(float partialTick) {
        relight(lid, drawer1, drawer2);
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(lid);
        consumer.accept(drawer1);
        consumer.accept(drawer2);
    }
}