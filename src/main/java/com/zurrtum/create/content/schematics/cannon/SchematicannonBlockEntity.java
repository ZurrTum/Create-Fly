package com.zurrtum.create.content.schematics.cannon;

import com.mojang.serialization.Codec;
import com.zurrtum.create.*;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
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
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.utility.BlockHelper;
import com.zurrtum.create.infrastructure.component.SchematicannonOptions;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.config.CSchematics;
import com.zurrtum.create.infrastructure.packet.c2s.ConfigureSchematicannonPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.PistonHeadBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.BedPart;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.component.ComponentMap.Builder;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Direction.AxisDirection;
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
    public LinkedHashSet<Inventory> attachedInventories;
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
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        ItemScatterer.spawn(world, pos, inventory);
    }

    public void findInventories() {
        hasCreativeCrate = false;
        attachedInventories.clear();
        for (Direction facing : Iterate.directions) {

            BlockPos target = pos.offset(facing);
            if (!world.isPosLoaded(target))
                continue;

            BlockState state = world.getBlockState(target);
            if (state.isOf(AllBlocks.CREATIVE_CRATE))
                hasCreativeCrate = true;

            BlockEntity blockEntity = world.getBlockEntity(target);
            if (blockEntity != null) {
                Inventory capability = ItemHelper.getInventory(world, target, state, blockEntity, facing);
                if (capability != null) {
                    attachedInventories.add(capability);
                }
            }
        }
    }

    @Override
    public void clear() {
        inventory.clear();
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        if (!clientPacket) {
            inventory.read(view);
        }

        // Gui information
        statusMsg = view.getString("Status", "");
        schematicProgress = view.getFloat("Progress", 0);
        bookPrintingProgress = view.getFloat("PaperProgress", 0);
        remainingFuel = view.getInt("RemainingFuel", 0);
        state = view.read("State", State.CODEC).orElse(State.STOPPED);
        blocksPlaced = view.getInt("AmountPlaced", 0);
        blocksToPlace = view.getInt("AmountToPlace", 0);

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
        view.getOptionalReadView("Printer").ifPresent(data -> printer.read(data, clientPacket));
        view.getOptionalListReadView("FlyingBlocks").ifPresent(this::readFlyingBlocks);

        defaultYaw = view.getFloat("DefaultYaw", 0);

        super.read(view, clientPacket);
    }

    protected void readFlyingBlocks(ReadView.ListReadView list) {
        if (list.isEmpty()) {
            flyingBlocks.clear();
            return;
        }

        boolean pastDead = false;
        int i = -1;
        for (ReadView item : list) {
            i++;
            LaunchedItem launched = LaunchedItem.from(item, blockHolderGetter());
            BlockPos readBlockPos = launched.target;

            // Always write to Server block entity
            if (world == null || !world.isClient()) {
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
    public void write(WriteView view, boolean clientPacket) {
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
        view.put("State", State.CODEC, state);
        view.putInt("AmountPlaced", blocksPlaced);
        view.putInt("AmountToPlace", blocksToPlace);

        if (missingItem != null)
            view.put("MissingItem", ItemStack.OPTIONAL_CODEC, missingItem);

        // Settings
        view.put("Options", SchematicannonOptions.CODEC, new SchematicannonOptions(replaceMode, skipMissing, replaceBlockEntities));

        // Printer & Flying Blocks
        printer.write(view.get("Printer"));

        WriteView.ListView blocks = view.getList("FlyingBlocks");
        for (LaunchedItem b : flyingBlocks)
            b.write(blocks.add());

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

        if (world.isClient())
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
            world.updateListeners(pos, getCachedState(), getCachedState(), 6);
        }
    }

    public CSchematics config() {
        return AllConfigs.server().schematics;
    }

    protected void tickPrinter() {
        ItemStack blueprint = inventory.getStack(0);
        blockSkipped = false;

        if (blueprint.isEmpty() && !statusMsg.equals("idle") && inventory.getStack(1).isEmpty()) {
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
        if (!world.isPosLoaded(printer.getCurrentTarget())) {
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
        if (requirement.isInvalid() || !printer.shouldPlaceCurrent(world, this::shouldPlace)) {
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
        if (!blueprint.contains(AllDataComponents.SCHEMATIC_ANCHOR)) {
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
        printer.loadSchematic(blueprint, world, true);

        if (printer.isErrored()) {
            state = State.STOPPED;
            statusMsg = "schematicErrored";
            inventory.setStack(0, ItemStack.EMPTY);
            inventory.setStack(1, AllItems.EMPTY_SCHEMATIC.getDefaultStack());
            printer.resetSchematic();
            sendUpdate = true;
            return;
        }

        if (printer.isWorldEmpty()) {
            state = State.STOPPED;
            statusMsg = "schematicExpired";
            inventory.setStack(0, ItemStack.EMPTY);
            inventory.setStack(1, AllItems.EMPTY_SCHEMATIC.getDefaultStack());
            printer.resetSchematic();
            sendUpdate = true;
            return;
        }

        if (!printer.getAnchor().isWithinDistance(getPos(), MAX_ANCHOR_DISTANCE)) {
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
        return item == Items.AIR ? ItemStack.EMPTY : item.getDefaultStack();
    }

    protected boolean grabItemsFromAttachedInventories(ItemRequirement.StackRequirement required, boolean simulate) {
        if (hasCreativeCrate)
            return true;

        attachedInventories.removeIf(Objects::isNull);

        ItemUseType usage = required.usage;

        // Find and apply damage
        if (usage == ItemUseType.DAMAGE) {
            for (Inventory cap : attachedInventories) {
                if (cap == null) {
                    continue;
                }
                if (simulate) {
                    if (!cap.count(stack -> required.matches(stack) && stack.isDamageable(), 1).isEmpty()) {
                        return true;
                    }
                } else {
                    if (cap.update(
                        stack -> required.matches(stack) && stack.isDamageable(), stack -> {
                            int damage = stack.getDamage() + 1;
                            int maxDamage = stack.getMaxDamage();
                            if (damage >= maxDamage) {
                                return ItemStack.EMPTY;
                            }
                            stack.setDamage(damage);
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
        for (Inventory cap : attachedInventories) {
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
            for (Inventory cap : attachedInventories) {
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
            printer.sendBlockUpdates(world);
        inventory.setStack(0, ItemStack.EMPTY);
        inventory.setStack(1, new ItemStack(AllItems.EMPTY_SCHEMATIC, inventory.getStack(1).getCount() + 1));
        state = State.STOPPED;
        statusMsg = "finished";
        resetPrinter();
        AllSoundEvents.SCHEMATICANNON_FINISH.playOnServer(world, pos);
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
        if (pos.isWithinDistance(getPos(), 2f))
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
        if (replaceMode == 1 && (isNormalCube || (!toReplace.isSolidBlock(
            world,
            pos
        ) && (toReplaceOther == null || !toReplaceOther.isSolidBlock(world, pos)))) && !placingAir)
            return true;
        return replaceMode == 0 && !toReplace.isSolidBlock(world, pos) && (toReplaceOther == null || !toReplaceOther.isSolidBlock(
            world,
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
        if (state.contains(Properties.DOUBLE_BLOCK_HALF) && state.get(Properties.DOUBLE_BLOCK_HALF) == DoubleBlockHalf.UPPER)
            return true;
        if (state.contains(Properties.BED_PART) && state.get(Properties.BED_PART) == BedPart.HEAD)
            return true;
        if (state.getBlock() instanceof PistonHeadBlock)
            return true;
        if (state.isOf(AllBlocks.BELT))
            return state.get(BeltBlock.PART) == BeltPart.MIDDLE;

        return false;
    }

    protected void tickFlyingBlocks() {
        List<LaunchedItem> toRemove = new LinkedList<>();
        for (LaunchedItem b : flyingBlocks)
            if (b.update(world))
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

        ItemStack gunpowder = inventory.getStack(4);
        if (!gunpowder.isEmpty()) {
            gunpowder.decrement(1);
        } else {
            boolean externalGunpowderFound = false;
            for (Inventory cap : attachedInventories) {
                if (cap == null) {
                    continue;
                }
                if (cap.extractAll(stack -> inventory.isValid(4, stack), 1) == 0)
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

        ItemStack blueprint = inventory.getStack(0);
        ItemStack paper = inventory.getStack(BookInput);
        ItemStack output = inventory.getStack(BookOutput);
        boolean outputFull = output.getCount() == output.getMaxCount();

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
            inventory.setStack(BookInput, ItemStack.EMPTY);
            ItemStack stack = paper.isOf(AllItems.CLIPBOARD) ? checklist.createWrittenClipboard() : checklist.createWrittenBook();
            stack.setCount(inventory.getStack(BookOutput).getCount() + 1);
            inventory.setStack(BookOutput, stack);
            inventory.markDirty();
            sendUpdate = true;
            return;
        }

        bookPrintingProgress += 0.05f;
        sendUpdate = true;
    }

    public static BlockState stripBeltIfNotLast(BlockState blockState) {
        BeltPart part = blockState.get(BeltBlock.PART);
        if (part == BeltPart.MIDDLE)
            return Blocks.AIR.getDefaultState();

        // is highest belt?
        boolean isLastSegment = false;
        Direction facing = blockState.get(BeltBlock.HORIZONTAL_FACING);
        BeltSlope slope = blockState.get(BeltBlock.SLOPE);
        boolean positive = facing.getDirection() == AxisDirection.POSITIVE;
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

        return AllBlocks.SHAFT.getDefaultState()
            .with(AbstractSimpleShaftBlock.AXIS, slope == BeltSlope.SIDEWAYS ? Axis.Y : facing.rotateYClockwise().getAxis());
    }

    protected void launchBlockOrBelt(BlockPos target, ItemStack icon, BlockState blockState, BlockEntity blockEntity) {
        if (blockState.isOf(AllBlocks.BELT)) {
            blockState = stripBeltIfNotLast(blockState);
            if (blockEntity instanceof BeltBlockEntity bbe && blockState.isOf(AllBlocks.BELT)) {
                CasingType[] casings = new CasingType[bbe.beltLength];
                Arrays.fill(casings, CasingType.NONE);
                BlockPos currentPos = target;
                for (int i = 0; i < bbe.beltLength; i++) {
                    BlockState currentState = bbe.getWorld().getBlockState(currentPos);
                    if (!(currentState.getBlock() instanceof BeltBlock))
                        break;
                    if (!(bbe.getWorld().getBlockEntity(currentPos) instanceof BeltBlockEntity beltAtSegment))
                        break;
                    casings[i] = beltAtSegment.casing;
                    currentPos = BeltBlock.nextSegmentPosition(currentState, currentPos, blockState.get(BeltBlock.PART) != BeltPart.END);
                }
                launchBelt(target, blockState, bbe.beltLength, casings);
            } else if (blockState != Blocks.AIR.getDefaultState())
                launchBlock(target, icon, blockState, null);
            return;
        }

        NbtCompound data = BlockHelper.prepareBlockEntityData(world, blockState, blockEntity);
        launchBlock(target, icon, blockState, data);
    }

    protected void launchBelt(BlockPos target, BlockState state, int length, CasingType[] casings) {
        blocksPlaced++;
        ItemStack connector = AllItems.BELT_CONNECTOR.getDefaultStack();
        flyingBlocks.add(new LaunchedItem.ForBelt(getPos(), target, connector, state, casings));
        playFiringSound();
    }

    protected void launchBlock(BlockPos target, ItemStack stack, BlockState state, @Nullable NbtCompound data) {
        if (!state.isAir())
            blocksPlaced++;
        flyingBlocks.add(new LaunchedItem.ForBlockState(getPos(), target, stack, state, data));
        playFiringSound();
    }

    protected void launchEntity(BlockPos target, ItemStack stack, Entity entity) {
        blocksPlaced++;
        flyingBlocks.add(new LaunchedItem.ForEntity(getPos(), target, stack, entity));
        playFiringSound();
    }

    public void playFiringSound() {
        AllSoundEvents.SCHEMATICANNON_LAUNCH_BLOCK.playOnServer(world, pos);
    }

    @Override
    public SchematicannonMenu createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
        sendToMenu(extraData);
        return new SchematicannonMenu(id, inv, this);
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("create.gui.schematicannon.title");
    }

    public void updateChecklist() {
        checklist.required.clear();
        checklist.damageRequired.clear();
        checklist.blocksNotLoaded = false;

        if (printer.isLoaded() && !printer.isErrored()) {
            blocksToPlace = blocksPlaced;
            blocksToPlace += printer.markAllBlockRequirements(checklist, world, this::shouldPlace);
            printer.markAllEntityRequirements(checklist);
        }

        checklist.gathered.clear();
        findInventories();
        for (Inventory cap : attachedInventories) {
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
    protected void readComponents(ComponentsAccess componentInput) {
        SchematicannonOptions options = componentInput.getOrDefault(
            AllDataComponents.SCHEMATICANNON_OPTIONS,
            new SchematicannonOptions(2, true, false)
        );
        replaceMode = options.replaceMode();
        skipMissing = options.skipMissing();
        replaceBlockEntities = options.replaceBlockEntities();
    }

    @Override
    protected void addComponents(Builder components) {
        components.add(AllDataComponents.SCHEMATICANNON_OPTIONS, new SchematicannonOptions(replaceMode, skipMissing, replaceBlockEntities));
    }

    public enum State implements StringIdentifiable {
        STOPPED,
        PAUSED,
        RUNNING;

        public static final Codec<State> CODEC = StringIdentifiable.createCodec(State::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
