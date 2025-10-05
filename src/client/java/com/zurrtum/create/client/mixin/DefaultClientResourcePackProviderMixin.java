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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import static com.zurrtum.create.Create.MOD_ID;

@Mixin(DefaultClientResourcePackProvider.class)
public abstract class DefaultClientResourcePackProviderMixin {
    @Inject(method = "forEachProfile(Ljava/util/function/BiConsumer;)V", at = @At("TAIL"))
    private void loadResourcePack(BiConsumer<String, Function<String, ResourcePackProfile>> consumer, CallbackInfo ci) {
        ModContainer mod = FabricLoader.getInstance().getModContainer(MOD_ID).orElseThrow();
        consumer.accept(
            MOD_ID, id -> {
                ModMetadata metadata = mod.getMetadata();
                ResourcePackInfo info = new ResourcePackInfo(
                    id,
                    Text.of(metadata.getName()),
                    ResourcePackSource.BUILTIN,
                    Optional.of(new VersionedIdentifier(id, "assets", metadata.getVersion().getFriendlyString()))
                );
                ResourceType type = ResourceType.CLIENT_RESOURCES;
                ResourceMetadataMap metadataMap = ResourceMetadataMap.of(
                    PackResourceMetadata.CLIENT_RESOURCES_SERIALIZER,
                    new PackResourceMetadata(
                        Text.translatable("advancement.create.root"),
                        SharedConstants.getGameVersion().packVersion(type).majorRange()
                    )
                );
                DefaultResourcePackBuilder builder = new DefaultResourcePackBuilder().withMetadataMap(metadataMap)
                    .withNamespaces(id, "minecraft", "flywheel", "vanillin", "ponder", "fabric");
                String directory = type.getDirectory();
                for (Path path : mod.getRootPaths()) {
                    builder.withPath(type, path.resolve(directory));
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
                ResourcePackPosition position = new ResourcePackPosition(true, ResourcePackProfile.InsertionPosition.BOTTOM, false);
                return ResourcePackProfile.create(info, packFactory, type, position);
            }
        );
    }
}
