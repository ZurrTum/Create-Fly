package com.zurrtum.create.client.catnip.render;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.catnip.client.render.model.BakedModelBufferer;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.BlockStateModel;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.chunk.ChunkSectionLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class SuperBufferFactory {

    private static final RandomSource random = RandomSource.create();
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
    private static SuperBufferFactory instance = new SuperBufferFactory();

    public static SuperBufferFactory getInstance() {
        return instance;
    }

    static void setInstance(SuperBufferFactory factory) {
        instance = factory;
    }

    public SuperByteBuffer create(MeshData data) {
        return new ShadeSeparatingSuperByteBuffer(new MutableTemplateMesh(data).toImmutable());
    }

    public SuperByteBuffer createForBlock(BlockState renderedState) {
        Minecraft client = Minecraft.getInstance();
        BlockStateModel model = client.getBlockRenderer().getBlockModel(renderedState);
        return createForBlock(model.collectParts(client.level != null ? client.level.getRandom() : random), renderedState, new PoseStack());
    }

    public SuperByteBuffer createForBlock(SimpleModelWrapper model, BlockState referenceState) {
        return createForBlock(List.of(model), referenceState, new PoseStack());
    }

    public SuperByteBuffer createForBlock(SimpleModelWrapper model, BlockState state, @Nullable PoseStack poseStack) {
        return createForBlock(List.of(model), state, poseStack);
    }

    public SuperByteBuffer createForBlock(List<BlockModelPart> parts, BlockState state, @Nullable PoseStack poseStack) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        SbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.prepare();
        BakedModelBufferer.bufferModel(parts, BlockPos.ZERO, EmptyVirtualBlockGetter.FULL_DARK, state, poseStack, sbbBuilder);
        return sbbBuilder.build();
    }

    private static class SbbBuilder extends SuperByteBufferBuilder implements ShadeSeparatedResultConsumer {
        @Override
        public void accept(ChunkSectionLayer renderType, boolean shaded, MeshData data) {
            add(data, shaded);
        }
    }

    private static class ThreadLocalObjects {
        public final SbbBuilder sbbBuilder = new SbbBuilder();
    }
}
