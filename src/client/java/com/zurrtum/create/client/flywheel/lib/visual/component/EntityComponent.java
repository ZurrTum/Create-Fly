package com.zurrtum.create.client.flywheel.lib.visual.component;

import com.zurrtum.create.client.flywheel.api.visual.DynamicVisual;

public interface EntityComponent {
    void beginFrame(DynamicVisual.Context context);

    void delete();
}
