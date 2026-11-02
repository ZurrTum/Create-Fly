package com.zurrtum.create.mixin;

import com.zurrtum.create.Create;
import net.fabricmc.loader.api.FabricLoader;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;

import java.util.ArrayList;
import java.util.List;

public class MixinPlugin implements IMixinConfigPlugin {
    private List<String> mixins;

    @Override
    public void onLoad(String mixinPackage) {
        mixins = new ArrayList<>();
        FabricLoader loader = FabricLoader.getInstance();
        Create.Lazy = loader.isModLoaded("fabric-api");
        //        if (FabricLoader.getInstance().isModLoaded("architectury")) {
        //            mixins.add("ArchitecturyMixin");
        //        }
        if (loader.isModLoaded("fabric-registry-sync-v0")) {
            mixins.add("RegistriesMixin");
        }
        if (loader.isModLoaded("fabric-networking-api-v1")) {
            mixins.add("MenuProviderMixin");
        }
        if (loader.isModLoaded("fabric-resource-loader-v1")) {
            mixins.add("ReloadListenerMixin");
        }
        if (!loader.isModLoaded("fabric-tag-api-v1")) {
            mixins.add("TagFileMixin");
            mixins.add("TagLoaderMixin");
        }
        if (!loader.isModLoaded("fabric-recipe-api-v1")) {
            mixins.add("IngredientMixin");
        }
        if (!loader.isModLoaded("fabric-creative-tab-api-v1")) {
            mixins.add("CreativeModeTabMixin");
            mixins.add("CreativeModeTabsMixin");
        }
        if (!loader.isModLoaded("fabric-object-builder-api-v1")) {
            mixins.add("SavedDataStorageMixin");
        }
    }

    @Override
    public List<String> getMixins() {
        return mixins;
    }
}
