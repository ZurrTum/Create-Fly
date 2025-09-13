package com.zurrtum.create.client.flywheel.lib.internal;

import com.zurrtum.create.client.flywheel.impl.FlwLibXplatImpl;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedModelBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.BlockModelBuilder;

public interface FlwLibXplat {
    FlwLibXplat INSTANCE = new FlwLibXplatImpl();

    SimpleModel buildBakedModelBuilder(BakedModelBuilder builder);

    SimpleModel buildBlockModelBuilder(BlockModelBuilder builder);
}
