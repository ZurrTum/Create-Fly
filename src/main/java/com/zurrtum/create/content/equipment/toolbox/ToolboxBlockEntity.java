package com.zurrtum.create.content.equipment.toolbox;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.animatedContainer.AnimatedContainerBehaviour;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.foundation.utility.ResetableLazy;
import net.minecraft.block.BlockState;
import net.minecraft.component.ComponentMap.Builder;
import net.minecraft.component.ComponentsAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Nameable;
import net.minecraft.util.Uuids;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.*;

public class ToolboxBlockEntity extends SmartBlockEntity implements MenuProvider, Nameable {

    public LerpedFloat lid = LerpedFloat.linear().startWithValue(0);

    public LerpedFloat drawers = LerpedFloat.linear().startWithValue(0);

    UUID uniqueId;
    public ToolboxInventory inventory;
    ResetableLazy<DyeColor> colorProvider;

    Map<Integer, WeakHashMap<PlayerEntity, Integer>> connectedPlayers;

    private Text customName;

    private AnimatedContainerBehaviour<ToolboxMenu> openTracker;

    public ToolboxBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.TOOLBOX, pos, state);
        connectedPlayers = new HashMap<>();
        inventory = new ToolboxInventory(this);
        colorProvider = ResetableLazy.of(() -> {
            BlockState blockState = getCachedState();
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

        if (world.isClient()) {
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

        for (Iterator<Map.Entry<Integer, WeakHashMap<PlayerEntity, Integer>>> toolboxSlots = connectedPlayers.entrySet()
            .iterator(); toolboxSlots.hasNext(); ) {

            Map.Entry<Integer, WeakHashMap<PlayerEntity, Integer>> toolboxSlotEntry = toolboxSlots.next();
            WeakHashMap<PlayerEntity, Integer> set = toolboxSlotEntry.getValue();
            int slot = toolboxSlotEntry.getKey();

            ItemStack referenceItem = inventory.filters.get(slot);
            boolean clear = referenceItem.isEmpty();

            for (Iterator<Map.Entry<PlayerEntity, Integer>> playerEntries = set.entrySet().iterator(); playerEntries.hasNext(); ) {
                Map.Entry<PlayerEntity, Integer> playerEntry = playerEntries.next();

                PlayerEntity player = playerEntry.getKey();
                int hotbarSlot = playerEntry.getValue();

                if (!clear && !ToolboxHandler.withinRange(player, this))
                    continue;

                PlayerInventory playerInv = player.getInventory();
                ItemStack playerStack = playerInv.getStack(hotbarSlot);

                if (clear || !playerStack.isEmpty() && !ToolboxInventory.canItemsShareCompartment(playerStack, referenceItem)) {
                    NbtCompound compound = AllSynchedDatas.TOOLBOX.get(player);
                    compound.remove(String.valueOf(hotbarSlot));
                    playerEntries.remove();
                    if (player instanceof ServerPlayerEntity)
                        ToolboxHandler.syncData(player, compound);
                    continue;
                }

                int count = playerStack.getCount();
                int targetAmount = (referenceItem.getMaxCount() + 1) / 2;

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
                        playerInv.setStack(hotbarSlot, template.copyWithCount(count + extracted.getCount()));
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
                        playerInv.setStack(hotbarSlot, playerStack.copyWithCount(count - deposited));
                    }
                }
            }

            if (clear)
                toolboxSlots.remove();
        }

        if (update)
            sendData();

    }

    private boolean isOpenInContainer(PlayerEntity player) {
        return player.currentScreenHandler instanceof ToolboxMenu toolboxMenu && toolboxMenu.contentHolder == this;
    }

    public void unequipTracked() {
        if (world.isClient())
            return;

        Set<ServerPlayerEntity> affected = new HashSet<>();

        for (Map.Entry<Integer, WeakHashMap<PlayerEntity, Integer>> toolboxSlotEntry : connectedPlayers.entrySet()) {

            WeakHashMap<PlayerEntity, Integer> set = toolboxSlotEntry.getValue();

            for (Map.Entry<PlayerEntity, Integer> playerEntry : set.entrySet()) {
                PlayerEntity player = playerEntry.getKey();
                int hotbarSlot = playerEntry.getValue();

                ToolboxHandler.unequip(player, hotbarSlot, false);
                if (player instanceof ServerPlayerEntity serverPlayer)
                    affected.add(serverPlayer);
            }
        }

        for (ServerPlayerEntity player : affected)
            ToolboxHandler.syncData(player, AllSynchedDatas.TOOLBOX.get(player));
        connectedPlayers.clear();
    }

    public void unequip(int slot, PlayerEntity player, int hotbarSlot, boolean keepItems) {
        if (!connectedPlayers.containsKey(slot))
            return;
        connectedPlayers.get(slot).remove(player);
        if (keepItems)
            return;

        PlayerInventory playerInv = player.getInventory();
        ItemStack playerStack = playerInv.getStack(hotbarSlot);
        ItemStack toInsert = ToolboxInventory.cleanItemNBT(playerStack.copy());
        int insert = inventory.distributeToCompartment(toInsert, slot, false);

        if (insert != 0) {
            int count = playerStack.getCount();
            if (insert == count) {
                playerInv.setStack(hotbarSlot, ItemStack.EMPTY);
            } else {
                playerStack.setCount(count - insert);
            }
        }
    }

    private void tickAudio() {
        Vec3d vec = VecHelper.getCenterOf(pos);
        if (lid.settled()) {
            if (openTracker.openCount > 0 && lid.getChaseTarget() == 0) {
                world.playSoundClient(
                    vec.x,
                    vec.y,
                    vec.z,
                    SoundEvents.BLOCK_IRON_DOOR_OPEN,
                    SoundCategory.BLOCKS,
                    0.25F,
                    world.random.nextFloat() * 0.1F + 1.2F,
                    true
                );
                world.playSoundClient(
                    vec.x,
                    vec.y,
                    vec.z,
                    SoundEvents.BLOCK_CHEST_OPEN,
                    SoundCategory.BLOCKS,
                    0.1F,
                    world.random.nextFloat() * 0.1F + 1.1F,
                    true
                );
            }
            if (openTracker.openCount == 0 && lid.getChaseTarget() == 1)
                world.playSoundClient(
                    vec.x,
                    vec.y,
                    vec.z,
                    SoundEvents.BLOCK_CHEST_CLOSE,
                    SoundCategory.BLOCKS,
                    0.1F,
                    world.random.nextFloat() * 0.1F + 1.1F,
                    true
                );

        } else if (openTracker.openCount == 0 && lid.getChaseTarget() == 0 && lid.getValue(0) > 1 / 16f && lid.getValue(1) < 1 / 16f)
            world.playSoundClient(
                vec.x,
                vec.y,
                vec.z,
                SoundEvents.BLOCK_IRON_DOOR_CLOSE,
                SoundCategory.BLOCKS,
                0.25F,
                world.random.nextFloat() * 0.1F + 1.2F,
                true
            );
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        inventory.read(view.getReadView("Inventory"));
        super.read(view, clientPacket);
        view.read("UniqueId", Uuids.INT_STREAM_CODEC).ifPresent(uuid -> uniqueId = uuid);
        view.read("CustomName", TextCodecs.CODEC).ifPresent(name -> customName = name);
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        if (uniqueId == null)
            uniqueId = UUID.randomUUID();

        inventory.write(view.get("Inventory"));
        view.put("UniqueId", Uuids.INT_STREAM_CODEC, uniqueId);

        if (customName != null)
            view.put("CustomName", TextCodecs.CODEC, customName);
        super.write(view, clientPacket);
    }

    @Override
    public ToolboxMenu createMenu(int id, PlayerInventory inv, PlayerEntity player, RegistryByteBuf extraData) {
        sendToMenu(extraData);
        return new ToolboxMenu(id, inv, this);
    }

    @Override
    public void lazyTick() {
        // keep re-advertising active TEs
        ToolboxHandler.onLoad(this);
        super.lazyTick();
    }

    public void connectPlayer(int slot, PlayerEntity player, int hotbarSlot) {
        if (world.isClient())
            return;
        WeakHashMap<PlayerEntity, Integer> map = connectedPlayers.computeIfAbsent(slot, WeakHashMap::new);
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
            DefaultedList<ItemStack> filters = inv.filters;
            for (int i = 0, size = filters.size(); i < size; i++) {
                inventory.filters.set(i, filters.get(i));
            }
            for (int i = 0, size = inv.size(); i < size; i++)
                inventory.setStack(i, inv.getStack(i));
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

    public void setCustomName(Text customName) {
        this.customName = customName;
    }

    @Override
    public Text getDisplayName() {
        return customName != null ? customName : getCachedState().getBlock().getName();
    }

    @Override
    public Text getCustomName() {
        return customName;
    }

    @Override
    public boolean hasCustomName() {
        return customName != null;
    }

    @Override
    public Text getName() {
        return customName;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setCachedState(BlockState state) {
        super.setCachedState(state);
        colorProvider.reset();
    }

    @Override
    protected void readComponents(ComponentsAccess componentInput) {
        setUniqueId(componentInput.get(AllDataComponents.TOOLBOX_UUID));
        readInventory(componentInput.get(AllDataComponents.TOOLBOX_INVENTORY));
    }

    @Override
    protected void addComponents(Builder components) {
        components.add(AllDataComponents.TOOLBOX_UUID, uniqueId);
        components.add(AllDataComponents.TOOLBOX_INVENTORY, inventory);
    }
}