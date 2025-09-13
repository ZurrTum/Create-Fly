package com.zurrtum.create.client.flywheel.impl.visualization.storage;

import com.zurrtum.create.client.flywheel.api.visual.SectionTrackedVisual;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.longs.LongSets;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;

public class SectionTracker implements SectionTrackedVisual.SectionCollector {
    private final List<Runnable> listeners = new ArrayList<>(2);

    @Unmodifiable
    private LongSet sections = LongSet.of();

    @Unmodifiable
    public LongSet sections() {
        return sections;
    }

    @Override
    public void sections(LongSet sections) {
        this.sections = LongSets.unmodifiable(new LongArraySet(sections));
        listeners.forEach(Runnable::run);
    }

    public void addListener(Runnable listener) {
        listeners.add(listener);
    }
}
