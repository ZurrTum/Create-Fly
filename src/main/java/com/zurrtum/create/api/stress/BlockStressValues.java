package com.zurrtum.create.api.stress;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.registry.SimpleRegistry;

import java.util.function.DoubleSupplier;

import net.minecraft.world.level.block.Block;

public class BlockStressValues {
    /**
     * Registry for suppliers of stress impacts. Determine the base impact at 1 RPM.
     */
    public static final SimpleRegistry<Block, DoubleSupplier> IMPACTS = SimpleRegistry.create();
    /**
     * Registry for suppliers of stress capacities. Determine the base capacity at 1 RPM.
     */
    public static final SimpleRegistry<Block, DoubleSupplier> CAPACITIES = SimpleRegistry.create();
    /**
     * Registry for generator RPM values. This is only used for tooltips; actual functionality is determined by the block.
     */
    public static final SimpleRegistry<Block, GeneratedRpm> RPM = SimpleRegistry.create();

    public static double getImpact(Block block) {
        DoubleSupplier supplier = IMPACTS.get(block);
        return supplier == null ? 0 : supplier.getAsDouble();
    }

    public static double getCapacity(Block block) {
        DoubleSupplier supplier = CAPACITIES.get(block);
        return supplier == null ? 0 : supplier.getAsDouble();
    }

    /**
     * Shortcut for when a generator always generates the same RPM.
     */
    public static void setGeneratorSpeed(Block block, int value) {
        RPM.register(block, new GeneratedRpm(value, false));
    }

    /**
     * Utility for Registrate. Registers the given RPM generation info to blocks passed to the returned consumer.
     */
    public static void setGeneratorSpeed(Block block, int value, boolean mayGenerateLess) {
        RPM.register(block, new GeneratedRpm(value, mayGenerateLess));
    }

    public record GeneratedRpm(int value, boolean mayGenerateLess) {
    }

    private BlockStressValues() {
        throw new AssertionError("This class should not be instantiated");
    }

    public static void register() {
        BlockStressValues.setGeneratorSpeed(AllBlocks.LARGE_WATER_WHEEL, 4);
        BlockStressValues.setGeneratorSpeed(AllBlocks.WATER_WHEEL, 8);
        BlockStressValues.setGeneratorSpeed(AllBlocks.WINDMILL_BEARING, 16, true);
        BlockStressValues.setGeneratorSpeed(AllBlocks.HAND_CRANK, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.COPPER_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.WHITE_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.ORANGE_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.MAGENTA_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.LIGHT_BLUE_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.YELLOW_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.LIME_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.PINK_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.GRAY_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.LIGHT_GRAY_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.CYAN_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.PURPLE_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.BLUE_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.BROWN_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.GREEN_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.RED_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.BLACK_VALVE_HANDLE, 32);
        BlockStressValues.setGeneratorSpeed(AllBlocks.STEAM_ENGINE, 64, true);
        BlockStressValues.setGeneratorSpeed(AllBlocks.CREATIVE_MOTOR, 256, true);
    }
}