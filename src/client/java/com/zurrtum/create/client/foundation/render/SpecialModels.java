package com.zurrtum.create.client.foundation.render;

import com.zurrtum.create.client.flywheel.api.material.CardinalLightingMode;
import com.zurrtum.create.client.flywheel.api.material.LightShader;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.material.LightShaders;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedModelBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;

public class SpecialModels {
    private static final RendererReloadCache<Key, Model> FLAT = new RendererReloadCache<>(it -> new BakedModelBuilder(it.partial.get()).materialFunc((renderType, shaded) -> {
        var material = ModelUtil.getMaterial(renderType, shaded);
        if (material == null) {
            return null;
        }
        return SimpleMaterial.builderOf(material).light(it.light).cardinalLightingMode(shaded ? it.cardinalLightingMode : CardinalLightingMode.OFF)
            .build();
    }).build());

    public static Model flatLit(PartialModel partial) {
        return FLAT.get(new Key(partial, LightShaders.FLAT, CardinalLightingMode.ENTITY));
    }

    public static Model smoothLit(PartialModel partial) {
        return FLAT.get(new Key(partial, LightShaders.SMOOTH, CardinalLightingMode.ENTITY));
    }

    public static Model flatChunk(PartialModel partial) {
        return FLAT.get(new Key(partial, LightShaders.FLAT, CardinalLightingMode.CHUNK));
    }

    public static Model chunkDiffuse(PartialModel partial) {
        return FLAT.get(new Key(partial, LightShaders.SMOOTH_WHEN_EMBEDDED, CardinalLightingMode.CHUNK));
    }

    private record Key(PartialModel partial, LightShader light, CardinalLightingMode cardinalLightingMode) {
    }
}
