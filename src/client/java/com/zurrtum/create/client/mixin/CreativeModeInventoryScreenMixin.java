/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zurrtum.create.client.mixin;

import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.client.infrastructure.itemGroup.FabricCreativeGuiComponents;
import com.zurrtum.create.client.infrastructure.itemGroup.FabricCreativeInventoryScreen;
import com.zurrtum.create.infrastructure.itemGroup.FabricItemGroupImpl;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu> implements FabricCreativeInventoryScreen {
    public CreativeModeInventoryScreenMixin(
        CreativeModeInventoryScreen.ItemPickerMenu screenHandler,
        Inventory playerInventory,
        Component text
    ) {
        super(screenHandler, playerInventory, text);
    }

    @Shadow
    protected abstract void selectTab(CreativeModeTab tab);

    @Shadow
    private static CreativeModeTab selectedTab;

    // "static" matches selectedTab
    @Unique
    private static int currentPage;

    @Inject(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/EditBox;setTextColor(I)V", shift = At.Shift.AFTER))
    private void init(CallbackInfo info) {
        currentPage = fabric_getPage(selectedTab);

        int xpos = leftPos + 171;
        int ypos = topPos + 4;

        addRenderableWidget(new FabricCreativeGuiComponents.ItemGroupButtonWidget(
            xpos + 10,
            ypos,
            FabricCreativeGuiComponents.Type.NEXT,
            this
        ));
        addRenderableWidget(new FabricCreativeGuiComponents.ItemGroupButtonWidget(
            xpos,
            ypos,
            FabricCreativeGuiComponents.Type.PREVIOUS,
            this
        ));
    }

    @Inject(method = "selectTab", at = @At("HEAD"), cancellable = true)
    private void setSelectedTab(CreativeModeTab tab, CallbackInfo info) {
        if (!isGroupVisible(tab)) {
            info.cancel();
        }
    }

    @Inject(method = "checkTabHovering", at = @At("HEAD"), cancellable = true)
    private void renderTabTooltipIfHovered(
        GuiGraphicsExtractor graphics,
        CreativeModeTab tab,
        int xm,
        int ym,
        CallbackInfoReturnable<Boolean> info
    ) {
        if (!isGroupVisible(tab)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "checkTabClicked", at = @At("HEAD"), cancellable = true)
    private void isClickInTab(CreativeModeTab tab, double xm, double ym, CallbackInfoReturnable<Boolean> info) {
        if (!isGroupVisible(tab)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method = "extractTabButton", at = @At("HEAD"), cancellable = true)
    private void renderTabIcon(
        GuiGraphicsExtractor graphics,
        int mouseX,
        int mouseY,
        CreativeModeTab tab,
        CallbackInfo info
    ) {
        if (!isGroupVisible(tab)) {
            info.cancel();
        }
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void keyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        int keyCode = event.key();
        if (keyCode == InputConstants.KEY_PAGEUP) {
            if (fabric_switchToPreviousPage()) {
                cir.setReturnValue(true);
            }
        } else if (keyCode == InputConstants.KEY_PAGEDOWN) {
            if (fabric_switchToNextPage()) {
                cir.setReturnValue(true);
            }
        }
    }

    @Override
    public int fabric_getCurrentPage() {
        return currentPage;
    }

    @Override
    public boolean fabric_switchToPage(int page) {
        if (!hasGroupForPage(page)) {
            return false;
        }

        if (currentPage == page) {
            return false;
        }

        currentPage = page;
        updateSelection();
        return true;
    }

    @Override
    public int fabric_getPage(@NonNull CreativeModeTab itemGroup) {
        if (FabricCreativeGuiComponents.COMMON_GROUPS.contains(itemGroup)) {
            return currentPage;
        }

        final FabricItemGroupImpl fabricItemGroup = (FabricItemGroupImpl) itemGroup;
        return fabricItemGroup.fabric_getPage();
    }

    @Override
    public boolean fabric_hasAdditionalPages() {
        return CreativeModeTabs.tabs().size() > (
            Objects.requireNonNull(CreativeModeTabs.CACHED_PARAMETERS).hasPermissions() ? 14 : 13);
    }

    @Override
    public int fabric_getPageCount() {
        return FabricCreativeGuiComponents.getPageCount();
    }

    @Unique
    private boolean isGroupVisible(CreativeModeTab itemGroup) {
        return itemGroup.shouldDisplay() && currentPage == fabric_getPage(itemGroup);
    }

    @Unique
    private void updateSelection() {
        if (!isGroupVisible(selectedTab)) {
            CreativeModeTabs.allTabs().stream().filter(this::isGroupVisible)
                .min((a, b) -> Boolean.compare(a.isAlignedRight(), b.isAlignedRight())).ifPresent(this::selectTab);
        }
    }

    @Unique
    private boolean hasGroupForPage(int page) {
        return CreativeModeTabs.tabs().stream().anyMatch(itemGroup -> fabric_getPage(itemGroup) == page);
    }
}
