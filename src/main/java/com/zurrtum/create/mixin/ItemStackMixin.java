package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {
    @Inject(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;"))
    private void cacheState(
        UseOnContext context,
        CallbackInfoReturnable<InteractionResult> cir,
        @Local Item item,
        @Share("place") LocalRef<BlockPlaceContext> place
    ) {
        if (item instanceof BlockItem) {
            Level world = context.getLevel();
            if (!world.isClientSide()) {
                place.set(new BlockPlaceContext(context));
            }
        }
    }

    @Inject(method = "useOn(Lnet/minecraft/world/item/context/UseOnContext;)Lnet/minecraft/world/InteractionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionResult$Success;wasItemInteraction()Z"))
    private void useOnBlock(
        UseOnContext context,
        CallbackInfoReturnable<InteractionResult> cir,
        @Local Player player,
        @Share("place") LocalRef<BlockPlaceContext> place
    ) {
        BlockPlaceContext placementContext = place.get();
        if (placementContext != null) {
            ServerLevel world = (ServerLevel) context.getLevel();
            BlockPos pos = placementContext.getClickedPos();
            SuperGlueHandler.glueListensForBlockPlacement(world, player, pos);
            if (!context.getItemInHand().components.has(DataComponents.CONSUMABLE)) {
                SymmetryHandler.onBlockPlaced(world, player, pos, placementContext);
            }
        }
    }
}
