package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.BufferPoseEmitter;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector3f;

public class EntityBlockTransformSbbBuilder extends EntityBlockSbbBuilder implements BufferPoseEmitter {
    private final Pose pose = new Pose();
    private @UnknownNullability Pose origin;

    public EntityBlockTransformSbbBuilder() {
        super();
    }

    public EntityBlockTransformSbbBuilder(EntityBlockSbbBuilder builder) {
        super(builder);
    }

    public EntityBlockTransformSbbBuilder wrap(Pose pose) {
        origin = pose;
        return this;
    }

    @Override
    protected void shadeDirectionOverride(Direction direction, Vector3f normal) {
        origin.transformNormal(direction.getUnitVec3f(), normal);
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        VertexConsumer buffer = getBuffer(info.shadeDirectionOverride(), info.layer());
        if (x != 0 || y != 0 || z != 0) {
            pose.set(origin);
            pose.translate(x, y, z);
            buffer.putBakedQuad(pose, quad, instance);
        } else {
            buffer.putBakedQuad(origin, quad, instance);
        }
    }

    @Override
    public SuperByteBuffer build() {
        origin = null;
        return super.build();
    }

    @Override
    public Pose getPose() {
        return origin;
    }
}
