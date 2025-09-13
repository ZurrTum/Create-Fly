package com.zurrtum.create.client.flywheel.lib.util;

import com.zurrtum.create.client.flywheel.lib.internal.FlwLibLink;

public final class ShadersModHelper {
    public static final boolean IS_IRIS_LOADED = FlwLibLink.INSTANCE.isIrisLoaded();
    public static final boolean IS_OPTIFINE_INSTALLED = FlwLibLink.INSTANCE.isOptifineInstalled();

    private ShadersModHelper() {
    }

    public static boolean isShaderPackInUse() {
        return FlwLibLink.INSTANCE.isShaderPackInUse();
    }

    public static boolean isRenderingShadowPass() {
        return FlwLibLink.INSTANCE.isRenderingShadowPass();
    }
}
