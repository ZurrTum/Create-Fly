package com.zurrtum.create.client.flywheel.lib.task;

import com.zurrtum.create.client.flywheel.api.task.Plan;
import com.zurrtum.create.client.flywheel.api.task.TaskExecutor;
import com.zurrtum.create.client.flywheel.lib.task.functional.BooleanSupplierWithContext;

/**
 * Executes one plan or another, depending on a dynamically evaluated condition.
 *
 * @param condition The condition to branch on.
 * @param onTrue    The plan to execute if the condition is true.
 * @param <C>       The type of the context object.
 */
public record ConditionalPlan<C>(
    BooleanSupplierWithContext<C> condition, Plan<C> onTrue
) implements SimplyComposedPlan<C> {
    public static <C> Builder<C> on(BooleanSupplierWithContext<C> condition) {
        return new Builder<>(condition);
    }

    public static <C> Builder<C> on(BooleanSupplierWithContext.Ignored<C> condition) {
        return new Builder<>(condition);
    }

    @Override
    public void execute(TaskExecutor taskExecutor, C context, Runnable onCompletion) {
        if (condition.getAsBoolean(context)) {
            onTrue.execute(taskExecutor, context, onCompletion);
        } else {
            onCompletion.run();
        }
    }

    public static final class Builder<C> {
        private final BooleanSupplierWithContext<C> condition;

        public Builder(BooleanSupplierWithContext<C> condition) {
            this.condition = condition;
        }

        public ConditionalPlan<C> then(Plan<C> onTrue) {
            return new ConditionalPlan<>(condition, onTrue);
        }
    }
}
