package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.FogShader;
import net.minecraft.util.Identifier;

public record SimpleFogShader(@Override Identifier source) implements FogShader {
}
