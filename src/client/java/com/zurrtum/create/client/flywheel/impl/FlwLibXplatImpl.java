package com.zurrtum.create.client.flywheel.impl;

import com.zurrtum.create.client.flywheel.lib.internal.FlwLibXplat;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedModelBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.BlockModelBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.ModelBuilderImpl;

public class FlwLibXplatImpl implements FlwLibXplat {

    @Override
    public SimpleModel buildBakedModelBuilder(BakedModelBuilder builder) {
        return ModelBuilderImpl.buildBakedModelBuilder(builder);
    }

    @Override
    public SimpleModel buildBlockModelBuilder(BlockModelBuilder builder) {
        return ModelBuilderImpl.buildBlockModelBuilder(builder);
    }
}
