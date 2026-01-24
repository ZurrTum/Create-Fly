package com.zurrtum.create.client.mixin;

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
        //        if (loader.isModLoaded("sodium")) {
        //            mixins.add("FabricModelAccessMixin");
        //            mixins.add("AbstractBlockRenderContextMixin");
        //        }
        //        if (loader.isModLoaded("iris")) {
        //            mixins.add("IrisPipelinesMixin");
        //        }
        //        if (loader.isModLoaded("eiv")) {
        //            mixins.add("ItemSlotMixin");
        //            mixins.add("FabricEIVMixin");
        //            mixins.add("RecipeViewMenuMixin");
        //            mixins.add("ViewTypeButtonMixin");
        //            mixins.add("FluidItemSpecialRendererMixin");
        //            mixins.add("RecipeViewScreenMixin");
        //            mixins.add("CraftingViewRecipeAccessor");
        //        }
        if (!loader.isModLoaded("fabric-creative-tab-api-v1")) {
            mixins.add("CreativeModeInventoryScreenMixin");
        }
        if (loader.isModLoaded("fabric-renderer-api-v1")) {
            mixins.add("FabricBlockStateModelMixin");
        }
        if (loader.isModLoaded("fabric-renderer-indigo")) {
            mixins.add("BlockRenderInfoMixin");
            mixins.add("AbstractTerrainRenderContextMixin");
        }
        if (loader.isModLoaded("fabric-rendering-fluids-v1")) {
            mixins.add("WaterRenderHandlerMixin");
        }
        if (loader.isModLoaded("fabric-transfer-api-v1")) {
            mixins.add("FluidVariantRenderHandlerMixin");
        }
        if (loader.isModLoaded("fabric-model-loading-api-v1")) {
            mixins.add("WrapperBlockStateModelMixin");
        } else {
            mixins.add("LoadBlockModelMixin");
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
