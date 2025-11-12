package com.zurrtum.create.client.ponder.foundation;

import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiItemRenderBuilder;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

public class PonderTag implements ScreenElement {

    /**
     * Highlight.ALL is a special PonderTag, used to indicate that all Tags
     * for a certain Scene should be highlighted instead of selected single ones
     */
    public static final class Highlight {
        public static final ResourceLocation ALL = Ponder.asResource("_all");
    }

    private final ResourceLocation id;
    @Nullable
    private final ResourceLocation textureIconLocation;
    private final ItemStack mainItem;
    private final GuiItemRenderBuilder itemIcon;


    public PonderTag(ResourceLocation id, @Nullable ResourceLocation textureIconLocation, ItemStack itemIcon, ItemStack mainItem) {
        this.id = id;
        this.textureIconLocation = textureIconLocation;
        this.mainItem = mainItem;
        if (textureIconLocation == null && !itemIcon.isEmpty()) {
            this.itemIcon = GuiGameElement.of(itemIcon).scale(1.25f).at(-2, -2);
        } else {
            this.itemIcon = null;
        }
    }

    public ResourceLocation getId() {
        return id;
    }

    public ItemStack getMainItem() {
        return mainItem;
    }

    public String getTitle() {
        return PonderIndex.getLangAccess().getTagName(id);
    }

    public String getDescription() {
        return PonderIndex.getLangAccess().getTagDescription(id);
    }

    public void render(GuiGraphics graphics, int x, int y) {
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.translate(x, y);
        if (textureIconLocation != null) {
            //RenderSystem.setShaderTexture(0, icon);
            poseStack.scale(0.25f, 0.25f);
            graphics.blit(RenderPipelines.GUI_TEXTURED, textureIconLocation, 0, 0, 0, 0, 0, 64, 64, 64, 64);
        } else if (itemIcon != null) {
            itemIcon.render(graphics);
        }
        poseStack.popMatrix();
    }

    public void clear() {
        if (itemIcon != null) {
            itemIcon.clear();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;

        if (!(other instanceof PonderTag otherTag))
            return false;

        return getId().equals(otherTag.getId());
    }
}