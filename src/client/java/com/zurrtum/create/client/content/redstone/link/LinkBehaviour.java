package com.zurrtum.create.client.content.redstone.link;

import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.zurrtum.create.content.redstone.link.RedstoneLinkNetworkHandler;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;

public class LinkBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<LinkBehaviour> TYPE = new BehaviourType<>();
    ValueBoxTransform firstSlot;
    ValueBoxTransform secondSlot;
    private ServerLinkBehaviour behaviour;

    public LinkBehaviour(SmartBlockEntity be) {
        super(be);
        firstSlot = new RedstoneLinkFrequencySlot(true);
        secondSlot = new RedstoneLinkFrequencySlot(false);
    }

    public boolean isLoad() {
        return behaviour != null;
    }

    @Override
    public void initialize() {
        behaviour = blockEntity.getBehaviour(ServerLinkBehaviour.TYPE);
    }

    @Override
    public void onBehaviourAdded(BehaviourType<?> type, BlockEntityBehaviour<?> behaviour) {
        if (type == ServerLinkBehaviour.TYPE) {
            this.behaviour = (ServerLinkBehaviour) behaviour;
        }
    }

    public void setFrequency(boolean first, ItemStack heldItem) {
        behaviour.setFrequency(first, heldItem);
    }

    public boolean testHit(Boolean first, Vec3d hit) {
        BlockState state = blockEntity.getCachedState();
        Vec3d localHit = hit.subtract(Vec3d.of(blockEntity.getPos()));
        return (first ? firstSlot : secondSlot).testHit(getWorld(), getPos(), state, localHit);
    }

    public Couple<RedstoneLinkNetworkHandler.Frequency> getNetworkKey() {
        return behaviour.getNetworkKey();
    }

    public ItemStack getFirstStack() {
        return behaviour.frequencyFirst.getStack();
    }

    public ItemStack getLastStack() {
        return behaviour.frequencyLast.getStack();
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }
}
