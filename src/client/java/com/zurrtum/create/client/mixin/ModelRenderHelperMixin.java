package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.flywheel.lib.model.baked.FabricAoModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.FabricModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelConsumer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelRenderHelper.ThreadLocalObjects;
import net.fabricmc.fabric.api.client.renderer.v1.Renderer;
import net.fabricmc.fabric.api.client.renderer.v1.render.AltModelBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import org.jetbrains.annotations.UnknownNullability;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ModelRenderHelper.class)
public class ModelRenderHelperMixin {
    @Shadow
    private static @UnknownNullability ModelConsumer CULL_INSTANCE;
    @Shadow
    private static @UnknownNullability ModelConsumer INSTANCE;
    @Shadow
    private static @UnknownNullability ModelConsumer AO_CULL_INSTANCE;
    @Shadow
    private static @UnknownNullability ModelConsumer AO_INSTANCE;

    @Overwrite(remap = false)
    public static void onReloadLevelRenderer() {
        Minecraft mc = Minecraft.getInstance();
        boolean ao = mc.options.ambientOcclusion().get();
        BlockColors blockColors = mc.getBlockColors();
        Renderer renderer = Renderer.get();
        AltModelBlockRenderer aoRender = renderer.altModelBlockRenderer(ao, false, blockColors);
        AltModelBlockRenderer cullRender = renderer.altModelBlockRenderer(ao, true, blockColors);
        INSTANCE = new FabricModelConsumer(aoRender);
        CULL_INSTANCE = new FabricModelConsumer(cullRender);
        if (ao) {
            AO_INSTANCE = new FabricAoModelConsumer(aoRender);
            AO_CULL_INSTANCE = new FabricAoModelConsumer(cullRender);
        } else {
            AO_INSTANCE = INSTANCE;
            AO_CULL_INSTANCE = CULL_INSTANCE;
        }
    }

    @Overwrite(remap = false)
    private static void onReloadLevelRenderer(boolean ao, BlockColors blockColors, ThreadLocalObjects objects) {
        Renderer renderer = Renderer.get();
        AltModelBlockRenderer aoRender = renderer.altModelBlockRenderer(ao, false, blockColors);
        AltModelBlockRenderer cullRender = renderer.altModelBlockRenderer(ao, true, blockColors);
        objects.instance = new FabricModelConsumer(aoRender);
        objects.cullInstance = new FabricModelConsumer(cullRender);
        if (ao) {
            objects.aoInstance = new FabricAoModelConsumer(aoRender);
            objects.aoCullInstance = new FabricAoModelConsumer(cullRender);
        } else {
            objects.aoInstance = objects.instance;
            objects.aoCullInstance = objects.cullInstance;
        }
    }
}
