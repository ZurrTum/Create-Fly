package com.zurrtum.create.client.foundation.blockEntity;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ValueSettingsFormatter {

    @Nullable
    private final Function<ValueSettings, MutableText> formatter;

    public ValueSettingsFormatter(@Nullable Function<ValueSettings, MutableText> formatter) {
        this.formatter = formatter;
    }

    public ValueSettingsFormatter() {
        this(null);
    }

    public MutableText format(ValueSettings valueSettings) {
        return formatter == null ? toLocaleNumber(valueSettings) : formatter.apply(valueSettings);
    }

    public static MutableText toLocaleNumber(ValueSettings valueSettings) {
        return CreateLang.number(valueSettings.value()).component();
    }

    public static class ScrollOptionSettingsFormatter extends ValueSettingsFormatter {

        private final INamedIconOptions[] options;

        public ScrollOptionSettingsFormatter(INamedIconOptions[] options) {
            super(v -> Text.translatable(options[v.value()].getTranslationKey()));
            this.options = options;
        }

        public AllIcons getIcon(ValueSettings valueSettings) {
            return options[valueSettings.value()].getIcon();
        }

    }

}
