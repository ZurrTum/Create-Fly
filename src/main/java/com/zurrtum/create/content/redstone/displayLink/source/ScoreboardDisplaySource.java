package com.zurrtum.create.content.redstone.displayLink.source;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;

import java.util.stream.Stream;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.Objective;

public class ScoreboardDisplaySource extends ValueListDisplaySource {

    @Override
    protected Stream<IntAttached<MutableComponent>> provideEntries(DisplayLinkContext context, int maxRows) {
        Level level = context.blockEntity().getLevel();
        if (!(level instanceof ServerLevel sLevel))
            return Stream.empty();

        String name = context.sourceConfig().getStringOr("Objective", "");

        return showScoreboard(sLevel, name, maxRows);
    }

    protected Stream<IntAttached<MutableComponent>> showScoreboard(ServerLevel sLevel, String objectiveName, int maxRows) {
        Objective objective = sLevel.getScoreboard().getObjective(objectiveName);
        if (objective == null)
            return notFound(objectiveName).stream();

        return sLevel.getScoreboard().listPlayerScores(objective).stream()
            .map(score -> IntAttached.with(score.value(), Component.literal(score.owner()).copy())).sorted(IntAttached.comparator()).limit(maxRows);
    }

    private ImmutableList<IntAttached<MutableComponent>> notFound(String objective) {
        return ImmutableList.of(IntAttached.with(404, Component.translatable("create.display_source.scoreboard.objective_not_found", objective)));
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