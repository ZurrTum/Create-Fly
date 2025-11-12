package com.zurrtum.create.client.catnip.render;

import com.zurrtum.create.client.ponder.enums.PonderSpecialTextures;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderType.CompositeState;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;
import static net.minecraft.client.renderer.RenderStateShard.*;

public class PonderRenderTypes {
    private static final Function<ResourceLocation, RenderType> GUI_TEXTURED = Util.memoize(texture -> RenderType.create(
        createLayerName("gui_textured"),
        786432,
        false,
        false,
        RenderPipelines.GUI_TEXTURED,
        CompositeState.builder().setTextureState(new RenderStateShard.TextureStateShard(texture, false)).createCompositeState(false)
    ));
    private static final RenderType GUI = RenderType.create(
        createLayerName("gui"),
        786432,
        false,
        false,
        RenderPipelines.GUI,
        CompositeState.builder().createCompositeState(false)
    );
    private static final RenderType.CompositeRenderType GUI_INVERT = RenderType.create(
        "gui_text_highlight",
        1536,
        false,
        false,
        RenderPipelines.GUI_INVERT,
        CompositeState.builder().createCompositeState(false)
    );
    private static final RenderType.CompositeRenderType GUI_TEXT_HIGHLIGHT = RenderType.create(
        "gui_text_highlight",
        1536,
        false,
        false,
        RenderPipelines.GUI_TEXT_HIGHLIGHT,
        CompositeState.builder().createCompositeState(false)
    );
    private static final RenderType TRANSLUCENT = RenderType.create(
        createLayerName("translucent"),
        786432,
        true,
        true,
        RenderPipelines.TRANSLUCENT,
        CompositeState.builder().setLightmapState(LIGHTMAP).setTextureState(BLOCK_SHEET_MIPPED).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(true)
    );

    private static final RenderType OUTLINE_SOLID = RenderType.create(
        createLayerName("outline_solid"),
        256,
        false,
        false,
        RenderPipelines.ENTITY_SOLID,
        CompositeState.builder().setTextureState(new TextureStateShard(PonderSpecialTextures.BLANK.getLocation(), false)).setLightmapState(LIGHTMAP)
            .setOverlayState(OVERLAY).createCompositeState(false)
    );

    private static final BiFunction<ResourceLocation, Boolean, RenderType> OUTLINE_TRANSLUCENT = Util.memoize((texture, cull) -> RenderType.create(
        createLayerName("outline_translucent" + (cull ? "_cull" : "")),
        256,
        false,
        true,
        cull ? PonderRenderPipelines.RENDERTYPE_ITEM_ENTITY_TRANSLUCENT_CULL : PonderRenderPipelines.ENTITY_TRANSLUCENT,
        CompositeState.builder().setTextureState(new TextureStateShard(texture, false)).setLightmapState(LIGHTMAP).setOverlayState(OVERLAY)
            .setOutputState(ITEM_ENTITY_TARGET).createCompositeState(false)
    ));

    private static final RenderType FLUID = RenderType.create(
        createLayerName("fluid"),
        256,
        false,
        true,
        RenderPipelines.TRANSLUCENT,
        CompositeState.builder().setTextureState(BLOCK_SHEET_MIPPED).setLightmapState(LIGHTMAP).setOutputState(ITEM_ENTITY_TARGET).createCompositeState(true)
    );

    public static RenderType getGui() {
        return GUI;
    }

    public static RenderType getGuiInvert() {
        return GUI_INVERT;
    }

    public static RenderType getGuiTextHighlight() {
        return GUI_TEXT_HIGHLIGHT;
    }

    public static RenderType getGuiTextured(ResourceLocation texture) {
        return GUI_TEXTURED.apply(texture);
    }

    public static RenderType translucent() {
        return TRANSLUCENT;
    }

    public static RenderType outlineSolid() {
        return OUTLINE_SOLID;
    }

    public static RenderType outlineTranslucent(ResourceLocation texture, boolean cull) {
        return OUTLINE_TRANSLUCENT.apply(texture, cull);
    }

    //TODO vanilla uses the translucent render type for fluids, need to investigate if this is even needed
    public static RenderType fluid() {
        return FLUID;
    }

    private static String createLayerName(String name) {
        return MOD_ID + ":" + name;
    }
}
