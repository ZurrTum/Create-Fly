package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.api.behaviour.display.DisplaySource;
import com.zurrtum.create.content.fluids.tank.BoilerData;
import com.zurrtum.create.content.fluids.tank.FluidTankBlockEntity;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import com.zurrtum.create.content.trains.display.FlapDisplaySection;
import joptsimple.internal.Strings;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BoilerDisplaySource extends DisplaySource {

    public static final List<MutableText> notEnoughSpaceSingle = List.of(Text.translatable("create.display_source.boiler.not_enough_space")
        .append(Text.translatable("create.display_source.boiler.for_boiler_status")));

    public static final List<MutableText> notEnoughSpaceDouble = List.of(
        Text.translatable("create.display_source.boiler.not_enough_space"),
        Text.translatable("create.display_source.boiler.for_boiler_status")
    );

    public static final List<List<MutableText>> notEnoughSpaceFlap = List.of(
        List.of(Text.translatable("create.display_source.boiler.not_enough_space")),
        List.of(Text.translatable("create.display_source.boiler.for_boiler_status"))
    );

    @Override
    public List<MutableText> provideText(DisplayLinkContext context, DisplayTargetStats stats) {
        if (stats.maxRows() < 2)
            return notEnoughSpaceSingle;
        else if (stats.maxRows() < 4)
            return notEnoughSpaceDouble;

        boolean isBook = context.getTargetBlockEntity() instanceof LecternBlockEntity;

        if (isBook) {
            Stream<MutableText> componentList = getComponents(context, false).map(components -> {
                Optional<MutableText> reduce = components.stream().reduce(MutableText::append);
                return reduce.orElse(EMPTY_LINE);
            });

            return List.of(componentList.reduce((comp1, comp2) -> {
                return comp1.append(Text.literal("\n")).append(comp2);
            }).orElse(EMPTY_LINE));
        }

        return getComponents(context, false).map(components -> {
            Optional<MutableText> reduce = components.stream().reduce(MutableText::append);
            return reduce.orElse(EMPTY_LINE);
        }).toList();
    }

    @Override
    public List<List<MutableText>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
        if (stats.maxRows() < 4) {
            context.flapDisplayContext = Boolean.FALSE;
            return notEnoughSpaceFlap;
        }

        List<List<MutableText>> components = getComponents(context, true).toList();

        if (stats.maxColumns() * FlapDisplaySection.MONOSPACE < 6 * FlapDisplaySection.MONOSPACE + components.get(1).get(1).getString()
            .length() * FlapDisplaySection.WIDE_MONOSPACE) {
            context.flapDisplayContext = Boolean.FALSE;
            return notEnoughSpaceFlap;
        }

        return components;
    }

    @Override
    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout, int lineIndex) {
        if (lineIndex == 0 || context.flapDisplayContext instanceof Boolean b && !b) {
            if (layout.isLayout("Default"))
                return;

            layout.loadDefault(flapDisplay.getMaxCharCount());
            return;
        }

        String layoutKey = "Boiler";
        if (layout.isLayout(layoutKey))
            return;

        int labelLength = (int) (labelWidth() * FlapDisplaySection.MONOSPACE);
        float maxSpace = flapDisplay.getMaxCharCount(1) * FlapDisplaySection.MONOSPACE;
        FlapDisplaySection label = new FlapDisplaySection(labelLength, "alphabet", false, true);
        FlapDisplaySection symbols = new FlapDisplaySection(maxSpace - labelLength, "pixel", false, false).wideFlaps();

        layout.configure(layoutKey, List.of(label, symbols));
    }

    private Stream<List<MutableText>> getComponents(DisplayLinkContext context, boolean forFlapDisplay) {
        BlockEntity sourceBE = context.getSourceBlockEntity();
        if (!(sourceBE instanceof FluidTankBlockEntity tankBlockEntity))
            return Stream.of(EMPTY);

        tankBlockEntity = tankBlockEntity.getControllerBE();
        if (tankBlockEntity == null)
            return Stream.of(EMPTY);

        BoilerData boiler = tankBlockEntity.boiler;

        int totalTankSize = tankBlockEntity.getTotalTankSize();

        boiler.calcMinMaxForSize(totalTankSize);

        String label = forFlapDisplay ? "create.boiler.status" : "create.boiler.status_short";
        MutableText size = labelOf(forFlapDisplay ? "size" : "");
        MutableText water = labelOf(forFlapDisplay ? "water" : "");
        MutableText heat = labelOf(forFlapDisplay ? "heat" : "");

        int lw = labelWidth();
        if (forFlapDisplay) {
            size = Text.literal(Strings.repeat(' ', lw - labelWidthOf("size"))).append(size);
            water = Text.literal(Strings.repeat(' ', lw - labelWidthOf("water"))).append(water);
            heat = Text.literal(Strings.repeat(' ', lw - labelWidthOf("heat"))).append(heat);
        }

        return Stream.of(
            List.of(Text.translatable(label, boiler.getHeatLevelTextComponent())),
            List.of(size, boiler.getSizeComponent(!forFlapDisplay, forFlapDisplay, Formatting.RESET)),
            List.of(water, boiler.getWaterComponent(!forFlapDisplay, forFlapDisplay, Formatting.RESET)),
            List.of(heat, boiler.getHeatComponent(!forFlapDisplay, forFlapDisplay, Formatting.RESET))
        );
    }

    private int labelWidth() {
        return Math.max(labelWidthOf("water"), Math.max(labelWidthOf("size"), labelWidthOf("heat")));
    }

    private int labelWidthOf(String label) {
        return labelOf(label).getString().length();
    }

    private MutableText labelOf(String label) {
        if (label.isBlank())
            return Text.empty();
        return Text.translatable("create.boiler." + label);
    }

    @Override
    protected String getTranslationKey() {
        return "boiler_status";
    }
}