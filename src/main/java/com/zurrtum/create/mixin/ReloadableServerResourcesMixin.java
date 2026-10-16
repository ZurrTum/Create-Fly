package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.foundation.recipe.trie.RecipeTrieFinder;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(ReloadableServerResources.class)
public class ReloadableServerResourcesMixin {
    @ModifyReturnValue(method = "listeners()Ljava/util/List;", at = @At("RETURN"))
    private List<PreparableReloadListener> add(List<PreparableReloadListener> original) {
        List<PreparableReloadListener> list = new ArrayList<>(original);
        list.add(RecipeFinder.LISTENER);
        list.add(RecipeTrieFinder.LISTENER);
        list.add(BeltHelper.LISTENER);
        list.add(AllConfigs.LISTENER);
        return Collections.unmodifiableList(list);
    }
}
