package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.*;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperItem;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Clearable;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemStackWithSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.zurrtum.create.Create.LOGGER;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerBlockEntity extends KineticBlockEntity implements Clearable {
    public State state;
    public Mode mode;
    public ItemStack heldItem;
    protected DeployerPlayer player;
    public int timer;
    public float reach;
    public boolean fistBump = false;
    public List<ItemStack> overflowItems = new ArrayList<>();
    protected ServerFilteringBehaviour filtering;
    protected boolean redstoneLocked;
    protected UUID owner;
    protected String ownerName;
    public Container invHandler;
    private CompoundTag deferredInventoryList;

    public LerpedFloat animatedOffset;

    public BeltProcessingBehaviour processingBehaviour;

    public enum State implements StringRepresentable {
        WAITING,
        EXPANDING,
        RETRACTING,
        DUMPING;

        public static final Codec<State> CODEC = StringRepresentable.fromEnum(State::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Mode implements StringRepresentable {
        PUNCH,
        USE;

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public DeployerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.DEPLOYER, pos, state);
        this.state = State.WAITING;
        mode = Mode.USE;
        heldItem = ItemStack.EMPTY;
        redstoneLocked = false;
        animatedOffset = LerpedFloat.linear().startWithValue(0);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        discardPlayer();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        filtering = new ServerFilteringBehaviour.CustomInteract(this, List.of(AllItems.MECHANICAL_ARM));
        behaviours.add(filtering);
        processingBehaviour = new BeltProcessingBehaviour(this).whenItemEnters((s, i) -> BeltDeployerCallbacks.onItemReceived(s, i, this))
            .whileItemHeld((s, i) -> BeltDeployerCallbacks.whenItemHeld(s, i, this));
        behaviours.add(processingBehaviour);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(
            AllAdvancements.TRAIN_CASING,
            AllAdvancements.ANDESITE_CASING,
            AllAdvancements.BRASS_CASING,
            AllAdvancements.COPPER_CASING,
            AllAdvancements.FIST_BUMP,
            AllAdvancements.DEPLOYER,
            AllAdvancements.SELF_DEPLOYING
        );
    }

    @Override
    public void initialize() {
        super.initialize();
        initHandler();
    }

    public void initHandler() {
        if (invHandler != null)
            return;
        if (level instanceof ServerLevel sLevel) {
            player = DeployerPlayer.create(sLevel, owner, ownerName);
            ServerPlayer serverPlayer = player.cast();
            if (deferredInventoryList != null) {
                try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), LOGGER)) {
                    ValueInput view = TagValueInput.create(logging, level.registryAccess(), deferredInventoryList);
                    serverPlayer.getInventory().load(view.listOrEmpty("Inventory", ItemStackWithSlot.CODEC));
                }
                deferredInventoryList = null;
                heldItem = serverPlayer.getMainHandItem();
                sendData();
            }
            Vec3 initialPos = VecHelper.getCenterOf(worldPosition.relative(getBlockState().getValue(FACING)));
            serverPlayer.setPos(initialPos.x, initialPos.y, initialPos.z);
        }
        invHandler = createHandler();
    }

    protected void onExtract(ItemStack stack) {
        player.cast().setItemInHand(InteractionHand.MAIN_HAND, stack.copy());
        sendData();
        setChanged();
    }

    public int getTimerSpeed() {
        return (int) (getSpeed() == 0 ? 0 : Mth.clamp(Math.abs(getSpeed() * 2), 8, 512));
    }

    @Override
    public void tick() {
        super.tick();

        if (getSpeed() == 0)
            return;
        if (!level.isClientSide() && player != null && player.getBlockBreakingProgress() != null) {
            if (level.isEmptyBlock(player.getBlockBreakingProgress().getKey())) {
                level.destroyBlockProgress(player.cast().getId(), player.getBlockBreakingProgress().getKey(), -1);
                player.setBlockBreakingProgress(null);
            }
        }
        if (timer > 0) {
            timer -= getTimerSpeed();
            return;
        }
        if (level.isClientSide())
            return;
        if (player == null)
            return;

        ServerPlayer serverPlayer = player.cast();
        ItemStack stack = serverPlayer.getMainHandItem();
        if (state == State.WAITING) {
            if (!overflowItems.isEmpty()) {
                timer = getTimerSpeed() * 10;
                return;
            }

            boolean changed = false;
            Inventory inventory = serverPlayer.getInventory();
            for (int i = 0, size = inventory.getContainerSize(); i < size; i++) {
                if (overflowItems.size() > 10)
                    break;
                ItemStack item = inventory.getItem(i);
                if (item.isEmpty())
                    continue;
                if (item != stack || !filtering.test(item)) {
                    overflowItems.add(item);
                    inventory.setItem(i, ItemStack.EMPTY);
                    changed = true;
                }
            }

            if (changed) {
                sendData();
                timer = getTimerSpeed() * 10;
                return;
            }

            Direction facing = getBlockState().getValue(FACING);
            if (mode == Mode.USE && !DeployerHandler.shouldActivate(stack, level, worldPosition.relative(facing, 2), facing)) {
                timer = getTimerSpeed() * 10;
                return;
            }

            // Check for advancement conditions
            if (mode == Mode.PUNCH && !fistBump && startFistBump(facing))
                return;
            if (redstoneLocked)
                return;

            start();
            return;
        }

        if (state == State.EXPANDING) {
            if (fistBump)
                triggerFistBump();
            activate();

            state = State.RETRACTING;
            timer = 1000;
            sendData();
            return;
        }

        if (state == State.RETRACTING) {
            state = State.WAITING;
            timer = 500;
            sendData();
            return;
        }

    }

    protected void start() {
        state = State.EXPANDING;
        Vec3 movementVector = getMovementVector();
        Vec3 rayOrigin = VecHelper.getCenterOf(worldPosition).add(movementVector.scale(3 / 2f));
        Vec3 rayTarget = VecHelper.getCenterOf(worldPosition).add(movementVector.scale(5 / 2f));
        ClipContext rayTraceContext = new ClipContext(rayOrigin, rayTarget, Block.OUTLINE, Fluid.NONE, player.cast());
        BlockHitResult result = level.clip(rayTraceContext);
        reach = (float) (.5f + Math.min(result.getLocation().subtract(rayOrigin).length(), .75f));
        timer = 1000;
        sendData();
    }

    public boolean startFistBump(Direction facing) {
        int i = 0;
        DeployerBlockEntity partner = null;

        for (i = 2; i < 5; i++) {
            BlockPos otherDeployer = worldPosition.relative(facing, i);
            if (!level.isLoaded(otherDeployer))
                return false;
            BlockEntity other = level.getBlockEntity(otherDeployer);
            if (other instanceof DeployerBlockEntity dpe) {
                partner = dpe;
                break;
            }
        }

        if (partner == null)
            return false;

        if (level.getBlockState(partner.getBlockPos()).getValue(FACING).getOpposite() != facing || partner.mode != Mode.PUNCH)
            return false;
        if (partner.getSpeed() == 0)
            return false;

        for (DeployerBlockEntity be : Arrays.asList(this, partner)) {
            be.fistBump = true;
            be.reach = ((i - 2)) * .5f;
            be.timer = 1000;
            be.state = State.EXPANDING;
            be.sendData();
        }

        return true;
    }

    public void triggerFistBump() {
        int i = 0;
        DeployerBlockEntity deployerBlockEntity = null;
        for (i = 2; i < 5; i++) {
            BlockPos pos = this.worldPosition.relative(getBlockState().getValue(BlockStateProperties.FACING), i);
            if (!level.isLoaded(pos))
                return;
            if (level.getBlockEntity(pos) instanceof DeployerBlockEntity dpe) {
                deployerBlockEntity = dpe;
                break;
            }
        }

        if (deployerBlockEntity == null)
            return;
        if (!deployerBlockEntity.fistBump || deployerBlockEntity.state != State.EXPANDING)
            return;
        if (deployerBlockEntity.timer > 0)
            return;

        fistBump = false;
        deployerBlockEntity.fistBump = false;
        deployerBlockEntity.state = State.RETRACTING;
        deployerBlockEntity.timer = 1000;
        deployerBlockEntity.sendData();
        award(AllAdvancements.FIST_BUMP);

        BlockPos soundLocation = BlockPos.containing(Vec3.atCenterOf(worldPosition).add(Vec3.atCenterOf(deployerBlockEntity.getBlockPos()))
            .scale(.5f));
        level.playSound(null, soundLocation, SoundEvents.PLAYER_ATTACK_NODAMAGE, SoundSource.BLOCKS, .75f, .75f);
    }

    protected void activate() {
        Vec3 movementVector = getMovementVector();
        Direction direction = getBlockState().getValue(BlockStateProperties.FACING);
        Vec3 center = VecHelper.getCenterOf(worldPosition);
        BlockPos clickedPos = worldPosition.relative(direction, 2);
        ServerPlayer serverPlayer = player.cast();
        serverPlayer.setXRot(direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0);
        serverPlayer.setYRot(direction.toYRot());

        if (direction == Direction.DOWN && BlockEntityBehaviour.get(level, clickedPos, TransportedItemStackHandlerBehaviour.TYPE) != null)
            return; // Belt processing handled in BeltDeployerCallbacks

        DeployerHandler.activate(player, center, clickedPos, movementVector, mode);
        award(AllAdvancements.DEPLOYER);

        if (player != null) {
            int count = heldItem.getCount();
            heldItem = serverPlayer.getMainHandItem();
            if (count != heldItem.getCount())
                setChanged();
        }
    }

    protected Vec3 getMovementVector() {
        BlockState state = getBlockState();
        if (!state.is(AllBlocks.DEPLOYER))
            return Vec3.ZERO;
        return Vec3.atLowerCornerOf(state.getValue(FACING).getUnitVec3i());
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        state = view.read("State", State.CODEC).orElse(State.WAITING);
        mode = view.read("Mode", Mode.CODEC).orElse(Mode.PUNCH);
        timer = view.getIntOr("Timer", 0);
        redstoneLocked = view.getBooleanOr("Powered", false);
        owner = view.read("Owner", UUIDUtil.CODEC).orElse(null);
        ownerName = view.read("OwnerName", Codec.STRING).orElse(null);

        deferredInventoryList = view.read("Inventory", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        overflowItems = new ArrayList<>();
        view.read("Overflow", CreateCodecs.ITEM_LIST_CODEC).ifPresent(overflowItems::addAll);
        view.read("HeldItem", ItemStack.OPTIONAL_CODEC).ifPresent(item -> heldItem = item);
        super.read(view, clientPacket);

        if (!clientPacket)
            return;
        fistBump = view.getBooleanOr("Fistbump", false);
        reach = view.getFloatOr("Reach", 0);
        view.read("Particle", ItemStack.CODEC).ifPresent(particleStack -> {
            SandPaperItem.spawnParticles(VecHelper.getCenterOf(worldPosition).add(getMovementVector().scale(reach + 1)), particleStack, level);
        });
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        view.store("Mode", Mode.CODEC, mode);
        view.store("State", State.CODEC, state);
        view.putInt("Timer", timer);
        view.putBoolean("Powered", redstoneLocked);
        if (owner != null) {
            view.store("Owner", UUIDUtil.CODEC, owner);
            view.store("OwnerName", Codec.STRING, ownerName);
        }

        if (player != null) {
            ServerPlayer serverPlayer = player.cast();
            try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(problemPath(), LOGGER)) {
                TagValueOutput writeView = TagValueOutput.createWithContext(logging, level.registryAccess());
                serverPlayer.getInventory().save(writeView.list("Inventory", ItemStackWithSlot.CODEC));
                view.store("Inventory", CompoundTag.CODEC, writeView.buildResult());
            }
            ItemStack stack = serverPlayer.getMainHandItem();
            view.store("HeldItem", ItemStack.OPTIONAL_CODEC, stack);
            view.store("Overflow", CreateCodecs.ITEM_LIST_CODEC, overflowItems);
        } else if (deferredInventoryList != null) {
            view.store("Inventory", CompoundTag.CODEC, deferredInventoryList);
        }

        super.write(view, clientPacket);

        if (!clientPacket)
            return;
        view.putBoolean("Fistbump", fistBump);
        view.putFloat("Reach", reach);
        if (player == null)
            return;
        if (player.getSpawnedItemEffects() != null) {
            ItemStack stack = player.getSpawnedItemEffects();
            if (!stack.isEmpty()) {
                view.store("Particle", ItemStack.CODEC, stack);
            }
            player.setSpawnedItemEffects(null);
        }
    }

    @Override
    public void writeSafe(ValueOutput view) {
        view.store("Mode", Mode.CODEC, mode);
        super.writeSafe(view);
    }

    private Container createHandler() {
        return new DeployerItemHandler(this);
    }

    public void redstoneUpdate() {
        if (level.isClientSide())
            return;
        boolean blockPowered = level.hasNeighborSignal(worldPosition);
        if (blockPowered == redstoneLocked)
            return;
        redstoneLocked = blockPowered;
        sendData();
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(3);
    }

    public void discardPlayer() {
        if (player == null)
            return;
        ServerPlayer serverPlayer = player.cast();
        serverPlayer.getInventory().dropAll();
        overflowItems.forEach(itemstack -> serverPlayer.drop(itemstack, true, false));
        serverPlayer.discard();
        player = null;
    }

    @Override
    public void clearContent() {
        filtering.setFilter(ItemStack.EMPTY);
    }

    public void changeMode() {
        mode = mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH;
        setChanged();
        sendData();
    }

    public void setAnimatedOffset(float offset) {
        animatedOffset.setValue(offset);
    }

    @Nullable
    public Recipe<? extends RecipeInput> getRecipe(ItemStack stack) {
        if (player == null || level == null)
            return null;

        ItemStack heldItemMainhand = player.cast().getMainHandItem();
        RecipeMap preparedRecipes = ((ServerLevel) level).recipeAccess().recipes;
        if (heldItemMainhand.getItem() instanceof SandPaperItem) {
            return preparedRecipes.getRecipesFor(AllRecipeTypes.SANDPAPER_POLISHING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED).map(RecipeHolder::value).findFirst().orElse(null);
        }

        ItemApplicationInput input = new ItemApplicationInput(stack, heldItemMainhand);
        return AllRecipeTypes.DEPLOYER_RECIPES.stream().flatMap(type -> preparedRecipes.getRecipesFor(type, input, level))
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED).map(RecipeHolder::value).findFirst().orElse(null);
    }

    public DeployerPlayer getPlayer() {
        return player;
    }
}
