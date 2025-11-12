package com.zurrtum.create.client.ponder.foundation;

import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry;
import com.zurrtum.create.client.ponder.api.scene.PonderStoryBoard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.resources.Identifier;

public class PonderStoryBoardEntry implements StoryBoardEntry {

    private final PonderStoryBoard board;
    private final String namespace;
    private final Identifier schematicLocation;
    private final Identifier component;
    private final List<Identifier> tags;
    private final List<SceneOrderingEntry> orderingEntries;

    public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, Identifier schematicLocation, Identifier component) {
        this.board = board;
        this.namespace = namespace;
        this.schematicLocation = schematicLocation;
        this.component = component;
        this.tags = new ArrayList<>();
        this.orderingEntries = new ArrayList<>();
    }

    public PonderStoryBoardEntry(PonderStoryBoard board, String namespace, String schematicPath, Identifier component) {
        this(board, namespace, Identifier.fromNamespaceAndPath(namespace, schematicPath), component);
    }

    @Override
    public PonderStoryBoard getBoard() {
        return board;
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public Identifier getSchematicLocation() {
        return schematicLocation;
    }

    @Override
    public Identifier getComponent() {
        return component;
    }

    @Override
    public List<Identifier> getTags() {
        return tags;
    }

    @Override
    public List<SceneOrderingEntry> getOrderingEntries() {
        return orderingEntries;
    }

    // Builder start

    @Override
    public StoryBoardEntry orderBefore(String namespace, String otherSceneId) {
        this.orderingEntries.add(SceneOrderingEntry.before(namespace, otherSceneId));
        return this;
    }

    @Override
    public StoryBoardEntry orderAfter(String namespace, String otherSceneId) {
        this.orderingEntries.add(SceneOrderingEntry.after(namespace, otherSceneId));
        return this;
    }

    @Override
    public StoryBoardEntry highlightTag(Identifier tag) {
        tags.add(tag);
        return this;
    }

    @Override
    public StoryBoardEntry highlightTags(Identifier... tags) {
        Collections.addAll(this.tags, tags);
        return this;
    }

    @Override
    public StoryBoardEntry highlightAllTags() {
        tags.add(PonderTag.Highlight.ALL);
        return this;
    }

}