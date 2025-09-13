package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.DepthTest;
import com.zurrtum.create.client.flywheel.api.material.Material;
import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.material.WriteMask;
import net.minecraft.client.render.item.ItemRenderer;

import static net.minecraft.client.render.RenderPhase.*;

public final class Materials {
    public static final Material SOLID_BLOCK = SimpleMaterial.builder().build();
    public static final Material SOLID_UNSHADED_BLOCK = SimpleMaterial.builder().diffuse(false).build();

    public static final Material CUTOUT_MIPPED_BLOCK = SimpleMaterial.builder().cutout(CutoutShaders.HALF).build();
    public static final Material CUTOUT_MIPPED_UNSHADED_BLOCK = SimpleMaterial.builder().cutout(CutoutShaders.HALF).diffuse(false).build();

    public static final Material CUTOUT_BLOCK = SimpleMaterial.builder().cutout(CutoutShaders.ONE_TENTH).mipmap(false).build();
    public static final Material CUTOUT_UNSHADED_BLOCK = SimpleMaterial.builder().cutout(CutoutShaders.ONE_TENTH).mipmap(false).diffuse(false)
        .build();

    public static final Material TRANSLUCENT_BLOCK = SimpleMaterial.builder().transparency(Transparency.ORDER_INDEPENDENT).target(TRANSLUCENT_TARGET)
        .build();
    public static final Material TRANSLUCENT_UNSHADED_BLOCK = SimpleMaterial.builder().transparency(Transparency.ORDER_INDEPENDENT).diffuse(false)
        .target(TRANSLUCENT_TARGET).build();

    public static final Material TRIPWIRE_BLOCK = SimpleMaterial.builder().cutout(CutoutShaders.ONE_TENTH)
        .transparency(Transparency.ORDER_INDEPENDENT).target(WEATHER_TARGET).build();
    public static final Material TRIPWIRE_UNSHADED_BLOCK = SimpleMaterial.builder().cutout(CutoutShaders.ONE_TENTH)
        .transparency(Transparency.ORDER_INDEPENDENT).diffuse(false).target(WEATHER_TARGET).build();

    public static final Material GLINT = SimpleMaterial.builder().texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT).shaders(StandardMaterialShaders.GLINT)
        .transparency(Transparency.GLINT).writeMask(WriteMask.COLOR).depthTest(DepthTest.EQUAL).backfaceCulling(false).blur(true).mipmap(false)
        .build();
    public static final Material TRANSLUCENT_GLINT = SimpleMaterial.builder().texture(ItemRenderer.ITEM_ENCHANTMENT_GLINT)
        .shaders(StandardMaterialShaders.GLINT).transparency(Transparency.GLINT).writeMask(WriteMask.COLOR).depthTest(DepthTest.EQUAL)
        .backfaceCulling(false).blur(true).mipmap(false).target(ITEM_ENTITY_TARGET).build();

    public static final Material GLINT_ENTITY = SimpleMaterial.builderOf(GLINT).texture(ItemRenderer.ENTITY_ENCHANTMENT_GLINT).build();

    public static final Material TRANSLUCENT_ENTITY = SimpleMaterial.builder().transparency(Transparency.TRANSLUCENT).cutout(CutoutShaders.ONE_TENTH)
        .blur(false).mipmap(false).target(ITEM_ENTITY_TARGET).build();

    private Materials() {
    }
}
