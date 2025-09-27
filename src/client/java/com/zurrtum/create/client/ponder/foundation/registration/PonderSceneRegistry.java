package com.zurrtum.create.client.ponder.foundation.registration;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.zurrtum.create.client.ponder.Ponder;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.registration.SceneRegistryAccess;
import com.zurrtum.create.client.ponder.api.registration.StoryBoardEntry;
import com.zurrtum.create.client.ponder.api.scene.SceneBuilder;
import com.zurrtum.create.client.ponder.foundation.PonderIndex;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.Registries;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class PonderSceneRegistry implements SceneRegistryAccess {

    private final PonderLocalization localization;
    private final Multimap<Identifier, StoryBoardEntry> scenes;

    private boolean allowRegistration = true;

    public PonderSceneRegistry(PonderLocalization localization) {
        this.localization = localization;
        scenes = LinkedHashMultimap.create();
    }

    public void clearRegistry() {
        scenes.clear();
        allowRegistration = true;
    }

    //

    public void addStoryBoard(StoryBoardEntry entry) {
        if (!allowRegistration)
            throw new IllegalStateException("Registration Phase has already ended!");

        scenes.put(entry.getComponent(), entry);
    }

    //

    @Override
    public Collection<Map.Entry<Identifier, StoryBoardEntry>> getRegisteredEntries() {
        return scenes.entries();
    }

    @Override
    public boolean doScenesExistForId(Identifier id) {
        return scenes.containsKey(id);
    }

    //

    @Override
    public List<PonderScene> compile(Identifier id) {
        if (PonderIndex.editingModeActive())
            PonderIndex.reload();

        Collection<StoryBoardEntry> entries = scenes.get(id);

        if (entries.isEmpty())
            return Collections.emptyList();

        return compile(entries);

    }

    @Override
    public List<PonderScene> compile(Collection<StoryBoardEntry> entries) {
        if (PonderIndex.editingModeActive()) {
            localization.clearShared();
            PonderIndex.gatherSharedText();
        }

        List<PonderScene> scenes = new ArrayList<>();

        ClientWorld world = MinecraftClient.getInstance().world;
        for (StoryBoardEntry storyBoard : entries) {
            StructureTemplate activeTemplate = loadSchematic(storyBoard.getSchematicLocation());
            PonderLevel level = new PonderLevel(BlockPos.ORIGIN, world);
            activeTemplate.place(level, BlockPos.ORIGIN, BlockPos.ORIGIN, new StructurePlacementData(), level.random, Block.NOTIFY_LISTENERS);
            level.createBackup();
            PonderScene scene = compileScene(localization, storyBoard, level);
            scene.begin();
            scenes.add(scene);
        }

        return scenes;
    }

    public static PonderScene compileScene(PonderLocalization localization, StoryBoardEntry sb, @Nullable PonderLevel level) {
        PonderScene scene = new PonderScene(level, localization, sb.getNamespace(), sb.getComponent(), sb.getTags(), sb.getOrderingEntries());
        SceneBuilder builder = scene.builder();
        sb.getBoard().program(builder, scene.getSceneBuildingUtil());
        return scene;
    }

    public static StructureTemplate loadSchematic(Identifier location) {
        return loadSchematic(MinecraftClient.getInstance().getResourceManager(), location);
    }

    public static StructureTemplate loadSchematic(ResourceManager resourceManager, Identifier location) {
        String namespace = location.getNamespace();
        String path = "ponder/" + location.getPath() + ".nbt";
        Identifier location1 = Identifier.of(namespace, path);

        Optional<Resource> optionalResource = resourceManager.getResource(location1);
        if (optionalResource.isEmpty()) {
            Ponder.LOGGER.error("Ponder schematic missing: " + location1);

            return new StructureTemplate();
        }

        Resource resource = optionalResource.get();
        try (InputStream inputStream = resource.getInputStream()) {
            return loadSchematic(inputStream);
        } catch (IOException e) {
            Ponder.LOGGER.error("Failed to read ponder schematic: " + location1, e);
        }

        return new StructureTemplate();
    }

    public static StructureTemplate loadSchematic(InputStream resourceStream) throws IOException {
        StructureTemplate t = new StructureTemplate();
        DataInputStream stream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(resourceStream)));
        NbtCompound nbt = NbtIo.readCompound(stream, NbtSizeTracker.of(0x20000000L));
        //t.load(Minecraft.getInstance().level.holderLookup(Registries.BLOCK), nbt);
        t.readNbt(Registries.BLOCK, nbt);
        return t;
    }
}