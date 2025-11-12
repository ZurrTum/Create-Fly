package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.MaterialShaders;
import net.minecraft.resources.Identifier;

public record SimpleMaterialShaders(Identifier vertexSource, Identifier fragmentSource) implements MaterialShaders {
}
