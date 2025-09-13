package com.zurrtum.create.client.flywheel.api.backend;

import com.zurrtum.create.client.flywheel.api.internal.FlwApiLink;

public final class BackendManager {
    private BackendManager() {
    }

    /**
     * Get the current backend.
     */
    public static Backend currentBackend() {
        return FlwApiLink.INSTANCE.getCurrentBackend();
    }

    public static boolean isBackendOn() {
        return FlwApiLink.INSTANCE.isBackendOn();
    }

    public static Backend offBackend() {
        return FlwApiLink.INSTANCE.getOffBackend();
    }

    public static Backend defaultBackend() {
        return FlwApiLink.INSTANCE.getDefaultBackend();
    }
}
