package com.zurrtum.create.client;

import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.client.content.trains.bogey.BogeyVisual;
import com.zurrtum.create.client.content.trains.bogey.SizeRenderer;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class AllBogeyStyleRenders {
    public static final Map<Identifier, Map<BogeySize, SizeRenderer>> ALL = new HashMap<>();

    public static void render(
        BogeyStyle style,
        BogeySize size,
        float partialTick,
        MatrixStack poseStack,
        VertexConsumerProvider buffers,
        int light,
        int overlay,
        float wheelAngle,
        @Nullable NbtCompound bogeyData,
        boolean inContraption
    ) {
        Map<BogeySize, SizeRenderer> sizeRenderers = ALL.get(style.id);
        if (sizeRenderers == null) {
            return;
        }
        if (bogeyData == null)
            bogeyData = new NbtCompound();
        poseStack.translate(0, -1.5 - 1 / 128f, 0);
        sizeRenderers.get(size).renderer().render(bogeyData, wheelAngle, partialTick, poseStack, buffers, light, overlay, inContraption);
    }

    @Nullable
    public static BogeyVisual createVisual(BogeyStyle style, BogeySize size, VisualizationContext ctx, float partialTick, boolean inContraption) {
        Map<BogeySize, SizeRenderer> sizeRenderers = ALL.get(style.id);
        if (sizeRenderers == null) {
            return null;
        }
        return sizeRenderers.get(size).visualizer().createVisual(ctx, partialTick, inContraption);
    }

    @SafeVarargs
    public static void register(BogeyStyle style, Supplier<SizeRenderer>... renderers) {
        Set<BogeySize> sizes = style.validSizes();
        if (sizes.size() != renderers.length) {
            throw new IllegalArgumentException("Mismatched number of renderers for bogey style " + style.id);
        }
        int i = 0;
        Map<BogeySize, SizeRenderer> sizeRenderers = new IdentityHashMap<>();
        for (BogeySize size : sizes) {
            sizeRenderers.put(size, renderers[i].get());
            i++;
        }
        ALL.put(style.id, sizeRenderers);
    }

    public static void register() {
        register(AllBogeyStyles.STANDARD, SizeRenderer::small, SizeRenderer::large);
    }
}
