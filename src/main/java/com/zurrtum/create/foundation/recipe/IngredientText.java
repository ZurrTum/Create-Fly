package com.zurrtum.create.foundation.recipe;

import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.crafting.Ingredient;

public record IngredientText(IngredientTextContent content) implements Component {
    public IngredientText(Ingredient content) {
        this(new IngredientTextContent(content));
    }

    @Override
    public <T> Optional<T> visit(ContentConsumer<T> visitor) {
        return content.visit(visitor);
    }

    @Override
    public Style getStyle() {
        return Style.EMPTY;
    }

    @Override
    public ComponentContents getContents() {
        return content;
    }

    @Override
    public List<Component> getSiblings() {
        return List.of();
    }

    @Override
    public FormattedCharSequence getVisualOrderText() {
        return FormattedCharSequence.EMPTY;
    }

    @Override
    public <T> Optional<T> visit(StyledContentConsumer<T> visitor, Style style) {
        return content.visit(visitor, style);
    }
}
