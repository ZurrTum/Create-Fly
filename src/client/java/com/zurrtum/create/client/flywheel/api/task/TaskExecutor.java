package com.zurrtum.create.client.flywheel.api.task;

import org.jetbrains.annotations.ApiStatus;

import java.util.concurrent.Executor;

@ApiStatus.NonExtendable
public interface TaskExecutor extends Executor {
    int threadCount();
}
