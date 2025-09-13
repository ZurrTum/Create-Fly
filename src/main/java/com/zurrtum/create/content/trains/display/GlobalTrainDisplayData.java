package com.zurrtum.create.content.trains.display;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Glob;
import com.zurrtum.create.content.trains.entity.Train;
import net.minecraft.text.MutableText;

import java.util.*;

public class GlobalTrainDisplayData {

    public static final Map<String, Collection<TrainDeparturePrediction>> statusByDestination = new HashMap<>();
    public static boolean updateTick = false;

    public static void refresh() {
        statusByDestination.clear();
        for (Train train : Create.RAILWAYS.trains.values()) {
            if (train.runtime.paused || train.runtime.getSchedule() == null)
                continue;
            if (train.derailed || train.graph == null)
                continue;
            for (TrainDeparturePrediction prediction : train.runtime.submitPredictions())
                statusByDestination.computeIfAbsent(prediction.destination, $ -> new ArrayList<>()).add(prediction);
        }
    }

    public static List<TrainDeparturePrediction> prepare(String filter, int maxLines) {
        String regex = Glob.toRegexPattern(filter, "");
        return statusByDestination.entrySet().stream().filter(e -> e.getKey().matches(regex)).flatMap(e -> e.getValue().stream()).sorted()
            .limit(maxLines).toList();
    }

    public static class TrainDeparturePrediction implements Comparable<TrainDeparturePrediction> {
        public Train train;
        public int ticks;
        public MutableText scheduleTitle;
        public String destination;

        public TrainDeparturePrediction(Train train, int ticks, MutableText scheduleTitle, String destination) {
            this.scheduleTitle = scheduleTitle;
            this.destination = destination;
            this.train = train;
            this.ticks = ticks;
        }

        private int getCompareTicks() {
            if (ticks == -1)
                return Integer.MAX_VALUE;
            if (ticks < 200)
                return 0;
            return ticks;
        }

        @Override
        public int compareTo(TrainDeparturePrediction o) {
            int compare = Integer.compare(getCompareTicks(), o.getCompareTicks());
            if (compare == 0)
                return train.name.getString().compareTo(o.train.name.getString());
            return compare;
        }

    }

}
