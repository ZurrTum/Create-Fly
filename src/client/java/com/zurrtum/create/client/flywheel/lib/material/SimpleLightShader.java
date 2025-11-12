package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.LightShader;
import net.minecraft.resources.Identifier;

public record SimpleLightShader(@Override Identifier source) implements LightShader {
}
