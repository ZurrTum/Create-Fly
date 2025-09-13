package com.zurrtum.create.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.server.DataPackContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(DataPackContents.class)
public class DataPackContentsMixin {
    @ModifyReturnValue(method = "getContents()Ljava/util/List;", at = @At("RETURN"))
    private List<ResourceReloader> add(List<ResourceReloader> original) {
        List<ResourceReloader> list = new ArrayList<>(original);
        list.add(BeltHelper.LISTENER);
        return Collections.unmodifiableList(list);
    }
}
