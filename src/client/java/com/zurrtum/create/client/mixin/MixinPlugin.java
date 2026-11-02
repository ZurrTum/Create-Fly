package com.zurrtum.create.client.mixin;

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
        if (loader.isModLoaded("sodium")) {
            mixins.add("QuadRenderHelperMixin");
        }
        if (loader.isModLoaded("iris")) {
            mixins.add("IrisPipelinesMixin");
        }
        //        if (loader.isModLoaded("eiv")) {
        //            mixins.add("ItemSlotMixin");
        //            mixins.add("FabricEIVMixin");
        //            mixins.add("RecipeViewMenuMixin");
        //            mixins.add("ViewTypeButtonMixin");
        //            mixins.add("FluidItemSpecialRendererMixin");
        //            mixins.add("RecipeViewScreenMixin");
        //            mixins.add("CraftingViewRecipeAccessor");
        //        }
        if (loader.isModLoaded("rrv")) {
            mixins.add("RrvRecipeViewMenuMixin");
            mixins.add("RrvRecipeViewScreenMixin");
        }
        if (loader.isModLoaded("fabric-renderer-api-v1")) {
            mixins.add("SimpleModelPartMixin");
            mixins.add("SimpleModelWrapperMixin");
            mixins.add("WrapperBlockStateModelMixin");
            mixins.add("WrapperBlockStateModelAccessor");
            mixins.add("CopycatModelMixin");
            mixins.add("CopycatStepModelMixin");
            mixins.add("CopycatPanelModelMixin");
            mixins.add("ModelRenderHelperMixin");
            mixins.add("BufferEmitterMixin");
            mixins.add("BufferPoseEmitterMixin");
            mixins.add("BufferAoEmitterMixin");
            mixins.add("BufferAoPoseEmitterMixin");
            mixins.add("ItemModelRenderHelperMixin");
            mixins.add("BakedModelHelperMixin");
        } else {
            mixins.add("CuboidModelElementDeserializerMixin");
            mixins.add("CuboidModelElementMixin");
            mixins.add("UnbakedCuboidGeometryMixin");
            mixins.add("BakedQuadMixin");
            mixins.add("VertexConsumerMixin");
            mixins.add("ModelBlockRendererMixin");
            mixins.add("TerrainParticleMixin");
        }
        if (loader.isModLoaded("fabric-block-api-v1")) {
            mixins.add("ConnectedTextureBehaviourMixin");
        }
        if (!loader.isModLoaded("fabric-creative-tab-api-v1")) {
            mixins.add("CreativeModeInventoryScreenMixin");
        }
        if (!loader.isModLoaded("fabric-model-loading-api-v1")) {
            mixins.add("LoadBlockModelMixin");
        }
    }

    @Override
    public List<String> getMixins() {
        return mixins;
    }
}
