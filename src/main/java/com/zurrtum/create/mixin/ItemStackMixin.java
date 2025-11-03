package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.contraptions.glue.SuperGlueHandler;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryHandler;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.MergedComponentMap;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.BiFunction;

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

    @SuppressWarnings("removal")
    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;ILnet/minecraft/component/MergedComponentMap;)V", at = @At("TAIL"))
    private void create$migrateOldClipboardComponents(ItemConvertible item, int count, MergedComponentMap components, CallbackInfo ci) {
        ClipboardContent content = ClipboardContent.EMPTY;

        content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_PAGES, ClipboardContent::setPages);
        content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_TYPE, ClipboardContent::setType);
        content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_READ_ONLY, (c, v) -> c.setReadOnly(true));
        content = create$migrateComponent(content, components, AllDataComponents.CLIPBOARD_COPIED_VALUES, ClipboardContent::setCopiedValues);
        content = create$migrateComponent(
            content,
            components,
            AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE,
            ClipboardContent::setPreviouslyOpenedPage
        );

        if (content != ClipboardContent.EMPTY) {
            components.set(AllDataComponents.CLIPBOARD_CONTENT, content);
        }
    }

    @Unique
    private static <T> ClipboardContent create$migrateComponent(
        ClipboardContent content,
        MergedComponentMap components,
        ComponentType<T> componentType,
        BiFunction<ClipboardContent, T, ClipboardContent> function
    ) {
        T value = components.get(componentType);
        if (value != null) {
            components.remove(componentType);
            content = function.apply(content, value);
        }

        return content;
    }
}
