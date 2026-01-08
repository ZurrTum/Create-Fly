package com.zurrtum.create.api.registry;

import com.zurrtum.create.catnip.data.Pair;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public interface CreateRegisterPlugin {
    List<CreateRegisterPlugin> PLUGINS = createPlugins();

    default void onBlockRegister() {
    }

    default void onFluidRegister() {
    }

    default void onDataLoaderRegister() {
    }

    default void onEntityAttributeRegister() {
    }

    static List<CreateRegisterPlugin> createPlugins() {
        List<EntrypointContainer<CreateRegisterPlugin>> list = FabricLoader.getInstance()
            .getEntrypointContainers("create_plugin", CreateRegisterPlugin.class);
        List<Pair<String, CreateRegisterPlugin>> entries = new ArrayList<>(list.size());
        Comparator<Pair<String, CreateRegisterPlugin>> pluginComparator = Comparator.comparing((Pair<String, CreateRegisterPlugin> pair) -> !pair.getFirst()
            .equals("create")).thenComparing(Pair::getFirst);
        for (EntrypointContainer<CreateRegisterPlugin> container : list) {
            Pair<String, CreateRegisterPlugin> plugin = Pair.of(container.getProvider().getMetadata().getId(), container.getEntrypoint());
            int index = Collections.binarySearch(entries, plugin, pluginComparator);
            int insertionPoint = index >= 0 ? index : -index - 1;
            entries.add(insertionPoint, plugin);
        }
        return entries.stream().map(Pair::getSecond).toList();
    }

    static void registerBlock() {
        PLUGINS.forEach(CreateRegisterPlugin::onBlockRegister);
    }

    static void registerFluid() {
        PLUGINS.forEach(CreateRegisterPlugin::onFluidRegister);
    }

    static void registerDataLoader() {
        PLUGINS.forEach(CreateRegisterPlugin::onDataLoaderRegister);
    }

    static void registerEntityAttributes() {
        PLUGINS.forEach(CreateRegisterPlugin::onEntityAttributeRegister);
    }
}
