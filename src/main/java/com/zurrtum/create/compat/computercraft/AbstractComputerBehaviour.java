package com.zurrtum.create.compat.computercraft;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.compat.computercraft.events.ComputerEvent;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.observer.TrackObserver;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class AbstractComputerBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<AbstractComputerBehaviour> TYPE = new BehaviourType<>();

    public static boolean contains(SmartBlockEntity be) {
        AbstractComputerBehaviour behaviour = be.getBehaviour(TYPE);
        if (behaviour == null) {
            return false;
        }
        return behaviour.hasAttachedComputer();
    }

    @Nullable
    public static AbstractComputerBehaviour get(SmartBlockEntity be) {
        AbstractComputerBehaviour behaviour = be.getBehaviour(TYPE);
        if (behaviour == null) {
            return null;
        }
        if (behaviour.isActive() && behaviour.hasAttachedComputer()) {
            return behaviour;
        }
        return null;
    }

    public AbstractComputerBehaviour(SmartBlockEntity te) {
        super(te);
    }

    public abstract boolean isActive();

    public abstract void setHasAttachedComputer(boolean hasAttachedComputer);

    public abstract boolean hasAttachedComputer();

    public abstract void queueKineticsChange(float speed, float capacity, float stress, boolean overStressed);

    public abstract void queuePackageReceived(ItemStack box);

    public abstract void queuePackageCreated(ItemStack createdBox);

    public abstract void queueRepackage(List<BigItemStack> boxesToExport);

    public abstract void queueTrainPass(TrackObserver observer, boolean shouldBePowered);

    public abstract void queueSignalState(SignalBlockEntity.SignalState state);

    public abstract void queueStationTrain(Train imminentTrain, boolean newlyArrived, boolean trainPresent);

    public abstract void prepareComputerEvent(@NotNull ComputerEvent event);

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
