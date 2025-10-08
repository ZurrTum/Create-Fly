package com.zurrtum.create.client.content.trains.station;

import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.text.OrderedText;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import org.joml.Matrix4f;

import java.util.List;

public class NoShadowFontWrapper extends TextRenderer {
    private final TextRenderer wrapped;

    public NoShadowFontWrapper(TextRenderer wrapped) {
        super(wrapped.fonts);
        this.wrapped = wrapped;
    }

    @Override
    public void draw(
        Text text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextLayerType layerType,
        int backgroundColor,
        int light
    ) {
        wrapped.draw(text, x, y, color, false, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public void draw(
        String string,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextLayerType layerType,
        int backgroundColor,
        int light
    ) {
        wrapped.draw(string, x, y, color, false, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public void draw(
        OrderedText text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        TextLayerType layerType,
        int backgroundColor,
        int light
    ) {
        wrapped.draw(text, x, y, color, false, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public int getWrappedLinesHeight(StringVisitable text, int maxWidth) {
        return wrapped.getWrappedLinesHeight(text, maxWidth);
    }

    @Override
    public String mirror(String text) {
        return wrapped.mirror(text);
    }

    @Override
    public void drawWithOutline(
        OrderedText text,
        float x,
        float y,
        int color,
        int outlineColor,
        Matrix4f matrix,
        VertexConsumerProvider vertexConsumers,
        int light
    ) {
        wrapped.drawWithOutline(text, x, y, color, outlineColor, matrix, vertexConsumers, light);
    }

    @Override
    public int getWidth(String text) {
        return wrapped.getWidth(text);
    }

    @Override
    public int getWidth(OrderedText text) {
        return wrapped.getWidth(text);
    }

    @Override
    public int getWidth(StringVisitable text) {
        return wrapped.getWidth(text);
    }

    @Override
    public String trimToWidth(String text, int maxWidth) {
        return wrapped.trimToWidth(text, maxWidth);
    }

    @Override
    public String trimToWidth(String text, int maxWidth, boolean backwards) {
        return wrapped.trimToWidth(text, maxWidth, backwards);
    }

    @Override
    public StringVisitable trimToWidth(StringVisitable text, int width) {
        return wrapped.trimToWidth(text, width);
    }

    @Override
    public int getWrappedLinesHeight(String text, int maxWidth) {
        return wrapped.getWrappedLinesHeight(text, maxWidth);
    }

    @Override
    public List<OrderedText> wrapLines(StringVisitable text, int width) {
        return wrapped.wrapLines(text, width);
    }

    @Override
    public List<StringVisitable> wrapLinesWithoutLanguage(StringVisitable text, int width) {
        return wrapped.wrapLinesWithoutLanguage(text, width);
    }

    @Override
    public boolean isRightToLeft() {
        return wrapped.isRightToLeft();
    }

    @Override
    public TextHandler getTextHandler() {
        return wrapped.getTextHandler();
    }

    @Override
    public GlyphDrawable prepare(String string, float x, float y, int color, boolean shadow, int backgroundColor) {
        return wrapped.prepare(string, x, y, color, false, backgroundColor);
    }

    @Override
    public GlyphDrawable prepare(OrderedText text, float x, float y, int color, boolean shadow, int backgroundColor) {
        return wrapped.prepare(text, x, y, color, false, backgroundColor);
    }
}
