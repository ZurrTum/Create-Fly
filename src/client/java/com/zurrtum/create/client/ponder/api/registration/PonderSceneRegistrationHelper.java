package com.zurrtum.create.client.ponder.api.registration;

import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public interface PonderSceneRegistrationHelper<T> {

    <S> PonderSceneRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen);

    StoryBoardEntry addStoryBoard(T component, ResourceLocation schematicLocation, PonderStoryBoard storyBoard, ResourceLocation... tags);

    StoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard, ResourceLocation... tags);

    MultiSceneBuilder forComponents(T... components);

    MultiSceneBuilder forComponents(Iterable<? extends T> components);

    ResourceLocation asLocation(String path);
}