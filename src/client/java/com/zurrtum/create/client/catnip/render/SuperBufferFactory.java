package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.client.catnip.client.render.model.BakedModelBufferer;
import com.zurrtum.create.client.catnip.client.render.model.ShadeSeparatedResultConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.EmptyVirtualBlockGetter;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BlockRenderLayer;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.model.BlockModelPart;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SuperBufferFactory {

    private static final Random random = Random.create();
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(ThreadLocalObjects::new);
    private static SuperBufferFactory instance = new SuperBufferFactory();

    public static SuperBufferFactory getInstance() {
        return instance;
    }

    static void setInstance(SuperBufferFactory factory) {
        instance = factory;
    }

    public SuperByteBuffer create(BuiltBuffer data) {
        return new ShadeSeparatingSuperByteBuffer(new MutableTemplateMesh(data).toImmutable());
    }

    public SuperByteBuffer createForBlock(BlockState renderedState) {
        MinecraftClient client = MinecraftClient.getInstance();
        BlockStateModel model = client.getBlockRenderManager().getModel(renderedState);
        return createForBlock(model.getParts(client.world != null ? client.world.random : random), renderedState, new MatrixStack());
    }

    public SuperByteBuffer createForBlock(GeometryBakedModel model, BlockState referenceState) {
        return createForBlock(List.of(model), referenceState, new MatrixStack());
    }

    public SuperByteBuffer createForBlock(GeometryBakedModel model, BlockState state, @Nullable MatrixStack poseStack) {
        return createForBlock(List.of(model), state, poseStack);
    }

    public SuperByteBuffer createForBlock(List<BlockModelPart> parts, BlockState state, @Nullable MatrixStack poseStack) {
        ThreadLocalObjects objects = THREAD_LOCAL_OBJECTS.get();
        SbbBuilder sbbBuilder = objects.sbbBuilder;
        sbbBuilder.prepare();
        BakedModelBufferer.bufferModel(parts, BlockPos.ORIGIN, EmptyVirtualBlockGetter.FULL_DARK, state, poseStack, sbbBuilder);
        return sbbBuilder.build();
    }

    private static class SbbBuilder extends SuperByteBufferBuilder implements ShadeSeparatedResultConsumer {
        @Override
        public void accept(BlockRenderLayer renderType, boolean shaded, BuiltBuffer data) {
            add(data, shaded);
        }
    }

    private static class ThreadLocalObjects {
        public final SbbBuilder sbbBuilder = new SbbBuilder();
    }
}
