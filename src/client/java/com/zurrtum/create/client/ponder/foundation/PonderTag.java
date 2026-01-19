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
import net.minecraft.world.item.ItemStackTemplate;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

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
    @Nullable
    private final Identifier textureIconLocation;
    private final @Nullable Supplier<ItemStack> mainItem;
    private final @Nullable Supplier<GuiItemRenderBuilder> itemIcon;

    public PonderTag(
        Identifier id,
        @Nullable Identifier textureIconLocation,
        @Nullable ItemStackTemplate itemIcon,
        @Nullable ItemStackTemplate mainItem
    ) {
        this.id = id;
        this.textureIconLocation = textureIconLocation;
        this.mainItem = mainItem != null ? Suppliers.memoize(mainItem::create) : null;
        if (textureIconLocation == null && itemIcon != null) {
            this.itemIcon = Suppliers.memoize(() -> GuiGameElement.of(itemIcon.create()).scale(1.25f).at(-2, -2));
        } else {
            this.itemIcon = null;
        }
    }

    public Identifier getId() {
        return id;
    }

    public ItemStack getMainItem() {
        return mainItem != null ? mainItem.get() : ItemStack.EMPTY;
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
        if (this == other)
            return true;

        if (!(other instanceof PonderTag otherTag))
            return false;

        return getId().equals(otherTag.getId());
    }
}