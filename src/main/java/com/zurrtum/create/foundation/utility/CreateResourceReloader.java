package com.zurrtum.create.foundation.utility;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;

import static com.zurrtum.create.Create.MOD_ID;

public abstract class CreateResourceReloader implements ResourceManagerReloadListener {
    private final Identifier id;

    public CreateResourceReloader(String id) {
        this.id = Identifier.fromNamespaceAndPath(MOD_ID, id);
    }

    public CreateResourceReloader(Identifier id) {
        this.id = id;
    }

    public Identifier getId() {
        return id;
    }
}
