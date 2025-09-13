package com.zurrtum.create.client.catnip.lang;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.MathHelper;

import java.text.NumberFormat;
import java.util.Locale;

public class LangNumberFormat {

    public static LangNumberFormat numberFormat = new LangNumberFormat();
    private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

    public static String format(double d) {
        if (MathHelper.approximatelyEquals(d, 0))
            d = 0;
        return numberFormat.get().format(d).replace("\u00A0", " ");
    }

    public NumberFormat get() {
        return format;
    }

    @SuppressWarnings("deprecation")
    public void update() {
        String selected = MinecraftClient.getInstance().getLanguageManager().getLanguage();
        final String[] langSplit = selected.split("_", 2);
        Locale locale = langSplit.length == 1 ? new Locale(langSplit[0]) : new Locale(langSplit[0], langSplit[1]);
        format = NumberFormat.getInstance(locale);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        format.setGroupingUsed(true);
    }

}
