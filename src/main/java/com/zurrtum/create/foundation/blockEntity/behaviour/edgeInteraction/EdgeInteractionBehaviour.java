package com.zurrtum.create.foundation.blockEntity.behaviour.edgeInteraction;

import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class EdgeInteractionBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<EdgeInteractionBehaviour> TYPE = new BehaviourType<>();

    ConnectionCallback connectionCallback;
    ConnectivityPredicate connectivityPredicate;
    public Predicate<Item> requiredItem;

    public EdgeInteractionBehaviour(SmartBlockEntity be, ConnectionCallback callback) {
        super(be);
        connectionCallback = callback;
        requiredItem = item -> true;
        connectivityPredicate = (world, pos, face, face2) -> true;
    }

    public EdgeInteractionBehaviour connectivity(ConnectivityPredicate pred) {
        connectivityPredicate = pred;
        return this;
    }

    public EdgeInteractionBehaviour require(Item required) {
        return require(item -> item == required);
    }

    public EdgeInteractionBehaviour require(Predicate<Item> predicate) {
        requiredItem = predicate;
        return this;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    @FunctionalInterface
    public interface ConnectionCallback {
        void apply(Level world, BlockPos clicked, BlockPos neighbour);
    }

    @FunctionalInterface
    public interface ConnectivityPredicate {
        boolean test(Level world, BlockPos pos, Direction selectedFace, Direction connectedFace);
    }

}
