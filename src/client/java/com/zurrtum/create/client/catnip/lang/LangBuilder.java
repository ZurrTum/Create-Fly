package com.zurrtum.create.client.catnip.lang;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.serialization.JsonOps;
import com.zurrtum.create.catnip.theme.Color;
import joptsimple.internal.Strings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public class LangBuilder {
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    public static final float DEFAULT_SPACE_WIDTH = 4.0F; // space width in vanilla's default font
    String namespace;
    @Nullable MutableText component;

    public LangBuilder(String namespace) {
        this.namespace = namespace;
    }

    public static Object[] resolveBuilders(Object[] args) {
        for (int i = 0; i < args.length; i++)
            if (args[i] instanceof LangBuilder cb)
                args[i] = cb.component();
        return args;
    }

    static int getIndents(TextRenderer font, int defaultIndents) {
        int spaceWidth = font.getWidth(" ");
        if (DEFAULT_SPACE_WIDTH == spaceWidth) {
            return defaultIndents;
        }
        return MathHelper.ceil(DEFAULT_SPACE_WIDTH * defaultIndents / spaceWidth);
    }

    public LangBuilder space() {
        return text(" ");
    }

    public LangBuilder newLine() {
        return text("\n");
    }

    /**
     * Appends a localised component<br>
     * To add an independently formatted localised component, use add() and a nested
     * builder
     *
     * @param langKey
     * @param args
     * @return
     */
    public LangBuilder translate(String langKey, Object... args) {
        Object[] args1 = resolveBuilders(args);
        return add(Text.translatable(namespace + "." + langKey, args1));
    }

    /**
     * Appends a text component
     *
     * @param literalText
     * @return
     */
    public LangBuilder text(String literalText) {
        return add(Text.literal(literalText));
    }

    //

    /**
     * Appends a colored text component
     *
     * @param format
     * @param literalText
     * @return
     */
    public LangBuilder text(Formatting format, String literalText) {
        return add(Text.literal(literalText).formatted(format));
    }

    /**
     * Appends a colored text component
     *
     * @param color
     * @param literalText
     * @return
     */
    public LangBuilder text(int color, String literalText) {
        return add(Text.literal(literalText).styled(s -> s.withColor(color)));
    }

    /**
     * Appends the contents of another builder
     *
     * @param otherBuilder
     * @return
     */
    public LangBuilder add(LangBuilder otherBuilder) {
        return add(otherBuilder.component());
    }

    //

    /**
     * Appends a component
     *
     * @param customComponent
     * @return
     */
    public LangBuilder add(MutableText customComponent) {
        component = component == null ? customComponent : component.append(customComponent);
        return this;
    }

    /**
     * Appends a component
     *
     * @param component the component to append
     * @return this builder
     */
    public LangBuilder add(Text component) {
        if (component instanceof MutableText mutableComponent)
            return add(mutableComponent);
        else
            return add(component.copy());
    }

    /**
     * Applies the format to all added components
     *
     * @param format
     * @return
     */
    public LangBuilder style(Formatting format) {
        assertComponent();
        component = component.formatted(format);
        return this;
    }

    /**
     * Applies the color to all added components
     */
    public LangBuilder color(int color) {
        assertComponent();
        component = component.styled(s -> s.withColor(color));
        return this;
    }

    /**
     * Applies the color to all added components
     */
    public LangBuilder color(Color color) {
        return this.color(color.getRGB());
    }

    public MutableText component() {
        assertComponent();
        return component;
    }

    public String string() {
        return component().getString();
    }

    public String json() {
        return GSON.toJson(TextCodecs.CODEC.encodeStart(DynamicRegistryManager.EMPTY.getOps(JsonOps.INSTANCE), component())
            .getOrThrow(JsonParseException::new));
    }

    public void sendStatus(PlayerEntity player) {
        player.sendMessage(component(), true);
    }

    //

    public void sendChat(PlayerEntity player) {
        player.sendMessage(component(), false);
    }

    public void addTo(List<? super MutableText> tooltip) {
        tooltip.add(component());
    }

    public void addTo(Consumer<? super MutableText> tooltip) {
        tooltip.accept(component());
    }

    private void assertComponent() {
        if (component == null)
            throw new IllegalStateException("No components were added to builder");
    }

    public void forGoggles(List<? super MutableText> tooltip) {
        forGoggles(tooltip, 0);
    }

    public void forGoggles(List<? super MutableText> tooltip, int indents) {
        tooltip.add(new LangBuilder(namespace).text(Strings.repeat(' ', getIndents(MinecraftClient.getInstance().textRenderer, 4 + indents)))
            .add(this).component());
    }
}
