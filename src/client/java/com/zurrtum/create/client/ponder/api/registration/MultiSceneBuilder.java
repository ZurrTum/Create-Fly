package com.zurrtum.create.client.ponder.api.registration;

import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

public interface MultiSceneBuilder {
    MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard);

    MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard, ResourceLocation... tags);

    MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard, Consumer<StoryBoardEntry> extras);

    MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard);

    MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, ResourceLocation... tags);

    MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, Consumer<StoryBoardEntry> extras);
}