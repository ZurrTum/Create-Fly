package com.zurrtum.create.client.ponder.foundation;

import com.google.common.base.Stopwatch;
import com.zurrtum.create.client.ponder.api.registration.LangRegistryAccess;
import com.zurrtum.create.client.ponder.api.registration.PonderPlugin;
import com.zurrtum.create.client.ponder.api.registration.SceneRegistryAccess;
import com.zurrtum.create.client.ponder.api.registration.TagRegistryAccess;
import com.zurrtum.create.client.ponder.enums.PonderConfig;
import com.zurrtum.create.client.ponder.foundation.registration.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class PonderIndex {

    private static final PonderLocalization LOCALIZATION = new PonderLocalization();
    private static final PonderSceneRegistry SCENES = new PonderSceneRegistry(LOCALIZATION);
    private static final PonderTagRegistry TAGS = new PonderTagRegistry(LOCALIZATION);

    private static final Set<PonderPlugin> plugins = new TreeSet<>(Comparator.comparing((PonderPlugin plugin) -> !plugin.getModId().equals("create"))
        .thenComparing(PonderPlugin::getModId));

    private static final Logger LOGGER = LogManager.getLogger("PonderIndex");

    public static void addPlugin(PonderPlugin plugin) {
        plugins.add(plugin);
    }

    public static void forEachPlugin(Consumer<PonderPlugin> action) {
        plugins.forEach(action);
    }

    public static Stream<PonderPlugin> streamPlugins() {
        return plugins.stream();
    }

    public static void reload() {
        LOGGER.info("Reloading all Ponder Plugins ...");
        Stopwatch stopwatch = Stopwatch.createStarted();
        LOCALIZATION.clearShared();
        SCENES.clearRegistry();
        TAGS.clearRegistry();

        registerAll();
        gatherSharedText();
        LOGGER.info("Reloading Ponder Plugins took {}", stopwatch.stop());
    }

    public static void registerAll() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        forEachPlugin(plugin -> plugin.registerScenes(new DefaultPonderSceneRegistrationHelper(plugin.getModId(), SCENES)));
        LOGGER.info("Registering Ponder Scenes took {}", stopwatch.stop());

        stopwatch.reset().start();
        forEachPlugin(plugin -> plugin.registerTags(new DefaultPonderTagRegistrationHelper(plugin.getModId(), TAGS, LOCALIZATION)));
        LOGGER.info("Registering Ponder Tags took {}", stopwatch.stop());
    }

    public static void gatherSharedText() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        forEachPlugin(plugin -> plugin.registerSharedText(new DefaultSharedTextRegistrationHelper(plugin.getModId(), LOCALIZATION)));
        LOGGER.info("Collecting Shared Ponder Text took {}", stopwatch.stop());
    }

    public static SceneRegistryAccess getSceneAccess() {
        return SCENES;
    }

    public static TagRegistryAccess getTagAccess() {
        return TAGS;
    }

    public static LangRegistryAccess getLangAccess() {
        return LOCALIZATION;
    }

    public static boolean editingModeActive() {
        return PonderConfig.client().editingMode.get();
    }
}