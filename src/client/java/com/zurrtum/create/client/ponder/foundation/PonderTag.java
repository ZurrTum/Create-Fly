package com.zurrtum.create.client.ponder.foundation;

import com.google.common.base.Suppliers;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement.GuiItemRenderBuilder;
import com.zurrtum.create.client.catnip.gui.element.ScreenElement;
import com.zurrtum.create.client.ponder.Ponder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.function.Supplier;

public class PonderTag implements ScreenElement {

    /**
     * Highlight.ALL is a special PonderTag, used to indicate that all Tags
     * for a certain Scene should be highlighted instead of selected single ones
     */
    public static final class Highlight {
        public static final Identifier ALL = Ponder.asResource("_all");
    }

    private final Identifier id;
    private final @Nullable TextureIconRenderer textureIcon;
    private final ItemStack mainItem;
    private final @Nullable Supplier<GuiItemRenderBuilder> itemIcon;


    public PonderTag(Identifier id, @Nullable Identifier textureIconLocation, ItemStack itemIcon, ItemStack mainItem) {
        this.id = id;
        this.textureIcon = textureIconLocation == null ? null : (graphics, poseStack, x, y) -> {
            poseStack.translate(x, y);
            poseStack.scale(0.25f, 0.25f);
            graphics.blit(RenderPipelines.GUI_TEXTURED, textureIconLocation, 0, 0, 0, 0, 0, 64, 64, 64, 64);
        };
        this.mainItem = mainItem;
        if (textureIconLocation == null && !itemIcon.isEmpty()) {
            this.itemIcon = Suppliers.memoize(() -> GuiGameElement.of(itemIcon).scale(1.25f).at(-2, -2));
        } else {
            this.itemIcon = null;
        }
    }

    public PonderTag(PonderTag tag, float scale) {
        id = tag.id;
        TextureIconRenderer prevTextureIcon = tag.textureIcon;
        textureIcon = prevTextureIcon == null ? null : (graphics, poseStack, x, y) -> {
            poseStack.scale(scale);
            prevTextureIcon.render(graphics, poseStack, x, y);
        };
        mainItem = tag.mainItem;
        if (prevTextureIcon == null && tag.itemIcon != null) {
            itemIcon = Suppliers.memoize(() -> tag.itemIcon.get().copy().scale(1.25f * scale));
        } else {
            itemIcon = null;
        }
    }

    public Identifier getId() {
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
        if (textureIcon != null) {
            textureIcon.render(graphics, poseStack, x, y);
        } else if (itemIcon != null) {
            poseStack.translate(x, y);
            itemIcon.get().render(graphics);
        }
        poseStack.popMatrix();
    }

    public void clear() {
        if (itemIcon != null) {
            itemIcon.get().clear();
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }

        if (!(other instanceof PonderTag otherTag)) {
            return false;
        }

        return getId().equals(otherTag.getId());
    }

    private interface TextureIconRenderer {
        void render(GuiGraphics graphics, Matrix3x2fStack poseStack, int x, int y);
    }
}