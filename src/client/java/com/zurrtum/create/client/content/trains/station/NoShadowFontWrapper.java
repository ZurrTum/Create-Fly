package com.zurrtum.create.client.content.trains.station;

import org.joml.Matrix4f;

import java.util.List;

import net.minecraft.client.StringSplitter;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;

public class NoShadowFontWrapper extends Font {
    private final Font wrapped;

    public NoShadowFontWrapper(Font wrapped) {
        super(wrapped.provider);
        this.wrapped = wrapped;
    }

    @Override
    public void drawInBatch(
        Component text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        MultiBufferSource vertexConsumers,
        DisplayMode layerType,
        int backgroundColor,
        int light
    ) {
        wrapped.drawInBatch(text, x, y, color, false, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public void drawInBatch(
        String string,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        MultiBufferSource vertexConsumers,
        DisplayMode layerType,
        int backgroundColor,
        int light
    ) {
        wrapped.drawInBatch(string, x, y, color, false, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public void drawInBatch(
        FormattedCharSequence text,
        float x,
        float y,
        int color,
        boolean shadow,
        Matrix4f matrix,
        MultiBufferSource vertexConsumers,
        DisplayMode layerType,
        int backgroundColor,
        int light
    ) {
        wrapped.drawInBatch(text, x, y, color, false, matrix, vertexConsumers, layerType, backgroundColor, light);
    }

    @Override
    public int wordWrapHeight(FormattedText text, int maxWidth) {
        return wrapped.wordWrapHeight(text, maxWidth);
    }

    @Override
    public String bidirectionalShaping(String text) {
        return wrapped.bidirectionalShaping(text);
    }

    @Override
    public void drawInBatch8xOutline(
        FormattedCharSequence text,
        float x,
        float y,
        int color,
        int outlineColor,
        Matrix4f matrix,
        MultiBufferSource vertexConsumers,
        int light
    ) {
        wrapped.drawInBatch8xOutline(text, x, y, color, outlineColor, matrix, vertexConsumers, light);
    }

    @Override
    public int width(String text) {
        return wrapped.width(text);
    }

    @Override
    public int width(FormattedCharSequence text) {
        return wrapped.width(text);
    }

    @Override
    public int width(FormattedText text) {
        return wrapped.width(text);
    }

    @Override
    public String plainSubstrByWidth(String text, int maxWidth) {
        return wrapped.plainSubstrByWidth(text, maxWidth);
    }

    @Override
    public String plainSubstrByWidth(String text, int maxWidth, boolean backwards) {
        return wrapped.plainSubstrByWidth(text, maxWidth, backwards);
    }

    @Override
    public FormattedText substrByWidth(FormattedText text, int width) {
        return wrapped.substrByWidth(text, width);
    }

    @Override
    public int wordWrapHeight(String text, int maxWidth) {
        return wrapped.wordWrapHeight(text, maxWidth);
    }

    @Override
    public List<FormattedCharSequence> split(FormattedText text, int width) {
        return wrapped.split(text, width);
    }

    @Override
    public List<FormattedText> splitIgnoringLanguage(FormattedText text, int width) {
        return wrapped.splitIgnoringLanguage(text, width);
    }

    @Override
    public boolean isBidirectional() {
        return wrapped.isBidirectional();
    }

    @Override
    public StringSplitter getSplitter() {
        return wrapped.getSplitter();
    }

    @Override
    public PreparedText prepareText(String string, float x, float y, int color, boolean shadow, int backgroundColor) {
        return wrapped.prepareText(string, x, y, color, false, backgroundColor);
    }

    @Override
    public PreparedText prepareText(FormattedCharSequence text, float x, float y, int color, boolean shadow, int backgroundColor) {
        return wrapped.prepareText(text, x, y, color, false, backgroundColor);
    }
}
