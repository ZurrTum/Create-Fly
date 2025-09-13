package com.zurrtum.create.content.redstone.displayLink.source;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;

public class DeathCounterDisplaySource extends StatTrackingDisplaySource {

    @Override
    protected int updatedScoreOf(ServerPlayerEntity player) {
        return player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.DEATHS));
    }

    @Override
    protected String getTranslationKey() {
        return "player_deaths";
    }

    @Override
    protected String getObjectiveName() {
        return "deaths";
    }

    @Override
    protected Text getObjectiveDisplayName() {
        return Text.translatable("create.display_source.scoreboard.objective.deaths");
    }

}
