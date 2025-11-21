package com.zurrtum.create.client.ponder.foundation.instruction;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.ponder.api.element.MinecartElement;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.AnimatedSceneElementBase;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.CameraRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class MinecartElementImpl extends AnimatedSceneElementBase implements MinecartElement {

    private final Vec3 location;
    private final LerpedFloat rotation;
    @Nullable
    private AbstractMinecart entity;
    private final MinecartConstructor constructor;
    private final float initialRotation;

    public MinecartElementImpl(Vec3 location, float rotation, MinecartConstructor constructor) {
        initialRotation = rotation;
        this.location = location.add(0, 1 / 16f, 0);
        this.constructor = constructor;
        this.rotation = LerpedFloat.angular().startWithValue(rotation);
    }

    @Override
    public void reset(PonderScene scene) {
        super.reset(scene);
        entity.setPosRaw(0, 0, 0);
        entity.xo = 0;
        entity.yo = 0;
        entity.zo = 0;
        entity.xOld = 0;
        entity.yOld = 0;
        entity.zOld = 0;
        rotation.startWithValue(initialRotation);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (entity == null)
            entity = constructor.create(scene.getLevel(), 0, 0, 0);

        entity.tickCount++;
        entity.setOnGround(true);
        entity.xo = entity.getX();
        entity.yo = entity.getY();
        entity.zo = entity.getZ();
        entity.xOld = entity.getX();
        entity.yOld = entity.getY();
        entity.zOld = entity.getZ();
    }

    @Override
    public void setPositionOffset(Vec3 position, boolean immediate) {
        if (entity == null)
            return;
        entity.setPosRaw(position.x, position.y, position.z);
        if (!immediate)
            return;
        entity.xo = position.x;
        entity.yo = position.y;
        entity.zo = position.z;
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
    public Vec3 getPositionOffset() {
        return entity != null ? entity.position() : Vec3.ZERO;
    }

    @Override
    public Vec3 getRotation() {
        return new Vec3(0, rotation.getValue(), 0);
    }

    @Override
    public void renderLast(
        EntityRenderDispatcher entityRenderManager,
        ItemModelResolver itemModelManager,
        PonderLevel world,
        MultiBufferSource buffer,
        SubmitNodeCollector queue,
        Camera camera,
        CameraRenderState cameraRenderState,
        PoseStack poseStack,
        float fade,
        float pt
    ) {
        if (entity == null)
            entity = constructor.create(world, 0, 0, 0);

        poseStack.pushPose();
        poseStack.translate(location.x, location.y, location.z);
        poseStack.translate(Mth.lerp(pt, entity.xo, entity.getX()), Mth.lerp(pt, entity.yo, entity.getY()), Mth.lerp(pt, entity.zo, entity.getZ()));

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation.getValue(pt)));

        EntityRenderState state = entityRenderManager.extractEntity(entity, pt);
        entityRenderManager.submit(state, cameraRenderState, 0, 0, 0, poseStack, queue);
        poseStack.popPose();
    }

}