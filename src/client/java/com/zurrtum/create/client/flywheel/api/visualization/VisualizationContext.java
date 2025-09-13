package com.zurrtum.create.client.flywheel.api.visualization;

import com.zurrtum.create.client.flywheel.api.backend.BackendImplemented;
import com.zurrtum.create.client.flywheel.api.instance.InstancerProvider;
import net.minecraft.util.math.Vec3i;

@BackendImplemented
public interface VisualizationContext {
    InstancerProvider instancerProvider();

    Vec3i renderOrigin();

    VisualEmbedding createEmbedding(Vec3i var1);
}
