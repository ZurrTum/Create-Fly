package com.zurrtum.create.client.infrastructure.config;

import com.zurrtum.create.catnip.config.Builder;

import static com.zurrtum.create.Create.MOD_ID;

public class AllConfigs {
    private static CClient client;

    public static CClient client() {
        return client;
    }

    public static void register() {
        client = Builder.create(CClient::new, MOD_ID, "client");
    }
}
