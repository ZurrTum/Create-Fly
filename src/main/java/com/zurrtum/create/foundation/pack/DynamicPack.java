package com.zurrtum.create.foundation.pack;

import com.google.gson.JsonElement;
import com.zurrtum.create.Create;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

// TODO - Move into catnip
public class DynamicPack implements PackResources {
    private final Map<String, IoSupplier<InputStream>> files = new HashMap<>();

    private final String packId;
    private final PackType packType;
    private final PackMetadataSection metadata;
    private final PackLocationInfo packLocationInfo;

    public DynamicPack(String packId, Component title, PackType packType) {
        this.packId = packId;
        this.packType = packType;

        metadata = new PackMetadataSection(title, SharedConstants.getCurrentVersion().packVersion(packType).minorRange());
        packLocationInfo = new PackLocationInfo(packId, Component.literal(packId), PackSource.BUILT_IN, Optional.empty());
    }

    private static String getPath(PackType packType, Identifier identifier) {
        return packType.getDirectory() + "/" + identifier.getNamespace() + "/" + identifier.getPath();
    }

    public DynamicPack put(Identifier location, IoSupplier<InputStream> stream) {
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
        return put(location.withSuffix(".json"), Create.GSON.toJson(json));
    }

    public boolean isEmpty() {
        return files.isEmpty();
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String @NotNull ... elements) {
        return files.getOrDefault(String.join("/", elements), null);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(@NotNull PackType packType, @NotNull Identifier identifier) {
        return files.getOrDefault(getPath(packType, identifier), null);
    }

    @Override
    public void listResources(@NotNull PackType packType, @NotNull String namespace, @NotNull String path, @NotNull ResourceOutput resourceOutput) {
        Identifier identifier = Identifier.fromNamespaceAndPath(namespace, path);
        String directoryAndNamespace = packType.getDirectory() + "/" + namespace + "/";
        String prefix = directoryAndNamespace + path + "/";
        files.forEach((filePath, streamSupplier) -> {
            if (filePath.startsWith(prefix))
                resourceOutput.accept(identifier.withPath(filePath.substring(directoryAndNamespace.length())), streamSupplier);
        });
    }

    @Override
    public @NotNull Set<String> getNamespaces(PackType packType) {
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
    public @Nullable <T> T getMetadataSection(@NotNull MetadataSectionType<T> deserializer) {
        return deserializer == PackMetadataSection.forPackType(packType) ? (T) metadata : null;
    }

    @Override
    public @NotNull PackLocationInfo location() {
        return packLocationInfo;
    }

    @Override
    public @NotNull String packId() {
        return packId;
    }

    @Override
    public void close() {
    } // NO-OP
}