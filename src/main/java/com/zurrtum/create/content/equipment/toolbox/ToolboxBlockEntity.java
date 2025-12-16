package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.utility.ResetableLazy;

import java.util.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap.Builder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class ToolboxBlockEntity extends SmartBlockEntity implements MenuProvider, Nameable {

    public LerpedFloat lid = LerpedFloat.linear().startWithValue(0);

    public LerpedFloat drawers = LerpedFloat.linear().startWithValue(0);

    UUID uniqueId;
    public ToolboxInventory inventory;
    ResetableLazy<DyeColor> colorProvider;

    Map<Integer, WeakHashMap<Player, Integer>> connectedPlayers;

    private Component customName;

    private AnimatedContainerBehaviour<ToolboxMenu> openTracker;
    private boolean keepAlive;

    public ToolboxBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TOOLBOX, pos, state);
        connectedPlayers = new HashMap<>();
        inventory = new ToolboxInventory(this);
        colorProvider = ResetableLazy.of(() -> {
            BlockState blockState = getBlockState();
            if (blockState != null && blockState.getBlock() instanceof ToolboxBlock)
                return ((ToolboxBlock) blockState.getBlock()).getColor();
            return DyeColor.BROWN;
        });
        setLazyTickRate(10);
    }

    public DyeColor getColor() {
        return colorProvider.get();
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(openTracker = new AnimatedContainerBehaviour<>(this, ToolboxMenu.class));
    }

    @Override
    public void initialize() {
        super.initialize();
        ToolboxHandler.onLoad(this);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        ToolboxHandler.onUnload(this);
    }

    @Override
    public void tick() {
        super.tick();

        if (level.isClientSide()) {
            tickAudio();
        } else {
            tickPlayers();
        }

        lid.chase(openTracker.openCount > 0 ? 1 : 0, 0.2f, Chaser.LINEAR);
        drawers.chase(openTracker.openCount > 0 ? 1 : 0, 0.2f, Chaser.EXP);
        lid.tickChaser();
        drawers.tickChaser();
    }

    private void tickPlayers() {
        boolean update = false;

        for (Iterator<Map.Entry<Integer, WeakHashMap<Player, Integer>>> toolboxSlots = connectedPlayers.entrySet()
            .iterator(); toolboxSlots.hasNext(); ) {

            Map.Entry<Integer, WeakHashMap<Player, Integer>> toolboxSlotEntry = toolboxSlots.next();
            WeakHashMap<Player, Integer> set = toolboxSlotEntry.getValue();
            int slot = toolboxSlotEntry.getKey();

            ItemStack referenceItem = inventory.filters.get(slot);
            boolean clear = referenceItem.isEmpty();

            for (Iterator<Map.Entry<Player, Integer>> playerEntries = set.entrySet().iterator(); playerEntries.hasNext(); ) {
                Map.Entry<Player, Integer> playerEntry = playerEntries.next();

                Player player = playerEntry.getKey();
                int hotbarSlot = playerEntry.getValue();

                if (!clear && !ToolboxHandler.withinRange(player, this))
                    continue;

                Inventory playerInv = player.getInventory();
                ItemStack playerStack = playerInv.getItem(hotbarSlot);

                if (clear || !playerStack.isEmpty() && !ToolboxInventory.canItemsShareCompartment(playerStack, referenceItem)) {
                    CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
                    compound.remove(String.valueOf(hotbarSlot));
                    playerEntries.remove();
                    if (player instanceof ServerPlayer)
                        ToolboxHandler.syncData(player, compound);
                    continue;
                }

                int count = playerStack.getCount();
                int targetAmount = (referenceItem.getMaxStackSize() + 1) / 2;

                if (count < targetAmount) {
                    int amountToReplenish = targetAmount - count;

                    if (isOpenInContainer(player)) {
                        ItemStack extracted = inventory.takeFromCompartment(amountToReplenish, slot, true);
                        if (!extracted.isEmpty()) {
                            ToolboxHandler.unequip(player, hotbarSlot, false);
                            ToolboxHandler.syncData(player, AllSynchedDatas.TOOLBOX.get(player));
                            continue;
                        }
                    }

                    ItemStack extracted = inventory.takeFromCompartment(amountToReplenish, slot, false);
                    if (!extracted.isEmpty()) {
                        update = true;
                        ItemStack template = playerStack.isEmpty() ? extracted : playerStack;
                        playerInv.setItem(hotbarSlot, template.copyWithCount(count + extracted.getCount()));
                    }
                }

                if (count > targetAmount) {
                    int amountToDeposit = count - targetAmount;
                    ItemStack toDistribute = playerStack.copyWithCount(amountToDeposit);

                    if (isOpenInContainer(player)) {
                        int deposited = inventory.distributeToCompartment(toDistribute, slot, true);
                        if (deposited > 0) {
                            ToolboxHandler.unequip(player, hotbarSlot, true);
                            ToolboxHandler.syncData(player, AllSynchedDatas.TOOLBOX.get(player));
                            continue;
                        }
                    }

                    int deposited = inventory.distributeToCompartment(toDistribute, slot, false);
                    if (deposited > 0) {
                        update = true;
                        playerInv.setItem(hotbarSlot, playerStack.copyWithCount(count - deposited));
                    }
                }
            }

            if (clear)
                toolboxSlots.remove();
        }

        if (update)
            sendData();

    }

    private boolean isOpenInContainer(Player player) {
        return player.containerMenu instanceof ToolboxMenu toolboxMenu && toolboxMenu.contentHolder == this;
    }

    public void unequipTracked() {
        if (level.isClientSide())
            return;

        Set<ServerPlayer> affected = new HashSet<>();

        for (Map.Entry<Integer, WeakHashMap<Player, Integer>> toolboxSlotEntry : connectedPlayers.entrySet()) {

            WeakHashMap<Player, Integer> set = toolboxSlotEntry.getValue();

            for (Map.Entry<Player, Integer> playerEntry : set.entrySet()) {
                Player player = playerEntry.getKey();
                int hotbarSlot = playerEntry.getValue();

                ToolboxHandler.unequip(player, hotbarSlot, false);
                if (player instanceof ServerPlayer serverPlayer)
                    affected.add(serverPlayer);
            }
        }

        for (ServerPlayer player : affected)
            ToolboxHandler.syncData(player, AllSynchedDatas.TOOLBOX.get(player));
        connectedPlayers.clear();
    }

    public void unequip(int slot, Player player, int hotbarSlot, boolean keepItems) {
        if (!connectedPlayers.containsKey(slot))
            return;
        connectedPlayers.get(slot).remove(player);
        if (keepItems)
            return;

        Inventory playerInv = player.getInventory();
        ItemStack playerStack = playerInv.getItem(hotbarSlot);
        ItemStack toInsert = ToolboxInventory.cleanItemNBT(playerStack.copy());
        int insert = inventory.distributeToCompartment(toInsert, slot, false);

        if (insert != 0) {
            int count = playerStack.getCount();
            if (insert == count) {
                playerInv.setItem(hotbarSlot, ItemStack.EMPTY);
            } else {
                playerStack.setCount(count - insert);
            }
        }
    }

    private void tickAudio() {
        Vec3 vec = VecHelper.getCenterOf(worldPosition);
        if (lid.settled()) {
            if (openTracker.openCount > 0 && lid.getChaseTarget() == 0) {
                level.playLocalSound(
                    vec.x,
                    vec.y,
                    vec.z,
                    SoundEvents.IRON_DOOR_OPEN,
                    SoundSource.BLOCKS,
                    0.25F,
                    level.getRandom().nextFloat() * 0.1F + 1.2F,
                    true
                );
                level.playLocalSound(
                    vec.x,
                    vec.y,
                    vec.z,
                    SoundEvents.CHEST_OPEN,
                    SoundSource.BLOCKS,
                    0.1F,
                    level.getRandom().nextFloat() * 0.1F + 1.1F,
                    true
                );
            }
            if (openTracker.openCount == 0 && lid.getChaseTarget() == 1)
                level.playLocalSound(
                    vec.x,
                    vec.y,
                    vec.z,
                    SoundEvents.CHEST_CLOSE,
                    SoundSource.BLOCKS,
                    0.1F,
                    level.getRandom().nextFloat() * 0.1F + 1.1F,
                    true
                );

        } else if (openTracker.openCount == 0 && lid.getChaseTarget() == 0 && lid.getValue(0) > 1 / 16f && lid.getValue(1) < 1 / 16f)
            level.playLocalSound(
                vec.x,
                vec.y,
                vec.z,
                SoundEvents.IRON_DOOR_CLOSE,
                SoundSource.BLOCKS,
                0.25F,
                level.getRandom().nextFloat() * 0.1F + 1.2F,
                true
            );
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        inventory.read(view.childOrEmpty("Inventory"));
        super.read(view, clientPacket);
        view.read("UniqueId", UUIDUtil.CODEC).ifPresent(uuid -> uniqueId = uuid);
        view.read("CustomName", ComponentSerialization.CODEC).ifPresent(name -> customName = name);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        if (uniqueId == null)
            uniqueId = UUID.randomUUID();

        inventory.write(view.child("Inventory"));
        view.store("UniqueId", UUIDUtil.CODEC, uniqueId);

        if (customName != null)
            view.store("CustomName", ComponentSerialization.CODEC, customName);
        super.write(view, clientPacket);
    }

    @Override
    public ToolboxMenu createMenu(int id, Inventory inv, Player player, RegistryFriendlyByteBuf extraData) {
        sendToMenu(extraData);
        return new ToolboxMenu(id, inv, this);
    }

    @Override
    public void lazyTick() {
        // keep re-advertising active TEs
        ToolboxHandler.onLoad(this);
        super.lazyTick();
    }

    public void connectPlayer(int slot, Player player, int hotbarSlot) {
        if (level.isClientSide())
            return;
        WeakHashMap<Player, Integer> map = connectedPlayers.computeIfAbsent(slot, WeakHashMap::new);
        Integer previous = map.get(player);
        if (previous != null) {
            if (previous == hotbarSlot)
                return;
            ToolboxHandler.unequip(player, previous, false);
        }
        map.put(player, hotbarSlot);
    }

    public void readInventory(ToolboxInventory inv) {
        if (inv != null) {
            NonNullList<ItemStack> filters = inv.filters;
            for (int i = 0, size = filters.size(); i < size; i++) {
                inventory.filters.set(i, filters.get(i));
            }
            for (int i = 0, size = inv.getContainerSize(); i < size; i++)
                inventory.setItem(i, inv.getItem(i));
        }
    }

    public void setUniqueId(UUID uniqueId) {
        this.uniqueId = uniqueId;
    }

    public UUID getUniqueId() {
        return uniqueId;
    }

    public boolean isFullyInitialized() {
        // returns true when uniqueId has been initialized
        return uniqueId != null;
    }

    public void setCustomName(Component customName) {
        this.customName = customName;
    }

    @Override
    public Component getDisplayName() {
        return customName != null ? customName : getBlockState().getBlock().getName();
    }

    @Override
    public Component getCustomName() {
        return customName;
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    @Override
    public Component getName() {
        return customName;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setBlockState(BlockState state) {
        super.setBlockState(state);
        colorProvider.reset();
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentInput) {
        setUniqueId(componentInput.get(AllDataComponents.TOOLBOX_UUID));
        readInventory(componentInput.get(AllDataComponents.TOOLBOX_INVENTORY));
    }

    @Override
    protected void collectImplicitComponents(Builder components) {
        components.set(AllDataComponents.TOOLBOX_UUID, uniqueId);
        components.set(AllDataComponents.TOOLBOX_INVENTORY, inventory);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        BlockState state = level.getBlockState(pos);
        if (getType().isValid(state)) {
            keepAlive = true;
            setBlockState(state);
        } else {
            super.preRemoveSideEffects(pos, oldState);
        }
    }

    @Override
    public void setRemoved() {
        if (keepAlive) {
            keepAlive = false;
            level.getChunk(worldPosition).setBlockEntity(this);
        } else {
            super.setRemoved();
        }
    }
}