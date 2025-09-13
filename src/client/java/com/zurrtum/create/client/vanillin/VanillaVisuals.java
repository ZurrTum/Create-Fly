package com.zurrtum.create.client.vanillin;

import com.zurrtum.create.client.vanillin.compose.*;
import com.zurrtum.create.client.vanillin.config.BlockEntityVisualizerBuilder;
import com.zurrtum.create.client.vanillin.config.Configurator;
import com.zurrtum.create.client.vanillin.config.EntityVisualizerBuilder;
import com.zurrtum.create.client.vanillin.elements.ShadowElement;
import com.zurrtum.create.client.vanillin.visuals.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class VanillaVisuals {
    public static final Configurator CONFIGURATOR = new Configurator();

    // Stable visuals are enabled by default always.
    public static final boolean STABLE = true;
    // Experimental visuals are enabled by default in dev.
    public static final boolean EXPERIMENTAL = VanillinXplat.INSTANCE.isDevelopmentEnvironment();

    public static void init() {
        builder(BlockEntityType.CHEST).factory(ChestVisual::new).apply(STABLE);
        builder(BlockEntityType.ENDER_CHEST).factory(ChestVisual::new).apply(STABLE);
        builder(BlockEntityType.TRAPPED_CHEST).factory(ChestVisual::new).apply(STABLE);

        builder(BlockEntityType.BELL).factory(BellVisual::new).apply(STABLE);

        builder(BlockEntityType.SHULKER_BOX).factory(ShulkerBoxVisual::new).apply(STABLE);

        builder(EntityType.BLOCK_DISPLAY).factory(BlockDisplayVisual::new).apply(STABLE);

        composable(EntityType.ITEM_DISPLAY).with(element(VisualElements.ITEM_DISPLAY).build())
            .shouldVisualize((ctx, e) -> ItemDisplayVisual.shouldVisualize(e)).build().skipVanillaRender(ItemDisplayVisual::shouldVisualize)
            .apply(EXPERIMENTAL);

        minecart(EntityType.CHEST_MINECART, EntityModelLayers.CHEST_MINECART).apply(STABLE);
        minecart(EntityType.COMMAND_BLOCK_MINECART, EntityModelLayers.COMMAND_BLOCK_MINECART).apply(STABLE);
        minecart(EntityType.FURNACE_MINECART, EntityModelLayers.FURNACE_MINECART).apply(STABLE);
        minecart(EntityType.HOPPER_MINECART, EntityModelLayers.HOPPER_MINECART).apply(STABLE);
        minecart(EntityType.MINECART, EntityModelLayers.MINECART).apply(STABLE);
        minecart(EntityType.SPAWNER_MINECART, EntityModelLayers.SPAWNER_MINECART).apply(STABLE);

        composable(EntityType.TNT_MINECART).apply(VanillaVisuals::commonElements)
            .with(element(VisualElements.SHADOW).configure(new ShadowElement.Config(0.7f, ShadowElement.Config.DEFAULT_STRENGTH)).build())
            .with(element(VisualElements.FIRE).build()).with(element(VisualElements.TNT_MINECART).build()).build()
            .skipVanillaRender(MinecartVisual::shouldSkipRender).apply(STABLE);

        itemFrame(EntityType.ITEM_FRAME).apply(EXPERIMENTAL);
        itemFrame(EntityType.GLOW_ITEM_FRAME).apply(EXPERIMENTAL);

        composable(EntityType.ITEM).apply(VanillaVisuals::commonElements).with(element(VisualElements.FIRE).build())
            .with(element(VisualElements.SHADOW).configure(new ShadowElement.Config(0.15f, 0.75f)).build())
            .with(element(VisualElements.ITEM_ENTITY).build()).shouldVisualize(((ctx, entity) -> ItemVisual.isSupported(entity))).build()
            .skipVanillaRender(ItemVisual::isSupported).apply(EXPERIMENTAL);

    }

    public static <T extends Entity> void commonElements(EntityBuilder<T> builder) {
        builder.with(element(VisualElements.HITBOX).configure(false).build());
    }

    public static <T extends ItemFrameEntity> EntityVisualizerBuilder<T> itemFrame(EntityType<T> type) {
        return composable(type).apply(VanillaVisuals::commonElements).with(element(VisualElements.ITEM_FRAME).build())
            .shouldVisualize((ctx, entity) -> ItemFrameVisual.shouldVisualize(entity)).build().skipVanillaRender(ItemFrameVisual::shouldVisualize);
    }

    public static <T extends AbstractMinecartEntity> EntityVisualizerBuilder<T> minecart(EntityType<T> type, EntityModelLayer variant) {
        return composable(type).apply(VanillaVisuals::commonElements)
            .with(element(VisualElements.SHADOW).configure(new ShadowElement.Config(0.7f, ShadowElement.Config.DEFAULT_STRENGTH)).build())
            .with(element(VisualElements.FIRE).build()).with(element(VisualElements.MINECART).configure(variant).build()).build()
            .skipVanillaRender(MinecartVisual::shouldSkipRender);
    }

    public static <T extends Entity> EntityBuilder<T> composable(EntityType<T> entityType) {
        return new EntityBuilder<>(entityType);
    }

    public static <T, C> ConfiguredElementImpl.ConfiguredElementBuilder<T, C> element(VisualElement<T, C> element) {
        return new ConfiguredElementImpl.ConfiguredElementBuilder<>(element);
    }

    public static class EntityBuilder<T extends Entity> {
        private final List<ConfiguredElement<? super T>> elements = new ArrayList<>();

        private final EntityType<T> entityType;
        @Nullable
        private VisualizationPredicate<T> predicate;

        public EntityBuilder(EntityType<T> entityType) {
            this.entityType = entityType;
        }

        /**
         * Set a predicate to control whether <em>all</em> elements are visualized.
         * <p>This is useful when you can't guarantee than an entity will support visualization for its entire lifetime.
         *
         * @param predicate A visualization predicate, returning {@code true} to indicate the entity should be visualized.
         */
        public EntityBuilder<T> shouldVisualize(VisualizationPredicate<T> predicate) {
            this.predicate = predicate;
            return this;
        }

        /**
         * Add a configured visual element to this visualizer.
         *
         * @param element The configured visual element.
         */
        public EntityBuilder<T> with(ConfiguredElement<? super T> element) {
            elements.add(element);
            return this;
        }

        public EntityBuilder<T> apply(Consumer<EntityBuilder<T>> mutate) {
            mutate.accept(this);
            return this;
        }

        public EntityVisualizerBuilder<T> build() {
            var elementsArray = elements.toArray(new ConfiguredElement[0]);

            if (predicate == null) {
                predicate = VisualizationPredicate.alwaysTrue();
            }

            var controller = new ComposableEntityVisual.Controller<T>(elementsArray, predicate);

            return builder(entityType).factory((ctx, entity, partialTick) -> new ComposableEntityVisual<T>(ctx, entity, partialTick, controller));
        }
    }

    public static <T extends BlockEntity> BlockEntityVisualizerBuilder<T> builder(BlockEntityType<T> type) {
        return new BlockEntityVisualizerBuilder<>(CONFIGURATOR, type);
    }

    public static <T extends Entity> EntityVisualizerBuilder<T> builder(EntityType<T> type) {
        return new EntityVisualizerBuilder<>(CONFIGURATOR, type);
    }
}
