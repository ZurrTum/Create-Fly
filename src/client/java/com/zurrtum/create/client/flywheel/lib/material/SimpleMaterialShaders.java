package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.MaterialShaders;
import net.minecraft.util.Identifier;

public record SimpleMaterialShaders(Identifier vertexSource, Identifier fragmentSource) implements MaterialShaders {
}
