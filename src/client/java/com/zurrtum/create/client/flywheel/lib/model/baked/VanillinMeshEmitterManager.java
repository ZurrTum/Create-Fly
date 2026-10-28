package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.QuadInstance;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.UnknownNullability;

public class VanillinMeshEmitterManager extends MeshEmitterManager<MeshEmitter> implements BufferAoPoseEmitter, FluidRenderer.Output {
    private final TransformingVertexConsumer transformingWrapper = new TransformingVertexConsumer();
    private boolean useAo;
    @UnknownNullability
    private PoseStack poseStack;
    @UnknownNullability
    private BlockPos pos;

    VanillinMeshEmitterManager() {
        super(MeshEmitter::new);
    }

    public void prepare(BlockMaterialFunction blockMaterialFunction, PoseStack poseStack) {
        prepare(blockMaterialFunction);
        transformingWrapper.setPoseStack(poseStack);
        this.poseStack = poseStack;
    }

    public void prepareForModelLayer(boolean useAo) {
        this.useAo = useAo;
    }

    @Override
    public void put(float x, float y, float z, BakedQuad quad, QuadInstance instance) {
        MaterialInfo info = quad.materialInfo();
        BufferBuilder buffer = getBuffer(info.layer(), info.shade(), useAo);
        if (buffer != null) {
            if (x != 0.0F || y != 0.0F || z != 0.0F) {
                poseStack.pushPose();
                Pose pose = poseStack.last();
                pose.translate(x, y, z);
                buffer.putBakedQuad(pose, quad, instance);
                poseStack.popPose();
            } else {
                buffer.putBakedQuad(poseStack.last(), quad, instance);
            }
        }
    }

    public void prepareForFluid(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public VertexConsumer getBuilder(ChunkSectionLayer layer) {
        BufferBuilder buffer = getBuffer(layer, true, false);
        if (buffer == null) {
            return EmptyVertexConsumer.INSTANCE;
        }
        poseStack.translate(
            pos.getX() - (pos.getX() & 0xF),
            pos.getY() - (pos.getY() & 0xF),
            pos.getZ() - (pos.getZ() & 0xF)
        );
        return transformingWrapper.wrap(buffer);
    }

    @Override
    public SimpleModel end() {
        poseStack = null;
        pos = null;
        transformingWrapper.clear();
        return super.end();
    }

    @Override
    public Pose getPose() {
        return poseStack.last();
    }
}
