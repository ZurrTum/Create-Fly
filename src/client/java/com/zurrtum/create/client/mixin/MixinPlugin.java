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
    private List<String> mixins;

    @Override
    public void onLoad(String mixinPackage) {
        mixins = new ArrayList<>();
        //        if (FabricLoader.getInstance().isModLoaded("sodium")) {
        //            mixins.add("FabricModelAccessMixin");
        //            mixins.add("AbstractBlockRenderContextMixin");
        //        }
        //        if (FabricLoader.getInstance().isModLoaded("iris")) {
        //            mixins.add("IrisPipelinesMixin");
        //        }
        //        if (FabricLoader.getInstance().isModLoaded("holdmyitems")) {
        //            mixins.add("HoldMyItemsMixin");
        //            mixins.add("AnimationResourceLoaderMixin");
        //        }
        //        if (FabricLoader.getInstance().isModLoaded("eiv")) {
        //            mixins.add("ItemSlotMixin");
        //            mixins.add("FabricEIVMixin");
        //            mixins.add("RecipeViewMenuMixin");
        //            mixins.add("ViewTypeButtonMixin");
        //            mixins.add("FluidItemSpecialRendererMixin");
        //            mixins.add("RecipeViewScreenMixin");
        //            mixins.add("CraftingViewRecipeAccessor");
        //        }
        if (Create.Lazy) {
            mixins.add("FabricBlockStateModelMixin");
            mixins.add("WrapperBlockStateModelMixin");
            mixins.add("FluidVariantRenderHandlerMixin");
            mixins.add("BlockRenderInfoMixin");
            mixins.add("AbstractTerrainRenderContextMixin");
        } else {
            mixins.add("CreativeModeInventoryScreenMixin");
        }
        if (FabricLoader.getInstance().isModLoaded("fabric-rendering-fluids-v1")) {
            mixins.add("WaterRenderHandlerMixin");
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
