package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public interface BogeyRenderer {
    BogeyRenderState getRenderData(@Nullable CompoundTag bogeyData, float wheelAngle, float tickProgress, int light, boolean inContraption);
}
