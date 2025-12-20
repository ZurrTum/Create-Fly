package com.zurrtum.create.mixin;

import com.zurrtum.create.Create;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    private List<String> mixins;

    @Override
    public void onLoad(String mixinPackage) {
        mixins = new ArrayList<>();
        FabricLoader loader = FabricLoader.getInstance();
        Create.Lazy = loader.isModLoaded("fabric-api");
        if (loader.isModLoaded("computercraft")) {
            mixins.add("CreateIntegrationMixin");
        }
        if (loader.isModLoaded("architectury")) {
            mixins.add("ArchitecturyMixin");
        }
        if (loader.isModLoaded("jei")) {
            mixins.add("JustEnoughItemsMixin");
        }
        if (Create.Lazy) {
            mixins.add("RegistryKeysMixin");
        } else {
            mixins.add("ItemGroupMixin");
            mixins.add("ItemGroupsMixin");
            mixins.add("PersistentStateManagerMixin");
            mixins.add("IngredientMixin");
        }
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
