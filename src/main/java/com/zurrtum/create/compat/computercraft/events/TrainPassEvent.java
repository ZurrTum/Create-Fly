package com.zurrtum.create.compat.computercraft.events;

import com.zurrtum.create.content.trains.entity.Train;

public class TrainPassEvent implements ComputerEvent {

    public Train train;
    public boolean passing;

    public TrainPassEvent(Train train, boolean passing) {
        this.train = train;
        this.passing = passing;
    }

}
