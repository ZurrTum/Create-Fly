package com.zurrtum.create.mixin;

import com.zurrtum.create.foundation.pack.DynamicPack;
import com.zurrtum.create.foundation.pack.RuntimeDataGenerator;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
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
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(VanillaResourcePackProvider.class)
public class VanillaResourcePackProviderMixin {
    @Inject(method = "register(Ljava/util/function/Consumer;)V", at = @At("TAIL"))
    private void addDataPack(Consumer<ResourcePackProfile> consumer, CallbackInfo ci) {
        if ((Object) this instanceof VanillaDataPackProvider) {
            FabricLoader loader = FabricLoader.getInstance();
            if (!loader.isModLoaded("fabric-api")) {
                String directory = ResourceType.SERVER_DATA.getDirectory();
                ModContainer mod = loader.getModContainer(MOD_ID).orElseThrow();
                List<Path> paths = mod.getRootPaths();
                addDataPack(
                    consumer,
                    createInfo("fabric-convention-tags-v2", "Fabric Convention Tags (v2)", "2.15.2+d9a8963096"),
                    paths,
                    Text.of("fabric-convention-tags-v2-2.15.2+d9a8963096"),
                    directory + "/fabric",
                    "c"
                );
                ModMetadata metadata = mod.getMetadata();
                addDataPack(
                    consumer,
                    createInfo(MOD_ID, metadata.getName(), metadata.getVersion().getFriendlyString()),
                    paths,
                    Text.translatable("advancement.create.root"),
                    directory,
                    MOD_ID,
                    "minecraft",
                    "c"
                );
            }
            DynamicPack dynamicPack = new DynamicPack("create:dynamic_data", Text.translatable("advancement.create.root"), ResourceType.SERVER_DATA);
            RuntimeDataGenerator.insertIntoPack(dynamicPack);
            if (!dynamicPack.isEmpty()) {
                addDataPack(consumer, false, dynamicPack);
            }
        }
    }

    @Unique
    private static ResourcePackInfo createInfo(String id, String name, String version) {
        return new ResourcePackInfo(id, Text.of(name), ResourcePackSource.BUILTIN, Optional.of(new VersionedIdentifier(id, "data", version)));
    }

    @Unique
    private static void addDataPack(
        Consumer<ResourcePackProfile> consumer,
        ResourcePackInfo info,
        List<Path> paths,
        Text title,
        String directory,
        String... namespace
    ) {
        ResourceMetadataMap metadataMap = ResourceMetadataMap.of(
            PackResourceMetadata.SERIALIZER,
            new PackResourceMetadata(title, SharedConstants.getGameVersion().packVersion(ResourceType.SERVER_DATA), Optional.empty())
        );
        DefaultResourcePackBuilder builder = new DefaultResourcePackBuilder().withMetadataMap(metadataMap).withNamespaces(namespace);
        for (Path path : paths) {
            builder.withPath(ResourceType.SERVER_DATA, path.resolve(directory));
        }
        addDataPack(consumer, true, builder.build(info));
    }

    @Unique
    private static void addDataPack(Consumer<ResourcePackProfile> consumer, boolean required, ResourcePack pack) {
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
        ResourcePackPosition position = new ResourcePackPosition(required, ResourcePackProfile.InsertionPosition.BOTTOM, false);
        consumer.accept(ResourcePackProfile.create(pack.getInfo(), packFactory, ResourceType.SERVER_DATA, position));
    }
}
