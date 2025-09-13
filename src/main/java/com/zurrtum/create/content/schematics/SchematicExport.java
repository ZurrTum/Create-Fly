package com.zurrtum.create.content.schematics;

import com.zurrtum.create.Create;
import com.zurrtum.create.foundation.utility.FilesHelper;
import net.minecraft.block.Blocks;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class SchematicExport {
    /**
     * Save a schematic to a file from a world.
     *
     * @param dir       the directory the schematic will be created in
     * @param fileName  the ideal name of the schematic, may not be the name of the created file
     * @param overwrite whether overwriting an existing schematic is allowed
     * @param level     the level where the schematic structure is placed
     * @param first     the first corner of the schematic area
     * @param second    the second corner of the schematic area
     * @return a SchematicExportResult, or null if an error occurred.
     */
    @Nullable
    public static SchematicExportResult saveSchematic(Path dir, String fileName, boolean overwrite, World level, BlockPos first, BlockPos second) {
        BlockBox bb = BlockBox.create(first, second);
        BlockPos origin = new BlockPos(bb.getMinX(), bb.getMinY(), bb.getMinZ());
        BlockPos bounds = new BlockPos(bb.getBlockCountX(), bb.getBlockCountY(), bb.getBlockCountZ());

        StructureTemplate structure = new StructureTemplate();
        structure.saveFromWorld(level, origin, bounds, true, List.of(Blocks.AIR));
        NbtCompound data = structure.writeNbt(new NbtCompound());
        SchematicAndQuillItem.replaceStructureVoidWithAir(data);
        SchematicAndQuillItem.clampGlueBoxes(level, new Box(Vec3d.of(origin), Vec3d.of(origin.add(bounds))), data);

        if (fileName.isEmpty())
            fileName = Text.translatable("create.schematicAndQuill.fallbackName").getString();
        if (!overwrite)
            fileName = FilesHelper.findFirstValidFilename(fileName, dir, "nbt");
        if (!fileName.endsWith(".nbt"))
            fileName += ".nbt";
        Path file = dir.resolve(fileName).toAbsolutePath();

        try {
            Files.createDirectories(dir);
            boolean overwritten = Files.deleteIfExists(file);
            try (OutputStream out = Files.newOutputStream(file, StandardOpenOption.CREATE)) {
                NbtIo.writeCompressed(data, out);
            }
            return new SchematicExportResult(file, dir, fileName, overwritten, origin, bounds);
        } catch (IOException e) {
            Create.LOGGER.error("An error occurred while saving schematic [" + fileName + "]", e);
            return null;
        }
    }

    public record SchematicExportResult(
        Path file, Path dir, String fileName, boolean overwritten, BlockPos origin, BlockPos bounds
    ) {
    }
}
