package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.pack.DynamicPack;
import com.zurrtum.create.foundation.pack.RuntimeDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.*;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.repository.Pack.Metadata;
import net.minecraft.server.packs.repository.Pack.ResourcesSupplier;
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(BuiltInPackSource.class)
public class BuiltInPackSourceMixin {
    @Inject(method = "loadPacks(Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void addDataPack(Consumer<Pack> result, CallbackInfo ci) {
        if ((Object) this instanceof ServerPacksSource) {
            FabricLoader loader = FabricLoader.getInstance();
            if (!loader.isModLoaded("fabric-api")) {
                String directory = PackType.SERVER_DATA.getDirectory();
                ModContainer mod = loader.getModContainer(MOD_ID).orElseThrow();
                List<Path> paths = mod.getRootPaths();
                addDataPack(
                    result,
                    createInfo("fabric-convention-tags-v2", "Fabric Convention Tags (v2)", "4.10.5+187b64905d"),
                    paths,
                    Component.literal("fabric-convention-tags-v2-4.10.5+187b64905d"),
                    directory + "/fabric",
                    "c"
                );
                ModMetadata metadata = mod.getMetadata();
                addDataPack(
                    result,
                    createInfo(MOD_ID, metadata.getName(), metadata.getVersion().getFriendlyString()),
                    paths,
                    Component.translatable("advancement.create.root"),
                    directory,
                    MOD_ID,
                    "minecraft",
                    "c"
                );
            }
            DynamicPack dynamicPack = new DynamicPack(
                "create:dynamic_data",
                Component.translatable("advancement.create.root"),
                PackType.SERVER_DATA
            );
            RuntimeDataGenerator.insertIntoPack(dynamicPack);
            if (!dynamicPack.isEmpty()) {
                ResourcesSupplier resources = new ResourcesSupplier() {
                    @Override
                    public @NonNull PackMetadataResources openMetadata(@NonNull PackLocationInfo location) {
                        return dynamicPack;
                    }

                    @Override
                    public @NonNull Stream<PackResources> openResources(
                        @NonNull PackLocationInfo location,
                        @NonNull Metadata metadata
                    ) {
                        return Stream.of(dynamicPack);
                    }
                };
                addDataPack(result, false, dynamicPack.location(), resources);
            }
        }
    }

    @Unique
    private static PackLocationInfo createInfo(String id, String name, String version) {
        return new PackLocationInfo(
            id,
            Component.literal(name),
            PackSource.BUILT_IN,
            Optional.of(new KnownPack(id, "data", version))
        );
    }

    @Unique
    private static void addDataPack(
        Consumer<Pack> consumer,
        PackLocationInfo info,
        List<Path> paths,
        Component title,
        String directory,
        String... namespace
    ) {
        ResourceMetadata metadataMap = ResourceMetadata.of(
            PackMetadataSection.SERVER_TYPE,
            new PackMetadataSection(
                title,
                SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minorRange()
            )
        );
        VanillaPackResourcesBuilder builder = new VanillaPackResourcesBuilder().setMetadata(metadataMap)
            .exposeNamespace(namespace);
        for (Path path : paths) {
            builder.pushAssetPath(PackType.SERVER_DATA, path.resolve(directory));
        }
        addDataPack(consumer, true, info, builder.build(info).asResourcesSupplier());
    }

    @Unique
    private static void addDataPack(
        Consumer<Pack> consumer,
        boolean required,
        PackLocationInfo info,
        ResourcesSupplier resources
    ) {
        PackSelectionConfig position = new PackSelectionConfig(required, Pack.Position.BOTTOM, false);
        consumer.accept(Pack.readMetaAndCreate(info, resources, PackType.SERVER_DATA, position));
    }
}
