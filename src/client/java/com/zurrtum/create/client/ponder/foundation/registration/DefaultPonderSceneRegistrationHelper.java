package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.client.ponder.api.registration.MultiSceneBuilder;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry;
import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;
import com.zurrtum.create.client.ponder.foundation.PonderStoryBoardEntry;

import java.util.Arrays;
import java.util.function.Function;

import net.minecraft.resources.Identifier;

public class DefaultPonderSceneRegistrationHelper implements PonderSceneRegistrationHelper<Identifier> {

    protected String namespace;
    protected PonderSceneRegistry sceneRegistry;

    public DefaultPonderSceneRegistrationHelper(String namespace, PonderSceneRegistry sceneRegistry) {
        this.namespace = namespace;
        this.sceneRegistry = sceneRegistry;
    }

    @Override
    public <T> GenericPonderSceneRegistrationHelper<T> withKeyFunction(Function<T, Identifier> keyGen) {
        return new GenericPonderSceneRegistrationHelper<>(this, keyGen);
    }

    @Override
    public StoryBoardEntry addStoryBoard(Identifier component, Identifier schematicLocation, PonderStoryBoard storyBoard, Identifier... tags) {
        StoryBoardEntry entry = this.createStoryBoardEntry(storyBoard, schematicLocation, component);
        entry.highlightTags(tags);
        sceneRegistry.addStoryBoard(entry);
        return entry;
    }

    @Override
    public StoryBoardEntry addStoryBoard(Identifier component, String schematicPath, PonderStoryBoard storyBoard, Identifier... tags) {
        return addStoryBoard(component, asLocation(schematicPath), storyBoard, tags);
    }

    @Override
    public MultiSceneBuilder forComponents(Identifier... components) {
        return new GenericMultiSceneBuilder<>(this, Arrays.asList(components));
    }

    @Override
    public MultiSceneBuilder forComponents(Iterable<? extends Identifier> components) {
        return new GenericMultiSceneBuilder<>(this, components);
    }

    @Override
    public Identifier asLocation(String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }

    private PonderStoryBoardEntry createStoryBoardEntry(PonderStoryBoard storyBoard, Identifier schematicLocation, Identifier component) {
        return new PonderStoryBoardEntry(storyBoard, namespace, schematicLocation, component);
    }

}