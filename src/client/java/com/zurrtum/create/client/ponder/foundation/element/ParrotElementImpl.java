package com.zurrtum.create.client.ponder.foundation.element;

import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.ponder.api.element.ParrotElement;
import com.zurrtum.create.client.ponder.api.element.ParrotPose;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class ParrotElementImpl extends AnimatedSceneElementBase implements ParrotElement {

    protected Vec3d location;
    @Nullable
    protected ParrotEntity entity;
    protected ParrotPose pose;
    protected Supplier<? extends ParrotPose> initialPose;

    public static ParrotElement create(Vec3d location, Supplier<? extends ParrotPose> pose) {
        return new ParrotElementImpl(location, pose);
    }

    protected ParrotElementImpl(Vec3d location, Supplier<? extends ParrotPose> pose) {
        this.location = location;
        initialPose = pose;
        this.pose = initialPose.get();
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        setPose(initialPose.get());
        entity.setPos(0, 0, 0);
        entity.lastX = 0;
        entity.lastY = 0;
        entity.lastZ = 0;
        entity.lastRenderX = 0;
        entity.lastRenderY = 0;
        entity.lastRenderZ = 0;
        entity.setPitch(entity.lastPitch = 0);
        entity.setYaw(entity.lastYaw = 180);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (entity == null) {
            entity = pose.create(scene.getWorld());
            entity.setYaw(entity.lastYaw = 180);
        }

        entity.age++;
        entity.lastHeadYaw = entity.headYaw;
        entity.lastMaxWingDeviation = entity.maxWingDeviation;
        entity.lastFlapProgress = entity.flapProgress;
        entity.setOnGround(true);

        entity.lastX = entity.getX();
        entity.lastY = entity.getY();
        entity.lastZ = entity.getZ();
        entity.lastYaw = entity.getYaw();
        entity.lastPitch = entity.getPitch();

        pose.tick(scene, entity, location);

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
    public void setRotation(Vec3d eulers, boolean immediate) {
        if (entity == null)
            return;
        entity.setPitch((float) eulers.x);
        entity.setYaw((float) eulers.y);
        if (!immediate)
            return;
        entity.lastPitch = entity.getPitch();
        entity.lastY = entity.getYaw();
    }

    @Override
    public Vec3d getPositionOffset() {
        return entity != null ? entity.getPos() : Vec3d.ZERO;
    }

    @Override
    public Vec3d getRotation() {
        return entity != null ? new Vec3d(entity.getPitch(), entity.getYaw(), 0) : Vec3d.ZERO;
    }

    @Override
    protected void renderLast(PonderLevel world, VertexConsumerProvider buffer, MatrixStack poseStack, float fade, float pt) {
        EntityRenderDispatcher entityrenderermanager = MinecraftClient.getInstance().getEntityRenderDispatcher();

        if (entity == null) {
            entity = pose.create(world);
            entity.setYaw(entity.lastYaw = 180);
        }

        poseStack.push();
        poseStack.translate(location.x, location.y, location.z);
        poseStack.translate(
            MathHelper.lerp(pt, entity.lastX, entity.getX()),
            MathHelper.lerp(pt, entity.lastY, entity.getY()),
            MathHelper.lerp(pt, entity.lastZ, entity.getZ())
        );

        float angle = AngleHelper.angleLerp(pt, entity.lastYaw, entity.getYaw());
        poseStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(angle));

        entityrenderermanager.render(entity, 0, 0, 0, pt, poseStack, buffer, lightCoordsFromFade(fade));
        poseStack.pop();
    }

    @Override
    public void setPose(ParrotPose pose) {
        this.pose = pose;
    }

}