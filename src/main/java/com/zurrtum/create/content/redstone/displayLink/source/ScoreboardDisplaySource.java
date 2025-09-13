package com.zurrtum.create.content.redstone.displayLink.source;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.stream.Stream;

public class ScoreboardDisplaySource extends ValueListDisplaySource {

    @Override
    protected Stream<IntAttached<MutableText>> provideEntries(DisplayLinkContext context, int maxRows) {
        World level = context.blockEntity().getWorld();
        if (!(level instanceof ServerWorld sLevel))
            return Stream.empty();

        String name = context.sourceConfig().getString("Objective", "");

        return showScoreboard(sLevel, name, maxRows);
    }

    protected Stream<IntAttached<MutableText>> showScoreboard(ServerWorld sLevel, String objectiveName, int maxRows) {
        ScoreboardObjective objective = sLevel.getScoreboard().getNullableObjective(objectiveName);
        if (objective == null)
            return notFound(objectiveName).stream();

        return sLevel.getScoreboard().getScoreboardEntries(objective).stream()
            .map(score -> IntAttached.with(score.value(), Text.literal(score.owner()).copy())).sorted(IntAttached.comparator()).limit(maxRows);
    }

    private ImmutableList<IntAttached<MutableText>> notFound(String objective) {
        return ImmutableList.of(IntAttached.with(404, Text.translatable("create.display_source.scoreboard.objective_not_found", objective)));
    }

    @Override
    protected String getTranslationKey() {
        return "scoreboard";
    }

    @Override
    protected boolean valueFirst() {
        return false;
    }

}