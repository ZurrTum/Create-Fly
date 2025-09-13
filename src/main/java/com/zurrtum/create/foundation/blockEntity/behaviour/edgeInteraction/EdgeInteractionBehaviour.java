package com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class EdgeInteractionBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<EdgeInteractionBehaviour> TYPE = new BehaviourType<>();

    ConnectionCallback connectionCallback;
    ConnectivityPredicate connectivityPredicate;
    @Nullable
    public Item requiredItem;

    public EdgeInteractionBehaviour(SmartBlockEntity be, ConnectionCallback callback) {
        super(be);
        this.connectionCallback = callback;
        connectivityPredicate = (world, pos, face, face2) -> true;
    }

    public EdgeInteractionBehaviour connectivity(ConnectivityPredicate pred) {
        this.connectivityPredicate = pred;
        return this;
    }

    public EdgeInteractionBehaviour require(Item item) {
        this.requiredItem = item;
        return this;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @FunctionalInterface
    public interface ConnectionCallback {
        void apply(World world, BlockPos clicked, BlockPos neighbour);
    }

    @FunctionalInterface
    public interface ConnectivityPredicate {
        boolean test(World world, BlockPos pos, Direction selectedFace, Direction connectedFace);
    }

}
