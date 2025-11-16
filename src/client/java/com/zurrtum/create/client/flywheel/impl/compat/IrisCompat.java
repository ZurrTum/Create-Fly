package com.zurrtum.create.client.flywheel.impl.compat;

import com.zurrtum.create.client.flywheel.impl.FlwImpl;
//import net.irisshaders.iris.api.v0.IrisApi;

public final class IrisCompat {
    public static final boolean ACTIVE = CompatMod.IRIS.isLoaded;

    static {
        if (ACTIVE) {
            FlwImpl.LOGGER.debug("Detected Iris");
        }
    }

    private IrisCompat() {
    }

    public static boolean isShaderPackInUse() {
        if (!ACTIVE) {
            return false;
        }

        return Internals.isShaderPackInUse();
    }

    public static boolean isRenderingShadowPass() {
        if (!ACTIVE) {
            return false;
        }

        return Internals.isRenderingShadowPass();
    }

    private static final class Internals {
        static boolean isShaderPackInUse() {
            //            return IrisApi.getInstance().isShaderPackInUse();
            return false;
        }

        static boolean isRenderingShadowPass() {
            //            return IrisApi.getInstance().isRenderingShadowPass();
            return false;
        }
    }
}