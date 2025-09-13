package com.zurrtum.create.client.vanillin;

public interface VanillinXplat {
    VanillinXplat INSTANCE = new VanillinXplatImpl();

    boolean isDevelopmentEnvironment();

    boolean isModLoaded(String modId);
}
