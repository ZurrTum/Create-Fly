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

package com.zurrtum.create.mixin;

import com.zurrtum.create.infrastructure.itemGroup.FabricItemGroupImpl;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(CreativeModeTab.class)
public class CreativeModeTabMixin implements FabricItemGroupImpl {
    @Unique
    private int page = -1;

    @Override
    public int fabric_getPage() {
        if (page < 0) {
            throw new IllegalStateException("Item group has no page");
        }

        return page;
    }

    @Override
    public void fabric_setPage(int page) {
        this.page = page;
    }
}
