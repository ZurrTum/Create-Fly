package com.zurrtum.create.compat.computercraft.events;

import com.zurrtum.create.content.trains.entity.Train;
import org.jetbrains.annotations.NotNull;

public class TrainPassEvent implements ComputerEvent {

    public @NotNull Train train;
    public boolean passing;

    public TrainPassEvent(@NotNull Train train, boolean passing) {
        this.train = train;
        this.passing = passing;
    }

}
