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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(ClientPackSource.class)
public abstract class DefaultClientResourcePackProviderMixin {
    @Inject(method = "populatePackList(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void loadResourcePack(BiConsumer<String, Function<String, Pack>> consumer, CallbackInfo ci) {
        ModContainer mod = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
        consumer.accept(
            MOD_ID, id -> {
                ModMetadata metadata = mod.getMetadata();
                PackLocationInfo info = new PackLocationInfo(
                    id,
                    Component.nullToEmpty(metadata.getName()),
                    PackSource.BUILT_IN,
                    Optional.of(new KnownPack(id, "assets", metadata.getVersion().getFriendlyString()))
                );
                PackType type = PackType.CLIENT_RESOURCES;
                BuiltInMetadata metadataMap = BuiltInMetadata.of(
                    PackMetadataSection.CLIENT_TYPE,
                    new PackMetadataSection(
                        Component.translatable("advancement.create.root"),
                        SharedConstants.getCurrentVersion().packVersion(type).minorRange()
                    )
                );
                VanillaPackResourcesBuilder builder = new VanillaPackResourcesBuilder().setMetadata(metadataMap)
                    .exposeNamespace(id, "minecraft", "flywheel", "vanillin", "ponder", "fabric");
                String directory = type.getDirectory();
                for (Path path : mod.getRootPaths()) {
                    builder.pushAssetPath(type, path.resolve(directory));
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
                PackSelectionConfig position = new PackSelectionConfig(true, Pack.Position.BOTTOM, false);
                return Pack.readMetaAndCreate(info, packFactory, type, position);
            }
        );
    }
}
