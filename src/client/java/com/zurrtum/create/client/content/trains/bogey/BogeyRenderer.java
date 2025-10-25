package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyBlockEntityRenderState;
import com.zurrtum.create.client.content.trains.bogey.BogeyBlockEntityRenderer.BogeyRenderState;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import net.minecraft.util.math.Vec3d;

public interface BogeyRenderer {
    BogeyRenderState getRenderData(
        AbstractBogeyBlockEntity be,
        BogeyBlockEntityRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        boolean inContraption
    );
}
