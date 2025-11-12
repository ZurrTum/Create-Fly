package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.MaterialShaders;
import net.minecraft.resources.ResourceLocation;

public record SimpleMaterialShaders(ResourceLocation vertexSource, ResourceLocation fragmentSource) implements MaterialShaders {
}
