package com.zurrtum.create.client.flywheel.api.task;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.Executor;

@ApiStatus.NonExtendable
public interface TaskExecutor extends Executor {
    /**
     * Check for the number of threads this executor uses.
     * <br>
     * May be helpful when determining how many chunks to divide a task into.
     *
     * @return The number of threads this executor uses.
     */
    int threadCount();
}
