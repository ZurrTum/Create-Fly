package com.zurrtum.create.client.flywheel.api.task;

public interface Plan<C> {
    void execute(TaskExecutor var1, C var2, Runnable var3);

    default void execute(TaskExecutor taskExecutor, C context) {
        this.execute(
            taskExecutor, context, () -> {
            }
        );
    }

    Plan<C> then(Plan<C> var1);

    Plan<C> and(Plan<C> var1);
}
