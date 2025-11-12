package com.zurrtum.create.mixin;

import com.zurrtum.create.content.equipment.symmetryWand.SymmetryPlacementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockPlaceContext.class)
public class ItemPlacementContextMixin {
    @Shadow
    protected boolean replaceClicked;

    @Shadow
    public BlockPos relativePos;

    @Inject(method = "<init>(Lnet/minecraft/world/item/context/UseOnContext;)V", at = @At("TAIL"))
    private void init(UseOnContext context, CallbackInfo ci) {
        if (context instanceof SymmetryPlacementContext placementContext) {
            this.replaceClicked = placementContext.replacingClickedOnBlock();
            this.relativePos = placementContext.relativePos;
        }
    }
}
