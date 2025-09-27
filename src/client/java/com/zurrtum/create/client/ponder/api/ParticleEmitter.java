package com.zurrtum.create.client.ponder.api;

import com.zurrtum.create.client.ponder.api.level.PonderLevel;

@FunctionalInterface
public interface ParticleEmitter {
    void create(PonderLevel world, double x, double y, double z);
}