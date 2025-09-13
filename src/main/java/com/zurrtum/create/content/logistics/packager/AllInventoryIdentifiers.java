package com.zurrtum.create.content.logistics.packager;

import com.zurrtum.create.AllBlockTags;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.packager.InventoryIdentifier;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.math.BlockFace;
import com.zurrtum.create.content.logistics.vault.ItemVaultBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.InventoryProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

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
            Collection<Property<?>> properties = block.getStateManager().getProperties();
            if (properties.contains(ChestBlock.CHEST_TYPE) && properties.contains(ChestBlock.FACING)) {
                return AllInventoryIdentifiers::chest;
            }
            return null;
        });

        // best-effort for WorldlyContainerHolders (just composters in vanilla)
        InventoryIdentifier.REGISTRY.registerProvider(block -> {
            if (block instanceof InventoryProvider) {
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

    private static InventoryIdentifier single(World level, BlockState state, BlockFace face) {
        return new InventoryIdentifier.Single(face.getPos());
    }

    private static InventoryIdentifier chest(World level, BlockState state, BlockFace face) {
        ChestType type = state.get(ChestBlock.CHEST_TYPE);

        if (type != ChestType.SINGLE) {
            Direction toOther = ChestBlock.getFacing(state);
            BlockPos otherPos = face.getPos().offset(toOther);
            BlockState otherState = level.getBlockState(otherPos);
            if (otherState.isOf(state.getBlock()) && ChestBlock.getFacing(otherState) == toOther.getOpposite()) {
                return new InventoryIdentifier.Pair(face.getPos(), otherPos);
            }
        }

        return new InventoryIdentifier.Single(face.getPos());
    }

    private static InventoryIdentifier worldlyContainerBlock(World level, BlockState state, BlockFace face) {
        InventoryProvider holder = (InventoryProvider) state.getBlock();
        SidedInventory container = holder.getInventory(state, level, face.getPos());
        return ofWorldlyContainer(container, face);
    }

    private static InventoryIdentifier ofWorldlyContainer(SidedInventory container, BlockFace face) {
        Direction side = face.getFace();
        int[] slots = container.getAvailableSlots(side);
        // get all faces that have the same slots as the given one
        Set<Direction> directions = EnumSet.of(side);
        for (Direction direction : Iterate.directions) {
            if (direction != side) {
                int[] faceSlots = container.getAvailableSlots(direction);
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
    public static InventoryIdentifier fallback(World level, BlockState state, BlockFace face) {
        BlockEntity be = level.getBlockEntity(face.getPos());
        if (be instanceof SidedInventory container) {
            return ofWorldlyContainer(container, face);
        }

        return null;
    }
}
