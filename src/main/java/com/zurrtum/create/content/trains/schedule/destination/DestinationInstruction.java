package com.zurrtum.create.content.trains.schedule.destination;

import com.zurrtum.create.catnip.data.Glob;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.schedule.ScheduleRuntime;
import com.zurrtum.create.content.trains.station.GlobalStation;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class DestinationInstruction extends TextScheduleInstruction {
    public DestinationInstruction(Identifier id) {
        super(id);
    }

    @Override
    public boolean supportsConditions() {
        return true;
    }

    public String getFilter() {
        return getLabelText();
    }

    public String getFilterForRegex() {
        return Glob.toRegexPattern(getFilter(), "");
    }

    @Override
    @Nullable
    public DiscoveredPath start(ScheduleRuntime runtime, World level) {
        String regex = getFilterForRegex();
        boolean anyMatch = false;
        ArrayList<GlobalStation> validStations = new ArrayList<>();
        Train train = runtime.train;

        if (!train.hasForwardConductor() && !train.hasBackwardConductor()) {
            train.status.missingConductor();
            runtime.startCooldown();
            return null;
        }


        for (GlobalStation globalStation : train.graph.getPoints(EdgePointType.STATION)) {
            if (!globalStation.name.matches(regex))
                continue;
            anyMatch = true;
            validStations.add(globalStation);
        }

        DiscoveredPath best = train.navigation.findPathTo(validStations, Double.MAX_VALUE);
        if (best == null) {
            if (anyMatch)
                train.status.failedNavigation();
            else
                train.status.failedNavigationNoTarget(getFilter());
            runtime.startCooldown();
            return null;
        }

        return best;
    }

}