package com.zurrtum.create.content.schematics;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.catnip.math.BBHelper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.schematics.cannon.MaterialChecklist;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.IMergeableBE;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class SchematicPrinter {

    public enum PrintStage implements StringRepresentable {
        BLOCKS,
        DEFERRED_BLOCKS,
        ENTITIES;

        public static final Codec<PrintStage> CODEC = StringRepresentable.fromEnum(PrintStage::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private boolean schematicLoaded;
    private boolean isErrored;
    private @Nullable SchematicLevel blockReader;
    private @Nullable BlockPos schematicAnchor;

    private @Nullable BlockPos currentPos;
    private int printingEntityIndex;
    private PrintStage printStage;
    private List<BlockPos> deferredBlocks;

    public SchematicPrinter() {
        printingEntityIndex = -1;
        printStage = PrintStage.BLOCKS;
        deferredBlocks = new LinkedList<>();
    }

    public void read(ValueInput view, boolean clientPacket) {
        view.read("CurrentPos", BlockPos.CODEC).ifPresent(pos -> currentPos = pos);
        if (clientPacket) {
            schematicLoaded = false;
            view.read("Anchor", BlockPos.CODEC).ifPresent(pos -> {
                schematicAnchor = pos;
                schematicLoaded = true;
            });
        }

        printingEntityIndex = view.getIntOr("EntityProgress", 0);
        printStage = view.read("PrintStage", PrintStage.CODEC).orElse(PrintStage.BLOCKS);
        deferredBlocks.clear();
        view.read("DeferredBlocks", CreateCodecs.BLOCK_POS_LIST_CODEC).ifPresent(deferredBlocks::addAll);
    }

    public void write(ValueOutput view) {
        if (currentPos != null)
            view.store("CurrentPos", BlockPos.CODEC, currentPos);
        if (schematicAnchor != null)
            view.store("Anchor", BlockPos.CODEC, schematicAnchor);
        view.putInt("EntityProgress", printingEntityIndex);
        view.store("PrintStage", PrintStage.CODEC, printStage);
        view.store("DeferredBlocks", CreateCodecs.BLOCK_POS_LIST_CODEC, deferredBlocks);
    }

    public void loadSchematic(ItemStack blueprint, Level originalWorld, boolean processNBT) {
        if (!blueprint.has(AllDataComponents.SCHEMATIC_ANCHOR) || !blueprint.has(AllDataComponents.SCHEMATIC_DEPLOYED))
            return;

        StructureTemplate activeTemplate = SchematicItem.loadSchematic(originalWorld, blueprint);
        StructurePlaceSettings settings = SchematicItem.getSettings(blueprint, processNBT);

        schematicAnchor = blueprint.get(AllDataComponents.SCHEMATIC_ANCHOR);
        blockReader = new SchematicLevel(schematicAnchor, originalWorld);

        try {
            activeTemplate.placeInWorld(blockReader, schematicAnchor, schematicAnchor, settings, blockReader.getRandom(), Block.UPDATE_CLIENTS);
        } catch (Exception e) {
            Create.LOGGER.error("Failed to load Schematic for Printing", e);
            schematicLoaded = true;
            isErrored = true;
            return;
        }

        BlockPos extraBounds = StructureTemplate.calculateRelativePosition(settings, new BlockPos(activeTemplate.getSize()).offset(-1, -1, -1));
        blockReader.setBounds(BBHelper.encapsulate(blockReader.getBounds(), extraBounds));

        StructureTransform transform = new StructureTransform(
            settings.getRotationPivot(),
            Direction.Axis.Y,
            settings.getRotation(),
            settings.getMirror()
        );
        for (BlockEntity be : blockReader.getBlockEntities())
            transform.apply(be);

        printingEntityIndex = -1;
        printStage = PrintStage.BLOCKS;
        deferredBlocks.clear();
        BoundingBox bounds = blockReader.getBounds();
        currentPos = new BlockPos(bounds.minX() - 1, bounds.minY(), bounds.minZ());
        schematicLoaded = true;
    }

    public void resetSchematic() {
        schematicLoaded = false;
        schematicAnchor = null;
        isErrored = false;
        currentPos = null;
        blockReader = null;
        printingEntityIndex = -1;
        printStage = PrintStage.BLOCKS;
        deferredBlocks.clear();
    }

    public boolean isLoaded() {
        return schematicLoaded;
    }

    public boolean isErrored() {
        return isErrored;
    }

    @Nullable
    public BlockPos getCurrentTarget() {
        if (!isLoaded() || isErrored())
            return null;
        return schematicAnchor.offset(currentPos);
    }

    public PrintStage getPrintStage() {
        return printStage;
    }

    @Nullable
    public BlockPos getAnchor() {
        return schematicAnchor;
    }

    public boolean isWorldEmpty() {
        return blockReader.getAllPositions().isEmpty();
        //return blockReader.getBounds().getLength().equals(new Vector3i(0,0,0));
    }

    @FunctionalInterface
    public interface BlockTargetHandler {
        void handle(BlockPos target, BlockState blockState, @Nullable BlockEntity blockEntity);
    }

    @FunctionalInterface
    public interface EntityTargetHandler {
        void handle(BlockPos target, Entity entity);
    }

    public void handleCurrentTarget(BlockTargetHandler blockHandler, EntityTargetHandler entityHandler) {
        BlockPos target = getCurrentTarget();

        if (printStage == PrintStage.ENTITIES) {
            Entity entity = blockReader.getEntityList().get(printingEntityIndex);
            entityHandler.handle(target, entity);
        } else {
            BlockState blockState = BlockHelper.setZeroAge(blockReader.getBlockState(target));
            BlockEntity blockEntity = blockReader.getBlockEntity(target);
            blockHandler.handle(target, blockState, blockEntity);
        }
    }

    @FunctionalInterface
    public interface PlacementPredicate {
        boolean shouldPlace(
            BlockPos target,
            BlockState blockState,
            @Nullable BlockEntity blockEntity,
            BlockState toReplace,
            @Nullable BlockState toReplaceOther,
            boolean isNormalCube
        );
    }

    public boolean shouldPlaceCurrent(Level world) {
        return shouldPlaceCurrent(world, (a, b, c, d, e, f) -> true);
    }

    public boolean shouldPlaceCurrent(@Nullable Level world, PlacementPredicate predicate) {
        if (world == null)
            return false;

        if (printStage == PrintStage.ENTITIES)
            return true;

        return shouldPlaceBlock(world, predicate, getCurrentTarget());
    }

    public boolean shouldPlaceBlock(Level world, PlacementPredicate predicate, BlockPos pos) {
        BlockState state = BlockHelper.setZeroAge(blockReader.getBlockState(pos));
        BlockEntity blockEntity = blockReader.getBlockEntity(pos);

        BlockState toReplace = world.getBlockState(pos);
        BlockEntity toReplaceBE = world.getBlockEntity(pos);
        BlockState toReplaceOther = null;

        if (state.hasProperty(BlockStateProperties.BED_PART) && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && state.getValue(
            BlockStateProperties.BED_PART) == BedPart.FOOT)
            toReplaceOther = world.getBlockState(pos.relative(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.LOWER)
            toReplaceOther = world.getBlockState(pos.above());

        boolean mergeTEs = blockEntity != null && toReplaceBE instanceof IMergeableBE && toReplaceBE.getType().equals(blockEntity.getType());

        if (!world.isLoaded(pos))
            return false;
        if (!world.getWorldBorder().isWithinBounds(pos))
            return false;
        if (toReplace == state && !mergeTEs)
            return false;
        if (toReplace.getDestroySpeed(world, pos) == -1 || (toReplaceOther != null && toReplaceOther.getDestroySpeed(world, pos) == -1))
            return false;

        boolean isNormalCube = state.isRedstoneConductor(blockReader, currentPos);
        return predicate.shouldPlace(pos, state, blockEntity, toReplace, toReplaceOther, isNormalCube);
    }

    public ItemRequirement getCurrentRequirement() {
        if (printStage == PrintStage.ENTITIES)
            return ItemRequirement.of(blockReader.getEntityList().get(printingEntityIndex));

        BlockPos target = getCurrentTarget();
        BlockState blockState = BlockHelper.setZeroAge(blockReader.getBlockState(target));
        BlockEntity blockEntity = null;
        if (blockState.hasBlockEntity()) {
            blockEntity = ((EntityBlock) blockState.getBlock()).newBlockEntity(target, blockState);
            CompoundTag data = BlockHelper.prepareBlockEntityData(blockReader, blockState, blockReader.getBlockEntity(target));
            if (blockEntity != null && data != null) {
                try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(blockEntity.problemPath(), Create.LOGGER)) {
                    blockEntity.loadWithComponents(TagValueInput.create(logging, blockReader.registryAccess(), data));
                }
            }
        }
        return ItemRequirement.of(blockState, blockEntity);
    }

    public int markAllBlockRequirements(MaterialChecklist checklist, Level world, PlacementPredicate predicate) {
        int blocksToPlace = 0;
        for (BlockPos pos : blockReader.getAllPositions()) {
            BlockPos relPos = pos.offset(schematicAnchor);
            BlockState required = blockReader.getBlockState(relPos);
            BlockEntity requiredBE = blockReader.getBlockEntity(relPos);

            if (!world.isLoaded(pos.offset(schematicAnchor))) {
                checklist.warnBlockNotLoaded();
                continue;
            }
            if (!shouldPlaceBlock(world, predicate, relPos))
                continue;
            ItemRequirement requirement = ItemRequirement.of(required, requiredBE);
            if (requirement.isEmpty())
                continue;
            if (requirement.isInvalid())
                continue;
            checklist.require(requirement);
            blocksToPlace++;
        }
        return blocksToPlace;
    }

    public void markAllEntityRequirements(MaterialChecklist checklist) {
        for (Entity entity : blockReader.getEntityList()) {
            ItemRequirement requirement = ItemRequirement.of(entity);
            if (requirement.isEmpty())
                return;
            if (requirement.isInvalid())
                return;
            checklist.require(requirement);
        }
    }

    public boolean advanceCurrentPos() {
        List<Entity> entities = blockReader.getEntityList();

        do {
            if (printStage == PrintStage.BLOCKS) {
                while (tryAdvanceCurrentPos()) {
                    deferredBlocks.add(currentPos);
                }
            }

            if (printStage == PrintStage.DEFERRED_BLOCKS) {
                if (deferredBlocks.isEmpty()) {
                    printStage = PrintStage.ENTITIES;
                } else {
                    currentPos = deferredBlocks.removeFirst();
                }
            }

            if (printStage == PrintStage.ENTITIES) {
                if (printingEntityIndex + 1 < entities.size()) {
                    printingEntityIndex++;
                    currentPos = entities.get(printingEntityIndex).blockPosition().subtract(schematicAnchor);
                } else {
                    // Reached end of printing
                    return false;
                }
            }
        } while (!blockReader.getBounds().isInside(currentPos));

        // More things available to print
        return true;
    }

    public boolean tryAdvanceCurrentPos() {
        currentPos = currentPos.relative(Direction.EAST);
        BoundingBox bounds = blockReader.getBounds();
        BlockPos posInBounds = currentPos.offset(-bounds.minX(), -bounds.minY(), -bounds.minZ());

        if (posInBounds.getX() > bounds.getXSpan())
            currentPos = new BlockPos(bounds.minX(), currentPos.getY(), currentPos.getZ() + 1).west();
        if (posInBounds.getZ() > bounds.getZSpan())
            currentPos = new BlockPos(currentPos.getX(), currentPos.getY() + 1, bounds.minZ()).west();

        // End of blocks reached
        if (currentPos.getY() > bounds.getYSpan()) {
            printStage = PrintStage.DEFERRED_BLOCKS;
            return false;
        }

        return shouldDeferBlock(blockReader.getBlockState(getCurrentTarget()));
    }

    public static boolean shouldDeferBlock(BlockState state) {
        return state.is(AllBlocks.GANTRY_CARRIAGE) || state.is(AllBlocks.MECHANICAL_ARM) || BlockMovementChecks.isBrittle(state);
    }

    public void sendBlockUpdates(Level level) {
        BoundingBox bounds = blockReader.getBounds();
        BlockPos.betweenClosedStream(bounds.inflatedBy(1)).filter(pos -> !bounds.isInside(pos))
            .filter(pos -> level.isLoaded(pos.offset(schematicAnchor)) && level.getFluidState(pos.offset(schematicAnchor)).is(Fluids.WATER))
            .forEach(pos -> level.scheduleTick(pos.offset(schematicAnchor), Fluids.WATER, Fluids.WATER.getTickDelay(level)));
    }

}
