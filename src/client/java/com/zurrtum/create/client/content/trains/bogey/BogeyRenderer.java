package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

public interface BogeyRenderer {
    BogeyRenderState getRenderData(@Nullable NbtCompound bogeyData, float wheelAngle, float tickProgress, int light, boolean inContraption);
}
