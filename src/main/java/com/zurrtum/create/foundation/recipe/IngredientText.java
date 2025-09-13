package com.zurrtum.create.foundation.recipe;

import net.minecraft.recipe.Ingredient;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;

import java.util.List;
import java.util.Optional;

public record IngredientText(IngredientTextContent content) implements Text {
    public IngredientText(Ingredient content) {
        this(new IngredientTextContent(content));
    }

    @Override
    public <T> Optional<T> visit(Visitor<T> visitor) {
        return content.visit(visitor);
    }

    @Override
    public Style getStyle() {
        return Style.EMPTY;
    }

    @Override
    public TextContent getContent() {
        return content;
    }

    @Override
    public List<Text> getSiblings() {
        return List.of();
    }

    @Override
    public OrderedText asOrderedText() {
        return OrderedText.EMPTY;
    }

    @Override
    public <T> Optional<T> visit(StyledVisitor<T> visitor, Style style) {
        return content.visit(visitor, style);
    }
}
