package com.zurrtum.create.content.logistics.packager;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.packager.InventoryIdentifier;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.logistics.vault.ItemVaultBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class AllInventoryIdentifiers {
    public static void registerDefaults() {
        // identify known single blocks
        InventoryIdentifier.REGISTRY.registerProvider(SimpleRegistry.Provider.forBlockTag(
            AllBlockTags.SINGLE_BLOCK_INVENTORIES,
            AllInventoryIdentifiers::single
        ));

        // connect double chests
        InventoryIdentifier.REGISTRY.registerProvider(block -> {
            Collection<Property<?>> properties = block.getStateDefinition().getProperties();
            if (properties.contains(ChestBlock.TYPE) && properties.contains(ChestBlock.FACING)) {
                return AllInventoryIdentifiers::chest;
            }
            return null;
        });

        // best-effort for WorldlyContainerHolders (just composters in vanilla)
        InventoryIdentifier.REGISTRY.registerProvider(block -> {
            if (block instanceof WorldlyContainerHolder) {
                return AllInventoryIdentifiers::worldlyContainerBlock;
            }
            return null;
        });

        // connect vaults
        InventoryIdentifier.REGISTRY.register(
            AllBlocks.ITEM_VAULT, (level, state, face) -> {
                BlockEntity be = level.getBlockEntity(face.getPos());
                return be instanceof ItemVaultBlockEntity vault ? vault.getInvId() : null;
            }
        );
    }

    private static InventoryIdentifier single(Level level, BlockState state, BlockFace face) {
        return new InventoryIdentifier.Single(face.getPos());
    }

    private static InventoryIdentifier chest(Level level, BlockState state, BlockFace face) {
        ChestType type = state.getValue(ChestBlock.TYPE);

        if (type != ChestType.SINGLE) {
            Direction toOther = ChestBlock.getConnectedDirection(state);
            BlockPos otherPos = face.getPos().relative(toOther);
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.is(state.getBlock()) && ChestBlock.getConnectedDirection(otherState) == toOther.getOpposite()) {
                return new InventoryIdentifier.Pair(face.getPos(), otherPos);
            }
        }

        return new InventoryIdentifier.Single(face.getPos());
    }

    private static InventoryIdentifier worldlyContainerBlock(Level level, BlockState state, BlockFace face) {
        WorldlyContainerHolder holder = (WorldlyContainerHolder) state.getBlock();
        WorldlyContainer container = holder.getContainer(state, level, face.getPos());
        return ofWorldlyContainer(container, face);
    }

    private static InventoryIdentifier ofWorldlyContainer(WorldlyContainer container, BlockFace face) {
        Direction side = face.getFace();
        int[] slots = container.getSlotsForFace(side);
        // get all faces that have the same slots as the given one
        Set<Direction> directions = EnumSet.of(side);
        for (Direction direction : Iterate.directions) {
            if (direction != side) {
                int[] faceSlots = container.getSlotsForFace(direction);
                if (Arrays.equals(slots, faceSlots)) {
                    directions.add(direction);
                }
            }
        }
        return new InventoryIdentifier.MultiFace(face.getPos(), directions);
    }

    // called manually when no other Finder is found.
    // currently just checks for WorldlyContainer BlockEntities, which would
    // fill the registry with Finders pointlessly if done through a provider.
    @Nullable
    public static InventoryIdentifier fallback(Level level, BlockState state, BlockFace face) {
        BlockEntity be = level.getBlockEntity(face.getPos());
        if (be instanceof WorldlyContainer container) {
            return ofWorldlyContainer(container, face);
        }

        return null;
    }
}
