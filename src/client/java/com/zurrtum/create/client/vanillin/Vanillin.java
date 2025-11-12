package com.zurrtum.create.client.vanillin;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Vanillin {
    public static final String MOD_ID = "vanillin";

    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final Logger CONFIG_LOGGER = LoggerFactory.getLogger(MOD_ID + "/config");

    public static Identifier rl(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public void onInitializeClient() {
        VanillaVisuals.init();
        VanillinConfig.register();
        VanillinConfig.apply(VanillaVisuals.CONFIGURATOR);
    }
}
