package com.zurrtum.create.client;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.content.trains.track.StandardTrackBlockRenderer;
import com.zurrtum.create.client.content.trains.track.TrackBlockRenderer;
import com.zurrtum.create.content.trains.track.ITrackBlock;
import org.jspecify.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class AllTrackRenders {
    public static final Map<ITrackBlock, TrackBlockRenderer> ALL = new IdentityHashMap<>();

    @Nullable
    public static TrackBlockRenderer get(ITrackBlock block) {
        return ALL.get(block);
    }

    public static void register(ITrackBlock block, Supplier<TrackBlockRenderer> factory) {
        ALL.put(block, factory.get());
    }

    public static void register() {
        register(AllBlocks.TRACK, StandardTrackBlockRenderer::new);
    }
}
