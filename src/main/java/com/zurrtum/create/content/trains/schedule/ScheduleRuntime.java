package com.zurrtum.create.content.trains.schedule;

import com.mojang.serialization.*;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.trains.display.GlobalTrainDisplayData.TrainDeparturePrediction;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.content.trains.graph.DiscoveredPath;
import com.zurrtum.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.zurrtum.create.content.trains.schedule.condition.ScheduledDelay;
import com.zurrtum.create.content.trains.schedule.destination.ChangeTitleInstruction;
import com.zurrtum.create.content.trains.schedule.destination.DestinationInstruction;
import com.zurrtum.create.content.trains.schedule.destination.ScheduleInstruction;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import java.util.*;

public class ScheduleRuntime {

    private static final int TBD = -1;
    private static final int INVALID = -2;

    public enum State implements StringIdentifiable {
        PRE_TRANSIT,
        IN_TRANSIT,
        POST_TRANSIT;

        public static final Codec<State> CODEC = StringIdentifiable.createCodec(State::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public Train train;
    public Schedule schedule;

    public boolean isAutoSchedule;
    public boolean paused;
    public boolean completed;
    public int currentEntry;
    public State state;

    public List<Integer> conditionProgress;
    public List<NbtCompound> conditionContext;
    public String currentTitle;

    public int ticksInTransit;
    public List<Integer> predictionTicks;

    public boolean displayLinkUpdateRequested;

    private static final int INTERVAL = 40;
    private int cooldown;

    public ScheduleRuntime(Train train) {
        this.train = train;
        reset();
    }

    public void startCooldown() {
        cooldown = INTERVAL;
    }

    public void destinationReached() {
        if (state != State.IN_TRANSIT)
            return;
        state = State.POST_TRANSIT;
        conditionProgress.clear();
        conditionContext.clear();
        displayLinkUpdateRequested = true;
        for (Carriage carriage : train.carriages)
            carriage.storage.resetIdleCargoTracker();

        if (ticksInTransit > 0) {
            int current = predictionTicks.get(currentEntry);
            if (current > 0)
                ticksInTransit = (current + ticksInTransit) / 2;
            predictionTicks.set(currentEntry, ticksInTransit);
        }

        if (currentEntry >= schedule.entries.size())
            return;
        List<List<ScheduleWaitCondition>> conditions = schedule.entries.get(currentEntry).conditions;
        for (int i = 0; i < conditions.size(); i++) {
            conditionProgress.add(0);
            conditionContext.add(new NbtCompound());
        }
    }

    public void transitInterrupted() {
        if (schedule == null || state != State.IN_TRANSIT)
            return;
        state = State.PRE_TRANSIT;
        cooldown = 0;
    }

    public void tick(World level) {
        if (schedule == null)
            return;
        if (paused)
            return;
        if (train.derailed)
            return;
        if (train.navigation.destination != null) {
            ticksInTransit++;
            return;
        }

        if (checkEndOfScheduleReached())
            return;
        if (cooldown-- > 0)
            return;
        if (state == State.IN_TRANSIT)
            return;
        if (state == State.POST_TRANSIT) {
            tickConditions(level);
            return;
        }

        DiscoveredPath nextPath = startCurrentInstruction(level);
        if (nextPath == null)
            return;

        train.status.successfulNavigation();
        if (nextPath.destination == train.getCurrentStation()) {
            state = State.IN_TRANSIT;
            destinationReached();
            return;
        }
        if (train.navigation.startNavigation(nextPath) != TBD) {
            state = State.IN_TRANSIT;
            ticksInTransit = 0;
        }
    }

    private boolean checkEndOfScheduleReached() {
        if (currentEntry < schedule.entries.size())
            return false;

        currentEntry = 0;
        if (!schedule.cyclic) {
            paused = true;
            completed = true;
        }
        return true;
    }

    public void tickConditions(World level) {
        ScheduleEntry entry = schedule.entries.get(currentEntry);
        List<List<ScheduleWaitCondition>> conditions = entry.conditions;

        if (!entry.instruction.supportsConditions()) {
            state = State.PRE_TRANSIT;
            currentEntry++;
            return;
        }

        for (int i = 0; i < conditions.size(); i++) {
            List<ScheduleWaitCondition> list = conditions.get(i);
            int progress = conditionProgress.get(i);

            if (progress >= list.size()) {
                state = State.PRE_TRANSIT;
                currentEntry++;
                return;
            }

            NbtCompound tag = conditionContext.get(i);
            ScheduleWaitCondition condition = list.get(progress);
            int prevVersion = tag.getInt("StatusVersion", 0);

            if (condition.tickCompletion(level, train, tag)) {
                conditionContext.set(i, new NbtCompound());
                conditionProgress.set(i, progress + 1);
                displayLinkUpdateRequested |= i == 0;
            }

            displayLinkUpdateRequested |= i == 0 && prevVersion != tag.getInt("StatusVersion", 0);
        }

        for (Carriage carriage : train.carriages)
            carriage.storage.tickIdleCargoTracker();
    }

    public DiscoveredPath startCurrentInstruction(World level) {
        if (checkEndOfScheduleReached())
            return null;

        ScheduleEntry entry = schedule.entries.get(currentEntry);
        ScheduleInstruction instruction = entry.instruction;
        return instruction.start(this, level);
    }

    public void setSchedule(Schedule schedule, boolean auto) {
        reset();
        this.schedule = schedule;
        currentEntry = MathHelper.clamp(schedule.savedProgress, 0, schedule.entries.size() - 1);
        paused = false;
        isAutoSchedule = auto;
        train.status.newSchedule();
        predictionTicks = new ArrayList<>();
        schedule.entries.forEach($ -> predictionTicks.add(TBD));
        displayLinkUpdateRequested = true;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void discardSchedule() {
        train.navigation.cancelNavigation();
        reset();
    }

    private void reset() {
        paused = true;
        completed = false;
        isAutoSchedule = false;
        currentEntry = 0;
        currentTitle = "";
        schedule = null;
        state = State.PRE_TRANSIT;
        conditionProgress = new ArrayList<>();
        conditionContext = new ArrayList<>();
        predictionTicks = new ArrayList<>();
    }

    public Collection<TrainDeparturePrediction> submitPredictions() {
        Collection<TrainDeparturePrediction> predictions = new ArrayList<>();
        int entryCount = schedule.entries.size();
        int accumulatedTime = 0;
        int current = currentEntry;

        // Current
        if (state == State.POST_TRANSIT || current >= entryCount) {
            GlobalStation currentStation = train.getCurrentStation();
            if (currentStation != null)
                predictions.add(createPrediction(current, currentStation.name, currentTitle, 0));
            int departureTime = estimateStayDuration(current);
            if (departureTime == INVALID)
                accumulatedTime = INVALID;
            else
                accumulatedTime += departureTime;

        } else {
            GlobalStation destination = train.navigation.destination;
            if (destination != null) {
                double speed = Math.min(train.throttle * train.maxSpeed(), (train.maxSpeed() + train.maxTurnSpeed()) / 2);
                int timeRemaining = (int) (train.navigation.distanceToDestination / speed) * 2;

                if (predictionTicks.size() > current && train.navigation.distanceStartedAt != 0) {
                    float predictedTime = predictionTicks.get(current);
                    if (predictedTime > 0) {
                        predictedTime *= MathHelper.clamp(train.navigation.distanceToDestination / train.navigation.distanceStartedAt, 0, 1);
                        timeRemaining = (timeRemaining + (int) predictedTime) / 2;
                    }
                }

                accumulatedTime += timeRemaining;
                predictions.add(createPrediction(current, destination.name, currentTitle, accumulatedTime));

                int departureTime = estimateStayDuration(current);
                if (departureTime != INVALID)
                    accumulatedTime += departureTime;
                else
                    accumulatedTime = INVALID;

            } else
                predictForEntry(current, currentTitle, accumulatedTime, predictions);
        }

        // Upcoming
        String currentTitle = this.currentTitle;
        for (int i = 1; i < entryCount; i++) {
            int index = (i + current) % entryCount;
            if (index == 0 && !schedule.cyclic)
                break;

            if (schedule.entries.get(index).instruction instanceof ChangeTitleInstruction title) {
                currentTitle = title.getScheduleTitle();
                continue;
            }

            accumulatedTime = predictForEntry(index, currentTitle, accumulatedTime, predictions);
        }

        predictions.removeIf(Objects::isNull);
        return predictions;
    }

    private int predictForEntry(int index, String currentTitle, int accumulatedTime, Collection<TrainDeparturePrediction> predictions) {
        ScheduleEntry entry = schedule.entries.get(index);
        if (!(entry.instruction instanceof DestinationInstruction filter))
            return accumulatedTime;
        if (predictionTicks.size() <= currentEntry)
            return accumulatedTime;

        int departureTime = estimateStayDuration(index);

        if (accumulatedTime < 0) {
            predictions.add(createPrediction(index, filter.getFilter(), currentTitle, accumulatedTime));
            return Math.min(accumulatedTime, departureTime);
        }

        int predictedTime = predictionTicks.get(index);
        accumulatedTime += predictedTime;

        if (predictedTime == TBD)
            accumulatedTime = TBD;

        predictions.add(createPrediction(index, filter.getFilter(), currentTitle, accumulatedTime));

        if (accumulatedTime != TBD)
            accumulatedTime += departureTime;

        if (departureTime == INVALID)
            accumulatedTime = INVALID;

        return accumulatedTime;
    }

    private int estimateStayDuration(int index) {
        if (index >= schedule.entries.size()) {
            if (!schedule.cyclic)
                return INVALID;
            index = 0;
        }

        ScheduleEntry scheduleEntry = schedule.entries.get(index);
        Columns:
        for (List<ScheduleWaitCondition> list : scheduleEntry.conditions) {
            int total = 0;
            for (ScheduleWaitCondition condition : list) {
                if (!(condition instanceof ScheduledDelay wait))
                    continue Columns;
                total += wait.totalWaitTicks();
            }
            return total;
        }

        return INVALID;
    }

    private TrainDeparturePrediction createPrediction(int index, String destination, String currentTitle, int time) {
        if (time == INVALID)
            return null;

        int size = schedule.entries.size();
        if (index >= size) {
            if (!schedule.cyclic) {
                return new TrainDeparturePrediction(train, time, ScreenTexts.space(), destination);
            }
            index %= size;
        }

        String text = currentTitle;
        if (text.isBlank()) {
            for (int i = 1; i < size; i++) {
                int j = (index + i) % size;
                ScheduleEntry scheduleEntry = schedule.entries.get(j);
                if (!(scheduleEntry.instruction instanceof DestinationInstruction instruction))
                    continue;
                text = instruction.getFilter().replaceAll("\\*", "").trim();
                break;
            }
        }

        return new TrainDeparturePrediction(train, time, Text.literal(text), destination);
    }

    public void write(WriteView view) {
        view.putInt("CurrentEntry", currentEntry);
        view.putBoolean("AutoSchedule", isAutoSchedule);
        view.putBoolean("Paused", paused);
        view.putBoolean("Completed", completed);
        if (schedule != null)
            schedule.write(view.get("Schedule"));
        view.put("State", State.CODEC, state);
        view.putIntArray("ConditionProgress", conditionProgress.stream().mapToInt(Integer::intValue).toArray());
        view.put("ConditionContext", CreateCodecs.NBT_COMPOUND_LIST_CODEC, conditionContext);
        view.putIntArray("TransitTimes", predictionTicks.stream().mapToInt(Integer::intValue).toArray());
    }

    public static <T> DataResult<T> encode(final ScheduleRuntime input, final DynamicOps<T> ops, final T empty) {
        RecordBuilder<T> map = ops.mapBuilder();
        map.add("CurrentEntry", ops.createInt(input.currentEntry));
        map.add("AutoSchedule", ops.createBoolean(input.isAutoSchedule));
        map.add("Paused", ops.createBoolean(input.paused));
        map.add("Completed", ops.createBoolean(input.completed));
        if (input.schedule != null)
            map.add("Schedule", Schedule.encode(input.schedule, ops, empty));
        map.add("State", input.state, State.CODEC);
        map.add("ConditionProgress", ops.createIntList(input.conditionProgress.stream().mapToInt(Integer::intValue)));
        map.add("ConditionContext", input.conditionContext, CreateCodecs.NBT_COMPOUND_LIST_CODEC);
        map.add("TransitTimes", ops.createIntList(input.predictionTicks.stream().mapToInt(Integer::intValue)));
        return map.build(empty);
    }

    public void read(ReadView view) {
        reset();
        paused = view.getBoolean("Paused", false);
        completed = view.getBoolean("Completed", false);
        isAutoSchedule = view.getBoolean("AutoSchedule", false);
        currentEntry = Math.max(0, view.getInt("CurrentEntry", 0));
        view.getOptionalReadView("Schedule").ifPresent(scheduleView -> schedule = Schedule.read(scheduleView));
        state = view.read("State", State.CODEC).orElse(State.PRE_TRANSIT);
        view.getOptionalIntArray("ConditionProgress").ifPresent(array -> {
            for (int i : array) {
                conditionProgress.add(i);
            }
        });
        view.read("ConditionContext", CreateCodecs.NBT_COMPOUND_LIST_CODEC).ifPresent(conditionContext::addAll);

        if (schedule != null) {
            schedule.entries.forEach($ -> predictionTicks.add(TBD));
            view.getOptionalIntArray("TransitTimes").ifPresent(readTransits -> {
                if (readTransits.length == schedule.entries.size()) {
                    for (int i = 0; i < readTransits.length; i++) {
                        predictionTicks.set(i, readTransits[i]);
                    }
                }
            });
        }
    }

    public <T> void decode(DynamicOps<T> ops, T input) {
        reset();
        MapLike<T> map = ops.getMap(input).getOrThrow();
        paused = ops.getBooleanValue(map.get("Paused")).result().orElse(false);
        completed = ops.getBooleanValue(map.get("Completed")).result().orElse(false);
        isAutoSchedule = ops.getBooleanValue(map.get("AutoSchedule")).result().orElse(false);
        currentEntry = Math.max(0, ops.getNumberValue(map.get("CurrentEntry"), 0).intValue());
        Optional.ofNullable(map.get("Schedule")).ifPresent(scheduleView -> schedule = Schedule.decode(ops, scheduleView));
        state = State.CODEC.parse(ops, map.get("State")).result().orElse(State.PRE_TRANSIT);
        ops.getIntStream(map.get("ConditionProgress")).ifSuccess(stream -> stream.forEach(i -> conditionProgress.add(i)));
        CreateCodecs.NBT_COMPOUND_LIST_CODEC.parse(ops, map.get("ConditionContext")).ifSuccess(list -> conditionContext.addAll(list));

        if (schedule != null) {
            schedule.entries.forEach($ -> predictionTicks.add(TBD));
            ops.getIntStream(map.get("TransitTimes")).ifSuccess(stream -> {
                int[] readTransits = stream.toArray();
                if (readTransits.length == schedule.entries.size()) {
                    for (int i = 0; i < readTransits.length; i++) {
                        predictionTicks.set(i, readTransits[i]);
                    }
                }
            });
        }
    }

    public ItemStack returnSchedule(RegistryWrapper.WrapperLookup registries) {
        if (schedule == null)
            return ItemStack.EMPTY;
        ItemStack stack = AllItems.SCHEDULE.getDefaultStack();
        schedule.savedProgress = currentEntry;
        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "Schedule", Create.LOGGER)) {
            NbtWriteView view = NbtWriteView.create(logging, registries);
            schedule.write(view);
            stack.set(AllDataComponents.TRAIN_SCHEDULE, view.getNbt());
        }
        stack = isAutoSchedule ? ItemStack.EMPTY : stack;
        discardSchedule();
        return stack;
    }

    public void setSchedulePresentClientside(boolean present) {
        schedule = present ? new Schedule() : null;
    }

    public MutableText getWaitingStatus(World level) {
        List<List<ScheduleWaitCondition>> conditions = schedule.entries.get(currentEntry).conditions;
        if (conditions.isEmpty() || conditionProgress.isEmpty() || conditionContext.isEmpty())
            return Text.empty();

        List<ScheduleWaitCondition> list = conditions.getFirst();
        int progress = conditionProgress.getFirst();
        if (progress >= list.size())
            return Text.empty();

        NbtCompound tag = conditionContext.getFirst();
        ScheduleWaitCondition condition = list.get(progress);
        return condition.getWaitingStatus(level, train, tag);
    }

}
