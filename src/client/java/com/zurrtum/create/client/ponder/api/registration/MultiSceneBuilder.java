package com.zurrtum.create.client.ponder.api.registration;

import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;

import java.util.function.Consumer;

import net.minecraft.resources.Identifier;

public interface MultiSceneBuilder {
    MultiSceneBuilder addStoryBoard(Identifier schematicLocation, PonderStoryBoard storyBoard);

    MultiSceneBuilder addStoryBoard(Identifier schematicLocation, PonderStoryBoard storyBoard, Identifier... tags);

    MultiSceneBuilder addStoryBoard(Identifier schematicLocation, PonderStoryBoard storyBoard, Consumer<StoryBoardEntry> extras);

    MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard);

    MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, Identifier... tags);

    MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, Consumer<StoryBoardEntry> extras);
}