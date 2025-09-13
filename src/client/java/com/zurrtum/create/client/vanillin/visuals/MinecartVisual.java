package com.zurrtum.create.client.vanillin.visuals;

import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;
import com.zurrtum.create.client.flywheel.api.visual.TickableVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.FlatLit;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.Models;
import com.zurrtum.create.client.flywheel.lib.model.part.InstanceTree;
import com.zurrtum.create.client.flywheel.lib.model.part.ModelTrees;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleTickableVisual;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.DefaultMinecartController;
import net.minecraft.entity.vehicle.ExperimentalMinecartController;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;

public class MinecartVisual<T extends AbstractMinecartEntity> extends AbstractEntityVisual<T> implements SimpleTickableVisual, SimpleDynamicVisual {
    private static final Identifier TEXTURE = Identifier.ofVanilla("textures/entity/minecart.png");
    private static final Material MATERIAL = SimpleMaterial.builder().texture(TEXTURE).mipmap(false).build();

    private final InstanceTree instances;
    @Nullable
    private TransformedInstance contents;

    private final Matrix4fStack stack = new Matrix4fStack(2);

    private BlockState blockState;

    public MinecartVisual(VisualizationContext ctx, T entity, float partialTick, EntityModelLayer layerLocation) {
        super(ctx, entity, partialTick);

        instances = InstanceTree.create(instancerProvider(), ModelTrees.of(layerLocation, MATERIAL));
        blockState = entity.getContainedBlock();
        contents = createContentsInstance();

        updateInstances(partialTick);
        updateLight(partialTick);
    }

    @Nullable
    private TransformedInstance createContentsInstance() {
        BlockRenderType shape = blockState.getRenderType();

        if (shape == BlockRenderType.INVISIBLE) {
            return null;
        }

        Block block = blockState.getBlock();
        if (MinecraftClient.getInstance().getBakedModelManager().getBlockEntityModelsSupplier().get().renderers.containsKey(block)) {
            instances.visible(false);
            return null;
        }

        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.block(blockState)).createInstance();
    }

    @Override
    public void tick(TickableVisual.Context context) {
        BlockState displayBlockState = entity.getContainedBlock();

        if (displayBlockState != blockState) {
            blockState = displayBlockState;
            if (contents != null) {
                contents.delete();
            }
            contents = createContentsInstance();
        }
    }

    @Override
    public void beginFrame(DynamicVisual.Context context) {
        if (!isVisible(context.frustum())) {
            return;
        }

        if (!instances.visible()) {
            return;
        }

        updateInstances(context.partialTick());
    }

    private void updateInstances(float partialTick) {
        stack.identity();

        double posX = MathHelper.lerp(partialTick, entity.lastRenderX, entity.getX());
        double posY = MathHelper.lerp(partialTick, entity.lastRenderY, entity.getY());
        double posZ = MathHelper.lerp(partialTick, entity.lastRenderZ, entity.getZ());

        var renderOrigin = renderOrigin();
        stack.translate((float) (posX - renderOrigin.getX()), (float) (posY - renderOrigin.getY()), (float) (posZ - renderOrigin.getZ()));
        float yaw = MathHelper.lerp(partialTick, entity.lastYaw, entity.getYaw());

        long randomBits = entity.getId() * 493286711L;
        randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
        float nudgeX = (((float) (randomBits >> 16 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
        float nudgeY = (((float) (randomBits >> 20 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
        float nudgeZ = (((float) (randomBits >> 24 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
        stack.translate(nudgeX, nudgeY, nudgeZ);

        float pitch = MathHelper.lerp(partialTick, entity.lastPitch, entity.getPitch());
        if (entity.getController() instanceof DefaultMinecartController controller) {
            Vec3d pos = controller.snapPositionToRail(posX, posY, posZ);
            if (pos != null) {
                Vec3d offset1 = controller.simulateMovement(posX, posY, posZ, 0.3F);
                Vec3d offset2 = controller.simulateMovement(posX, posY, posZ, -0.3F);

                if (offset1 == null) {
                    offset1 = pos;
                }

                if (offset2 == null) {
                    offset2 = pos;
                }

                stack.translate((float) (pos.x - posX), (float) ((offset1.y + offset2.y) / 2.0D - posY), (float) (pos.z - posZ));
                Vec3d vec = offset2.add(-offset1.x, -offset1.y, -offset1.z);
                if (vec.length() != 0.0D) {
                    vec = vec.normalize();
                    yaw = (float) (Math.atan2(vec.z, vec.x) * 180.0D / Math.PI);
                    pitch = (float) (Math.atan(vec.y) * 73.0D);
                }
            }
        }

        stack.translate(0.0F, 0.375F, 0.0F);
        stack.rotateY((180 - yaw) * MathHelper.RADIANS_PER_DEGREE);
        stack.rotateZ(-pitch * MathHelper.RADIANS_PER_DEGREE);

        float hurtTime = entity.getDamageWobbleTicks() - partialTick;
        float damage = entity.getDamageWobbleStrength() - partialTick;

        if (damage < 0) {
            damage = 0;
        }

        if (hurtTime > 0) {
            stack.rotateX((MathHelper.sin(hurtTime) * hurtTime * damage / 10.0F * (float) entity.getDamageWobbleSide()) * MathHelper.RADIANS_PER_DEGREE);
        }

        if (contents != null) {
            int displayOffset = entity.getBlockOffset();
            stack.pushMatrix();
            stack.scale(0.75F, 0.75F, 0.75F);
            stack.translate(-0.5F, (float) (displayOffset - 8) / 16, 0.5F);
            stack.rotateY(90 * MathHelper.RADIANS_PER_DEGREE);
            updateContents(contents, stack, partialTick);
            stack.popMatrix();
        }

        stack.scale(-1.0F, -1.0F, 1.0F);
        instances.updateInstances(stack);

        // TODO: Use LightUpdatedVisual/ShaderLightVisual if possible.
        updateLight(partialTick);
    }

    protected void updateContents(TransformedInstance contents, Matrix4f pose, float partialTick) {
        contents.setTransform(pose).setChanged();
    }

    public void updateLight(float partialTick) {
        int packedLight = computePackedLight(partialTick);
        instances.traverse(instance -> {
            instance.light(packedLight).setChanged();
        });
        FlatLit.relight(packedLight, contents);
    }

    @Override
    protected void _delete() {
        instances.delete();
        if (contents != null) {
            contents.delete();
        }
    }

    public static boolean shouldSkipRender(AbstractMinecartEntity minecart) {
        if (minecart.getController() instanceof ExperimentalMinecartController) {
            return true;
        }
        Block block = minecart.getContainedBlock().getBlock();
        return !MinecraftClient.getInstance().getBakedModelManager().getBlockEntityModelsSupplier().get().renderers.containsKey(block);
    }
}
