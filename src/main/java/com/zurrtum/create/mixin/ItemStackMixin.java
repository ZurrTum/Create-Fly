package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private void cacheState(
        ItemUsageContext context,
        CallbackInfoReturnable<ActionResult> cir,
        @Local Item item,
        @Share("place") LocalRef<ItemPlacementContext> place
    ) {
        if (item instanceof BlockItem) {
            World world = context.getWorld();
            if (!world.isClient()) {
                place.set(new ItemPlacementContext(context));
            }
        }
    }

    @Inject(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/ActionResult$Success;shouldIncrementStat()Z"))
    private void useOnBlock(
        ItemUsageContext context,
        CallbackInfoReturnable<ActionResult> cir,
        @Local PlayerEntity player,
        @Share("place") LocalRef<ItemPlacementContext> place
    ) {
        ItemPlacementContext placementContext = place.get();
        if (placementContext != null) {
            ServerWorld world = (ServerWorld) context.getWorld();
            BlockPos pos = placementContext.getBlockPos();
            SuperGlueHandler.glueListensForBlockPlacement(world, player, pos);
            if (!context.getStack().components.contains(DataComponentTypes.CONSUMABLE)) {
                SymmetryHandler.onBlockPlaced(world, player, pos, placementContext);
            }
        }
    }
}
