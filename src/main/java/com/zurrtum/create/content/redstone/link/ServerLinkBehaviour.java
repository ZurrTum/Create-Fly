package com.zurrtum.create.content.redstone.link;

import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.equipment.clipboard.ClipboardCloneable;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler.Frequency;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Optional;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

public class ServerLinkBehaviour extends BlockEntityBehaviour<SmartBlockEntity> implements IRedstoneLinkable, ClipboardCloneable {
    public static final BehaviourType<ServerLinkBehaviour> TYPE = new BehaviourType<>();

    enum Mode {
        TRANSMIT,
        RECEIVE
    }

    public Frequency frequencyFirst;
    public Frequency frequencyLast;
    Vec3d textShift;

    public boolean newPosition;
    private Mode mode;
    private IntSupplier transmission;
    private IntConsumer signalCallback;

    protected ServerLinkBehaviour(SmartBlockEntity be) {
        super(be);
        frequencyFirst = Frequency.EMPTY;
        frequencyLast = Frequency.EMPTY;
        textShift = Vec3d.ZERO;
        newPosition = true;
    }

    public static ServerLinkBehaviour receiver(SmartBlockEntity be, IntConsumer signalCallback) {
        ServerLinkBehaviour behaviour = new ServerLinkBehaviour(be);
        behaviour.signalCallback = signalCallback;
        behaviour.mode = Mode.RECEIVE;
        return behaviour;
    }

    public static ServerLinkBehaviour transmitter(SmartBlockEntity be, IntSupplier transmission) {
        ServerLinkBehaviour behaviour = new ServerLinkBehaviour(be);
        behaviour.transmission = transmission;
        behaviour.mode = Mode.TRANSMIT;
        return behaviour;
    }

    public ServerLinkBehaviour moveText(Vec3d shift) {
        textShift = shift;
        return this;
    }

    public void copyItemsFrom(ServerLinkBehaviour behaviour) {
        if (behaviour == null)
            return;
        frequencyFirst = behaviour.frequencyFirst;
        frequencyLast = behaviour.frequencyLast;
    }

    @Override
    public boolean isListening() {
        return mode == Mode.RECEIVE;
    }

    @Override
    public int getTransmittedStrength() {
        return mode == Mode.TRANSMIT ? transmission.getAsInt() : 0;
    }

    @Override
    public void setReceivedStrength(int networkPower) {
        if (!newPosition)
            return;
        signalCallback.accept(networkPower);
    }

    public void notifySignalChange() {
        Create.REDSTONE_LINK_NETWORK_HANDLER.updateNetworkOf(getWorld(), this);
    }

    @Override
    public void initialize() {
        super.initialize();
        if (getWorld().isClient())
            return;
        getHandler().addToNetwork(getWorld(), this);
        newPosition = true;
    }

    @Override
    public Couple<Frequency> getNetworkKey() {
        return Couple.create(frequencyFirst, frequencyLast);
    }

    @Override
    public void unload() {
        super.unload();
        if (getWorld().isClient())
            return;
        getHandler().removeFromNetwork(getWorld(), this);
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.put("FrequencyFirst", Frequency.CODEC, frequencyFirst);
        view.put("FrequencyLast", Frequency.CODEC, frequencyLast);
        view.put("LastKnownPosition", BlockPos.CODEC, blockEntity.getPos());
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        newPosition = view.read("LastKnownPosition", BlockPos.CODEC).map(pos -> !blockEntity.getPos().equals(pos)).orElse(true);

        super.read(view, clientPacket);
        frequencyFirst = view.read("FrequencyFirst", Frequency.CODEC).orElse(Frequency.EMPTY);
        frequencyLast = view.read("FrequencyLast", Frequency.CODEC).orElse(Frequency.EMPTY);
    }

    public void setFrequency(boolean first, ItemStack stack) {
        stack = stack.copy();
        stack.setCount(1);
        ItemStack toCompare = first ? frequencyFirst.getStack() : frequencyLast.getStack();
        boolean changed = !ItemStack.areItemsAndComponentsEqual(stack, toCompare);

        if (changed)
            getHandler().removeFromNetwork(getWorld(), this);

        if (first)
            frequencyFirst = Frequency.of(stack);
        else
            frequencyLast = Frequency.of(stack);

        if (!changed)
            return;

        blockEntity.sendData();
        getHandler().addToNetwork(getWorld(), this);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    private RedstoneLinkNetworkHandler getHandler() {
        return Create.REDSTONE_LINK_NETWORK_HANDLER;
    }

    @Override
    public boolean isAlive() {
        World level = getWorld();
        BlockPos pos = getPos();
        if (blockEntity.isChunkUnloaded())
            return false;
        if (blockEntity.isRemoved())
            return false;
        if (!level.isPosLoaded(pos))
            return false;
        return level.getBlockEntity(pos) == blockEntity;
    }

    @Override
    public BlockPos getLocation() {
        return getPos();
    }

    @Override
    public String getClipboardKey() {
        return "Frequencies";
    }

    @Override
    public boolean writeToClipboard(WriteView view, Direction side) {
        view.put("First", ItemStack.OPTIONAL_CODEC, frequencyFirst.getStack());
        view.put("Last", ItemStack.OPTIONAL_CODEC, frequencyLast.getStack());
        return true;
    }

    @Override
    public boolean readFromClipboard(ReadView view, PlayerEntity player, Direction side, boolean simulate) {
        Optional<ItemStack> first = view.read("First", ItemStack.OPTIONAL_CODEC);
        if (first.isEmpty()) {
            return false;
        }
        Optional<ItemStack> last = view.read("Last", ItemStack.OPTIONAL_CODEC);
        if (last.isEmpty()) {
            return false;
        }
        if (simulate)
            return true;
        setFrequency(true, first.get());
        setFrequency(false, last.get());
        return true;
    }
}
