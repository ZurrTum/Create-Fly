package com.zurrtum.create.content.kinetics.crafter;

import com.zurrtum.create.*;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.kinetics.crafter.ConnectedInputHandler.ConnectedInput;
import com.zurrtum.create.content.kinetics.crafter.RecipeGridHandler.GroupedItems;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction.EdgeInteractionBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.inventory.InvManipulationBehaviour;
import com.zurrtum.create.infrastructure.items.SidedItemInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class MechanicalCrafterBlockEntity extends KineticBlockEntity implements TransformableBlockEntity {

    public enum Phase {
        IDLE,
        ACCEPTING,
        ASSEMBLING,
        EXPORTING,
        WAITING,
        CRAFTING,
        INSERTING;
    }

    public class CrafterItemHandler implements SidedItemInventory {
        @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
        public static final Optional<Integer> LIMIT = Optional.of(1);
        private static final int[] SLOTS = {0};
        private ItemStack stack = ItemStack.EMPTY;

        @Override
        public int[] getAvailableSlots(Direction side) {
            return SLOTS;
        }

        @Override
        public boolean canInsert(int slot, ItemStack stack, @Nullable Direction dir) {
            return phase == Phase.IDLE && !covered;
        }

        @Override
        public boolean canExtract(int slot, ItemStack stack, Direction dir) {
            return false;
        }

        @Override
        public ItemStack onExtract(ItemStack stack) {
            return removeMaxSize(stack, LIMIT);
        }

        @Override
        public int getMaxCountPerStack() {
            return 1;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public ItemStack getStack(int slot) {
            if (slot != 0) {
                return ItemStack.EMPTY;
            }
            return stack;
        }

        @Override
        public void setStack(int slot, ItemStack stack) {
            if (slot == 0) {
                setStack(stack);
            }
        }

        @Override
        public void markDirty() {
            notifyUpdate();
            if (stack.isEmpty())
                return;
            if (phase == Phase.IDLE)
                checkCompletedRecipe(false);
        }

        public ItemStack getStack() {
            return stack;
        }

        public void setStack(ItemStack stack) {
            if (!stack.isEmpty()) {
                getWorld().playSound(null, getPos(), SoundEvents.ENTITY_ITEM_FRAME_ADD_ITEM, SoundCategory.BLOCKS, .25f, .5f);
            }
            if (stack != ItemStack.EMPTY) {
                setMaxSize(stack, LIMIT);
            }
            this.stack = stack;
        }

        public void write(WriteView view) {
            view.put("Stack", ItemStack.OPTIONAL_CODEC, stack);
        }

        public void read(ReadView view) {
            stack = view.read("Stack", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        }
    }

    protected CrafterItemHandler inventory;
    public GroupedItems groupedItems = new GroupedItems();
    protected ConnectedInput input = new ConnectedInput();
    @Nullable
    protected Inventory invCap;
    protected boolean reRender;
    public Phase phase;
    public int countDown;
    public boolean covered;
    protected boolean wasPoweredBefore;

    public GroupedItems groupedItemsBeforeCraft; // for rendering on client
    private InvManipulationBehaviour inserting;

    private ItemStack scriptedResult = ItemStack.EMPTY;

    public MechanicalCrafterBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_CRAFTER, pos, state);
        setLazyTickRate(20);
        phase = Phase.IDLE;
        groupedItemsBeforeCraft = new GroupedItems();
        inventory = new CrafterItemHandler();

        // Does not get serialized due to active checking in tick
        wasPoweredBefore = true;
    }

    public Inventory getInvCapability() {
        if (invCap == null) {
            invCap = input.getItemHandler(getWorld(), getPos());
        }
        return invCap;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        inserting = new InvManipulationBehaviour(this, this::getTargetFace);
        behaviours.add(inserting);
        //noinspection deprecation
        behaviours.add(new EdgeInteractionBehaviour(this, ConnectedInputHandler::toggleConnection).connectivity(ConnectedInputHandler::shouldConnect)
            .require(item -> item.getRegistryEntry().isIn(AllItemTags.TOOLS_WRENCH)));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.CRAFTER, AllAdvancements.CRAFTER_LAZY);
    }

    @Override
    public void onSpeedChanged(float previousSpeed) {
        super.onSpeedChanged(previousSpeed);
        if (!MathHelper.approximatelyEquals(getSpeed(), 0)) {
            award(AllAdvancements.CRAFTER);
            if (Math.abs(getSpeed()) < 5)
                award(AllAdvancements.CRAFTER_LAZY);
        }
    }

    public void blockChanged() {
        removeBehaviour(InvManipulationBehaviour.TYPE);
        inserting = new InvManipulationBehaviour(this, this::getTargetFace);
        attachBehaviourLate(inserting);
    }

    public BlockFace getTargetFace(World world, BlockPos pos, BlockState state) {
        return new BlockFace(pos, MechanicalCrafterBlock.getTargetDirection(state));
    }

    public Direction getTargetDirection() {
        return MechanicalCrafterBlock.getTargetDirection(getCachedState());
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);
        if (input == null)
            return;

        input.write(view.get("ConnectedInput"));
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        inventory.write(view);
        input.write(view.get("ConnectedInput"));
        if (groupedItemsBeforeCraft != null) {
            view.put("GroupedItemsBeforeCraft", GroupedItems.CODEC, groupedItemsBeforeCraft);
            groupedItemsBeforeCraft = null;
        }
        view.put("GroupedItems", GroupedItems.CODEC, groupedItems);
        view.putString("Phase", phase.name());
        view.putInt("CountDown", countDown);
        view.putBoolean("Cover", covered);

        super.write(view, clientPacket);

        if (clientPacket && reRender) {
            view.putBoolean("Redraw", true);
            reRender = false;
        }
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        Phase phaseBefore = phase;
        GroupedItems before = this.groupedItems;

        inventory.read(view);
        input.read(view.getReadView("ConnectedInput"));
        groupedItems = view.read("GroupedItems", GroupedItems.CODEC).orElseGet(GroupedItems::new);
        phase = Phase.IDLE;
        String name = view.getString("Phase", "");
        for (Phase phase : Phase.values())
            if (phase.name().equals(name))
                this.phase = phase;
        countDown = view.getInt("CountDown", 0);
        covered = view.getBoolean("Cover", false);
        super.read(view, clientPacket);
        if (!clientPacket)
            return;
        if (view.getBoolean("Redraw", false))
            world.updateListeners(getPos(), getCachedState(), getCachedState(), 16);
        if (phaseBefore != phase && phase == Phase.CRAFTING) {
            groupedItemsBeforeCraft = view.read("GroupedItemsBeforeCraft", GroupedItems.CODEC).orElse(before);
        }
        if (phaseBefore == Phase.EXPORTING && phase == Phase.WAITING) {
            if (before.onlyEmptyItems())
                return;
            Direction facing = getCachedState().get(MechanicalCrafterBlock.HORIZONTAL_FACING);
            Vec3d vec = Vec3d.of(facing.getVector()).multiply(.75).add(VecHelper.getCenterOf(pos));
            Direction targetDirection = MechanicalCrafterBlock.getTargetDirection(getCachedState());
            vec = vec.add(Vec3d.of(targetDirection.getVector()).multiply(1));
            world.addParticleClient(ParticleTypes.CRIT, vec.x, vec.y, vec.z, 0, 0, 0);
        }
    }

    public int getCountDownSpeed() {
        if (getSpeed() == 0)
            return 0;
        return MathHelper.clamp((int) Math.abs(getSpeed()), 4, 250);
    }

    @Override
    public void tick() {
        super.tick();

        if (phase == Phase.ACCEPTING)
            return;

        boolean onClient = world.isClient;
        boolean runLogic = !onClient || isVirtual();

        if (wasPoweredBefore != world.isReceivingRedstonePower(pos)) {
            wasPoweredBefore = world.isReceivingRedstonePower(pos);
            if (wasPoweredBefore) {
                if (!runLogic)
                    return;
                checkCompletedRecipe(true);
            }
        }

        if (phase == Phase.ASSEMBLING) {
            countDown -= getCountDownSpeed();
            if (countDown < 0) {
                countDown = 0;
                if (!runLogic)
                    return;
                if (RecipeGridHandler.getTargetingCrafter(this) != null) {
                    phase = Phase.EXPORTING;
                    countDown = groupedItems.onlyEmptyItems() ? 0 : 1000;
                    sendData();
                    return;
                }

                ItemStack result = isVirtual() ? scriptedResult : RecipeGridHandler.tryToApplyRecipe((ServerWorld) world, groupedItems);

                if (result != null) {
                    List<ItemStack> containers = new ArrayList<>();
                    groupedItems.grid.values().forEach(stack -> {
                        ItemStack remainder = stack.getItem().getRecipeRemainder();
                        if (!remainder.isEmpty())
                            containers.add(remainder);
                    });

                    groupedItemsBeforeCraft = groupedItems;

                    groupedItems = new GroupedItems(result);
                    for (int i = 0; i < containers.size(); i++) {
                        ItemStack stack = containers.get(i);
                        GroupedItems container = new GroupedItems();
                        container.grid.put(Pair.of(i, 0), stack);
                        container.mergeOnto(groupedItems, Pointing.LEFT);
                    }

                    phase = Phase.CRAFTING;
                    countDown = 2000;
                    sendData();
                    return;
                }
                ejectWholeGrid();
                return;
            }
        }

        if (phase == Phase.EXPORTING) {
            countDown -= getCountDownSpeed();

            if (countDown < 0) {
                countDown = 0;
                if (!runLogic)
                    return;

                MechanicalCrafterBlockEntity targetingCrafter = RecipeGridHandler.getTargetingCrafter(this);
                if (targetingCrafter == null) {
                    ejectWholeGrid();
                    return;
                }

                boolean empty = groupedItems.onlyEmptyItems();
                Pointing pointing = getCachedState().get(MechanicalCrafterBlock.POINTING);
                groupedItems.mergeOnto(targetingCrafter.groupedItems, pointing);
                groupedItems = new GroupedItems();

                float pitch = targetingCrafter.groupedItems.grid.size() * 1 / 16f + .5f;

                if (!empty)
                    AllSoundEvents.CRAFTER_CLICK.playOnServer(world, pos, 1, pitch);

                phase = Phase.WAITING;
                countDown = 0;
                sendData();
                targetingCrafter.continueIfAllPrecedingFinished();
                targetingCrafter.sendData();
                return;
            }
        }

        if (phase == Phase.CRAFTING) {

            if (onClient) {
                Direction facing = getCachedState().get(MechanicalCrafterBlock.HORIZONTAL_FACING);
                float progress = countDown / 2000f;
                Vec3d facingVec = Vec3d.of(facing.getVector());
                Vec3d vec = facingVec.multiply(.65).add(VecHelper.getCenterOf(pos));
                Vec3d offset = VecHelper.offsetRandomly(Vec3d.ZERO, world.random, .125f).multiply(VecHelper.axisAlingedPlaneOf(facingVec)).normalize()
                    .multiply(progress * .5f).add(vec);
                if (progress > .5f)
                    world.addParticleClient(ParticleTypes.CRIT, offset.x, offset.y, offset.z, 0, 0, 0);

                if (!groupedItemsBeforeCraft.grid.isEmpty() && progress < .5f) {
                    if (groupedItems.grid.containsKey(Pair.of(0, 0))) {
                        ItemStack stack = groupedItems.grid.get(Pair.of(0, 0));
                        groupedItemsBeforeCraft = new GroupedItems();

                        for (int i = 0; i < 10; i++) {
                            Vec3d randVec = VecHelper.offsetRandomly(Vec3d.ZERO, world.random, .125f)
                                .multiply(VecHelper.axisAlingedPlaneOf(facingVec)).normalize().multiply(.25f);
                            Vec3d offset2 = randVec.add(vec);
                            randVec = randVec.multiply(.35f);
                            world.addParticleClient(
                                new ItemStackParticleEffect(ParticleTypes.ITEM, stack),
                                offset2.x,
                                offset2.y,
                                offset2.z,
                                randVec.x,
                                randVec.y,
                                randVec.z
                            );
                        }
                    }
                }
            }

            int prev = countDown;
            countDown -= getCountDownSpeed();

            if (countDown < 1000 && prev >= 1000) {
                AllSoundEvents.CRAFTER_CLICK.playOnServer(world, pos, 1, 2);
                AllSoundEvents.CRAFTER_CRAFT.playOnServer(world, pos);
            }

            if (countDown < 0) {
                countDown = 0;
                if (!runLogic)
                    return;
                tryInsert();
                return;
            }
        }

        if (phase == Phase.INSERTING) {
            if (runLogic && isTargetingBelt())
                tryInsert();
        }
    }

    protected boolean isTargetingBelt() {
        DirectBeltInputBehaviour behaviour = getTargetingBelt();
        return behaviour != null && behaviour.canInsertFromSide(getTargetDirection());
    }

    protected DirectBeltInputBehaviour getTargetingBelt() {
        BlockPos targetPos = pos.offset(getTargetDirection());
        return BlockEntityBehaviour.get(world, targetPos, DirectBeltInputBehaviour.TYPE);
    }

    public void tryInsert() {
        if (!inserting.hasInventory() && !isTargetingBelt()) {
            ejectWholeGrid();
            return;
        }

        boolean chagedPhase = phase != Phase.INSERTING;
        final List<Pair<Integer, Integer>> inserted = new LinkedList<>();

        DirectBeltInputBehaviour behaviour = getTargetingBelt();
        for (Map.Entry<Pair<Integer, Integer>, ItemStack> entry : groupedItems.grid.entrySet()) {
            Pair<Integer, Integer> pair = entry.getKey();
            ItemStack stack = entry.getValue();
            BlockFace face = getTargetFace(world, pos, getCachedState());

            ItemStack remainder = behaviour == null ? inserting.insert(stack.copy()) : behaviour.handleInsertion(stack, face.getFace(), false);
            if (!remainder.isEmpty()) {
                stack.setCount(remainder.getCount());
                continue;
            }

            inserted.add(pair);
        }

        inserted.forEach(groupedItems.grid::remove);
        if (groupedItems.grid.isEmpty())
            ejectWholeGrid();
        else
            phase = Phase.INSERTING;
        if (!inserted.isEmpty() || chagedPhase)
            sendData();
    }

    public void ejectWholeGrid() {
        List<MechanicalCrafterBlockEntity> chain = RecipeGridHandler.getAllCraftersOfChain(this);
        if (chain == null)
            return;
        chain.forEach(MechanicalCrafterBlockEntity::eject);
    }

    public void eject() {
        BlockState blockState = getCachedState();
        boolean present = blockState.isOf(AllBlocks.MECHANICAL_CRAFTER);
        Vec3d vec = present ? Vec3d.of(blockState.get(HORIZONTAL_FACING).getVector()).multiply(.75f) : Vec3d.ZERO;
        Vec3d ejectPos = VecHelper.getCenterOf(pos).add(vec);
        groupedItems.grid.forEach((pair, stack) -> dropItem(ejectPos, stack));
        if (!inventory.getStack().isEmpty())
            dropItem(ejectPos, inventory.onExtract(inventory.getStack()));
        phase = Phase.IDLE;
        groupedItems = new GroupedItems();
        inventory.setStack(ItemStack.EMPTY);
        sendData();
    }

    public void dropItem(Vec3d ejectPos, ItemStack stack) {
        ItemEntity itemEntity = new ItemEntity(world, ejectPos.x, ejectPos.y, ejectPos.z, stack);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (world.isClient && !isVirtual())
            return;
        if (phase == Phase.IDLE && craftingItemPresent())
            checkCompletedRecipe(false);
        if (phase == Phase.INSERTING)
            tryInsert();
    }

    public boolean craftingItemPresent() {
        return !inventory.getStack().isEmpty();
    }

    public boolean craftingItemOrCoverPresent() {
        return !inventory.getStack().isEmpty() || covered;
    }

    public void checkCompletedRecipe(boolean poweredStart) {
        if (getSpeed() == 0)
            return;
        if (world.isClient && !isVirtual())
            return;
        List<MechanicalCrafterBlockEntity> chain = RecipeGridHandler.getAllCraftersOfChainIf(
            this,
            poweredStart ? MechanicalCrafterBlockEntity::craftingItemPresent : MechanicalCrafterBlockEntity::craftingItemOrCoverPresent,
            poweredStart
        );
        if (chain == null)
            return;
        chain.forEach(MechanicalCrafterBlockEntity::begin);
    }

    protected void begin() {
        phase = Phase.ACCEPTING;
        groupedItems = new GroupedItems(inventory.onExtract(inventory.getStack()));
        inventory.setStack(ItemStack.EMPTY);
        if (RecipeGridHandler.getPrecedingCrafters(this).isEmpty()) {
            phase = Phase.ASSEMBLING;
            countDown = 1;
        }
        sendData();
    }

    protected void continueIfAllPrecedingFinished() {
        List<MechanicalCrafterBlockEntity> preceding = RecipeGridHandler.getPrecedingCrafters(this);
        //        if (preceding == null) {
        //            ejectWholeGrid();
        //            return;
        //        }

        for (MechanicalCrafterBlockEntity blockEntity : preceding)
            if (blockEntity.phase != Phase.WAITING)
                return;

        phase = Phase.ASSEMBLING;
        countDown = 1;
    }

    public void connectivityChanged() {
        reRender = true;
        sendData();
        invCap = null;
    }

    public CrafterItemHandler getInventory() {
        return inventory;
    }

    public void setScriptedResult(ItemStack scriptedResult) {
        this.scriptedResult = scriptedResult;
    }

    public ConnectedInput getInput() {
        return input;
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        input.data.replaceAll(transform::applyWithoutOffset);
        notifyUpdate();
    }
}
