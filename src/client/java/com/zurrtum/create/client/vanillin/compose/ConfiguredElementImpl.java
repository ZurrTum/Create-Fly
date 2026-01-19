package com.zurrtum.create.client.vanillin.compose;

import com.zurrtum.create.client.flywheel.api.visual.Visual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class ConfiguredElementImpl<T, C> implements ConfiguredElement<T> {
    private final VisualElement<T, C> element;
    @Nullable
    private final C config;
    private final VisualizationPredicate<T> predicate;

    public ConfiguredElementImpl(VisualElement<T, C> element, @Nullable C config, VisualizationPredicate<T> predicate) {
        this.element = element;
        this.config = config;
        this.predicate = predicate;
    }

    @Override
    public Visual create(VisualizationContext ctx, T entity, float partialTick) {
        return element.create(ctx, entity, partialTick, config);
    }

    @Override
    public boolean shouldVisualize(VisualizationContext ctx, T entity) {
        return predicate.shouldVisualize(ctx, entity);
    }

    public static class ConfiguredElementBuilder<T, C> {
        private final VisualElement<T, C> element;

        @Nullable
        private C config;

        @Nullable
        private VisualizationPredicate<T> predicate;

        public ConfiguredElementBuilder(VisualElement<T, C> element) {
            this.element = element;
        }

        public ConfiguredElementBuilder<T, C> configure(@Nullable C config) {
            this.config = config;
            return this;
        }

        public ConfiguredElementBuilder<T, C> predicate(VisualizationPredicate<T> predicate) {
            this.predicate = predicate;
            return this;
        }

        public ConfiguredElement<T> build() {
            if (predicate == null) {
                predicate = VisualizationPredicate.alwaysTrue();
            }

            if (!(element instanceof VisualElement.Unit)) {
                Objects.requireNonNull(config, "Visual element requires a config but none was provided.");
            }

            return new ConfiguredElementImpl<>(element, config, predicate);
        }
    }
}
