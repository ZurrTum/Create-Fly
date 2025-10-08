package com.zurrtum.create.client.foundation.item;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;

import static net.minecraft.util.Formatting.*;

public record ItemDescription(
    ImmutableList<Text> lines, ImmutableList<Text> linesOnShift, ImmutableList<Text> linesOnCtrl
) {
    private static final Map<Item, Supplier<String>> CUSTOM_TOOLTIP_KEYS = new IdentityHashMap<>();

    @Nullable
    public static ItemDescription create(Item item, Palette palette) {
        return create(getTooltipTranslationKey(item), palette);
    }

    @Nullable
    public static ItemDescription create(String translationKey, Palette palette) {
        if (!canFillBuilder(translationKey + ".summary")) {
            return null;
        }

        Builder builder = new Builder(palette);
        fillBuilder(builder, translationKey);
        return builder.build();
    }

    public static boolean canFillBuilder(String translationKey) {
        return I18n.hasTranslation(translationKey);
    }

    public static void fillBuilder(Builder builder, String translationKey) {
        // Summary
        String summaryKey = translationKey + ".summary";
        if (I18n.hasTranslation(summaryKey)) {
            builder.addSummary(I18n.translate(summaryKey));
        }

        // Behaviours
        for (int i = 1; i < 100; i++) {
            String conditionKey = translationKey + ".condition" + i;
            String behaviourKey = translationKey + ".behaviour" + i;
            if (!I18n.hasTranslation(conditionKey))
                break;
            builder.addBehaviour(I18n.translate(conditionKey), I18n.translate(behaviourKey));
        }

        // Actions
        for (int i = 1; i < 100; i++) {
            String controlKey = translationKey + ".control" + i;
            String actionKey = translationKey + ".action" + i;
            if (!I18n.hasTranslation(controlKey))
                break;
            builder.addAction(I18n.translate(controlKey), I18n.translate(actionKey));
        }
    }

    public static void useKey(Item item, Supplier<String> supplier) {
        CUSTOM_TOOLTIP_KEYS.put(item, supplier);
    }

    public static void useKey(ItemConvertible item, String string) {
        useKey(item.asItem(), () -> string);
    }

    public static void referKey(ItemConvertible item, Supplier<? extends ItemConvertible> otherItem) {
        useKey(item.asItem(), () -> otherItem.get().asItem().getTranslationKey());
    }

    public static String getTooltipTranslationKey(Item item) {
        if (CUSTOM_TOOLTIP_KEYS.containsKey(item)) {
            return CUSTOM_TOOLTIP_KEYS.get(item).get() + ".tooltip";
        }
        return item.getTranslationKey() + ".tooltip";
    }

    public ImmutableList<Text> getCurrentLines() {
        if (AllKeys.hasShiftDown()) {
            return linesOnShift;
        } else if (AllKeys.hasControlDown()) {
            return linesOnCtrl;
        } else {
            return lines;
        }
    }

    public static class Builder {
        protected final Palette palette;
        protected final List<String> summary = new ArrayList<>();
        protected final List<Pair<String, String>> behaviours = new ArrayList<>();
        protected final List<Pair<String, String>> actions = new ArrayList<>();

        public Builder(Palette palette) {
            this.palette = palette;
        }

        public Builder addSummary(String summaryLine) {
            summary.add(summaryLine);
            return this;
        }

        public Builder addBehaviour(String condition, String behaviour) {
            behaviours.add(Pair.of(condition, behaviour));
            return this;
        }

        public Builder addAction(String condition, String action) {
            actions.add(Pair.of(condition, action));
            return this;
        }

        public ItemDescription build() {
            List<Text> lines = new ArrayList<>();
            List<Text> linesOnShift = new ArrayList<>();
            List<Text> linesOnCtrl = new ArrayList<>();

            for (String summaryLine : summary) {
                linesOnShift.addAll(TooltipHelper.cutStringTextComponent(summaryLine, palette));
            }

            if (!behaviours.isEmpty()) {
                linesOnShift.add(ScreenTexts.EMPTY);
            }

            for (Pair<String, String> behaviourPair : behaviours) {
                String condition = behaviourPair.getLeft();
                String behaviour = behaviourPair.getRight();
                linesOnShift.add(Text.literal(condition).formatted(GRAY));
                linesOnShift.addAll(TooltipHelper.cutStringTextComponent(behaviour, palette.primary(), palette.highlight(), 1));
            }

            for (Pair<String, String> actionPair : actions) {
                String condition = actionPair.getLeft();
                String action = actionPair.getRight();
                linesOnCtrl.add(Text.literal(condition).formatted(GRAY));
                linesOnCtrl.addAll(TooltipHelper.cutStringTextComponent(action, palette.primary(), palette.highlight(), 1));
            }

            boolean hasDescription = !linesOnShift.isEmpty();
            boolean hasControls = !linesOnCtrl.isEmpty();

            if (hasDescription || hasControls) {
                String[] holdDesc = CreateLang.translateDirect("tooltip.holdForDescription", "$").getString().split("\\$");
                String[] holdCtrl = CreateLang.translateDirect("tooltip.holdForControls", "$").getString().split("\\$");
                MutableText keyShift = CreateLang.translateDirect("tooltip.keyShift");
                MutableText keyCtrl = CreateLang.translateDirect("tooltip.keyCtrl");
                for (List<Text> list : Arrays.asList(lines, linesOnShift, linesOnCtrl)) {
                    boolean shift = list == linesOnShift;
                    boolean ctrl = list == linesOnCtrl;

                    if (holdDesc.length != 2 || holdCtrl.length != 2) {
                        list.add(0, Text.literal("Invalid lang formatting!"));
                        continue;
                    }

                    if (hasControls) {
                        MutableText tabBuilder = Text.empty();
                        tabBuilder.append(Text.literal(holdCtrl[0]).formatted(DARK_GRAY));
                        tabBuilder.append(keyCtrl.copyContentOnly().formatted(ctrl ? WHITE : GRAY));
                        tabBuilder.append(Text.literal(holdCtrl[1]).formatted(DARK_GRAY));
                        list.add(0, tabBuilder);
                    }

                    if (hasDescription) {
                        MutableText tabBuilder = Text.empty();
                        tabBuilder.append(Text.literal(holdDesc[0]).formatted(DARK_GRAY));
                        tabBuilder.append(keyShift.copyContentOnly().formatted(shift ? WHITE : GRAY));
                        tabBuilder.append(Text.literal(holdDesc[1]).formatted(DARK_GRAY));
                        list.add(0, tabBuilder);
                    }

                    if (shift || ctrl)
                        list.add(hasDescription && hasControls ? 2 : 1, ScreenTexts.EMPTY);
                }
            }

            if (!hasDescription) {
                linesOnCtrl.clear();
                linesOnShift.addAll(lines);
            }
            if (!hasControls) {
                linesOnCtrl.clear();
                linesOnCtrl.addAll(lines);
            }

            return new ItemDescription(ImmutableList.copyOf(lines), ImmutableList.copyOf(linesOnShift), ImmutableList.copyOf(linesOnCtrl));
        }
    }

    public static class Modifier implements TooltipModifier {
        protected final Item item;
        protected final Palette palette;
        protected String cachedLanguage;
        protected ItemDescription description;

        public Modifier(Item item, Palette palette) {
            this.item = item;
            this.palette = palette;
        }

        @Override
        public void modify(List<Text> tooltip, PlayerEntity player) {
            if (checkLocale()) {
                description = create(item, palette);
            }
            if (description == null) {
                return;
            }
            tooltip.addAll(1, description.getCurrentLines());
        }

        protected boolean checkLocale() {
            String currentLanguage = MinecraftClient.getInstance().getLanguageManager().getLanguage();
            if (!currentLanguage.equals(cachedLanguage)) {
                cachedLanguage = currentLanguage;
                return true;
            }
            return false;
        }
    }
}
