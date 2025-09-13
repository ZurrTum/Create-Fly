package com.zurrtum.create.foundation.blockEntity;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllTransfer;
import com.zurrtum.create.api.schematic.nbt.PartialSafeNBT;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockEntityItemRequirement;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.CachedInventoryBehaviour;
import com.zurrtum.create.foundation.utility.IInteractionChecker;
import com.zurrtum.create.ponder.api.VirtualBlockEntity;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

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
        AllTransfer.addBehaviours(this, list);
        list.forEach(b -> behaviours.put(b.getType(), b));
        list.clear();
        AllClientHandle.INSTANCE.addBehaviours(this, list);
        list.forEach(b -> behaviours.put(b.getType(), b));
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
            //TODO
            //            NeoForge.EVENT_BUS.post(new BlockEntityBehaviourEvent(this, behaviours));
        }

        forEachBehaviour(BlockEntityBehaviour::initialize);
        lazyTick();
    }

    public void tick() {
        if (!initialized && hasWorld()) {
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
    protected void write(WriteView view, boolean clientPacket) {
        super.writeData(view);
        forEachBehaviour(tb -> tb.write(view, clientPacket));
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeData(view);
        forEachBehaviour(tb -> {
            if (tb.isSafeNBT())
                tb.writeSafe(view);
        });
    }

    /**
     * Hook only these in future subclasses of STE
     */
    protected void read(ReadView view, boolean clientPacket) {
        if (firstNbtRead) {
            firstNbtRead = false;
            ArrayList<BlockEntityBehaviour<?>> list = new ArrayList<>();
            addBehavioursDeferred(list);
            list.forEach(b -> behaviours.put(b.getType(), b));
        }
        super.readData(view);
        forEachBehaviour(tb -> tb.read(view, clientPacket));
    }

    @Override
    protected void readData(ReadView view) {
        read(view, false);
    }

    public void onChunkUnloaded() {
        chunkUnloaded = true;
    }

    @Override
    public void markRemoved() {
        super.markRemoved();
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
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        destroy();
    }

    @Override
    protected void writeData(WriteView view) {
        write(view, false);
    }

    @Override
    public final void readClient(ReadView view) {
        read(view, true);
    }

    @Override
    public final void writeClient(WriteView view) {
        write(view, true);
    }

    @SuppressWarnings("unchecked")
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
    public boolean canPlayerUse(PlayerEntity player) {
        if (world == null || world.getBlockEntity(pos) != this)
            return false;
        return player.squaredDistanceTo(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    public void sendToMenu(RegistryByteBuf buffer) {
        buffer.writeBlockPos(getPos());
        buffer.writeNbt(toInitialChunkDataNbt(buffer.getRegistryManager()));
    }

    @SuppressWarnings("deprecation")
    public void refreshBlockState() {
        setCachedState(getWorld().getBlockState(getPos()));
    }

    public void addAdvancementBehaviour(ServerPlayerEntity player) {
        List<CreateTrigger> awardables = getAwardables();
        if (awardables != null) {
            behaviours.put(AdvancementBehaviour.TYPE, new AdvancementBehaviour(this, player, awardables.toArray(CreateTrigger[]::new)));
        }
    }

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