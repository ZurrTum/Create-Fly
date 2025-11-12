package com.zurrtum.create.client.vanillin.visuals;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.part.InstanceTree;
import com.zurrtum.create.client.flywheel.lib.model.part.ModelTrees;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractBlockEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;

import java.util.function.Consumer;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.blockentity.BellRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

public class BellVisual extends AbstractBlockEntityVisual<BellBlockEntity> implements SimpleDynamicVisual {
    private static final Material MATERIAL = SimpleMaterial.builder().mipmap(false).build();

    private final InstanceTree instances;
    private final InstanceTree bellBody;

    private final Matrix4fc initialPose;

    private boolean wasShaking = false;

    public BellVisual(VisualizationContext ctx, BellBlockEntity blockEntity, float partialTick) {
        super(ctx, blockEntity, partialTick);

        instances = InstanceTree.create(instancerProvider(), ModelTrees.of(ModelLayers.BELL, BellRenderer.BELL_RESOURCE_LOCATION, MATERIAL));
        bellBody = instances.childOrThrow("bell_body");

        BlockPos visualPos = getVisualPosition();
        initialPose = new Matrix4f().translate(visualPos.getX(), visualPos.getY(), visualPos.getZ());

        updateRotation(partialTick);
    }

    @Override
    public void beginFrame(Context context) {
        if (doDistanceLimitThisFrame(context) || !isVisible(context.frustum())) {
            return;
        }

        updateRotation(context.partialTick());
    }

    private void updateRotation(float partialTick) {
        float xRot = 0;
        float zRot = 0;

        if (blockEntity.shaking) {
            float ringTime = (float) blockEntity.ticks + partialTick;
            float angle = Mth.sin(ringTime / (float) Math.PI) / (4.0F + ringTime / 3.0F);

            switch (blockEntity.clickDirection) {
                case NORTH -> xRot = -angle;
                case SOUTH -> xRot = angle;
                case EAST -> zRot = -angle;
                case WEST -> zRot = angle;
            }

            wasShaking = true;
        } else if (wasShaking) {
            wasShaking = false;
        }

        bellBody.xRot(xRot);
        bellBody.zRot(zRot);
        instances.updateInstancesStatic(initialPose);
    }

    @Override
    public void updateLight(float partialTick) {
        int packedLight = computePackedLight();
        instances.traverse(instance -> {
            instance.light(packedLight).setChanged();
        });
    }

    @Override
    public void collectCrumblingInstances(Consumer<Instance> consumer) {
        instances.traverse(consumer);
    }

    @Override
    protected void _delete() {
        instances.delete();
    }
}
