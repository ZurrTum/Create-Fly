package com.zurrtum.create.client.mixin;

import cc.cassian.rrv.api.recipe.ReliableClientRecipe;
import cc.cassian.rrv.api.recipe.ReliableClientRecipeType;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewMenu;
import cc.cassian.rrv.common.recipe.inventory.RecipeViewScreen;
import cc.cassian.rrv.common.recipe.inventory.ReliablePlainButton;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.compat.rrv.RecipeButton;
import com.zurrtum.create.client.compat.rrv.RecipeTransferHandler;
import com.zurrtum.create.client.compat.rrv.RrvClientPlugin;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(RecipeViewScreen.class)
public class RrvRecipeViewScreenMixin extends AbstractContainerScreen<RecipeViewMenu> {
    private RrvRecipeViewScreenMixin(RecipeViewMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Inject(method = "checkGui()V", at = @At(value = "INVOKE_ASSIGN", target = "Lcc/cassian/rrv/common/recipe/inventory/RecipeViewMenu;guiOffsetLeft()I"), remap = false)
    private void initHandler(
        CallbackInfo ci,
        @Local RecipeViewMenu menu,
        @Share("context") LocalRef<RecipeTransferHandler> ref
    ) {
        Screen screen = menu.getParentScreen();
        ReliableClientRecipeType type = menu.getClientRecipeType();
        List<RecipeTransferHandler> handlers = RrvClientPlugin.TRANSFER.get(type);
        if (handlers != null) {
            for (RecipeTransferHandler handler : handlers) {
                if (handler.checkApplicable(screen, type)) {
                    ref.set(handler);
                    return;
                }
            }
        }
        for (RecipeTransferHandler handler : RrvClientPlugin.UNIVERSAL_TRANSFER) {
            if (handler.checkApplicable(screen, type)) {
                ref.set(handler);
                return;
            }
        }
    }

    @WrapOperation(method = "checkGui()V", at = @At(value = "NEW", target = "(Lnet/minecraft/network/chat/MutableComponent;Lnet/minecraft/client/gui/components/Button$OnPress;IIII)Lcc/cassian/rrv/common/recipe/inventory/ReliablePlainButton;"))
    private ReliablePlainButton createButton(
        MutableComponent literal,
        Button.OnPress o,
        int x,
        int y,
        int width,
        int height,
        Operation<ReliablePlainButton> original,
        @Local RecipeViewMenu menu,
        @Local ReliableClientRecipe currentRecipe,
        @Share("context") LocalRef<RecipeTransferHandler> ref
    ) {
        RecipeTransferHandler handler = ref.get();
        if (handler != null) {
            return new RecipeButton(menu.getParentScreen(), handler, currentRecipe, literal, x, y, width, height);
        }
        return original.call(literal, o, x, y, width, height);
    }

    @Inject(method = "checkGui()V", at = @At(value = "INVOKE", target = "Lcc/cassian/rrv/common/recipe/inventory/RecipeViewScreen;addRenderableWidget(Lnet/minecraft/client/gui/components/events/GuiEventListener;)Lnet/minecraft/client/gui/components/events/GuiEventListener;", ordinal = 0))
    private void initButton(CallbackInfo ci, @Local Button transferButton) {
        if (transferButton instanceof RecipeButton button) {
            button.init();
        }
    }

    @ModifyReceiver(method = "extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private Stream<Slot> filterSlots(
        Stream<Slot> stream,
        Predicate<Slot> predicate,
        @Local(name = "i") int i,
        @Local ReliableClientRecipeType recipeType
    ) {
        int size = recipeType.getSlotCount();
        int index = i * size;
        int end = index + size;
        return stream.filter(slot -> slot.index >= index && slot.index < end);
    }

    @Inject(method = "renderInvalidSlots(Lnet/minecraft/client/gui/GuiGraphicsExtractor;I)V", at = @At(value = "INVOKE", target = "Lcc/cassian/rrv/common/recipe/inventory/RecipeViewScreen;getMenu()Lnet/minecraft/world/inventory/AbstractContainerMenu;", ordinal = 0), cancellable = true)
    private void renderInvalidSlots(
        GuiGraphicsExtractor guiGraphics,
        int displayId,
        CallbackInfo ci,
        @Local Button button
    ) {
        if (button instanceof RecipeButton recipeButton) {
            recipeButton.renderInvalidSlots(getMenu(), guiGraphics, displayId);
            ci.cancel();
        }
    }
}
