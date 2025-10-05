package com.zurrtum.create.client.vanillin.visuals;

import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.component.ShadowComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class BlockDisplayVisual extends AbstractEntityVisual<DisplayEntity.BlockDisplayEntity> implements SimpleDynamicVisual {
    private final TransformedInstance instance;
    private BlockState currentBlockState;

    private final ShadowComponent shadowComponent;

    public BlockDisplayVisual(VisualizationContext ctx, DisplayEntity.BlockDisplayEntity entity, float partialTick) {
        super(ctx, entity, partialTick);

        var blockRenderState = entity.getData();

        var state = blockRenderState != null ? blockRenderState.blockState() : Blocks.AIR.getDefaultState();

        currentBlockState = state;

        instance = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.block(state)).createInstance();

        shadowComponent = new ShadowComponent(ctx, entity);
    }

    @Override
    public void beginFrame(Context ctx) {
        DisplayEntity.RenderState renderState = entity.getRenderState();
        if (renderState == null) {
            instance.handle().setVisible(false);
            return;
        }
        var object = entity.getData();
        if (object == null) {
            instance.handle().setVisible(false);
            return;
        }

        instance.handle().setVisible(true);

        if (currentBlockState != object.blockState()) {
            currentBlockState = object.blockState();
            visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.block(currentBlockState)).stealInstance(instance);
        }

        float f = entity.getLerpProgress(ctx.partialTick());

        shadowComponent.radius(renderState.shadowRadius().lerp(f));
        shadowComponent.strength(renderState.shadowStrength().lerp(f));
        shadowComponent.beginFrame(ctx);

        int i = renderState.brightnessOverride();
        int j = i != -1 ? i : computePackedLight(ctx.partialTick());
        AffineTransformation transformation = renderState.transformation().interpolate(f);

        Vec3d pos = entity.getEntityPos();
        var renderOrigin = renderOrigin();

        instance.setIdentityTransform()
            .translate((float) (pos.x - renderOrigin.getX()), (float) (pos.y - renderOrigin.getY()), (float) (pos.z - renderOrigin.getZ()));

        float partialTick = ctx.partialTick();
        Camera camera = ctx.camera();
        switch (renderState.billboardConstraints()) {
            case FIXED:
                instance.pose.rotateYXZ(
                    -0.017453292F * entityYRot(entity, partialTick),
                    ((float) Math.PI / 180F) * entityXRot(entity, partialTick),
                    0.0F
                );
                break;
            case HORIZONTAL:
                instance.pose.rotateYXZ(-0.017453292F * entityYRot(entity, partialTick), ((float) Math.PI / 180F) * cameraXRot(camera), 0.0F);
                break;
            case VERTICAL:
                instance.pose.rotateYXZ(-0.017453292F * cameraYrot(camera), ((float) Math.PI / 180F) * entityXRot(entity, partialTick), 0.0F);
                break;
            case CENTER:
                instance.pose.rotateYXZ(-0.017453292F * cameraYrot(camera), ((float) Math.PI / 180F) * cameraXRot(camera), 0.0F);
                break;
        }

        // getMatrix does a copy which is not strictly necessary here but oh well.
        instance.mul(transformation.getMatrix()).light(j).setChanged();
    }

    private static float cameraYrot(Camera camera) {
        return camera.getYaw() - 180.0F;
    }

    private static float cameraXRot(Camera camera) {
        return -camera.getPitch();
    }

    private static float entityYRot(Entity entity, float partialTick) {
        return MathHelper.lerpAngleDegrees(partialTick, entity.lastYaw, entity.getYaw());
    }

    private static float entityXRot(Entity entity, float partialTick) {
        return MathHelper.lerp(partialTick, entity.lastPitch, entity.getPitch());
    }

    @Override
    protected void _delete() {
        instance.delete();
    }
}
