package com.zurrtum.create.client.ponder.enums;

import com.zurrtum.create.catnip.config.Builder;
import com.zurrtum.create.client.ponder.config.CClient;

import static com.zurrtum.create.client.ponder.Ponder.MOD_ID;

public class PonderConfig {
    private static CClient client;

    public static void register() {
        client = Builder.create(CClient::new, MOD_ID, "client", true);
    }

    //TODO
    //    public static void onLoad(ModConfig config) {
    //        for (ConfigBase configBase : CONFIGS.values())
    //            if (configBase.specification == config.getSpec())
    //                configBase.onLoad();
    //    }
    //
    //    public static void onReload(ModConfig config) {
    //        for (ConfigBase configBase : CONFIGS.values())
    //            if (configBase.specification == config.getSpec())
    //                configBase.onReload();
    //    }

    public static CClient client() {
        return client;
    }

}
