package com.zurrtum.create.client.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.zurrtum.create.client.compat.eiv.*;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import de.crafty.eiv.common.api.recipe.IEivViewRecipe;
import de.crafty.eiv.common.recipe.inventory.RecipeViewMenu;
import de.crafty.eiv.common.recipe.inventory.RecipeViewScreen;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(RecipeViewScreen.class)
public abstract class RecipeViewScreenMixin extends HandledScreen<RecipeViewMenu> {
    private RecipeViewScreenMixin(RecipeViewMenu handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Inject(method = "checkGui()V", at = @At("HEAD"), remap = false)
    private void initHandler(CallbackInfo ci, @Share("context") LocalRef<RecipeTransferContext> ref) {
        RecipeViewMenu menu = getScreenHandler();
        Screen screen = menu.getParentScreen();
        List<RecipeTransferHandler> handlers = EivClientPlugin.TRANSFER.get(menu.getViewType());
        if (handlers != null) {
            for (RecipeTransferHandler handler : handlers) {
                if (handler.checkApplicable(screen)) {
                    ref.set(new RecipeTransferContext(menu, screen, handler));
                    return;
                }
            }
        }
        for (RecipeTransferHandler handler : EivClientPlugin.UNIVERSAL_TRANSFER) {
            if (handler.checkApplicable(screen)) {
                ref.set(new RecipeTransferContext(menu, screen, handler));
                return;
            }
        }
    }

    @WrapOperation(method = "checkGui()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/widget/ButtonWidget;builder(Lnet/minecraft/text/Text;Lnet/minecraft/client/gui/widget/ButtonWidget$PressAction;)Lnet/minecraft/client/gui/widget/ButtonWidget$Builder;"))
    private ButtonWidget.Builder createButton(
        Text message,
        ButtonWidget.PressAction onPress,
        Operation<ButtonWidget.Builder> original,
        @Local IEivViewRecipe view,
        @Share("context") LocalRef<RecipeTransferContext> ref
    ) {
        RecipeTransferContext context = ref.get();
        if (context != null) {
            return RecipeButton.builder(message, context, view);
        }
        return original.call(message, onPress);
    }

    @Inject(method = "checkGui()V", at = @At(value = "INVOKE", target = "Lde/crafty/eiv/common/recipe/inventory/RecipeViewScreen;addDrawableChild(Lnet/minecraft/client/gui/Element;)Lnet/minecraft/client/gui/Element;"))
    private void initButton(CallbackInfo ci, @Local ButtonWidget button) {
        if (button instanceof RecipeButton recipeButton) {
            recipeButton.init();
        }
    }

    @WrapOperation(method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V", at = @At(value = "INVOKE", target = "Lde/crafty/eiv/common/recipe/inventory/RecipeViewMenu;guiOffsetTop(I)I"))
    private int cacheIndex(RecipeViewMenu instance, int displayIndex, Operation<Integer> original, @Share("index") LocalIntRef ref) {
        ref.set(displayIndex);
        return original.call(instance, displayIndex);
    }

    @WrapOperation(method = "drawBackground(Lnet/minecraft/client/gui/DrawContext;FII)V", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;"))
    private Stream<Slot> filterSlots(
        Stream<Slot> stream,
        Predicate<Slot> predicate,
        Operation<Stream<Slot>> original,
        @Share("index") LocalIntRef ref,
        @Local IEivRecipeViewType type
    ) {
        if (type instanceof CreateCategory) {
            int size = type.getSlotCount();
            int index = ref.get() * size;
            int end = index + size;
            stream = stream.filter(slot -> slot.id >= index && slot.id < end);
        }
        return original.call(stream, predicate);
    }

    @Inject(method = "renderInvalidSlots(Lnet/minecraft/client/gui/DrawContext;I)V", at = @At(value = "INVOKE", target = "Lde/crafty/eiv/common/recipe/inventory/RecipeViewScreen;getScreenHandler()Lnet/minecraft/screen/ScreenHandler;", ordinal = 1), cancellable = true)
    private void renderInvalidSlots(DrawContext guiGraphics, int displayId, CallbackInfo ci, @Local ButtonWidget button) {
        if (button instanceof RecipeButton recipeButton) {
            recipeButton.renderInvalidSlots(guiGraphics, displayId);
            ci.cancel();
        }
    }
}
