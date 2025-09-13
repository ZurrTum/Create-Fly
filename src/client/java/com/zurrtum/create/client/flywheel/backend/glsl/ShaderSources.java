package com.zurrtum.create.client.flywheel.backend.glsl;

import com.zurrtum.create.client.flywheel.backend.compile.FlwPrograms;
import com.zurrtum.create.client.flywheel.lib.util.StringUtil;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The main object for loading and parsing source files.
 */
public class ShaderSources {
    public static final String SHADER_DIR = "flywheel/";

    @VisibleForTesting
    protected final Map<Identifier, LoadResult> cache;

    public ShaderSources(ResourceManager manager) {
        var sourceFinder = new SourceFinder(manager);

        long loadStart = System.nanoTime();
        manager.findResources("flywheel", ShaderSources::isShader).forEach(sourceFinder::rootLoad);

        long loadEnd = System.nanoTime();

        FlwPrograms.LOGGER.info("Loaded {} shader sources in {}", sourceFinder.results.size(), StringUtil.formatTime(loadEnd - loadStart));

        this.cache = sourceFinder.results;
    }

    private static Identifier locationWithoutFlywheelPrefix(Identifier loc) {
        return Identifier.of(loc.getNamespace(), loc.getPath().substring(SHADER_DIR.length()));
    }

    public LoadResult find(Identifier location) {
        return cache.computeIfAbsent(location, loc -> new LoadResult.Failure(new LoadError.ResourceError(loc)));
    }

    public SourceFile get(Identifier location) {
        return find(location).unwrap();
    }

    private static boolean isShader(Identifier loc) {
        var path = loc.getPath();
        return path.endsWith(".glsl") || path.endsWith(".vert") || path.endsWith(".frag") || path.endsWith(".comp");
    }

    private static class SourceFinder {
        private final Deque<Identifier> findStack = new ArrayDeque<>();
        private final Map<Identifier, LoadResult> results = new HashMap<>();
        private final ResourceManager manager;

        public SourceFinder(ResourceManager manager) {
            this.manager = manager;
        }

        public void rootLoad(Identifier loc, Resource resource) {
            var strippedLoc = locationWithoutFlywheelPrefix(loc);

            if (results.containsKey(strippedLoc)) {
                // Some other source already #included this one.
                return;
            }

            this.results.put(strippedLoc, readResource(strippedLoc, resource));
        }

        public LoadResult recursiveLoad(Identifier location) {
            if (findStack.contains(location)) {
                // Make a copy of the find stack with the offending location added on top to show the full path.
                findStack.addLast(location);
                var copy = List.copyOf(findStack);
                findStack.removeLast();
                return new LoadResult.Failure(new LoadError.CircularDependency(location, copy));
            }
            findStack.addLast(location);

            LoadResult out = _find(location);

            findStack.removeLast();
            return out;
        }

        private LoadResult _find(Identifier location) {
            // Can't use computeIfAbsent because mutual recursion causes ConcurrentModificationExceptions
            var out = results.get(location);
            if (out == null) {
                out = load(location);
                results.put(location, out);
            }
            return out;
        }

        private LoadResult load(Identifier loc) {
            return manager.getResource(loc.withPrefixedPath(SHADER_DIR)).map(resource -> readResource(loc, resource))
                .orElseGet(() -> new LoadResult.Failure(new LoadError.ResourceError(loc)));
        }

        private LoadResult readResource(Identifier loc, Resource resource) {
            try (InputStream stream = resource.getInputStream()) {
                String sourceString = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
                return SourceFile.parse(this::recursiveLoad, loc, sourceString);
            } catch (IOException e) {
                return new LoadResult.Failure(new LoadError.IOError(loc, e));
            }
        }
    }
}
