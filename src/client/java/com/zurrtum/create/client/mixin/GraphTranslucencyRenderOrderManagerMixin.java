package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.foundation.render.RenderTypes;
import de.odysseus.ithaka.digraph.Digraph;
import net.irisshaders.batchedentityrendering.impl.TransparencyType;
import net.irisshaders.batchedentityrendering.impl.ordering.GraphTranslucencyRenderOrderManager;
import net.irisshaders.iris.layer.BlockEntityRenderStateShard;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import net.minecraft.client.render.RenderLayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;

@Mixin(GraphTranslucencyRenderOrderManager.class)
public class GraphTranslucencyRenderOrderManagerMixin {
    @Unique
    private static final RenderLayer additive = RenderTypes.additive();
    @Unique
    private static final RenderLayer translucent = RenderTypes.translucent();
    @Unique
    private static final RenderLayer additive2 = RenderTypes.additive2();
    @Unique
    private static final RenderLayer wrap_additive = OuterWrappedRenderType.wrapExactlyOnce(
        "iris:block_entity",
        RenderTypes.additive(),
        BlockEntityRenderStateShard.INSTANCE
    );
    @Unique
    private static final RenderLayer wrap_translucent = OuterWrappedRenderType.wrapExactlyOnce(
        "iris:block_entity",
        RenderTypes.translucent(),
        BlockEntityRenderStateShard.INSTANCE
    );
    @Unique
    private static final RenderLayer wrap_additive2 = OuterWrappedRenderType.wrapExactlyOnce(
        "iris:block_entity",
        RenderTypes.additive2(),
        BlockEntityRenderStateShard.INSTANCE
    );
    @Shadow(remap = false)
    @Final
    private EnumMap<TransparencyType, Digraph<RenderLayer>> types;

    @Unique
    private static void initOrder(Digraph<RenderLayer> graph) {
        graph.add(additive);
        graph.add(translucent);
        graph.add(additive2);
        graph.add(wrap_additive);
        graph.add(wrap_translucent);
        graph.add(wrap_additive2);
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private void onInit(CallbackInfo ci) {
        initOrder(types.get(TransparencyType.GENERAL_TRANSPARENT));
    }

    @Inject(method = "reset()V", at = @At("TAIL"), remap = false)
    private void onReset(CallbackInfo ci) {
        initOrder(types.get(TransparencyType.GENERAL_TRANSPARENT));
    }

    @Inject(method = "resetType(Lnet/irisshaders/batchedentityrendering/impl/TransparencyType;)V", at = @At("TAIL"), remap = false)
    private void onResetType(TransparencyType type, CallbackInfo ci) {
        if (type == TransparencyType.GENERAL_TRANSPARENT) {
            initOrder(types.get(TransparencyType.GENERAL_TRANSPARENT));
        }
    }
}
