package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.client.ponder.enums.PonderSpecialTextures;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.OutputTarget;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.util.function.BiFunction;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;

public class PonderRenderTypes {
    private static final RenderType GUI = RenderType.create(
        createLayerName("gui"),
        RenderSetup.builder(RenderPipelines.GUI).bufferSize(786432).createRenderSetup()
    );

    private static final RenderType OUTLINE_SOLID = RenderType.create(
        createLayerName("outline_solid"),
        RenderSetup.builder(RenderPipelines.ENTITY_SOLID).bufferSize(256).withTexture("Sampler0", PonderSpecialTextures.BLANK.getLocation())
            .useLightmap().useOverlay().createRenderSetup()
    );

    private static final BiFunction<Identifier, Boolean, RenderType> OUTLINE_TRANSLUCENT = Util.memoize((texture, cull) -> RenderType.create(
        createLayerName("outline_translucent" + (cull ? "_cull" : "")),
        RenderSetup.builder(cull ? PonderRenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL : PonderRenderPipelines.ENTITY_TRANSLUCENT)
            .bufferSize(256).sortOnUpload().withTexture("Sampler0", texture).useLightmap().useOverlay()
            .setOutputTarget(OutputTarget.ITEM_ENTITY_TARGET).createRenderSetup()
    ));

    public static RenderType getGui() {
        return GUI;
    }

    public static RenderType outlineSolid() {
        return OUTLINE_SOLID;
    }

    public static RenderType outlineTranslucent(Identifier texture, boolean cull) {
        return OUTLINE_TRANSLUCENT.apply(texture, cull);
    }

    private static String createLayerName(String name) {
        return MOD_ID + ":" + name;
    }
}
