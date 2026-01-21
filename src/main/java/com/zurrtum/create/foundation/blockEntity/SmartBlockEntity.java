package com.zurrtum.create.foundation.blockEntity;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.schematic.nbt.PartialSafeNBT;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedInventoryBehaviour;
import com.zurrtum.create.foundation.utility.IInteractionChecker;
import com.zurrtum.create.ponder.api.VirtualBlockEntity;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class SmartBlockEntity extends CachedRenderBBBlockEntity implements PartialSafeNBT, IInteractionChecker, SpecialBlockEntityItemRequirement, VirtualBlockEntity {

    private final Map<BehaviourType<?>, BlockEntityBehaviour<?>> behaviours = new Reference2ObjectArrayMap<>();
    protected int lazyTickRate;
    protected int lazyTickCounter;
    private boolean initialized = false;
    private boolean firstNbtRead = true;
    private boolean chunkUnloaded;

    // Used for simulating this BE in a client-only setting
    private boolean virtualMode;

    public SmartBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);

        setLazyTickRate(10);

        ArrayList<BlockEntityBehaviour<?>> list = new ArrayList<>();
        addBehaviours(list);
        list.forEach(b -> behaviours.put(b.getType(), b));
        for (Function<SmartBlockEntity, BlockEntityBehaviour<?>> factory : BlockEntityBehaviour.REGISTRY.get(type)) {
            BlockEntityBehaviour<?> behaviour = factory.apply(this);
            behaviours.put(behaviour.getType(), behaviour);
        }
    }

    public abstract void addBehaviours(List<BlockEntityBehaviour<?>> behaviours);

    /**
     * Gets called just before reading block entity data for behaviours. Register
     * anything here that depends on your custom BE data.
     */
    public void addBehavioursDeferred(List<BlockEntityBehaviour<?>> behaviours) {
    }

    public void initialize() {
        if (firstNbtRead) {
            firstNbtRead = false;
            for (Function<SmartBlockEntity, BlockEntityBehaviour<?>> factory : BlockEntityBehaviour.FIRST_READ_REGISTRY.get(getType())) {
                BlockEntityBehaviour<?> behaviour = factory.apply(this);
                behaviours.put(behaviour.getType(), behaviour);
            }
        }
        if (level.isClientSide()) {
            for (Function<SmartBlockEntity, BlockEntityBehaviour<?>> factory : BlockEntityBehaviour.CLIENT_REGISTRY.get(getType())) {
                BlockEntityBehaviour<?> behaviour = factory.apply(this);
                behaviours.put(behaviour.getType(), behaviour);
            }
        }

        forEachBehaviour(BlockEntityBehaviour::initialize);
        lazyTick();
    }

    public void tick() {
        if (!initialized && hasLevel()) {
            initialize();
            initialized = true;
        }

        if (lazyTickCounter-- <= 0) {
            lazyTickCounter = lazyTickRate;
            lazyTick();
        }

        forEachBehaviour(BlockEntityBehaviour::tick);
    }

    public void lazyTick() {
    }

    /**
     * Hook only these in future subclasses of STE
     */
    protected void write(ValueOutput view, boolean clientPacket) {
        super.saveAdditional(view);
        forEachBehaviour(tb -> tb.write(view, clientPacket));
    }

    @Override
    public void writeSafe(ValueOutput view) {
        super.saveAdditional(view);
        forEachBehaviour(tb -> {
            if (tb.isSafeNBT())
                tb.writeSafe(view);
        });
    }

    /**
     * Hook only these in future subclasses of STE
     */
    protected void read(ValueInput view, boolean clientPacket) {
        if (firstNbtRead) {
            firstNbtRead = false;
            ArrayList<BlockEntityBehaviour<?>> list = new ArrayList<>();
            addBehavioursDeferred(list);
            list.forEach(b -> behaviours.put(b.getType(), b));
            for (Function<SmartBlockEntity, BlockEntityBehaviour<?>> factory : BlockEntityBehaviour.FIRST_READ_REGISTRY.get(getType())) {
                BlockEntityBehaviour<?> behaviour = factory.apply(this);
                behaviours.put(behaviour.getType(), behaviour);
            }
        }
        super.loadAdditional(view);
        forEachBehaviour(tb -> tb.read(view, clientPacket));
    }

    @Override
    protected void loadAdditional(ValueInput view) {
        read(view, false);
    }

    public void onChunkUnloaded() {
        chunkUnloaded = true;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!chunkUnloaded)
            remove();
        invalidate();
    }

    /**
     * Block destroyed or Chunk unloaded. Usually invalidates capabilities
     */
    public void invalidate() {
        forEachBehaviour(BlockEntityBehaviour::unload);
    }

    /**
     * Block destroyed or picked up by a contraption. Usually detaches kinetics
     */
    public void remove() {
    }

    /**
     * Block destroyed or replaced. Requires Block to call IBE::onRemove
     */
    public void destroy() {
        forEachBehaviour(BlockEntityBehaviour::destroy);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        destroy();
    }

    @Override
    protected void saveAdditional(ValueOutput view) {
        write(view, false);
    }

    @Override
    public final void readClient(ValueInput view) {
        read(view, true);
    }

    @Override
    public final void writeClient(ValueOutput view) {
        write(view, true);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public <T extends BlockEntityBehaviour<?>> T getBehaviour(BehaviourType<T> type) {
        return (T) behaviours.get(type);
    }

    public void forEachBehaviour(Consumer<BlockEntityBehaviour<?>> action) {
        getAllBehaviours().forEach(action);
    }

    public Collection<BlockEntityBehaviour<?>> getAllBehaviours() {
        return behaviours.values();
    }

    @SuppressWarnings("unchecked")
    public <T extends SmartBlockEntity> void attachBehaviourLate(BlockEntityBehaviour<T> behaviour) {
        BehaviourType<?> type = behaviour.getType();
        behaviours.values().forEach(b -> b.onBehaviourAdded(type, behaviour));
        behaviours.put(type, behaviour);
        behaviour.blockEntity = (T) this;
        behaviour.initialize();
    }

    public ItemRequirement getRequiredItems(BlockState state) {
        return getAllBehaviours().stream().reduce(ItemRequirement.NONE, (r, b) -> r.union(b.getRequiredItems()), ItemRequirement::union);
    }

    public void removeBehaviour(BehaviourType<?> type) {
        BlockEntityBehaviour<?> remove = behaviours.remove(type);
        if (remove != null) {
            remove.unload();
        }
    }

    public void setLazyTickRate(int slowTickRate) {
        this.lazyTickRate = slowTickRate;
        this.lazyTickCounter = slowTickRate;
    }

    public void markVirtual() {
        virtualMode = true;
    }

    public boolean isVirtual() {
        return virtualMode;
    }

    public boolean isChunkUnloaded() {
        return chunkUnloaded;
    }

    @Override
    public boolean canPlayerUse(Player player) {
        if (level == null || level.getBlockEntity(worldPosition) != this)
            return false;
        return player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    public void sendToMenu(RegistryFriendlyByteBuf buffer) {
        buffer.writeBlockPos(getBlockPos());
        buffer.writeNbt(getUpdateTag(buffer.registryAccess()));
    }

    @SuppressWarnings("deprecation")
    public void refreshBlockState() {
        setBlockState(getLevel().getBlockState(getBlockPos()));
    }

    public void addAdvancementBehaviour(ServerPlayer player) {
        List<CreateTrigger> awardables = getAwardables();
        if (awardables != null) {
            behaviours.put(AdvancementBehaviour.TYPE, new AdvancementBehaviour(this, player, awardables.toArray(CreateTrigger[]::new)));
        }
    }

    @Nullable
    public List<CreateTrigger> getAwardables() {
        return null;
    }

    public void award(CreateTrigger advancement) {
        AdvancementBehaviour behaviour = getBehaviour(AdvancementBehaviour.TYPE);
        if (behaviour != null)
            behaviour.awardPlayer(advancement);
    }

    public void awardIfNear(CreateTrigger advancement, int range) {
        AdvancementBehaviour behaviour = getBehaviour(AdvancementBehaviour.TYPE);
        if (behaviour != null)
            behaviour.awardPlayerIfNear(advancement, range);
    }

    public void resetTransferCache() {
        CachedInventoryBehaviour<?> behaviour = getBehaviour(CachedInventoryBehaviour.TYPE);
        if (behaviour != null) {
            behaviour.reset();
        }
    }
}