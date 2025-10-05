package com.zurrtum.create.content.schematics;

import com.mojang.logging.LogUtils;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.foundation.utility.CreatePaths;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.slf4j.Logger;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

public class SchematicItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    public SchematicItem(Settings properties) {
        super(properties);
    }

    public static ItemStack create(World level, String schematic, String owner) {
        ItemStack blueprint = AllItems.SCHEMATIC.getDefaultStack();

        blueprint.set(AllDataComponents.SCHEMATIC_DEPLOYED, false);
        blueprint.set(AllDataComponents.SCHEMATIC_OWNER, owner);
        blueprint.set(AllDataComponents.SCHEMATIC_FILE, schematic);
        blueprint.set(AllDataComponents.SCHEMATIC_ANCHOR, BlockPos.ORIGIN);
        blueprint.set(AllDataComponents.SCHEMATIC_ROTATION, BlockRotation.NONE);
        blueprint.set(AllDataComponents.SCHEMATIC_MIRROR, BlockMirror.NONE);

        writeSize(level, blueprint);
        return blueprint;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendTooltip(
        ItemStack stack,
        TooltipContext context,
        TooltipDisplayComponent displayComponent,
        Consumer<Text> tooltip,
        TooltipType flagIn
    ) {
        if (stack.contains(AllDataComponents.SCHEMATIC_FILE)) {
            tooltip.accept(Text.literal(Formatting.GOLD + stack.get(AllDataComponents.SCHEMATIC_FILE)));
        } else {
            tooltip.accept(Text.translatable("create.schematic.invalid").formatted(Formatting.RED));
        }
        super.appendTooltip(stack, context, displayComponent, tooltip, flagIn);
    }

    public static void writeSize(World level, ItemStack blueprint) {
        StructureTemplate t = loadSchematic(level, blueprint);
        blueprint.set(AllDataComponents.SCHEMATIC_BOUNDS, t.getSize());
        SchematicInstances.clearHash(blueprint);
    }

    public static StructurePlacementData getSettings(ItemStack blueprint) {
        return getSettings(blueprint, true);
    }

    public static StructurePlacementData getSettings(ItemStack blueprint, boolean processNBT) {
        StructurePlacementData settings = new StructurePlacementData();
        settings.setRotation(blueprint.getOrDefault(AllDataComponents.SCHEMATIC_ROTATION, BlockRotation.NONE));
        settings.setMirror(blueprint.getOrDefault(AllDataComponents.SCHEMATIC_MIRROR, BlockMirror.NONE));
        if (processNBT)
            settings.addProcessor(SchematicProcessor.INSTANCE);
        return settings;
    }

    public static StructureTemplate loadSchematic(World level, ItemStack blueprint) {
        StructureTemplate t = new StructureTemplate();
        String owner = blueprint.get(AllDataComponents.SCHEMATIC_OWNER);
        String schematic = blueprint.get(AllDataComponents.SCHEMATIC_FILE);

        if (owner == null || schematic == null || !schematic.endsWith(".nbt"))
            return t;

        Path dir;
        Path file;

        if (!level.isClient()) {
            dir = CreatePaths.UPLOADED_SCHEMATICS_DIR;
            file = Paths.get(owner, schematic);
        } else {
            dir = CreatePaths.SCHEMATICS_DIR;
            file = Paths.get(schematic);
        }

        Path path = dir.resolve(file).normalize();
        if (!path.startsWith(dir))
            return t;

        try (DataInputStream stream = new DataInputStream(new BufferedInputStream(new GZIPInputStream(Files.newInputStream(
            path,
            StandardOpenOption.READ
        ))))) {
            NbtCompound nbt = NbtIo.readCompound(stream, NbtSizeTracker.of(0x20000000L));
            t.readNbt(level.createCommandRegistryWrapper(RegistryKeys.BLOCK), nbt);
        } catch (IOException e) {
            LOGGER.warn("Failed to read schematic", e);
        }

        return t;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (context.getPlayer() != null && !onItemUse(context.getPlayer(), context.getHand()))
            return super.useOnBlock(context);
        return ActionResult.SUCCESS;
    }

    @Override
    public ActionResult use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        if (!onItemUse(playerIn, handIn))
            return super.use(worldIn, playerIn, handIn);
        return ActionResult.SUCCESS;
    }

    private boolean onItemUse(PlayerEntity player, Hand hand) {
        if (!player.isSneaking() || hand != Hand.MAIN_HAND)
            return false;
        if (!player.getStackInHand(hand).contains(AllDataComponents.SCHEMATIC_FILE))
            return false;
        if (!player.getEntityWorld().isClient())
            return true;
        AllClientHandle.INSTANCE.openSchematicEditScreen();
        return true;
    }
}
