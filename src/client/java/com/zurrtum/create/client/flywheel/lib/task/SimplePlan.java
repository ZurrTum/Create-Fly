package com.zurrtum.create.client.flywheel.lib.task;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.task.TaskExecutor;
import com.zurrtum.create.client.flywheel.lib.task.functional.RunnableWithContext;

import java.util.List;

public record SimplePlan<C>(List<RunnableWithContext<C>> parallelTasks) implements SimplyComposedPlan<C> {
    @SafeVarargs
    public static <C> SimplePlan<C> of(RunnableWithContext.Ignored<C>... tasks) {
        return new SimplePlan<>(List.of(tasks));
    }

    @SafeVarargs
    public static <C> SimplePlan<C> of(RunnableWithContext<C>... tasks) {
        return new SimplePlan<>(List.of(tasks));
    }

    public static <C> SimplePlan<C> of(List<RunnableWithContext<C>> tasks) {
        return new SimplePlan<>(tasks);
    }

    @Override
    public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
        if (parallelTasks.isEmpty()) {
            onCompletion.run();
            return;
        }

        taskExecutor.execute(() -> Distribute.tasks(taskExecutor, context, onCompletion, parallelTasks, RunnableWithContext::run));
    }

    @Override
    public Plan<C> and(Plan<C> plan) {
        if (plan instanceof SimplePlan<C> simple) {
            return of(ImmutableList.<RunnableWithContext<C>>builder().addAll(parallelTasks).addAll(simple.parallelTasks).build());
        }
        return SimplyComposedPlan.super.and(plan);
    }
}
