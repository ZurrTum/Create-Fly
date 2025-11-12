package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.FogShader;
import net.minecraft.resources.ResourceLocation;

public record SimpleFogShader(@Override ResourceLocation source) implements FogShader {
}
