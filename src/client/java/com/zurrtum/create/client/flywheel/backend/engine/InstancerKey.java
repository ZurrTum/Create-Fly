package com.zurrtum.create.client.flywheel.backend.engine;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import com.zurrtum.create.client.flywheel.api.instance.InstanceType;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.backend.engine.embed.Environment;

public record InstancerKey<I extends Instance>(Environment environment, InstanceType<I> type, Model model, int bias) {
}
