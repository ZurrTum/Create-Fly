package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardCriterion.RenderType;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.world.World;

import java.util.stream.Stream;

public abstract class StatTrackingDisplaySource extends ScoreboardDisplaySource {

    @Override
    protected Stream<IntAttached<MutableText>> provideEntries(DisplayLinkContext context, int maxRows) {
        World level = context.blockEntity().getWorld();
        if (!(level instanceof ServerWorld sLevel))
            return Stream.empty();

        String name = "create_auto_" + getObjectiveName();
        Scoreboard scoreboard = level.getScoreboard();
        if (scoreboard.getNullableObjective(name) == null)
            scoreboard.addObjective(name, ScoreboardCriterion.DUMMY, getObjectiveDisplayName(), RenderType.INTEGER, false, null);
        ScoreboardObjective objective = scoreboard.getNullableObjective(name);

        sLevel.getServer().getPlayerManager().getPlayerList()
            .forEach(s -> scoreboard.getOrCreateScore(ScoreHolder.fromName(s.getNameForScoreboard()), objective).setScore(updatedScoreOf(s)));

        return showScoreboard(sLevel, name, maxRows);
    }

    protected abstract String getObjectiveName();

    protected abstract Text getObjectiveDisplayName();

    protected abstract int updatedScoreOf(ServerPlayerEntity player);

    @Override
    protected boolean shortenNumbers(DisplayLinkContext context) {
        return false;
    }
}
