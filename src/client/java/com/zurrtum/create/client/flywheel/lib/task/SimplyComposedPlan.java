package com.zurrtum.create.client.flywheel.lib.task;

import com.zurrtum.create.client.flywheel.api.task.Plan;

public interface SimplyComposedPlan<C> extends Plan<C> {
    @Override
    default Plan<C> then(Plan<C> plan) {
        return BarrierPlan.of(this, plan);
    }

    @Override
    default Plan<C> and(Plan<C> plan) {
        return NestedPlan.of(this, plan);
    }
}
