package com.zurrtum.create.client.content.kinetics.steamEngine;

import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.ShaderLightVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.content.kinetics.steamEngine.PoweredShaftBlockEntity;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlock;
import com.zurrtum.create.content.kinetics.steamEngine.SteamEngineBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;

public class SteamEngineVisual extends AbstractBlockEntityVisual<SteamEngineBlockEntity> implements SimpleDynamicVisual, ShaderLightVisual {

    protected final TransformedInstance piston;
    protected final TransformedInstance linkage;
    protected final TransformedInstance connector;

    private @Nullable Float lastAngle = Float.NaN;
    private @Nullable Axis lastAxis;

    public SteamEngineVisual(VisualizationContext context, SteamEngineBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        piston = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.ENGINE_PISTON)
        ).createInstance();
        linkage = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.ENGINE_LINKAGE)
        ).createInstance();
        connector = instancerProvider().instancer(
            InstanceTypes.TRANSFORMED,
            Models.chunkPartial(AllPartialModels.ENGINE_CONNECTOR)
        ).createInstance();

        animate();
    }

    @Override
    public void setSectionCollector(SectionCollector sectionCollector) {
        switch (blockState.getValue(BlockStateProperties.ATTACH_FACE)) {
            case FLOOR -> setSectionCollector(sectionCollector, -1, 0, -1, 1, 3, 1);
            case CEILING -> setSectionCollector(sectionCollector, -1, -3, -1, 1, 0, 1);
            default -> {
                switch (blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)) {
                    case NORTH -> setSectionCollector(sectionCollector, -1, -1, -3, 1, 1, 0);
                    case SOUTH -> setSectionCollector(sectionCollector, -1, -1, 0, 1, 1, 3);
                    case EAST -> setSectionCollector(sectionCollector, 0, -1, -1, 3, 1, 1);
                    case WEST -> setSectionCollector(sectionCollector, -3, -1, -1, 0, 1, 1);
                }
            }
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {
        animate();
    }

    private void animate() {
        Float angle = SteamEngineRenderer.getTargetAngle(blockEntity);
        Axis axis = Axis.Y;

        PoweredShaftBlockEntity shaft = blockEntity.getShaft();
        if (shaft != null) {
            axis = KineticBlockEntityRenderer.getRotationAxisOf(shaft);
        }

        if (Objects.equals(angle, lastAngle) && lastAxis == axis) {
            return;
        }

        lastAngle = angle;
        lastAxis = axis;

        if (angle == null) {
            piston.setVisible(false);
            linkage.setVisible(false);
            connector.setVisible(false);
            return;
        }
        piston.setVisible(true);
        linkage.setVisible(true);
        connector.setVisible(true);

        Direction facing = SteamEngineBlock.getFacing(blockState);
        Axis facingAxis = facing.getAxis();

        boolean roll90 = facingAxis.isHorizontal() && axis == Axis.Y || facingAxis.isVertical() && axis == Axis.Z;
        float piston = 6 / 16.0f * Mth.sin(angle) - Mth.sqrt(Mth.square(14 / 16.0f) - Mth.square(6 / 16.0f) * Mth.square(
            Mth.cos(angle)));
        float distance = Mth.sqrt(Mth.square(piston - 6 / 16.0f * Mth.sin(angle)));
        float angle2 = (float) Math.acos(distance / (14 / 16.0f)) * (Mth.cos(angle) >= 0 ? 1.0f : -1.0f);

        transformed(this.piston, facing, roll90).translate(0, piston + 20 / 16.0f, 0).setChanged();

        transformed(linkage, facing, roll90).translate(0, 1 + piston + 20 / 16.0f + 4 / 16.0f, 8 / 16.0f)
            .rotateX(angle2).translate(0, -4 / 16.0f, -8 / 16.0f).setChanged();

        transformed(connector, facing, roll90).translate(0, 2, 0).rotateXCentered(-(angle + Mth.HALF_PI)).setChanged();
    }

    protected TransformedInstance transformed(TransformedInstance modelData, Direction facing, boolean roll90) {
        modelData.setIdentityTransform().translate(getVisualPosition()).center();
        float yRadians = Radians.horizontal(facing);
        if (yRadians != 0) {
            modelData.rotateY(yRadians);
        }
        float xRadians = Radians.verticalPlus90(facing);
        if (xRadians != 0) {
            modelData.rotateX(xRadians);
        }
        if (roll90) {
            modelData.rotateY(Radians.DEG_270);
        }
        return modelData.uncenter();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(piston, linkage, connector);
    }

    @Override
    protected void _delete() {
        piston.delete();
        linkage.delete();
        connector.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(piston);
        consumer.accept(linkage);
        consumer.accept(connector);
    }
}
