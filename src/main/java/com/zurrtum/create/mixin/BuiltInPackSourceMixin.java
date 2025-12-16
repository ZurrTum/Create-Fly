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
import net.minecraft.server.packs.resources.ResourceMetadata;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(BuiltInPackSource.class)
public class BuiltInPackSourceMixin {
    @Inject(method = "loadPacks(Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void addDataPack(Consumer<Pack> consumer, CallbackInfo ci) {
        if ((Object) this instanceof ServerPacksSource) {
            FabricLoader loader = FabricLoader.getInstance();
            if (!loader.isModLoaded("fabric-api")) {
                String directory = PackType.SERVER_DATA.getDirectory();
                ModContainer mod = loader.getModContainer(MOD_ID).orElseThrow();
                List<Path> paths = mod.getRootPaths();
                addDataPack(
                    consumer,
                    createInfo("fabric-convention-tags-v2", "Fabric Convention Tags (v2)", "2.15.2+d9a8963096"),
                    paths,
                    Component.nullToEmpty("fabric-convention-tags-v2-2.15.2+d9a8963096"),
                    directory + "/fabric",
                    "c"
                );
                ModMetadata metadata = mod.getMetadata();
                addDataPack(
                    consumer,
                    createInfo(MOD_ID, metadata.getName(), metadata.getVersion().getFriendlyString()),
                    paths,
                    Component.translatable("advancement.create.root"),
                    directory,
                    MOD_ID,
                    "minecraft",
                    "c"
                );
            }
            DynamicPack dynamicPack = new DynamicPack("create:dynamic_data", Component.translatable("advancement.create.root"), PackType.SERVER_DATA);
            RuntimeDataGenerator.insertIntoPack(dynamicPack);
            if (!dynamicPack.isEmpty()) {
                addDataPack(consumer, false, dynamicPack);
            }
        }
    }

    @Unique
    private static PackLocationInfo createInfo(String id, String name, String version) {
        return new PackLocationInfo(id, Component.nullToEmpty(name), PackSource.BUILT_IN, Optional.of(new KnownPack(id, "data", version)));
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
            new PackMetadataSection(title, SharedConstants.getCurrentVersion().packVersion(PackType.SERVER_DATA).minorRange())
        );
        VanillaPackResourcesBuilder builder = new VanillaPackResourcesBuilder().setMetadata(metadataMap).exposeNamespace(namespace);
        for (Path path : paths) {
            builder.pushAssetPath(PackType.SERVER_DATA, path.resolve(directory));
        }
        addDataPack(consumer, true, builder.build(info));
    }

    @Unique
    private static void addDataPack(Consumer<Pack> consumer, boolean required, PackResources pack) {
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
        PackSelectionConfig position = new PackSelectionConfig(required, Pack.Position.BOTTOM, false);
        consumer.accept(Pack.readMetaAndCreate(pack.location(), packFactory, PackType.SERVER_DATA, position));
    }
}
