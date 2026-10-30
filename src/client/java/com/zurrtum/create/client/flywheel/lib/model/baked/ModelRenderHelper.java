package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ModelRenderHelper {
    private static @UnknownNullability ModelConsumer INSTANCE;
    private static @UnknownNullability ModelConsumer CULL_INSTANCE;
    private static @UnknownNullability ModelConsumer AO_INSTANCE;
    private static @UnknownNullability ModelConsumer AO_CULL_INSTANCE;
    private static final ThreadLocal<ThreadLocalObjects> THREAD_LOCAL_OBJECTS = ThreadLocal.withInitial(
        ModelRenderHelper::createLocalObjects);

    public static ModelConsumer getCullHelper(BufferEmitterOutput output) {
        CULL_INSTANCE.updateOutput(output);
        return CULL_INSTANCE;
    }

    public static ModelConsumer getHelper(BufferEmitterOutput output) {
        INSTANCE.updateOutput(output);
        return INSTANCE;
    }

    public static ModelConsumer getAoCullHelper(BufferAoEmitter output) {
        AO_CULL_INSTANCE.updateOutput(output);
        return AO_CULL_INSTANCE;
    }

    public static ModelConsumer getAoHelper(BufferAoEmitter output) {
        AO_INSTANCE.updateOutput(output);
        return AO_INSTANCE;
    }

    public static ModelConsumer getCurrentThreadCullHelper(BufferEmitterOutput output) {
        ModelConsumer cullInstance = THREAD_LOCAL_OBJECTS.get().cullInstance;
        cullInstance.updateOutput(output);
        return cullInstance;
    }

    public static ModelConsumer getCurrentThreadHelper(BufferEmitterOutput output) {
        ModelConsumer instance = THREAD_LOCAL_OBJECTS.get().instance;
        instance.updateOutput(output);
        return instance;
    }

    public static ModelConsumer getCurrentThreadAoCullHelper(BufferAoEmitter output) {
        ModelConsumer cullInstance = THREAD_LOCAL_OBJECTS.get().aoCullInstance;
        cullInstance.updateOutput(output);
        return cullInstance;
    }

    public static ModelConsumer getCurrentThreadAoHelper(BufferAoEmitter output) {
        ModelConsumer instance = THREAD_LOCAL_OBJECTS.get().aoInstance;
        instance.updateOutput(output);
        return instance;
    }

    public static void onReloadLevelRenderer() {
        Minecraft mc = Minecraft.getInstance();
        boolean ao = mc.options.ambientOcclusion().get();
        BlockColors blockColors = mc.getBlockColors();
        AO_INSTANCE = INSTANCE = new Consumer(ao, false, blockColors);
        AO_CULL_INSTANCE = CULL_INSTANCE = new Consumer(ao, true, blockColors);
        ThreadLocalObjects.reload(ao, blockColors);
    }

    private static void onReloadLevelRenderer(boolean ao, BlockColors blockColors, ThreadLocalObjects objects) {
        objects.aoInstance = objects.instance = new Consumer(ao, false, blockColors);
        objects.aoCullInstance = objects.cullInstance = new Consumer(ao, true, blockColors);
    }

    private static ThreadLocalObjects createLocalObjects() {
        Minecraft mc = Minecraft.getInstance();
        boolean ao = mc.options.ambientOcclusion().get();
        BlockColors blockColors = mc.getBlockColors();
        return new ThreadLocalObjects(ao, blockColors);
    }

    private static class Consumer implements ModelConsumer {
        private final ModelBlockRenderer renderer;
        private @UnknownNullability BufferEmitterOutput output;

        public Consumer(boolean ambientOcclusion, boolean cull, BlockColors blockColors) {
            renderer = new ModelBlockRenderer(ambientOcclusion, cull, blockColors);
        }

        @Override
        public void updateOutput(BufferEmitterOutput output) {
            this.output = output;
        }

        @Override
        public void tesselateBlock(
            float x,
            float y,
            float z,
            BlockAndTintGetter level,
            BlockPos pos,
            BlockState blockState,
            BlockStateModel model,
            long seed
        ) {
            renderer.tesselateBlock(output, x, y, z, level, pos, blockState, model, seed);
        }
    }

    public static class ThreadLocalObjects {
        public static List<ThreadLocalObjects> ALL = new CopyOnWriteArrayList<>();
        public @UnknownNullability ModelConsumer instance;
        public @UnknownNullability ModelConsumer cullInstance;
        public @UnknownNullability ModelConsumer aoInstance;
        public @UnknownNullability ModelConsumer aoCullInstance;

        public ThreadLocalObjects(boolean ao, BlockColors blockColors) {
            ALL.add(this);
            onReloadLevelRenderer(ao, blockColors, this);
        }

        public static void reload(boolean ao, BlockColors blockColors) {
            for (ThreadLocalObjects objects : ALL) {
                onReloadLevelRenderer(ao, blockColors, objects);
            }
        }
    }
}
