package com.zurrtum.create.client.catnip.lang;

import java.text.NumberFormat;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;

public class LangNumberFormat {

    public static LangNumberFormat numberFormat = new LangNumberFormat();
    private NumberFormat format = NumberFormat.getNumberInstance(Locale.ROOT);

    public static String format(double d) {
        if (Mth.equal(d, 0))
            d = 0;
        return numberFormat.get().format(d).replace("\u00A0", " ");
    }

    public NumberFormat get() {
        return format;
    }

    public void update() {
        String selected = Minecraft.getInstance().getLanguageManager().getSelected();
        final String[] langSplit = selected.split("_", 2);
        Locale locale = langSplit.length == 1 ? Locale.of(langSplit[0]) : Locale.of(langSplit[0], langSplit[1]);
        format = NumberFormat.getInstance(locale);
        format.setMaximumFractionDigits(2);
        format.setMinimumFractionDigits(0);
        format.setGroupingUsed(true);
    }

}
