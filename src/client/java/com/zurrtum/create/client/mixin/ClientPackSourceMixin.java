package com.zurrtum.create.client.mixin;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.client.resources.ClientPackSource;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ResourceMetadata;
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

@Mixin(ClientPackSource.class)
public abstract class ClientPackSourceMixin {
    @Inject(method = "populatePackList(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void loadResourcePack(BiConsumer<String, Function<String, Pack>> consumer, CallbackInfo ci) {
        ModContainer mod = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
        List<Path> rootPaths = mod.getRootPaths();
        ModMetadata metadata = mod.getMetadata();
        String version = metadata.getVersion().getFriendlyString();
        if (!FabricLoader.getInstance().isModLoaded("fabric-api")) {
            consumer.accept(
                MOD_ID, id -> {
                    String directory = PackType.CLIENT_RESOURCES.getDirectory();
                    PackLocationInfo info = createInfo(id, metadata.getName(), id, directory, version);
                    ResourceMetadata meta = createMeta(Component.translatable("advancement.create.root"));
                    PackSelectionConfig position = new PackSelectionConfig(true, Pack.Position.BOTTOM, false);
                    return createPacket(rootPaths, directory, info, meta, position, id, "minecraft", "flywheel", "vanillin", "ponder", "fabric");
                }
            );
        }
        consumer.accept(
            MOD_ID + "_legacy_copper", id -> {
                String directory = "legacy_copper";
                PackLocationInfo info = createInfo(id, "Create legacy copper", MOD_ID, directory, version);
                ResourceMetadata meta = createMeta(Component.literal("Replacement textures for Vanilla Copper"));
                PackSelectionConfig position = new PackSelectionConfig(false, Pack.Position.TOP, false);
                return createPacket(rootPaths, directory, info, meta, position, "minecraft", MOD_ID);
            }
        );
    }

    @Unique
    private static PackLocationInfo createInfo(String id, String name, String namespace, String path, String version) {
        return new PackLocationInfo(id, Component.literal(name), PackSource.BUILT_IN, Optional.of(new KnownPack(namespace, path, version)));
    }

    @Unique
    private static ResourceMetadata createMeta(Component description) {
        return ResourceMetadata.of(
            PackMetadataSection.CLIENT_TYPE,
            new PackMetadataSection(description, SharedConstants.getCurrentVersion().packVersion(PackType.CLIENT_RESOURCES).minorRange())
        );
    }

    @Unique
    private static Pack createPacket(
        List<Path> rootPaths,
        String directory,
        PackLocationInfo info,
        ResourceMetadata meta,
        PackSelectionConfig position,
        String... namespace
    ) {
        VanillaPackResourcesBuilder builder = new VanillaPackResourcesBuilder().setMetadata(meta).exposeNamespace(namespace);
        for (Path path : rootPaths) {
            builder.pushAssetPath(PackType.CLIENT_RESOURCES, path.resolve(directory));
        }
        VanillaPackResources pack = builder.build(info);
        Pack.ResourcesSupplier packFactory = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo info) {
                return pack;
            }

            @Override
            public PackResources openFull(PackLocationInfo info, Pack.Metadata metadata) {
                return pack;
            }
        };
        return Pack.readMetaAndCreate(info, packFactory, PackType.CLIENT_RESOURCES, position);
    }
}
