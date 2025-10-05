package com.zurrtum.create.content.schematics;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.Create;
import com.zurrtum.create.content.schematics.SchematicExport.SchematicExportResult;
import com.zurrtum.create.content.schematics.table.SchematicTableBlockEntity;
import com.zurrtum.create.foundation.utility.CreatePaths;
import com.zurrtum.create.foundation.utility.FilesHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CSchematics;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class ServerSchematicLoader {

    private final Map<String, SchematicUploadEntry> activeUploads;

    public static class SchematicUploadEntry {
        public World world;
        public BlockPos tablePos;
        public OutputStream stream;
        public long bytesUploaded;
        public long totalBytes;
        public int idleTime;

        public SchematicUploadEntry(OutputStream stream, long totalBytes, World world, BlockPos tablePos) {
            this.stream = stream;
            this.totalBytes = totalBytes;
            this.tablePos = tablePos;
            this.world = world;
            this.bytesUploaded = 0;
            this.idleTime = 0;
        }
    }

    public ServerSchematicLoader() {
        activeUploads = new HashMap<>();
    }

    private final ObjectArrayList<String> deadEntries = ObjectArrayList.of();

    public void tick() {
        // Detect Timed out Uploads
        int timeout = getConfig().schematicIdleTimeout.get();
        for (String upload : activeUploads.keySet()) {
            SchematicUploadEntry entry = activeUploads.get(upload);

            if (entry.idleTime++ > timeout) {
                Create.LOGGER.warn("Schematic Upload timed out: " + upload);
                deadEntries.add(upload);
            }
        }

        // Remove Timed out Uploads
        for (String toRemove : deadEntries) {
            this.cancelUpload(toRemove);
        }
        deadEntries.clear();
    }

    public void shutdown() {
        // Close open streams
        new HashSet<>(activeUploads.keySet()).forEach(this::cancelUpload);
    }

    public void handleNewUpload(ServerPlayerEntity player, String schematic, long size, BlockPos pos) {
        String playerName = player.getGameProfile().getName();

        Path baseDir = CreatePaths.UPLOADED_SCHEMATICS_DIR;
        Path playerPath = baseDir.resolve(playerName).normalize();
        Path uploadPath = playerPath.resolve(schematic).normalize();
        String playerSchematicId = playerName + "/" + schematic;

        if (playerPath.startsWith(baseDir) && uploadPath.startsWith(playerPath)) {
            FilesHelper.createFolderIfMissing(playerPath);
        } else {
            Create.LOGGER.warn("Attempted Schematic Upload with path traversal: {}", playerSchematicId);
            return;
        }

        // Unsupported Format
        if (!schematic.endsWith(".nbt")) {
            Create.LOGGER.warn("Attempted Schematic Upload with non-supported Format: {}", playerSchematicId);
            return;
        }

        // Too big
        if (!validateSchematicSizeOnServer(player, size))
            return;

        // Skip existing Uploads
        if (activeUploads.containsKey(playerSchematicId))
            return;

        try {
            // Validate Referenced Block
            SchematicTableBlockEntity table = getTable(player.getEntityWorld(), pos);
            if (table == null)
                return;

            // Delete schematic with same name
            Files.deleteIfExists(uploadPath);

            // Too many Schematics
            long count;
            try (Stream<Path> list = Files.list(playerPath)) {
                count = list.count();
            }

            if (count >= getConfig().maxSchematics.get()) {
                Stream<Path> list2 = Files.list(playerPath);
                Optional<Path> lastFilePath = list2.filter(f -> !Files.isDirectory(f)).min(Comparator.comparingLong(f -> f.toFile().lastModified()));
                list2.close();
                if (lastFilePath.isPresent()) {
                    Files.deleteIfExists(lastFilePath.get());
                }
            }

            // Open Stream
            OutputStream writer = Files.newOutputStream(uploadPath);
            activeUploads.put(playerSchematicId, new SchematicUploadEntry(writer, size, player.getEntityWorld(), pos));

            // Notify Block Entity
            table.startUpload(schematic);
        } catch (IOException e) {
            Create.LOGGER.error("Exception Thrown when starting Upload: {}", playerSchematicId, e);
        }
    }

    protected boolean validateSchematicSizeOnServer(ServerPlayerEntity player, long size) {
        long maxFileSize = getConfig().maxTotalSchematicSize.get();
        if (size > maxFileSize * 1000) {
            player.sendMessage(Text.translatable("create.schematics.uploadTooLarge").append(Text.literal(" (" + size / 1000 + " KB).")));
            player.sendMessage(Text.translatable("create.schematics.maxAllowedSize").append(Text.literal(" " + maxFileSize + " KB")));
            return false;
        }
        return true;
    }

    public CSchematics getConfig() {
        return AllConfigs.server().schematics;
    }

    public void handleWriteRequest(ServerPlayerEntity player, String schematic, byte[] data) {
        String playerSchematicId = player.getGameProfile().getName() + "/" + schematic;

        if (activeUploads.containsKey(playerSchematicId)) {
            SchematicUploadEntry entry = activeUploads.get(playerSchematicId);
            entry.bytesUploaded += data.length;

            // Size Validations
            if (data.length > getConfig().maxSchematicPacketSize.get()) {
                Create.LOGGER.warn("Oversized Upload Packet received: {}", playerSchematicId);
                cancelUpload(playerSchematicId);
                return;
            }

            if (entry.bytesUploaded > entry.totalBytes) {
                Create.LOGGER.warn("Received more data than Expected: {}", playerSchematicId);
                cancelUpload(playerSchematicId);
                return;
            }

            try {
                entry.stream.write(data);
                entry.idleTime = 0;

                SchematicTableBlockEntity table = getTable(entry.world, entry.tablePos);
                if (table == null)
                    return;
                table.uploadingProgress = (float) ((double) entry.bytesUploaded / entry.totalBytes);
                table.sendUpdate = true;

            } catch (IOException e) {
                Create.LOGGER.error("Exception Thrown when uploading Schematic: {}", playerSchematicId, e);
                cancelUpload(playerSchematicId);
            }
        }
    }

    protected void cancelUpload(String playerSchematicId) {
        if (!activeUploads.containsKey(playerSchematicId))
            return;

        SchematicUploadEntry entry = activeUploads.remove(playerSchematicId);
        try {
            entry.stream.close();
            Files.deleteIfExists(CreatePaths.UPLOADED_SCHEMATICS_DIR.resolve(playerSchematicId));
            Create.LOGGER.warn("Cancelled Schematic Upload: {}", playerSchematicId);
        } catch (IOException e) {
            Create.LOGGER.error("Exception Thrown when cancelling Upload: {}", playerSchematicId, e);
        }

        BlockPos pos = entry.tablePos;
        if (pos == null)
            return;

        SchematicTableBlockEntity table = getTable(entry.world, pos);
        if (table != null)
            table.finishUpload();
    }

    public SchematicTableBlockEntity getTable(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof SchematicTableBlockEntity table))
            return null;
        return table;
    }

    public void handleFinishedUpload(ServerPlayerEntity player, String schematic) {
        String playerSchematicId = player.getGameProfile().getName() + "/" + schematic;

        if (activeUploads.containsKey(playerSchematicId)) {
            try {
                activeUploads.get(playerSchematicId).stream.close();
                SchematicUploadEntry removed = activeUploads.remove(playerSchematicId);
                World world = removed.world;
                BlockPos pos = removed.tablePos;

                Create.LOGGER.info("New Schematic Uploaded: " + playerSchematicId);
                if (pos == null)
                    return;

                BlockState blockState = world.getBlockState(pos);
                if (blockState.getBlock() != AllBlocks.SCHEMATIC_TABLE)
                    return;

                SchematicTableBlockEntity table = getTable(world, pos);
                if (table == null)
                    return;
                table.finishUpload();
                table.inventory.setStack(1, SchematicItem.create(world, schematic, player.getGameProfile().getName()));

            } catch (IOException e) {
                Create.LOGGER.error("Exception Thrown when finishing Upload: {}", playerSchematicId, e);
            }
        }
    }

    public void handleInstantSchematic(ServerPlayerEntity player, String schematic, World world, BlockPos pos, BlockPos bounds) {
        String playerName = player.getGameProfile().getName();

        Path baseDir = CreatePaths.UPLOADED_SCHEMATICS_DIR;
        Path playerPath = baseDir.resolve(playerName).normalize();
        Path uploadPath = playerPath.resolve(schematic).normalize();
        String playerSchematicId = playerName + "/" + schematic;

        if (playerPath.startsWith(baseDir) && uploadPath.startsWith(playerPath)) {
            FilesHelper.createFolderIfMissing(playerPath);
        } else {
            Create.LOGGER.warn("Attempted Schematic Upload with path traversal: {}", playerSchematicId);
            return;
        }

        // Unsupported Format
        if (!schematic.endsWith(".nbt")) {
            Create.LOGGER.warn("Attempted Schematic Upload with non-supported Format: {}", playerSchematicId);
            return;
        }

        // Not holding S&Q
        if (!player.getMainHandStack().isOf(AllItems.SCHEMATIC_AND_QUILL))
            return;

        // if there's too many schematics, delete oldest
        if (!tryDeleteOldestSchematic(playerPath))
            return;

        SchematicExportResult result = SchematicExport.saveSchematic(playerPath, schematic, true, world, pos, pos.add(bounds).add(-1, -1, -1));
        if (result != null)
            player.setStackInHand(Hand.MAIN_HAND, SchematicItem.create(world, schematic, playerName));
        else
            player.sendMessage(Text.translatable("create.schematicAndQuill.instant_failed").formatted(Formatting.RED));
    }

    private boolean tryDeleteOldestSchematic(Path dir) {
        try (Stream<Path> stream = Files.list(dir)) {
            List<Path> files = stream.toList();
            if (files.size() < getConfig().maxSchematics.get())
                return true;
            Optional<Path> oldest = files.stream().min(Comparator.comparingLong(this::getLastModifiedTime));
            Files.delete(oldest.orElseThrow());
            return true;
        } catch (IOException | IllegalStateException e) {
            Create.LOGGER.error("Error deleting oldest schematic", e);
            return false;
        }
    }

    private long getLastModifiedTime(Path file) {
        try {
            return Files.getLastModifiedTime(file).toMillis();
        } catch (IOException e) {
            Create.LOGGER.error("Error getting modification time of file {}", file.getFileName(), e);
            throw new IllegalStateException(e);
        }
    }

}
