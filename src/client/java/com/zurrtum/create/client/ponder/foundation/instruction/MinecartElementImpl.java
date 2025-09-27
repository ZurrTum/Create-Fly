package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.ponder.api.element.MinecartElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.AnimatedSceneElementBase;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

public class MinecartElementImpl extends AnimatedSceneElementBase implements MinecartElement {

    private final Vec3d location;
    private final LerpedFloat rotation;
    @Nullable
    private AbstractMinecartEntity entity;
    private final MinecartConstructor constructor;
    private final float initialRotation;

    public MinecartElementImpl(Vec3d location, float rotation, MinecartConstructor constructor) {
        initialRotation = rotation;
        this.location = location.add(0, 1 / 16f, 0);
        this.constructor = constructor;
        this.rotation = LerpedFloat.angular().startWithValue(rotation);
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        entity.setPos(0, 0, 0);
        entity.lastX = 0;
        entity.lastY = 0;
        entity.lastZ = 0;
        entity.lastRenderX = 0;
        entity.lastRenderY = 0;
        entity.lastRenderZ = 0;
        rotation.startWithValue(initialRotation);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (entity == null)
            entity = constructor.create(scene.getWorld(), 0, 0, 0);

        entity.age++;
        entity.setOnGround(true);
        entity.lastX = entity.getX();
        entity.lastY = entity.getY();
        entity.lastZ = entity.getZ();
        entity.lastRenderX = entity.getX();
        entity.lastRenderY = entity.getY();
        entity.lastRenderZ = entity.getZ();
    }

    @Override
    public void setPositionOffset(Vec3d position, boolean immediate) {
        if (entity == null)
            return;
        entity.setPos(position.x, position.y, position.z);
        if (!immediate)
            return;
        entity.lastX = position.x;
        entity.lastY = position.y;
        entity.lastZ = position.z;
    }

    @Override
    public void setRotation(float angle, boolean immediate) {
        if (entity == null)
            return;
        rotation.setValue(angle);
        if (!immediate)
            return;
        rotation.startWithValue(angle);
    }

    @Override
    public Vec3d getPositionOffset() {
        return entity != null ? entity.getPos() : Vec3d.ZERO;
    }

    @Override
    public Vec3d getRotation() {
        return new Vec3d(0, rotation.getValue(), 0);
    }

    @Override
    public void renderLast(PonderLevel world, VertexConsumerProvider buffer, MatrixStack poseStack, float fade, float pt) {
        EntityRenderDispatcher entityrenderermanager = MinecraftClient.getInstance().getEntityRenderDispatcher();
        if (entity == null)
            entity = constructor.create(world, 0, 0, 0);

        poseStack.push();
        poseStack.translate(location.x, location.y, location.z);
        poseStack.translate(
            MathHelper.lerp(pt, entity.lastX, entity.getX()),
            MathHelper.lerp(pt, entity.lastY, entity.getY()),
            MathHelper.lerp(pt, entity.lastZ, entity.getZ())
        );

        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(rotation.getValue(pt)));

        entityrenderermanager.render(entity, 0, 0, 0, pt, poseStack, buffer, lightCoordsFromFade(fade));
        poseStack.pop();
    }

}