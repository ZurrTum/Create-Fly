package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.client.ponder.api.registration.MultiSceneBuilder;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry;
import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;

import java.util.Arrays;
import java.util.function.Function;

import net.minecraft.resources.Identifier;

public class GenericPonderSceneRegistrationHelper<T> implements PonderSceneRegistrationHelper<T> {

    private final PonderSceneRegistrationHelper<Identifier> helperDelegate;
    private final Function<T, Identifier> keyGen;

    public GenericPonderSceneRegistrationHelper(PonderSceneRegistrationHelper<Identifier> helperDelegate, Function<T, Identifier> keyGen) {
        this.helperDelegate = helperDelegate;
        this.keyGen = keyGen;
    }

    @Override
    public <S> PonderSceneRegistrationHelper<S> withKeyFunction(Function<S, T> keyGen) {
        return new GenericPonderSceneRegistrationHelper<>(helperDelegate, keyGen.andThen(this.keyGen));
    }

    public StoryBoardEntry addStoryBoard(T component, Identifier schematicLocation, PonderStoryBoard storyBoard, Identifier... tags) {
        return helperDelegate.addStoryBoard(keyGen.apply(component), schematicLocation, storyBoard, tags);
    }

    public StoryBoardEntry addStoryBoard(T component, String schematicPath, PonderStoryBoard storyBoard, Identifier... tags) {
        return helperDelegate.addStoryBoard(keyGen.apply(component), schematicPath, storyBoard, tags);
    }

    @Override
    public MultiSceneBuilder forComponents(Iterable<? extends T> components) {
        return new GenericMultiSceneBuilder<>(this, components);
    }

    @Override
    @SafeVarargs
    public final MultiSceneBuilder forComponents(T... components) {
        return new GenericMultiSceneBuilder<>(this, Arrays.asList(components));
    }

    @Override
    public Identifier asLocation(String path) {
        return helperDelegate.asLocation(path);
    }
}