package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.client.ponder.api.registration.MultiSceneBuilder;
import com.zurrtum.create.client.ponder.api.registration.PonderSceneRegistrationHelper;
import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry;
import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;
import java.util.function.Consumer;
import net.minecraft.resources.ResourceLocation;

public class GenericMultiSceneBuilder<T> implements MultiSceneBuilder {

    protected Iterable<? extends T> components;
    protected PonderSceneRegistrationHelper<T> helper;

    protected GenericMultiSceneBuilder(PonderSceneRegistrationHelper<T> helper, Iterable<? extends T> components) {
        this.helper = helper;
        this.components = components;
    }

    @Override
    public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard) {
        return addStoryBoard(
            schematicLocation, storyBoard, $ -> {
            }
        );
    }

    @Override
    public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard, ResourceLocation... tags) {
        return addStoryBoard(schematicLocation, storyBoard, sb -> sb.highlightTags(tags));
    }

    @Override
    public MultiSceneBuilder addStoryBoard(ResourceLocation schematicLocation, PonderStoryBoard storyBoard, Consumer<StoryBoardEntry> extras) {
        components.forEach(c -> extras.accept(helper.addStoryBoard(c, schematicLocation, storyBoard)));
        return this;
    }

    @Override
    public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard) {
        return addStoryBoard(helper.asLocation(schematicPath), storyBoard);
    }

    @Override
    public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, ResourceLocation... tags) {
        return addStoryBoard(helper.asLocation(schematicPath), storyBoard, tags);
    }

    @Override
    public MultiSceneBuilder addStoryBoard(String schematicPath, PonderStoryBoard storyBoard, Consumer<StoryBoardEntry> extras) {
        return addStoryBoard(helper.asLocation(schematicPath), storyBoard, extras);
    }

}