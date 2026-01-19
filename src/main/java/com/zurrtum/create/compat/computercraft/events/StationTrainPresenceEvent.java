package com.zurrtum.create.compat.computercraft.events;

import com.zurrtum.create.content.trains.entity.Train;

public class StationTrainPresenceEvent implements ComputerEvent {

    public enum Type {
        IMMINENT("train_imminent"),
        ARRIVAL("train_arrival"),
        DEPARTURE("train_departure");

        public final String name;

        Type(String name) {
            this.name = name;
        }
    }

    public Type type;
    public Train train;

    public StationTrainPresenceEvent(Type type, Train train) {
        this.type = type;
        this.train = train;
    }

}
