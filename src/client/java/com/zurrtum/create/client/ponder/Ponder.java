package com.zurrtum.create.client.ponder;

import com.zurrtum.create.client.catnip.lang.LangBuilder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBufferCache;
import com.zurrtum.create.client.ponder.enums.PonderConfig;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Ponder {
    public static final String MOD_ID = "ponder";
    public static final String MOD_NAME = "Ponder";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static LangBuilder lang() {
        return new LangBuilder(MOD_ID);
    }

    public static Identifier asResource(String path) {
        return Identifier.of(MOD_ID, path);
    }

    public void onInitializeClient() {
        PonderConfig.register();
        SuperByteBufferCache.getInstance().registerCompartment(CachedBuffers.GENERIC_BLOCK);
    }
}
