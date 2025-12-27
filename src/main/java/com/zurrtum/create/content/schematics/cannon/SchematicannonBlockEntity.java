package com.zurrtum.create.content.schematics.cannon;

import com.mojang.serialization.Codec;
import com.zurrtum.create.*;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import com.zurrtum.create.content.kinetics.simpleRelays.AbstractSimpleShaftBlock;
import com.zurrtum.create.content.schematics.SchematicPrinter;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement.ItemUseType;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.component.SchematicannonOptions;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CSchematics;
import com.zurrtum.create.infrastructure.packet.c2s.ConfigureSchematicannonPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class SchematicannonBlockEntity extends SmartBlockEntity implements MenuProvider, Clearable {

    public static final int NEIGHBOUR_CHECKING = 100;
    public static final int MAX_ANCHOR_DISTANCE = 256;

    // Inventory
    public SchematicannonInventory inventory;

    public boolean sendUpdate;
    // Sync
    public boolean dontUpdateChecklist;
    public int neighbourCheckCooldown;

    // Printer
    public SchematicPrinter printer;
    public ItemStack missingItem;
    public boolean positionNotLoaded;
    public boolean hasCreativeCrate;
    private int printerCooldown;
    private int skipsLeft;
    private boolean blockSkipped;

    public BlockPos previousTarget;
    public LinkedHashSet<Container> attachedInventories;
    public List<LaunchedItem> flyingBlocks;
    public MaterialChecklist checklist;

    // Gui information
    public int remainingFuel;
    public float bookPrintingProgress;
    public float schematicProgress;
    public String statusMsg;
    public State state;
    public int blocksPlaced;
    public int blocksToPlace;

    // Settings
    public int replaceMode;
    public boolean skipMissing;
    public boolean replaceBlockEntities;

    // Render
    public boolean firstRenderTick;
    public float defaultYaw;

    public SchematicannonBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SCHEMATICANNON, pos, state);
        setLazyTickRate(30);
        attachedInventories = new LinkedHashSet<>();
        flyingBlocks = new LinkedList<>();
        inventory = new SchematicannonInventory(this);
        statusMsg = "idle";
        this.state = State.STOPPED;
        replaceMode = 2;
        checklist = new MaterialChecklist();
        printer = new SchematicPrinter();
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        Containers.dropContents(level, pos, inventory);
    }

    public void findInventories() {
        hasCreativeCrate = false;
        attachedInventories.clear();
        for (Direction facing : Iterate.directions) {

            BlockPos target = worldPosition.relative(facing);
            if (!level.isLoaded(target))
                continue;

            BlockState state = level.getBlockState(target);
            if (state.is(AllBlocks.CREATIVE_CRATE))
                hasCreativeCrate = true;

            BlockEntity blockEntity = level.getBlockEntity(target);
            if (blockEntity != null) {
                Container capability = ItemHelper.getInventory(level, target, state, blockEntity, facing);
                if (capability != null) {
                    attachedInventories.add(capability);
                }
            }
        }
    }

    @Override
    public void clearContent() {
        inventory.clearContent();
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        if (!clientPacket) {
            inventory.read(view);
        }

        // Gui information
        statusMsg = view.getStringOr("Status", "");
        schematicProgress = view.getFloatOr("Progress", 0);
        bookPrintingProgress = view.getFloatOr("PaperProgress", 0);
        remainingFuel = view.getIntOr("RemainingFuel", 0);
        state = view.read("State", State.CODEC).orElse(State.STOPPED);
        blocksPlaced = view.getIntOr("AmountPlaced", 0);
        blocksToPlace = view.getIntOr("AmountToPlace", 0);

        missingItem = null;
        view.read("MissingItem", ItemStack.OPTIONAL_CODEC).ifPresent(item -> missingItem = item);

        // Settings
        view.read("Options", SchematicannonOptions.CODEC).ifPresentOrElse(
            options -> {
                replaceMode = options.replaceMode();
                skipMissing = options.skipMissing();
                replaceBlockEntities = options.replaceBlockEntities();
            }, () -> {
                replaceMode = 2;
                skipMissing = false;
                replaceBlockEntities = false;
            }
        );

        // Printer & Flying Blocks
        view.child("Printer").ifPresent(data -> printer.read(data, clientPacket));
        view.childrenList("FlyingBlocks").ifPresent(this::readFlyingBlocks);

        defaultYaw = view.getFloatOr("DefaultYaw", 0);

        super.read(view, clientPacket);
    }

    protected void readFlyingBlocks(ValueInput.ValueInputList list) {
        if (list.isEmpty()) {
            flyingBlocks.clear();
            return;
        }

        boolean pastDead = false;
        int i = -1;
        for (ValueInput item : list) {
            i++;
            LaunchedItem launched = LaunchedItem.from(item, blockHolderGetter());
            BlockPos readBlockPos = launched.target;

            // Always write to Server block entity
            if (level == null || !level.isClientSide()) {
                flyingBlocks.add(launched);
                continue;
            }

            // Delete all Client side blocks that are now missing on the server
            while (!pastDead && !flyingBlocks.isEmpty() && !flyingBlocks.getFirst().target.equals(readBlockPos)) {
                flyingBlocks.removeFirst();
            }

            pastDead = true;

            // Add new server side blocks
            if (i >= flyingBlocks.size()) {
                flyingBlocks.add(launched);
            }

            // Don't do anything with existing
        }
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        if (!clientPacket) {
            inventory.write(view);
            if (state == State.RUNNING) {
                view.putBoolean("Running", true);
            }
        }

        // Gui information
        view.putFloat("Progress", schematicProgress);
        view.putFloat("PaperProgress", bookPrintingProgress);
        view.putInt("RemainingFuel", remainingFuel);
        view.putString("Status", statusMsg);
        view.store("State", State.CODEC, state);
        view.putInt("AmountPlaced", blocksPlaced);
        view.putInt("AmountToPlace", blocksToPlace);

        if (missingItem != null)
            view.store("MissingItem", ItemStack.OPTIONAL_CODEC, missingItem);

        // Settings
        view.store("Options", SchematicannonOptions.CODEC, new SchematicannonOptions(replaceMode, skipMissing, replaceBlockEntities));

        // Printer & Flying Blocks
        printer.write(view.child("Printer"));

        ValueOutput.ValueOutputList blocks = view.childrenList("FlyingBlocks");
        for (LaunchedItem b : flyingBlocks)
            b.write(blocks.addChild());

        view.putFloat("DefaultYaw", defaultYaw);

        super.write(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        if (state != State.STOPPED && neighbourCheckCooldown-- <= 0) {
            neighbourCheckCooldown = NEIGHBOUR_CHECKING;
            findInventories();
        }

        firstRenderTick = true;
        previousTarget = printer.getCurrentTarget();
        tickFlyingBlocks();

        if (level.isClientSide())
            return;

        // Update Fuel and Paper
        tickPaperPrinter();
        refillFuelIfPossible();

        // Update Printer
        skipsLeft = 1000;
        blockSkipped = true;

        while (blockSkipped && skipsLeft-- > 0)
            tickPrinter();

        schematicProgress = 0;
        if (blocksToPlace > 0)
            schematicProgress = (float) blocksPlaced / blocksToPlace;

        // Update Client block entity
        if (sendUpdate) {
            sendUpdate = false;
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 6);
        }
    }

    public CSchematics config() {
        return AllConfigs.server().schematics;
    }

    protected void tickPrinter() {
        ItemStack blueprint = inventory.getItem(0);
        blockSkipped = false;

        if (blueprint.isEmpty() && !statusMsg.equals("idle") && inventory.getItem(1).isEmpty()) {
            state = State.STOPPED;
            statusMsg = "idle";
            sendUpdate = true;
            return;
        }

        // Skip if not Active
        if (state == State.STOPPED) {
            if (printer.isLoaded())
                resetPrinter();
            return;
        }

        if (state == State.PAUSED && !positionNotLoaded && missingItem == null && remainingFuel > 0)
            return;

        // Initialize Printer
        if (!printer.isLoaded()) {
            initializePrinter(blueprint);
            return;
        }

        // Cooldown from last shot
        if (printerCooldown > 0) {
            printerCooldown--;
            return;
        }

        // Check Fuel
        if (remainingFuel <= 0 && !hasCreativeCrate) {
            refillFuelIfPossible();
            if (remainingFuel <= 0) {
                state = State.PAUSED;
                statusMsg = "noGunpowder";
                sendUpdate = true;
                return;
            }
        }

        if (hasCreativeCrate) {
            remainingFuel = 0;
            if (missingItem != null) {
                missingItem = null;
                state = State.RUNNING;
            }
        }

        // Update Target
        if (missingItem == null && !positionNotLoaded) {
            if (!printer.advanceCurrentPos()) {
                finishedPrinting();
                return;
            }
            sendUpdate = true;
        }

        // Check block
        if (!level.isLoaded(printer.getCurrentTarget())) {
            positionNotLoaded = true;
            statusMsg = "targetNotLoaded";
            state = State.PAUSED;
            return;
        } else {
            if (positionNotLoaded) {
                positionNotLoaded = false;
                state = State.RUNNING;
            }
        }

        // Get item requirement
        ItemRequirement requirement = printer.getCurrentRequirement();
        if (requirement.isInvalid() || !printer.shouldPlaceCurrent(level, this::shouldPlace)) {
            sendUpdate = !statusMsg.equals("searching");
            statusMsg = "searching";
            blockSkipped = true;
            return;
        }

        // Find item
        List<ItemRequirement.StackRequirement> requiredItems = requirement.getRequiredItems();
        if (!requirement.isEmpty()) {
            for (ItemRequirement.StackRequirement required : requiredItems) {
                if (!grabItemsFromAttachedInventories(required, true)) {
                    if (skipMissing) {
                        statusMsg = "skipping";
                        blockSkipped = true;
                        if (missingItem != null) {
                            missingItem = null;
                            state = State.RUNNING;
                        }
                        return;
                    }

                    missingItem = required.stack;
                    state = State.PAUSED;
                    statusMsg = "missingBlock";
                    return;
                }
            }

            for (ItemRequirement.StackRequirement required : requiredItems)
                grabItemsFromAttachedInventories(required, false);
        }

        // Success
        state = State.RUNNING;
        ItemStack icon = requirement.isEmpty() || requiredItems.isEmpty() ? ItemStack.EMPTY : requiredItems.get(0).stack;
        printer.handleCurrentTarget(
            (target, blockState, blockEntity) -> {
                // Launch block
                statusMsg = blockState.getBlock() != Blocks.AIR ? "placing" : "clearing";
                launchBlockOrBelt(target, icon, blockState, blockEntity);
            }, (target, entity) -> {
                // Launch entity
                statusMsg = "placing";
                launchEntity(target, icon, entity);
            }
        );

        printerCooldown = config().schematicannonDelay.get();
        remainingFuel -= 1;
        sendUpdate = true;
        missingItem = null;
    }

    public int getShotsPerGunpowder() {
        return hasCreativeCrate ? 0 : config().schematicannonShotsPerGunpowder.get();
    }

    protected void initializePrinter(ItemStack blueprint) {
        if (!blueprint.has(AllDataComponents.SCHEMATIC_ANCHOR)) {
            state = State.STOPPED;
            statusMsg = "schematicInvalid";
            sendUpdate = true;
            return;
        }

        if (!blueprint.getOrDefault(AllDataComponents.SCHEMATIC_DEPLOYED, false)) {
            state = State.STOPPED;
            statusMsg = "schematicNotPlaced";
            sendUpdate = true;
            return;
        }

        // Load blocks into reader
        printer.loadSchematic(blueprint, level, true);

        if (printer.isErrored()) {
            state = State.STOPPED;
            statusMsg = "schematicErrored";
            inventory.setItem(0, ItemStack.EMPTY);
            inventory.setItem(1, AllItems.EMPTY_SCHEMATIC.getDefaultInstance());
            printer.resetSchematic();
            sendUpdate = true;
            return;
        }

        if (printer.isWorldEmpty()) {
            state = State.STOPPED;
            statusMsg = "schematicExpired";
            inventory.setItem(0, ItemStack.EMPTY);
            inventory.setItem(1, AllItems.EMPTY_SCHEMATIC.getDefaultInstance());
            printer.resetSchematic();
            sendUpdate = true;
            return;
        }

        if (!printer.getAnchor().closerThan(getBlockPos(), MAX_ANCHOR_DISTANCE)) {
            state = State.STOPPED;
            statusMsg = "targetOutsideRange";
            printer.resetSchematic();
            sendUpdate = true;
            return;
        }

        state = State.PAUSED;
        statusMsg = "ready";
        updateChecklist();
        sendUpdate = true;
        blocksToPlace += blocksPlaced;
    }

    protected ItemStack getItemForBlock(BlockState blockState) {
        Item item = blockState.getBlock().asItem();
        return item == Items.AIR ? ItemStack.EMPTY : item.getDefaultInstance();
    }

    protected boolean grabItemsFromAttachedInventories(ItemRequirement.StackRequirement required, boolean simulate) {
        if (hasCreativeCrate)
            return true;

        attachedInventories.removeIf(Objects::isNull);

        ItemUseType usage = required.usage;

        // Find and apply damage
        if (usage == ItemUseType.DAMAGE) {
            for (Container cap : attachedInventories) {
                if (cap == null) {
                    continue;
                }
                if (simulate) {
                    if (!cap.count(stack -> required.matches(stack) && stack.isDamageableItem(), 1).isEmpty()) {
                        return true;
                    }
                } else {
                    if (cap.update(
                        stack -> required.matches(stack) && stack.isDamageableItem(), stack -> {
                            int damage = stack.getDamageValue() + 1;
                            int maxDamage = stack.getMaxDamage();
                            if (damage >= maxDamage) {
                                return ItemStack.EMPTY;
                            }
                            stack.setDamageValue(damage);
                            return stack;
                        }
                    )) {
                        return true;
                    }
                }
            }

            return false;
        }

        // Find and remove
        int remaining = required.stack.getCount();
        for (Container cap : attachedInventories) {
            if (cap == null) {
                continue;
            }
            remaining -= cap.countAll(required::matches, remaining);
            if (remaining == 0) {
                break;
            }
        }

        boolean success = remaining == 0;
        if (!simulate && success) {
            remaining = required.stack.getCount();
            for (Container cap : attachedInventories) {
                if (cap == null) {
                    continue;
                }
                remaining -= cap.extractAll(required::matches, remaining);
                if (remaining == 0) {
                    break;
                }
            }
        }

        return success;
    }

    public void finishedPrinting() {
        if (replaceMode == ConfigureSchematicannonPacket.Option.REPLACE_EMPTY.ordinal())
            printer.sendBlockUpdates(level);
        inventory.setItem(0, ItemStack.EMPTY);
        inventory.setItem(1, new ItemStack(AllItems.EMPTY_SCHEMATIC, inventory.getItem(1).getCount() + 1));
        state = State.STOPPED;
        statusMsg = "finished";
        resetPrinter();
        AllSoundEvents.SCHEMATICANNON_FINISH.playOnServer(level, worldPosition);
        sendUpdate = true;
    }

    protected void resetPrinter() {
        printer.resetSchematic();
        missingItem = null;
        sendUpdate = true;
        schematicProgress = 0;
        blocksPlaced = 0;
        blocksToPlace = 0;
    }

    protected boolean shouldPlace(
        BlockPos pos,
        BlockState state,
        BlockEntity be,
        BlockState toReplace,
        BlockState toReplaceOther,
        boolean isNormalCube
    ) {
        if (pos.closerThan(getBlockPos(), 2f))
            return false;
        if (!replaceBlockEntities && (toReplace.hasBlockEntity() || (toReplaceOther != null && toReplaceOther.hasBlockEntity())))
            return false;

        if (shouldIgnoreBlockState(state, be))
            return false;

        boolean placingAir = state.isAir();

        if (replaceMode == 3)
            return true;
        if (replaceMode == 2 && !placingAir)
            return true;
        if (replaceMode == 1 && (isNormalCube || (!toReplace.isRedstoneConductor(
            level,
            pos
        ) && (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(level, pos)))) && !placingAir)
            return true;
        return replaceMode == 0 && !toReplace.isRedstoneConductor(level, pos) && (toReplaceOther == null || !toReplaceOther.isRedstoneConductor(
            level,
            pos
        )) && !placingAir;
    }

    protected boolean shouldIgnoreBlockState(BlockState state, BlockEntity be) {
        // Block doesn't have a mapping (Water, lava, etc)
        if (state.getBlock() == Blocks.STRUCTURE_VOID)
            return true;

        ItemRequirement requirement = ItemRequirement.of(state, be);
        if (requirement.isEmpty())
            return false;
        if (requirement.isInvalid())
            return false;

        // Block doesn't need to be placed twice (Doors, beds, double plants)
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
            return true;
        if (state.hasProperty(BlockStateProperties.BED_PART) && state.getValue(BlockStateProperties.BED_PART) == BedPart.HEAD)
            return true;
        if (state.getBlock() instanceof PistonHeadBlock)
            return true;
        if (state.is(AllBlocks.BELT))
            return state.getValue(BeltBlock.PART) == BeltPart.MIDDLE;

        return false;
    }

    protected void tickFlyingBlocks() {
        List<LaunchedItem> toRemove = new LinkedList<>();
        for (LaunchedItem b : flyingBlocks)
            if (b.update(level))
                toRemove.add(b);
        flyingBlocks.removeAll(toRemove);
    }

    protected void refillFuelIfPossible() {
        if (hasCreativeCrate)
            return;
        if (remainingFuel > getShotsPerGunpowder()) {
            remainingFuel = getShotsPerGunpowder();
            sendUpdate = true;
            return;
        }

        if (remainingFuel > 0)
            return;

        ItemStack gunpowder = inventory.getItem(4);
        if (!gunpowder.isEmpty()) {
            gunpowder.shrink(1);
        } else {
            boolean externalGunpowderFound = false;
            for (Container cap : attachedInventories) {
                if (cap == null) {
                    continue;
                }
                if (cap.extractAll(stack -> inventory.canPlaceItem(4, stack), 1) == 0)
                    continue;
                externalGunpowderFound = true;
                break;
            }
            if (!externalGunpowderFound)
                return;
        }

        remainingFuel += getShotsPerGunpowder();
        if (statusMsg.equals("noGunpowder")) {
            if (blocksPlaced > 0)
                state = State.RUNNING;
            statusMsg = "ready";
        }
        sendUpdate = true;
    }

    protected void tickPaperPrinter() {
        int BookInput = 2;
        int BookOutput = 3;

        ItemStack blueprint = inventory.getItem(0);
        ItemStack paper = inventory.getItem(BookInput);
        ItemStack output = inventory.getItem(BookOutput);
        boolean outputFull = output.getCount() == output.getMaxStackSize();

        if (printer.isErrored())
            return;

        if (!printer.isLoaded()) {
            if (!blueprint.isEmpty())
                initializePrinter(blueprint);
            return;
        }

        if (paper.isEmpty() || outputFull) {
            if (bookPrintingProgress != 0)
                sendUpdate = true;
            bookPrintingProgress = 0;
            dontUpdateChecklist = false;
            return;
        }

        if (bookPrintingProgress >= 1) {
            bookPrintingProgress = 0;

            if (!dontUpdateChecklist)
                updateChecklist();

            dontUpdateChecklist = true;
            inventory.setItem(BookInput, ItemStack.EMPTY);
            ItemStack stack = paper.is(AllItems.CLIPBOARD) ? checklist.createWrittenClipboard() : checklist.createWrittenBook();
            stack.setCount(inventory.getItem(BookOutput).getCount() + 1);
            inventory.setItem(BookOutput, stack);
            inventory.setChanged();
            sendUpdate = true;
            return;
        }

        bookPrintingProgress += 0.05f;
        sendUpdate = true;
    }

    public static BlockState stripBeltIfNotLast(BlockState blockState) {
        BeltPart part = blockState.getValue(BeltBlock.PART);
        if (part == BeltPart.MIDDLE)
            return Blocks.AIR.defaultBlockState();

        // is highest belt?
        boolean isLastSegment = false;
        Direction facing = blockState.getValue(BeltBlock.HORIZONTAL_FACING);
        BeltSlope slope = blockState.getValue(BeltBlock.SLOPE);
        boolean positive = facing.getAxisDirection() == AxisDirection.POSITIVE;
        boolean start = part == BeltPart.START;
        boolean end = part == BeltPart.END;

        switch (slope) {
            case DOWNWARD:
                isLastSegment = start;
                break;
            case UPWARD:
                isLastSegment = end;
                break;
            default:
                isLastSegment = positive && end || !positive && start;
        }
        if (isLastSegment)
            return blockState;

        return AllBlocks.SHAFT.defaultBlockState()
            .setValue(AbstractSimpleShaftBlock.AXIS, slope == BeltSlope.SIDEWAYS ? Axis.Y : facing.getClockWise().getAxis());
    }

    protected void launchBlockOrBelt(BlockPos target, ItemStack icon, BlockState blockState, BlockEntity blockEntity) {
        if (blockState.is(AllBlocks.BELT)) {
            blockState = stripBeltIfNotLast(blockState);
            if (blockEntity instanceof BeltBlockEntity bbe && blockState.is(AllBlocks.BELT)) {
                CasingType[] casings = new CasingType[bbe.beltLength];
                Arrays.fill(casings, CasingType.NONE);
                BlockPos currentPos = target;
                for (int i = 0; i < bbe.beltLength; i++) {
                    BlockState currentState = bbe.getLevel().getBlockState(currentPos);
                    if (!(currentState.getBlock() instanceof BeltBlock))
                        break;
                    if (!(bbe.getLevel().getBlockEntity(currentPos) instanceof BeltBlockEntity beltAtSegment))
                        break;
                    casings[i] = beltAtSegment.casing;
                    currentPos = BeltBlock.nextSegmentPosition(currentState, currentPos, blockState.getValue(BeltBlock.PART) != BeltPart.END);
                }
                launchBelt(target, blockState, bbe.beltLength, casings);
            } else if (blockState != Blocks.AIR.defaultBlockState())
                launchBlock(target, icon, blockState, null);
            return;
        }

        CompoundTag data = BlockHelper.prepareBlockEntityData(level, blockState, blockEntity);
        launchBlock(target, icon, blockState, data);
    }

    protected void launchBelt(BlockPos target, BlockState state, int length, CasingType[] casings) {
        blocksPlaced++;
        ItemStack connector = AllItems.BELT_CONNECTOR.getDefaultInstance();
        flyingBlocks.add(new LaunchedItem.ForBelt(getBlockPos(), target, connector, state, casings));
        playFiringSound();
    }

    protected void launchBlock(BlockPos target, ItemStack stack, BlockState state, @Nullable CompoundTag data) {
        if (!state.isAir())
            blocksPlaced++;
        flyingBlocks.add(new LaunchedItem.ForBlockState(getBlockPos(), target, stack, state, data));
        playFiringSound();
    }

    protected void launchEntity(BlockPos target, ItemStack stack, Entity entity) {
        blocksPlaced++;
        flyingBlocks.add(new LaunchedItem.ForEntity(getBlockPos(), target, stack, entity));
        playFiringSound();
    }

    public void playFiringSound() {
        AllSoundEvents.SCHEMATICANNON_LAUNCH_BLOCK.playOnServer(level, worldPosition);
    }

    @Override
    public SchematicannonMenu createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        sendToMenu(extraData);
        return new SchematicannonMenu(id, inv, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("create.gui.schematicannon.title");
    }

    public void updateChecklist() {
        checklist.required.clear();
        checklist.damageRequired.clear();
        checklist.blocksNotLoaded = false;

        if (printer.isLoaded() && !printer.isErrored()) {
            blocksToPlace = blocksPlaced;
            blocksToPlace += printer.markAllBlockRequirements(checklist, level, this::shouldPlace);
            printer.markAllEntityRequirements(checklist);
        }

        checklist.gathered.clear();
        findInventories();
        for (Container cap : attachedInventories) {
            if (cap == null)
                continue;
            for (ItemStack stack : cap) {
                checklist.collect(stack);
            }
        }
        sendUpdate = true;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        findInventories();
    }

    //TODO
    //    @Override
    //    public AABB getRenderBoundingBox() {
    //        return AABB.INFINITE;
    //    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        SchematicannonOptions options = componentInput.getOrDefault(
            AllDataComponents.SCHEMATICANNON_OPTIONS,
            new SchematicannonOptions(2, true, false)
        );
        replaceMode = options.replaceMode();
        skipMissing = options.skipMissing();
        replaceBlockEntities = options.replaceBlockEntities();
    }

    @Override
    protected void collectImplicitComponents(Builder components) {
        components.set(AllDataComponents.SCHEMATICANNON_OPTIONS, new SchematicannonOptions(replaceMode, skipMissing, replaceBlockEntities));
    }

    public enum State implements StringRepresentable {
        STOPPED,
        PAUSED,
        RUNNING;

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
