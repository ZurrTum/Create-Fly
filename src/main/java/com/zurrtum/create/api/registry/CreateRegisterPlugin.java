package com.zurrtum.create.api.registry;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.impl.launch.FabricLauncherBase;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandleProxies;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CreateRegisterPlugin implements LanguageAdapter {
    private static final Comparator<PluginEntry> pluginComparator = Comparator.comparing((PluginEntry plugin) -> !plugin.id.equals("create"))
        .thenComparing(PluginEntry::id);

    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        if (type != Runnable.class) {
            throw new LanguageAdapterException(new UnsupportedOperationException("Only Runnable is supported."));
        }
        Class<?> c;
        try {
            c = Class.forName(value, false, FabricLauncherBase.getLauncher().getTargetClassLoader());
        } catch (ClassNotFoundException e) {
            throw new LanguageAdapterException(e);
        }
        Method method = null;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("register") && (m.getModifiers() & Modifier.STATIC) != 0 && m.getParameterCount() == 0) {
                method = m;
                break;
            }
        }
        if (method == null) {
            throw new LanguageAdapterException("The register method was not found.");
        }
        MethodHandle handle;
        try {
            handle = MethodHandles.lookup().unreflect(method);
        } catch (Exception ex) {
            throw new LanguageAdapterException(ex);
        }
        try {
            return MethodHandleProxies.asInterfaceInstance(type, handle);
        } catch (Exception ex) {
            throw new LanguageAdapterException(ex);
        }
    }

    public static void run(String key) {
        List<EntrypointContainer<Runnable>> list = FabricLoader.getInstance().getEntrypointContainers(key, Runnable.class);
        List<PluginEntry> plugins = new ArrayList<>(list.size());
        for (EntrypointContainer<Runnable> container : list) {
            PluginEntry plugin = new PluginEntry(container.getProvider().getMetadata().getId(), container);
            int index = Collections.binarySearch(plugins, plugin, pluginComparator);
            int insertionPoint = index >= 0 ? index : -index - 1;
            plugins.add(insertionPoint, plugin);
        }
        plugins.forEach(PluginEntry::run);
    }

    private record PluginEntry(String id, EntrypointContainer<Runnable> container) {
        void run() {
            container.getEntrypoint().run();
        }
    }
}
