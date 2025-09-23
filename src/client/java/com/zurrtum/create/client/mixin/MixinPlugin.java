package com.zurrtum.create.client.mixin;

import com.zurrtum.create.Create;
import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MixinPlugin implements IMixinConfigPlugin {
    public static boolean SODIUM = false;
    public static boolean IRIS = false;

    @Override
    public void onLoad(String mixinPackage) {
        SODIUM = FabricLoader.getInstance().isModLoaded("sodium");
        IRIS = FabricLoader.getInstance().isModLoaded("iris");
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
        if (SODIUM) {
            mixins.add("FabricModelAccessMixin");
        }
        if (IRIS) {
            mixins.add("IrisPipelinesMixin");
        }
        if (Create.Lazy) {
            mixins.add("FabricBlockStateModelMixin");
            mixins.add("WrapperBlockStateModelMixin");
        } else {
            mixins.add("CreativeInventoryScreenMixin");
            mixins.add("DefaultClientResourcePackProviderMixin");
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
