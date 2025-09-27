package com.zurrtum.create.client.ponder.api.scene;

@FunctionalInterface
public interface PonderStoryBoard {
    void program(SceneBuilder scene, SceneBuildingUtil util);
}