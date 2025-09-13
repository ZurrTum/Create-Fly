package com.zurrtum.create.mixin;

import com.zurrtum.create.content.equipment.symmetryWand.SymmetryPlacementContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemPlacementContext.class)
public class ItemPlacementContextMixin {
    @Shadow
    protected boolean canReplaceExisting;

    @Shadow
    public BlockPos placementPos;

    @Inject(method = "<init>(Lnet/minecraft/item/ItemUsageContext;)V", at = @At("TAIL"))
    private void init(ItemUsageContext context, CallbackInfo ci) {
        if (context instanceof SymmetryPlacementContext placementContext) {
            this.canReplaceExisting = placementContext.canReplaceExisting();
            this.placementPos = placementContext.placementPos;
        }
    }
}
