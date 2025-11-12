package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.CutoutShader;
import net.minecraft.resources.ResourceLocation;

public record SimpleCutoutShader(@Override ResourceLocation source) implements CutoutShader {
}
