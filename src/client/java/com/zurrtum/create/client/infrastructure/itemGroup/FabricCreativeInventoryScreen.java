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

package com.zurrtum.create.client.infrastructure.itemGroup;

import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.world.item.CreativeModeTab;

/**
 * Fabric provided extensions to {@link CreativeModeInventoryScreen}.
 * This interface is automatically implemented on all creative inventory screens via Mixin and interface injection.
 */
public interface FabricCreativeInventoryScreen {
    /**
     * Switches to the page with the given index if it exists.
     *
     * @param page the index of the page to switch to
     * @return Returns true when the page was changed
     */
    default boolean fabric_switchToPage(int page) {
        throw new AssertionError("Implemented by mixin");
    }

    /**
     * Switches to the next page if it exists.
     *
     * @return Returns true when the page was changed
     */
    default boolean fabric_switchToNextPage() {
        return fabric_switchToPage(fabric_getCurrentPage() + 1);
    }

    /**
     * Switches to the previous page if it exists.
     *
     * @return Returns true when the page was changed
     */
    default boolean fabric_switchToPreviousPage() {
        return fabric_switchToPage(fabric_getCurrentPage() - 1);
    }

    /**
     * Returns the index of the current page.
     */
    default int fabric_getCurrentPage() {
        throw new AssertionError("Implemented by mixin");
    }

    /**
     * Returns the total number of pages.
     */
    default int fabric_getPageCount() {
        throw new AssertionError("Implemented by mixin");
    }

    /**
     * Returns the page index of the given item group.
     *
     * <p>Item groups appearing on every page always return the current page index.
     *
     * @param itemGroup the item group to get the page index for
     * @return the page index of the item group
     */
    default int fabric_getPage(CreativeModeTab itemGroup) {
        throw new AssertionError("Implemented by mixin");
    }

    /**
     * Returns whether there are additional pages to show on top of the default vanilla pages.
     *
     * @return true if there are additional pages
     */
    default boolean fabric_hasAdditionalPages() {
        throw new AssertionError("Implemented by mixin");
    }
}
