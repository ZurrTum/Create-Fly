package com.zurrtum.create.client.flywheel.impl.task;

import com.zurrtum.create.client.flywheel.api.task.TaskExecutor;
import com.zurrtum.create.client.flywheel.lib.task.SimplyComposedPlan;

public record RaisePlan<C>(Flag flag) implements SimplyComposedPlan<C> {
    public static <C> RaisePlan<C> raise(Flag flag) {
        return new RaisePlan<>(flag);
    }

    @Override
    public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
        flag.raise();
        onCompletion.run();
    }
}
