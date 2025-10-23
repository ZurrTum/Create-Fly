package com.zurrtum.create.client;

import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.content.contraptions.render.ContraptionEntityRenderer;
import com.zurrtum.create.client.content.kinetics.base.KineticBlockEntityRenderer;
import com.zurrtum.create.client.content.kinetics.waterwheel.WaterWheelRenderer;

public class AllBufferCaches {
    public static void register(SuperByteBufferCache bc) {
        bc.registerCompartment(CachedBuffers.PARTIAL);
        bc.registerCompartment(CachedBuffers.DIRECTIONAL_PARTIAL);
        bc.registerCompartment(KineticBlockEntityRenderer.KINETIC_BLOCK);
        bc.registerCompartment(WaterWheelRenderer.WATER_WHEEL);
        bc.registerCompartment(ContraptionEntityRenderer.CONTRAPTION, 20);
    }
}
