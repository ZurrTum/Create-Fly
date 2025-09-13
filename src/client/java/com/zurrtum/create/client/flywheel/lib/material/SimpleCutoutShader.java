package com.zurrtum.create.client.flywheel.lib.material;

import com.zurrtum.create.client.flywheel.api.material.CutoutShader;
import net.minecraft.util.Identifier;

public record SimpleCutoutShader(@Override Identifier source) implements CutoutShader {
}
