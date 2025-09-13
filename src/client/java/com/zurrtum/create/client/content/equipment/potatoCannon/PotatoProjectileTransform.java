package com.zurrtum.create.client.content.equipment.potatoCannon;

import com.zurrtum.create.api.equipment.potatoCannon.PotatoProjectileRenderMode;
import com.zurrtum.create.client.content.equipment.potatoCannon.PotatoProjectileRenderer.PotatoProjectileState;
import net.minecraft.client.util.math.MatrixStack;

@FunctionalInterface
public interface PotatoProjectileTransform<T extends PotatoProjectileRenderMode> {
    void transform(T mode, MatrixStack ms, PotatoProjectileState state);
}
