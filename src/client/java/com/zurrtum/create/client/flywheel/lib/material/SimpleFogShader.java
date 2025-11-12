package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.FogShader;
import net.minecraft.resources.Identifier;

public record SimpleFogShader(@Override Identifier source) implements FogShader {
}
