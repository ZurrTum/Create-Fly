package com.zurrtum.create.client.foundation.utility;

import com.zurrtum.create.client.catnip.lang.Lang;
import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.catnip.lang.LangNumberFormat;
import com.zurrtum.create.infrastructure.fluids.FluidStack;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class CreateLang extends Lang {

    /**
     * legacy-ish. Use CreateLang.translate and other builder methods where possible
     */
    public static MutableText translateDirect(String key, Object... args) {
        Object[] args1 = LangBuilder.resolveBuilders(args);
        return Text.translatable(MOD_ID + "." + key, args1);
    }

    public static List<Text> translatedOptions(String prefix, String... keys) {
        List<Text> result = new ArrayList<>(keys.length);
        for (String key : keys)
            result.add(translate((prefix != null ? prefix + "." : "") + key).component());
        return result;
    }

    //

    public static LangBuilder builder() {
        return new LangBuilder(MOD_ID);
    }

    public static LangBuilder blockName(BlockState state) {
        return builder().add(state.getBlock().getName());
    }

    public static LangBuilder itemName(ItemStack stack) {
        return builder().add(stack.getName().copy());
    }

    public static LangBuilder fluidName(FluidStack stack) {
        return builder().add(stack.getName().copy());
    }

    public static LangBuilder number(double d) {
        return builder().text(LangNumberFormat.format(d));
    }

    public static LangBuilder translate(String langKey, Object... args) {
        return builder().translate(langKey, args);
    }

    public static LangBuilder text(String text) {
        return builder().text(text);
    }

    @Deprecated // Use while implementing and replace all references with Lang.translate
    public static LangBuilder temporaryText(String text) {
        return builder().text(text);
    }

}
