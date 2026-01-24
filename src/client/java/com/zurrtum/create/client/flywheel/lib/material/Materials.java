package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.*;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;

public final class Materials {
    public static final Material SOLID_BLOCK = SimpleMaterial.builder().build();
    public static final Material SOLID_UNSHADED_BLOCK = SimpleMaterial.builderOf(SOLID_BLOCK).cardinalLightingMode(CardinalLightingMode.OFF).build();

    public static final Material CUTOUT_BLOCK = SimpleMaterial.builder().cutout(CutoutShaders.HALF).build();
    public static final Material CUTOUT_UNSHADED_BLOCK = SimpleMaterial.builderOf(CUTOUT_BLOCK).cardinalLightingMode(CardinalLightingMode.OFF)
        .build();

    public static final Material TRANSLUCENT_BLOCK = SimpleMaterial.builder().transparency(Transparency.ORDER_INDEPENDENT).build();
    public static final Material TRANSLUCENT_UNSHADED_BLOCK = SimpleMaterial.builderOf(TRANSLUCENT_BLOCK)
        .cardinalLightingMode(CardinalLightingMode.OFF).build();

    public static final Material GLINT = SimpleMaterial.builder().texture(ItemRenderer.ENCHANTED_GLINT_ITEM).shaders(StandardMaterialShaders.GLINT)
        .transparency(Transparency.GLINT).writeMask(WriteMask.COLOR).depthTest(DepthTest.EQUAL).backfaceCulling(false).blur(true).mipmap(false)
        .build();

    public static final Material GLINT_ENTITY = SimpleMaterial.builderOf(GLINT).texture(ItemRenderer.ENCHANTED_GLINT_ARMOR).build();

    public static final Material TRANSLUCENT_ITEM_ENTITY_BLOCK = SimpleMaterial.builder().transparency(Transparency.TRANSLUCENT)
        .cutout(CutoutShaders.ONE_TENTH).mipmap(false).build();

    @SuppressWarnings("deprecation")
    public static final Material TRANSLUCENT_ITEM_ENTITY_ITEM = SimpleMaterial.builder().texture(TextureAtlas.LOCATION_ITEMS)
        .transparency(Transparency.TRANSLUCENT).cutout(CutoutShaders.ONE_TENTH).mipmap(false).build();

    private Materials() {
    }
}
