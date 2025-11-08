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
    public static boolean HMI = false;
    public static boolean HAS_RENDER;

    @Override
    public void onLoad(String mixinPackage) {
        SODIUM = FabricLoader.getInstance().isModLoaded("sodium");
        IRIS = FabricLoader.getInstance().isModLoaded("iris");
        HMI = FabricLoader.getInstance().isModLoaded("holdmyitems");
        HAS_RENDER = FabricLoader.getInstance().isModLoaded("fabric-rendering-fluids-v1");
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
        if (HMI) {
            mixins.add("HoldMyItemsMixin");
            mixins.add("AnimationResourceLoaderMixin");
        }
        if (Create.Lazy) {
            mixins.add("FabricBlockStateModelMixin");
            mixins.add("WrapperBlockStateModelMixin");
            mixins.add("FluidVariantRenderHandlerMixin");
        } else {
            mixins.add("CreativeInventoryScreenMixin");
            mixins.add("DefaultClientResourcePackProviderMixin");
        }
        if (HAS_RENDER) {
            mixins.add("WaterRenderHandlerMixin");
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
