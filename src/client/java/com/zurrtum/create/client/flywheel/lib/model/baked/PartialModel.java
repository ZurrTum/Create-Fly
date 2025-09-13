package com.zurrtum.create.client.flywheel.lib.model.baked;

import com.google.common.collect.MapMaker;
import net.minecraft.client.render.model.GeometryBakedModel;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.UnknownNullability;

import java.util.concurrent.ConcurrentMap;

/**
 * A helper class for loading and accessing JSON models not directly used by any blocks or items.
 * <br>
 * Creating a PartialModel will make Minecraft automatically load the associated modelLocation.
 * <br>
 * Once Minecraft has finished baking all models, all PartialModels will have their bakedModel fields populated.
 */
public final class PartialModel {
    static final ConcurrentMap<Identifier, PartialModel> ALL = new MapMaker().weakValues().makeMap();
    static boolean populateOnInit = false;

    private final Identifier modelLocation;
    @UnknownNullability
    GeometryBakedModel bakedModel;

    private PartialModel(Identifier modelLocation) {
        this.modelLocation = modelLocation;
        if (populateOnInit) {
            throw new RuntimeException("Loading new models after resolve models is not supported");
        }
    }

    public static PartialModel of(Identifier modelLocation) {
        return ALL.computeIfAbsent(modelLocation, PartialModel::new);
    }

    @UnknownNullability
    public GeometryBakedModel get() {
        return bakedModel;
    }

    public Identifier modelLocation() {
        return modelLocation;
    }
}
