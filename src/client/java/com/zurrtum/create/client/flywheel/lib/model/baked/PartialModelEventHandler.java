package com.zurrtum.create.client.flywheel.lib.model.baked;

import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.util.Identifier;

import java.util.Map;

public final class PartialModelEventHandler {
    private PartialModelEventHandler() {
    }

    public static Map<Identifier, PartialModel> getRegisterAdditional() {
        return PartialModel.ALL;
    }

    public static void onBakingCompleted(PartialModel partial, GeometryBakedModel bakedModel) {
        partial.bakedModel = bakedModel;
    }

    public static void onBakingCompleted(Map<Identifier, GeometryBakedModel> models) {
        PartialModel.populateOnInit = true;
    }
}
