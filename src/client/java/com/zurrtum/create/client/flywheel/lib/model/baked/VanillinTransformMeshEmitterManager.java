package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.Direction;
import org.jetbrains.annotations.UnknownNullability;
import org.joml.Vector3f;

public class VanillinTransformMeshEmitterManager extends VanillinMeshEmitterManager implements BufferAoPoseEmitter {
    private final Pose pose = new Pose();
    private @UnknownNullability Pose origin;

    VanillinTransformMeshEmitterManager(VanillinMeshEmitterManager manager) {
        super(manager);
        fluidBufferBuilder = new FluidTransformBufferBuilder();
    }

    public VanillinMeshEmitterManager prepare(BlockMaterialFunction blockMaterialFunction, PoseStack poseStack) {
        prepare(blockMaterialFunction);
        origin = poseStack.last();
        return this;
    }

    @Override
    protected void shadeDirectionOverride(Direction direction, Vector3f normal) {
        origin.transformNormal(direction.getUnitVec3f(), normal);
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        VertexConsumer buffer = getBuffer(info.layer(), info.shadeDirectionOverride(), useAo);
        if (buffer != null) {
            if (x != 0.0F || y != 0.0F || z != 0.0F) {
                pose.set(origin);
                pose.translate(x, y, z);
                buffer.putBakedQuad(pose, quad, instance);
            } else {
                buffer.putBakedQuad(origin, quad, instance);
            }
        }
    }

    @Override
    public SimpleModel end() {
        origin = null;
        return super.end();
    }

    @Override
    public Pose getPose() {
        return origin;
    }

    protected class FluidTransformBufferBuilder extends FluidBufferBuilder {
        private static final Vector3f position = new Vector3f();
        private static final Vector3f normal = new Vector3f();

        @Override
        public void addVertex(
            float x,
            float y,
            float z,
            int color,
            float u,
            float v,
            int overlayCoords,
            int lightCoords,
            float nx,
            float ny,
            float nz
        ) {
            position.set(fluidX + x, fluidY + y, fluidZ + z).mulPosition(origin.pose());
            origin.transformNormal(nx, ny, nz, normal);
            buffer.addVertex(
                position.x,
                position.y,
                position.z,
                color,
                u,
                v,
                overlayCoords,
                lightCoords,
                normal.x,
                normal.y,
                normal.z
            );
        }
    }
}
