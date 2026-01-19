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
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.entity.vehicle.minecart.MinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.NewMinecartBehavior;
import net.minecraft.world.entity.vehicle.minecart.OldMinecartBehavior;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class MinecartVisual<T extends AbstractMinecart> extends AbstractEntityVisual<T> implements SimpleTickableVisual, SimpleDynamicVisual {
    private static final Identifier TEXTURE = Identifier.withDefaultNamespace("textures/entity/minecart.png");
    private static final Material MATERIAL = SimpleMaterial.builder().texture(TEXTURE).mipmap(false).build();

    private final InstanceTree instances;
    @Nullable
    private TransformedInstance contents;

    private final Matrix4fStack stack = new Matrix4fStack(2);

    private BlockState blockState;

    public MinecartVisual(VisualizationContext ctx, T entity, float partialTick, ModelLayerLocation layerLocation) {
        super(ctx, entity, partialTick);

        instances = InstanceTree.create(instancerProvider(), ModelTrees.of(layerLocation, MATERIAL));
        blockState = entity.getDisplayBlockState();
        contents = createContentsInstance();

        updateInstances(partialTick);
        updateLight(partialTick);
    }

    @Nullable
    private TransformedInstance createContentsInstance() {
        RenderShape shape = blockState.getRenderShape();

        if (shape == RenderShape.INVISIBLE) {
            return null;
        }

        Block block = blockState.getBlock();
        if (Minecraft.getInstance().getModelManager().specialBlockModelRenderer().renderers.containsKey(block)) {
            instances.visible(false);
            return null;
        }

        return instancerProvider().instancer(InstanceTypes.TRANSFORMED, Models.block(blockState)).createInstance();
    }

    @Override
    public void tick(TickableVisual.Context context) {
        BlockState displayBlockState = entity.getDisplayBlockState();

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

        MinecartBehavior behavior = entity.getBehavior();
        if (behavior instanceof OldMinecartBehavior oldMinecartBehavior) {
            updateOldMinecart(entity, oldMinecartBehavior, partialTick);
        } else {
            updateNewMinecart(entity, (NewMinecartBehavior) behavior, partialTick);
        }

        float hurtTime = entity.getHurtTime() - partialTick;
        float damage = entity.getDamage() - partialTick;

        if (damage < 0) {
            damage = 0;
        }

        if (hurtTime > 0) {
            stack.rotateX((Mth.sin(hurtTime) * hurtTime * damage / 10.0F * (float) entity.getHurtDir()) * Mth.DEG_TO_RAD);
        }

        if (contents != null) {
            int displayOffset = entity.getDisplayOffset();
            stack.pushMatrix();
            stack.scale(0.75F, 0.75F, 0.75F);
            stack.translate(-0.5F, (float) (displayOffset - 8) / 16, 0.5F);
            stack.rotateY(90 * Mth.DEG_TO_RAD);
            updateContents(contents, stack, partialTick);
            stack.popMatrix();
        }

        stack.scale(-1.0F, -1.0F, 1.0F);
        instances.updateInstances(stack);

        // TODO: Use LightUpdatedVisual/ShaderLightVisual if possible.
        updateLight(partialTick);
    }

    private void updatePosition(T entity, double posX, double posY, double posZ) {
        var renderOrigin = renderOrigin();
        stack.translate((float) (posX - renderOrigin.getX()), (float) (posY - renderOrigin.getY()), (float) (posZ - renderOrigin.getZ()));
        long randomBits = entity.getId() * 493286711L;
        randomBits = randomBits * randomBits * 4392167121L + randomBits * 98761L;
        float nudgeX = (((float) (randomBits >> 16 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
        float nudgeY = (((float) (randomBits >> 20 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
        float nudgeZ = (((float) (randomBits >> 24 & 7L) + 0.5f) / 8.0f - 0.5F) * 0.004f;
        stack.translate(nudgeX, nudgeY, nudgeZ);
    }

    private void updateOldMinecart(T entity, OldMinecartBehavior behavior, float partialTick) {
        double posX = Mth.lerp(partialTick, entity.xOld, entity.getX());
        double posY = Mth.lerp(partialTick, entity.yOld, entity.getY());
        double posZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
        updatePosition(entity, posX, posY, posZ);
        Vec3 vec = behavior.getPos(posX, posY, posZ);
        if (vec != null) {
            Vec3 offset1 = Objects.requireNonNullElse(behavior.getPosOffs(posX, posY, posZ, 0.3F), vec);
            Vec3 offset2 = Objects.requireNonNullElse(behavior.getPosOffs(posX, posY, posZ, -0.3F), vec);
            stack.translate((float) (vec.x - posX), (float) ((offset1.y + offset2.y) / 2.0D - posY), (float) (vec.z - posZ));
            vec = offset2.add(-offset1.x, -offset1.y, -offset1.z);
            vec = vec.length() != 0.0D ? vec.normalize() : null;
        }
        float pitch, yaw;
        if (vec == null) {
            yaw = entity.getYRot(partialTick);
            pitch = entity.getXRot(partialTick);
        } else {
            yaw = (float) (Math.atan2(vec.z, vec.x) * 180.0D / Math.PI);
            pitch = (float) (Math.atan(vec.y) * 73.0D);
        }
        stack.translate(0.0F, 0.375F, 0.0F);
        stack.rotateY((180 - yaw) * Mth.DEG_TO_RAD);
        stack.rotateZ(-pitch * Mth.DEG_TO_RAD);
    }

    private void updateNewMinecart(T entity, NewMinecartBehavior behavior, float partialTick) {
        if (behavior.cartHasPosRotLerp()) {
            Vec3 pos = behavior.getCartLerpPosition(partialTick);
            updatePosition(entity, pos.x, pos.y, pos.z);
            stack.rotateY(behavior.getCartLerpYRot(partialTick) * Mth.DEG_TO_RAD);
            stack.rotateZ(-behavior.getCartLerpXRot(partialTick) * Mth.DEG_TO_RAD);
        } else {
            double posX = Mth.lerp(partialTick, entity.xOld, entity.getX());
            double posY = Mth.lerp(partialTick, entity.yOld, entity.getY());
            double posZ = Mth.lerp(partialTick, entity.zOld, entity.getZ());
            updatePosition(entity, posX, posY, posZ);
            stack.rotateY(entity.getYRot() * Mth.DEG_TO_RAD);
            stack.rotateZ(-entity.getXRot() * Mth.DEG_TO_RAD);
        }
        stack.translate(0.0F, 0.375F, 0.0F);
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

    public static boolean shouldSkipRender(AbstractMinecart minecart) {
        Block block = minecart.getDisplayBlockState().getBlock();
        return !Minecraft.getInstance().getModelManager().specialBlockModelRenderer().renderers.containsKey(block);
    }
}
