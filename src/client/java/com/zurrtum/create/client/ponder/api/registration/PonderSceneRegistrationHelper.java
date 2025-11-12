package com.zurrtum.create.client.ponder.api.registration;

import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;

import java.util.function.Function;

import net.minecraft.resources.Identifier;

public interface PonderSceneRegistrationHelper<T> {

    <S> PonderSceneRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen);

    StoryBoardEntry addStoryBoard(T component, Identifier schematicLocation, PonderStoryBoard storyBoard, Identifier... tags);

    StoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard, Identifier... tags);

    MultiSceneBuilder forComponents(T... components);

    MultiSceneBuilder forComponents(Iterable<? extends T> components);

    Identifier asLocation(String path);
}