package com.zurrtum.create.client.mixin;

import com.zurrtum.create.client.compat.eiv.CreateCategory;
import de.crafty.eiv.common.api.recipe.IEivRecipeViewType;
import net.minecraft.client.gui.GuiGraphics;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "de.crafty.eiv.common.recipe.inventory.RecipeViewScreen$ViewTypeButton")
public class ViewTypeButtonMixin {
    @Final
    @Shadow(remap = false)
    private IEivRecipeViewType viewType;
    @Final
    @Shadow(remap = false)
    private int x;
    @Final
    @Shadow(remap = false)
    private int y;

    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;IIF)V", at = @At(value = "INVOKE", target = "Lde/crafty/eiv/common/recipe/inventory/RecipeViewScreen$ViewTypeButton;onHover(Lnet/minecraft/client/gui/GuiGraphics;II)V"))
    private void renderIcon(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (viewType instanceof CreateCategory category) {
            category.renderSubIcon(guiGraphics, x, y);
        }
    }
}
