package com.zurrtum.create.client.content.kinetics.flywheel;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityVisual;
import com.zurrtum.create.client.content.kinetics.base.RotatingInstance;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.foundation.render.AllInstanceTypes;
import com.zurrtum.create.content.kinetics.flywheel.FlywheelBlockEntity;
import net.minecraft.util.math.Direction;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.function.Consumer;

public class FlywheelVisual extends KineticBlockEntityVisual<FlywheelBlockEntity> implements SimpleDynamicVisual {

    protected final RotatingInstance shaft;
    protected final TransformedInstance wheel;
    protected float lastAngle = Float.NaN;

    protected final Matrix4f baseTransform = new Matrix4f();

    public FlywheelVisual(VisualizationContext context, FlywheelBlockEntity blockEntity, float partialTick) {
        super(context, blockEntity, partialTick);

        var axis = rotationAxis();
        shaft = instancerProvider().instancer(AllInstanceTypes.ROTATING, Models.partial(AllPartialModels.SHAFT)).createInstance();

        shaft.setup(FlywheelVisual.this.blockEntity).setPosition(getVisualPosition()).rotateToFace(axis).setChanged();

        wheel = instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.partial(AllPartialModels.FLYWHEEL)).createInstance();


        Direction align = Direction.from(axis, Direction.AxisDirection.POSITIVE);

        wheel.translate(getVisualPosition()).center()
            .rotate(new Quaternionf().rotateTo(0, 1, 0, align.getOffsetX(), align.getOffsetY(), align.getOffsetZ()));

        baseTransform.set(wheel.pose);

        animate(blockEntity.angle);
    }

    @Override
    public void beginFrame(DynamicVisual.Context ctx) {

        float partialTicks = ctx.partialTick();

        float speed = blockEntity.visualSpeed.getValue(partialTicks) * 3 / 10f;
        float angle = blockEntity.angle + speed * partialTicks;

        if (Math.abs(angle - lastAngle) < 0.001)
            return;

        animate(angle);

        lastAngle = angle;
    }

    private void animate(float angle) {
        wheel.setTransform(baseTransform).rotateY(AngleHelper.rad(angle)).uncenter().setChanged();
    }

    @Override
    public void update(float pt) {
        shaft.setup(blockEntity).setChanged();
    }

    @Override
    public void updateLight(float partialTick) {
        relight(shaft, wheel);
    }

    @Override
    protected void _delete() {
        shaft.delete();
        wheel.delete();
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        consumer.accept(shaft);
        consumer.accept(wheel);
    }
}
