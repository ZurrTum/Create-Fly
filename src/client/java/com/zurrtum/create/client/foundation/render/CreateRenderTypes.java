package com.zurrtum.create.client.foundation.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.textures.AddressMode;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuSampler;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderSetup.OutlineProperty;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

@SuppressWarnings("deprecation")
public class CreateRenderTypes {
    private static final RenderType ADDITIVE = RenderType.create(
        createLayerName("additive"),
        RenderSetup.builder(AllRenderPipelines.ADDITIVE).setOitPipelines(AllRenderPipelines.OIT_ADDITIVE)
            .affectsCrumbling().sortOnUpload().withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap()
            .useOverlay().setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );
    private static final RenderType ITEM_GLOWING_SOLID = RenderType.create(
        createLayerName("item_glowing_solid"),
        RenderSetup.builder(AllRenderPipelines.GLOWING).affectsCrumbling()
            .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap().useOverlay()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );

    private static final RenderType ITEM_GLOWING_TRANSLUCENT = RenderType.create(
        createLayerName("item_glowing_translucent"),
        RenderSetup.builder(AllRenderPipelines.GLOWING_TRANSLUCENT).setOitPipelines(AllRenderPipelines.OIT_GLOWING)
            .affectsCrumbling().sortOnUpload().withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS).useLightmap()
            .setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    );

    public static final Supplier<GpuSampler> CHAIN_SAMPLER = () -> RenderSystem.getSamplerCache()
        .getSampler(AddressMode.REPEAT, AddressMode.REPEAT, FilterMode.LINEAR, FilterMode.NEAREST, true);
    private static final Function<Identifier, RenderType> CHAIN = Util.memoize(texture -> RenderType.create(
        "chain_conveyor_chain",
        RenderSetup.builder(RenderPipelines.CUTOUT_BLOCK).sortOnUpload().withTexture("Sampler0", texture, CHAIN_SAMPLER)
            .useLightmap().useOverlay().setOutline(OutlineProperty.AFFECTS_OUTLINE).createRenderSetup()
    ));

    public static RenderType additive() {
        return ADDITIVE;
    }

    public static BiFunction<Identifier, Boolean, RenderType> TRAIN_MAP = Util.memoize(CreateRenderTypes::getTrainMap);

    private static RenderType getTrainMap(Identifier locationIn, boolean linearFiltering) {
        return RenderType.create(
            "create_train_map", RenderSetup.builder(RenderPipelines.TEXT).sortOnUpload().useLightmap().withTexture(
                "Sampler0",
                locationIn,
                () -> RenderSystem.getSamplerCache()
                    .getClampToEdge(linearFiltering ? FilterMode.LINEAR : FilterMode.NEAREST)
            ).createRenderSetup()
        );
    }

    public static RenderType itemGlowingSolid() {
        return ITEM_GLOWING_SOLID;
    }

    public static RenderType itemGlowingTranslucent() {
        return ITEM_GLOWING_TRANSLUCENT;
    }

    public static RenderType chain(Identifier pLocation) {
        return CHAIN.apply(pLocation);
    }

    private static String createLayerName(String name) {
        return MOD_ID + ":" + name;
    }
}
