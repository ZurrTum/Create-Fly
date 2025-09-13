package com.zurrtum.create.content.redstone.displayLink.source;

import com.zurrtum.create.Create;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.observer.TrackObserverBlockEntity;
import net.minecraft.text.MutableText;

import java.util.UUID;

public class ObservedTrainNameSource extends SingleLineDisplaySource {
    @Override
    protected MutableText provideLine(DisplayLinkContext context, DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof TrackObserverBlockEntity observerBE))
            return EMPTY_LINE;
        TrackObserver observer = observerBE.getObserver();
        if (observer == null)
            return EMPTY_LINE;
        UUID currentTrain = observer.getCurrentTrain();
        if (currentTrain == null)
            return EMPTY_LINE;
        Train train = Create.RAILWAYS.trains.get(currentTrain);
        if (train == null)
            return EMPTY_LINE;
        return train.name.copy();
    }

    @Override
    public int getPassiveRefreshTicks() {
        return 400;
    }

    @Override
    protected String getTranslationKey() {
        return "observed_train_name";
    }

    @Override
    public boolean allowsLabeling(DisplayLinkContext context) {
        return true;
    }
}