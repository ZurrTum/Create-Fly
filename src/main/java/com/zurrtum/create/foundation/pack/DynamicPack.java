package com.zurrtum.create.foundation.pack;

import com.google.gson.JsonElement;
import com.zurrtum.create.Create;
import net.minecraft.SharedConstants;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataSerializer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO - Move into catnip
public class DynamicPack implements ResourcePack {
    private final Map<String, InputSupplier<InputStream>> files = new HashMap<>();

    private final String packId;
    private final ResourceType packType;
    private final PackResourceMetadata metadata;
    private final ResourcePackInfo packLocationInfo;

    public DynamicPack(String packId, Text title, ResourceType packType) {
        this.packId = packId;
        this.packType = packType;

        metadata = new PackResourceMetadata(title, SharedConstants.getGameVersion().packVersion(packType).majorRange());
        packLocationInfo = new ResourcePackInfo(packId, Text.literal(packId), ResourcePackSource.BUILTIN, Optional.empty());
    }

    private static String getPath(ResourceType packType, Identifier identifier) {
        return packType.getDirectory() + "/" + identifier.getNamespace() + "/" + identifier.getPath();
    }

    public DynamicPack put(Identifier location, InputSupplier<InputStream> stream) {
        files.put(getPath(packType, location), stream);
        return this;
    }

    public DynamicPack put(Identifier location, byte[] bytes) {
        return put(location, () -> new ByteArrayInputStream(bytes));
    }

    public DynamicPack put(Identifier location, String string) {
        return put(location, string.getBytes(StandardCharsets.UTF_8));
    }

    // Automatically suffixes the Identifier with .json
    public DynamicPack put(Identifier location, JsonElement json) {
        return put(location.withSuffixedPath(".json"), Create.GSON.toJson(json));
    }

    public boolean isEmpty() {
        return files.isEmpty();
    }

    @Override
    public @Nullable InputSupplier<InputStream> openRoot(String @NotNull ... elements) {
        return files.getOrDefault(String.join("/", elements), null);
    }

    @Override
    public @Nullable InputSupplier<InputStream> open(@NotNull ResourceType packType, @NotNull Identifier identifier) {
        return files.getOrDefault(getPath(packType, identifier), null);
    }

    @Override
    public void findResources(
        @NotNull ResourceType packType,
        @NotNull String namespace,
        @NotNull String path,
        @NotNull ResultConsumer resourceOutput
    ) {
        Identifier identifier = Identifier.of(namespace, path);
        String directoryAndNamespace = packType.getDirectory() + "/" + namespace + "/";
        String prefix = directoryAndNamespace + path + "/";
        files.forEach((filePath, streamSupplier) -> {
            if (filePath.startsWith(prefix))
                resourceOutput.accept(identifier.withPath(filePath.substring(directoryAndNamespace.length())), streamSupplier);
        });
    }

    @Override
    public @NotNull Set<String> getNamespaces(ResourceType packType) {
        Set<String> namespaces = new HashSet<>();
        String dir = packType.getDirectory() + "/";

        for (String path : files.keySet()) {
            if (path.startsWith(dir)) {
                String relative = path.substring(dir.length());
                if (relative.contains("/")) {
                    namespaces.add(relative.substring(0, relative.indexOf("/")));
                }
            }
        }

        return namespaces;
    }

    @SuppressWarnings("unchecked")
    @Override
    public @Nullable <T> T parseMetadata(@NotNull ResourceMetadataSerializer<T> deserializer) {
        return deserializer == PackResourceMetadata.getSerializerFor(packType) ? (T) metadata : null;
    }

    @Override
    public @NotNull ResourcePackInfo getInfo() {
        return packLocationInfo;
    }

    @Override
    public @NotNull String getId() {
        return packId;
    }

    @Override
    public void close() {
    } // NO-OP
}