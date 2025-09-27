package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.foundation.item.TooltipModifier;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.ponder.foundation.PonderTooltipHandler;
import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Shadow
    public abstract Item getItem();

    @ModifyReturnValue(method = "getTooltip", at = @At("RETURN"))
    private List<Text> appendTooltip(List<Text> tooltip, @Local(argsOnly = true) PlayerEntity player) {
        PonderTooltipHandler.addToTooltip(tooltip, (ItemStack) (Object) this);
        if (!AllConfigs.client().tooltips.get() || player == null)
            return tooltip;
        TooltipModifier modifier = TooltipModifier.REGISTRY.get(getItem());
        if (modifier != null) {
            modifier.modify(tooltip, player);
        }
        return tooltip;
    }

    @Inject(method = "useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/Item;useOnBlock(Lnet/minecraft/item/ItemUsageContext;)Lnet/minecraft/util/ActionResult;"))
    private void cacheState(
        ItemUsageContext context,
        CallbackInfoReturnable<ActionResult> cir,
        @Local Item item,
        @Share("place") LocalRef<ItemPlacementContext> place
    ) {
        if (item instanceof BlockItem) {
            World world = context.getWorld();
            if (!world.isClient) {
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
