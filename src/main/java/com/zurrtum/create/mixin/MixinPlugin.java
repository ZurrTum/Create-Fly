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
    public static boolean CCT = false;
    public static boolean ARCH = false;

    @Override
    public void onLoad(String mixinPackage) {
        Create.Lazy = FabricLoader.getInstance().isModLoaded("fabric-api");
        CCT = FabricLoader.getInstance().isModLoaded("computercraft");
        ARCH = FabricLoader.getInstance().isModLoaded("architectury");
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
        List<String> mixins = new ArrayList<>();
        if (CCT) {
            mixins.add("CreateIntegrationMixin");
        }
        if (ARCH) {
            mixins.add("ArchitecturyMixin");
        }
        if (Create.Lazy) {
            mixins.add("RegistryKeysMixin");
        } else {
            mixins.add("ItemGroupMixin");
            mixins.add("ItemGroupsMixin");
            mixins.add("PersistentStateManagerMixin");
            mixins.add("IngredientMixin");
        }
        return mixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
