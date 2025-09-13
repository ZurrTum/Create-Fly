package com.zurrtum.create.client.flywheel.impl.config;

import com.zurrtum.create.catnip.config.ConfigBase;

public class CClient extends ConfigBase {
    public final ConfigString backend = s("DEFAULT", "backend", Comments.backend, Comments.backend_allowed);
    public final ConfigBool limitUpdates = b(true, "limitUpdates", Comments.limitUpdates);
    public final ConfigInt workerThreads = i(-1, -1, Runtime.getRuntime().availableProcessors(), "workerThreads", Comments.workerThreads);

    public final CBackendConfig flwBackends = nested(0, CBackendConfig::new, Comments.flwBackends);

    @Override
    public String getName() {
        return "client";
    }

    private static class Comments {
        public static final String limitUpdates = "Enable or disable instance update limiting with distance.";
        public static final String backend = "Select the backend to use. Set to 'DEFAULT' to let Flywheel decide.";
        public static final String backend_allowed = "Allowed Values: DEFAULT, OFF, namespace:path";
        public static final String workerThreads = "The number of worker threads to use. Set to -1 to let Flywheel decide. Set to 0 to disable parallelism. Requires a game restart to take effect.";
        public static final String flwBackends = "Config options for Flywheel's built-in backends.";
    }
}
