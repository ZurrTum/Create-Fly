package com.zurrtum.create.client.flywheel.lib.task;

import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.task.TaskExecutor;

public record BarrierPlan<C>(Plan<C> first, Plan<C> second) implements SimplyComposedPlan<C> {
    public static <C> BarrierPlan<C> of(Plan<C> first, Plan<C> second) {
        return new BarrierPlan<>(first, second);
    }

    @Override
    public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
        first.execute(taskExecutor, context, () -> second.execute(taskExecutor, context, onCompletion));
    }
}
