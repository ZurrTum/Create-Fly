package com.zurrtum.create.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.client.resource.DefaultClientResourcePackProvider;
import net.minecraft.registry.VersionedIdentifier;
import net.minecraft.resource.*;
import net.minecraft.resource.metadata.PackResourceMetadata;
import net.minecraft.resource.metadata.ResourceMetadataMap;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(DefaultClientResourcePackProvider.class)
public abstract class DefaultClientResourcePackProviderMixin {
    @Inject(method = "forEachProfile(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void loadResourcePack(BiConsumer<String, Function<String, ResourcePackProfile>> consumer, CallbackInfo ci) {
        ModContainer mod = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
        List<Path> rootPaths = mod.getRootPaths();
        ModMetadata metadata = mod.getMetadata();
        String version = metadata.getVersion().getFriendlyString();
        if (!FabricLoader.getInstance().isModLoaded("fabric-api")) {
            consumer.accept(
                MOD_ID, id -> {
                    String directory = ResourceType.CLIENT_RESOURCES.getDirectory();
                    ResourcePackInfo info = createInfo(id, metadata.getName(), id, directory, version);
                    ResourceMetadataMap meta = createMeta(Text.translatable("advancement.create.root"));
                    ResourcePackPosition position = new ResourcePackPosition(true, ResourcePackProfile.InsertionPosition.BOTTOM, false);
                    return createPacket(rootPaths, directory, info, meta, position, id, "minecraft", "flywheel", "vanillin", "ponder", "fabric");
                }
            );
        }
        consumer.accept(
            MOD_ID + "_legacy_copper", id -> {
                String directory = "legacy_copper";
                ResourcePackInfo info = createInfo(id, "Create legacy copper", MOD_ID, directory, version);
                ResourceMetadataMap meta = createMeta(Text.literal("Replacement textures for Vanilla Copper"));
                ResourcePackPosition position = new ResourcePackPosition(false, ResourcePackProfile.InsertionPosition.TOP, false);
                return createPacket(rootPaths, directory, info, meta, position, "minecraft");
            }
        );
    }

    private ResourcePackProfile createPacket(
        List<Path> rootPaths,
        String directory,
        ResourcePackInfo info,
        ResourceMetadataMap meta,
        ResourcePackPosition position,
        String... namespace
    ) {
        DefaultResourcePackBuilder builder = new DefaultResourcePackBuilder().withMetadataMap(meta).withNamespaces(namespace);
        for (Path path : rootPaths) {
            builder.withPath(ResourceType.CLIENT_RESOURCES, path.resolve(directory));
        }
        DefaultResourcePack pack = builder.build(info);
        ResourcePackProfile.PackFactory packFactory = new ResourcePackProfile.PackFactory() {
            @Override
            public ResourcePack open(ResourcePackInfo info) {
                return pack;
            }

            @Override
            public ResourcePack openWithOverlays(ResourcePackInfo info, ResourcePackProfile.Metadata metadata) {
                return pack;
            }
        };
        return ResourcePackProfile.create(info, packFactory, ResourceType.CLIENT_RESOURCES, position);
    }

    @Unique
    private static ResourcePackInfo createInfo(String id, String name, String namespace, String path, String version) {
        return new ResourcePackInfo(
            id,
            Text.literal(name),
            ResourcePackSource.BUILTIN,
            Optional.of(new VersionedIdentifier(namespace, path, version))
        );
    }

    @Unique
    private static ResourceMetadataMap createMeta(Text description) {
        return ResourceMetadataMap.of(
            PackResourceMetadata.CLIENT_RESOURCES_SERIALIZER,
            new PackResourceMetadata(description, SharedConstants.getGameVersion().packVersion(ResourceType.CLIENT_RESOURCES).majorRange())
        );
    }
}
