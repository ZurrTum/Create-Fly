package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.zurrtum.create.client.vanillin.VanillaVisuals;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockEntityRenderDispatcher.class)
public class BlockEntityRenderDispatcherMixin {
    @Inject(method = "onResourceManagerReload(Lnet/minecraft/server/packs/resources/ResourceManager;)V", at = @At("TAIL"))
    private void onReload(
        ResourceManager resourceManager,
        CallbackInfo ci,
        @Local BlockEntityRendererProvider.Context context
    ) {
        VanillaVisuals.onReloadModel(context.entityModelSet(), context.blockModelResolver());
    }

    @Inject(method = "tryExtractRenderState", at = @At("RETURN"))
    private <E extends BlockEntity, S extends BlockEntityRenderState> void fillSkippedBaseState(
        E blockEntity,
        float tickProgress,
        net.minecraft.client.renderer.feature.ModelFeatureRenderer.CrumblingOverlay crumblingOverlay,
        CallbackInfoReturnable<S> cir
    ) {
        S state = cir.getReturnValue();
        if (state == null) {
            return;
        }
        if (state.blockEntityType == BlockEntityType.TEST_BLOCK && state.blockState.is(Blocks.AIR)) {
            BlockEntityRenderState.extractBase(blockEntity, state, crumblingOverlay);
        }
    }
}
