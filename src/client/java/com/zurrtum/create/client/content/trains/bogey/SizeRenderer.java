package com.zurrtum.create.client.content.trains.bogey;

import java.util.function.Supplier;

public record SizeRenderer(BogeyRenderer renderer, BogeyVisualizer visualizer) {
    public SizeRenderer(Supplier<BogeyRenderer> renderer, BogeyVisualizer visualizer) {
        this(renderer.get(), visualizer);
    }

    public static SizeRenderer small() {
        return new SizeRenderer(StandardBogeyRenderer.Small::new, StandardBogeyVisual.Small::new);
    }

    public static SizeRenderer large() {
        return new SizeRenderer(StandardBogeyRenderer.Large::new, StandardBogeyVisual.Large::new);
    }
}
