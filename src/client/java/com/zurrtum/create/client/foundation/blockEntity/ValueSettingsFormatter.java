package com.zurrtum.create.client.foundation.blockEntity;

import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.INamedIconOptions;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.foundation.blockEntity.behaviour.ValueSettings;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class ValueSettingsFormatter {

    @Nullable
    private final Function<ValueSettings, MutableComponent> formatter;

    public ValueSettingsFormatter(@Nullable Function<ValueSettings, MutableComponent> formatter) {
        this.formatter = formatter;
    }

    public ValueSettingsFormatter() {
        this(null);
    }

    public MutableComponent format(ValueSettings valueSettings) {
        return formatter == null ? toLocaleNumber(valueSettings) : formatter.apply(valueSettings);
    }

    public static MutableComponent toLocaleNumber(ValueSettings valueSettings) {
        return CreateLang.number(valueSettings.value()).component();
    }

    public static class ScrollOptionSettingsFormatter extends ValueSettingsFormatter {

        private final INamedIconOptions[] options;

        public ScrollOptionSettingsFormatter(INamedIconOptions[] options) {
            super(v -> Component.translatable(options[v.value()].getTranslationKey()));
            this.options = options;
        }

        public AllIcons getIcon(ValueSettings valueSettings) {
            return options[valueSettings.value()].getIcon();
        }

    }

}
