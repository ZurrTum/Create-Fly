package com.zurrtum.create.client.flywheel.lib.model.baked;

import java.util.Map;

import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.resources.Identifier;

public final class PartialModelEventHandler {
    private PartialModelEventHandler() {
    }

    public static Map<Identifier, PartialModel> getRegisterAdditional() {
        return PartialModel.ALL;
    }

    public static void onBakingCompleted(PartialModel partial, SimpleModelWrapper bakedModel) {
        partial.bakedModel = bakedModel;
    }

    public static void onBakingCompleted(Map<Identifier, SimpleModelWrapper> models) {
        PartialModel.populateOnInit = true;
    }
}
