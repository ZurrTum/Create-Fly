package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperItem;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackWithSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.recipe.PreparedRecipes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.input.RecipeInput;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.Uuids;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.zurrtum.create.Create.LOGGER;
import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class DeployerBlockEntity extends KineticBlockEntity {
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
    public Inventory invHandler;
    private NbtCompound deferredInventoryList;

    public LerpedFloat animatedOffset;

    public BeltProcessingBehaviour processingBehaviour;

    public enum State implements StringIdentifiable {
        WAITING,
        EXPANDING,
        RETRACTING,
        DUMPING;

        public static final Codec<State> CODEC = StringIdentifiable.createCodec(State::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public enum Mode implements StringIdentifiable {
        PUNCH,
        USE;

        public static final Codec<Mode> CODEC = StringIdentifiable.createCodec(Mode::values);

        @Override
        public String asString() {
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
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        discardPlayer();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        filtering = new ServerFilteringBehaviour(this);
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
        if (world instanceof ServerWorld sLevel) {
            player = DeployerPlayer.create(sLevel, owner, ownerName);
            ServerPlayerEntity serverPlayer = player.cast();
            if (deferredInventoryList != null) {
                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getReporterContext(), LOGGER)) {
                    ReadView view = NbtReadView.create(logging, world.getRegistryManager(), deferredInventoryList);
                    serverPlayer.getInventory().readData(view.getTypedListView("Inventory", StackWithSlot.CODEC));
                }
                deferredInventoryList = null;
                heldItem = serverPlayer.getMainHandStack();
                sendData();
            }
            Vec3d initialPos = VecHelper.getCenterOf(pos.offset(getCachedState().get(FACING)));
            serverPlayer.setPosition(initialPos.x, initialPos.y, initialPos.z);
        }
        invHandler = createHandler();
    }

    protected void onExtract(ItemStack stack) {
        player.cast().setStackInHand(Hand.MAIN_HAND, stack.copy());
        sendData();
        markDirty();
    }

    public int getTimerSpeed() {
        return (int) (getSpeed() == 0 ? 0 : MathHelper.clamp(Math.abs(getSpeed() * 2), 8, 512));
    }

    @Override
    public void tick() {
        super.tick();

        if (getSpeed() == 0)
            return;
        if (!world.isClient && player != null && player.getBlockBreakingProgress() != null) {
            if (world.isAir(player.getBlockBreakingProgress().getKey())) {
                world.setBlockBreakingInfo(player.cast().getId(), player.getBlockBreakingProgress().getKey(), -1);
                player.setBlockBreakingProgress(null);
            }
        }
        if (timer > 0) {
            timer -= getTimerSpeed();
            return;
        }
        if (world.isClient)
            return;
        if (player == null)
            return;

        ServerPlayerEntity serverPlayer = player.cast();
        ItemStack stack = serverPlayer.getMainHandStack();
        if (state == State.WAITING) {
            if (!overflowItems.isEmpty()) {
                timer = getTimerSpeed() * 10;
                return;
            }

            boolean changed = false;
            PlayerInventory inventory = serverPlayer.getInventory();
            for (int i = 0, size = inventory.size(); i < size; i++) {
                if (overflowItems.size() > 10)
                    break;
                ItemStack item = inventory.getStack(i);
                if (item.isEmpty())
                    continue;
                if (item != stack || !filtering.test(item)) {
                    overflowItems.add(item);
                    inventory.setStack(i, ItemStack.EMPTY);
                    changed = true;
                }
            }

            if (changed) {
                sendData();
                timer = getTimerSpeed() * 10;
                return;
            }

            Direction facing = getCachedState().get(FACING);
            if (mode == Mode.USE && !DeployerHandler.shouldActivate(stack, world, pos.offset(facing, 2), facing)) {
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
        Vec3d movementVector = getMovementVector();
        Vec3d rayOrigin = VecHelper.getCenterOf(pos).add(movementVector.multiply(3 / 2f));
        Vec3d rayTarget = VecHelper.getCenterOf(pos).add(movementVector.multiply(5 / 2f));
        RaycastContext rayTraceContext = new RaycastContext(rayOrigin, rayTarget, ShapeType.OUTLINE, FluidHandling.NONE, player.cast());
        BlockHitResult result = world.raycast(rayTraceContext);
        reach = (float) (.5f + Math.min(result.getPos().subtract(rayOrigin).length(), .75f));
        timer = 1000;
        sendData();
    }

    public boolean startFistBump(Direction facing) {
        int i = 0;
        DeployerBlockEntity partner = null;

        for (i = 2; i < 5; i++) {
            BlockPos otherDeployer = pos.offset(facing, i);
            if (!world.isPosLoaded(otherDeployer))
                return false;
            BlockEntity other = world.getBlockEntity(otherDeployer);
            if (other instanceof DeployerBlockEntity dpe) {
                partner = dpe;
                break;
            }
        }

        if (partner == null)
            return false;

        if (world.getBlockState(partner.getPos()).get(FACING).getOpposite() != facing || partner.mode != Mode.PUNCH)
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
            BlockPos pos = this.pos.offset(getCachedState().get(Properties.FACING), i);
            if (!world.isPosLoaded(pos))
                return;
            if (world.getBlockEntity(pos) instanceof DeployerBlockEntity dpe) {
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

        BlockPos soundLocation = BlockPos.ofFloored(Vec3d.ofCenter(pos).add(Vec3d.ofCenter(deployerBlockEntity.getPos())).multiply(.5f));
        world.playSound(null, soundLocation, SoundEvents.ENTITY_PLAYER_ATTACK_NODAMAGE, SoundCategory.BLOCKS, .75f, .75f);
    }

    protected void activate() {
        Vec3d movementVector = getMovementVector();
        Direction direction = getCachedState().get(Properties.FACING);
        Vec3d center = VecHelper.getCenterOf(pos);
        BlockPos clickedPos = pos.offset(direction, 2);
        ServerPlayerEntity serverPlayer = player.cast();
        serverPlayer.setPitch(direction == Direction.UP ? -90 : direction == Direction.DOWN ? 90 : 0);
        serverPlayer.setYaw(direction.getPositiveHorizontalDegrees());

        if (direction == Direction.DOWN && BlockEntityBehaviour.get(world, clickedPos, TransportedItemStackHandlerBehaviour.TYPE) != null)
            return; // Belt processing handled in BeltDeployerCallbacks

        DeployerHandler.activate(player, center, clickedPos, movementVector, mode);
        award(AllAdvancements.DEPLOYER);

        if (player != null) {
            int count = heldItem.getCount();
            heldItem = serverPlayer.getMainHandStack();
            if (count != heldItem.getCount())
                markDirty();
        }
    }

    protected Vec3d getMovementVector() {
        BlockState state = getCachedState();
        if (!state.isOf(AllBlocks.DEPLOYER))
            return Vec3d.ZERO;
        return Vec3d.of(state.get(FACING).getVector());
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        state = view.read("State", State.CODEC).orElse(State.WAITING);
        mode = view.read("Mode", Mode.CODEC).orElse(Mode.PUNCH);
        timer = view.getInt("Timer", 0);
        redstoneLocked = view.getBoolean("Powered", false);
        owner = view.read("Owner", Uuids.INT_STREAM_CODEC).orElse(null);
        ownerName = view.read("OwnerName", Codec.STRING).orElse(null);

        deferredInventoryList = view.read("Inventory", NbtCompound.CODEC).orElseGet(NbtCompound::new);
        overflowItems = new ArrayList<>();
        view.read("Overflow", CreateCodecs.ITEM_LIST_CODEC).ifPresent(overflowItems::addAll);
        view.read("HeldItem", ItemStack.OPTIONAL_CODEC).ifPresent(item -> heldItem = item);
        super.read(view, clientPacket);

        if (!clientPacket)
            return;
        fistBump = view.getBoolean("Fistbump", false);
        reach = view.getFloat("Reach", 0);
        view.read("Particle", ItemStack.CODEC).ifPresent(particleStack -> {
            SandPaperItem.spawnParticles(VecHelper.getCenterOf(pos).add(getMovementVector().multiply(reach + 1)), particleStack, world);
        });
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.put("Mode", Mode.CODEC, mode);
        view.put("State", State.CODEC, state);
        view.putInt("Timer", timer);
        view.putBoolean("Powered", redstoneLocked);
        if (owner != null) {
            view.put("Owner", Uuids.INT_STREAM_CODEC, owner);
            view.put("OwnerName", Codec.STRING, ownerName);
        }

        if (player != null) {
            ServerPlayerEntity serverPlayer = player.cast();
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(getReporterContext(), LOGGER)) {
                NbtWriteView writeView = NbtWriteView.create(logging, world.getRegistryManager());
                serverPlayer.getInventory().writeData(writeView.getListAppender("Inventory", StackWithSlot.CODEC));
                view.put("Inventory", NbtCompound.CODEC, writeView.getNbt());
            }
            ItemStack stack = serverPlayer.getMainHandStack();
            view.put("HeldItem", ItemStack.OPTIONAL_CODEC, stack);
            view.put("Overflow", CreateCodecs.ITEM_LIST_CODEC, overflowItems);
        } else if (deferredInventoryList != null) {
            view.put("Inventory", NbtCompound.CODEC, deferredInventoryList);
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
                view.put("Particle", ItemStack.CODEC, stack);
            }
            player.setSpawnedItemEffects(null);
        }
    }

    @Override
    public void writeSafe(WriteView view) {
        view.put("Mode", Mode.CODEC, mode);
        super.writeSafe(view);
    }

    private Inventory createHandler() {
        return new DeployerItemHandler(this);
    }

    public void redstoneUpdate() {
        if (world.isClient)
            return;
        boolean blockPowered = world.isReceivingRedstonePower(pos);
        if (blockPowered == redstoneLocked)
            return;
        redstoneLocked = blockPowered;
        sendData();
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().expand(3);
    }

    public void discardPlayer() {
        if (player == null)
            return;
        ServerPlayerEntity serverPlayer = player.cast();
        serverPlayer.getInventory().dropAll();
        overflowItems.forEach(itemstack -> serverPlayer.dropItem(itemstack, true, false));
        serverPlayer.discard();
        player = null;
    }

    public void changeMode() {
        mode = mode == Mode.PUNCH ? Mode.USE : Mode.PUNCH;
        markDirty();
        sendData();
    }

    public void setAnimatedOffset(float offset) {
        animatedOffset.setValue(offset);
    }

    @Nullable
    public Recipe<? extends RecipeInput> getRecipe(ItemStack stack) {
        if (player == null || world == null)
            return null;

        ItemStack heldItemMainhand = player.cast().getMainHandStack();
        PreparedRecipes preparedRecipes = ((ServerWorld) world).getRecipeManager().preparedRecipes;
        if (heldItemMainhand.getItem() instanceof SandPaperItem) {
            return preparedRecipes.find(AllRecipeTypes.SANDPAPER_POLISHING, new SingleStackRecipeInput(stack), world)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED).map(RecipeEntry::value).findFirst().orElse(null);
        }

        ItemApplicationInput input = new ItemApplicationInput(stack, heldItemMainhand);
        return AllRecipeTypes.DEPLOYER_RECIPES.stream().flatMap(type -> preparedRecipes.find(type, input, world))
            .filter(AllRecipeTypes.CAN_BE_AUTOMATED).map(RecipeEntry::value).findFirst().orElse(null);
    }

    public DeployerPlayer getPlayer() {
        return player;
    }
}
