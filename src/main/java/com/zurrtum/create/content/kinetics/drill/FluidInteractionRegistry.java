package com.zurrtum.create.content.kinetics.drill;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllFluids;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.fluid.FlowableFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Function;

/**
 * A registry which defines the interactions a source fluid can have with its
 * surroundings. Each possible flow direction is checked for all interactions with
 * the source.
 *
 * <p>Fluid interactions mimic the behavior of {@code FluidBlock#receiveNeighborFluids}.
 * As such, all directions, besides {@link Direction#DOWN} is tested and then replaced.
 * Any fluids which cause a change in the down interaction must be handled in
 * {@code FlowingFluid#flow} and not by this interaction manager.
 */
public final class FluidInteractionRegistry {
    public static final Map<Fluid, List<InteractionInformation>> INTERACTIONS = new HashMap<>();

    /**
     * Adds an interaction between a source and its surroundings.
     *
     * @param source      the source of the interaction, this will be replaced if the interaction occurs
     * @param interaction the interaction data to check and perform
     */
    public static synchronized void addInteraction(Fluid source, InteractionInformation interaction) {
        if (source instanceof FlowableFluid flowableFluid) {
            INTERACTIONS.computeIfAbsent(flowableFluid.getStill(), s -> new ArrayList<>()).add(interaction);
            INTERACTIONS.computeIfAbsent(flowableFluid.getFlowing(), s -> new ArrayList<>()).add(interaction);
        } else {
            INTERACTIONS.computeIfAbsent(source, s -> new ArrayList<>()).add(interaction);
        }
    }

    /**
     * Performs all potential fluid interactions at a given position.
     *
     * <p>Note: Only the first interaction check that succeeds will occur.
     *
     * @param level the level the interactions take place in
     * @param pos   the position of the source fluid
     * @return {@code true} if an interaction took place, {@code false} otherwise
     */
    public static boolean canInteract(World level, BlockPos pos) {
        FluidState state = level.getFluidState(pos);
        Fluid fluid = state.getFluid();
        for (Direction direction : FluidBlock.FLOW_DIRECTIONS) {
            BlockPos relativePos = pos.offset(direction.getOpposite());
            List<InteractionInformation> interactions = INTERACTIONS.getOrDefault(fluid, Collections.emptyList());
            for (InteractionInformation interaction : interactions) {
                if (interaction.predicate().test(level, pos, relativePos, state)) {
                    interaction.interaction().interact(level, pos, relativePos, state);
                    return true;
                }
            }
        }

        return false;
    }

    static {
        // Lava + Water = Obsidian (Source Lava) / Cobblestone (Flowing Lava)
        addInteraction(
            Fluids.LAVA,
            new InteractionInformation(
                Fluids.WATER,
                fluidState -> fluidState.isStill() ? Blocks.OBSIDIAN.getDefaultState() : Blocks.COBBLESTONE.getDefaultState()
            )
        );

        // Lava + Soul Soil (Below) + Blue Ice = Basalt
        addInteraction(
            Fluids.LAVA,
            new InteractionInformation(
                (level, currentPos, relativePos, currentState) -> level.getBlockState(currentPos.down())
                    .isOf(Blocks.SOUL_SOIL) && level.getBlockState(relativePos).isOf(Blocks.BLUE_ICE), Blocks.BASALT.getDefaultState()
            )
        );
        addInteraction(
            Fluids.LAVA, new InteractionInformation(
                AllFluids.HONEY, fluidState -> {
                if (fluidState.isStill()) {
                    return Blocks.OBSIDIAN.getDefaultState();
                } else {
                    return AllBlocks.LIMESTONE.getDefaultState();
                }
            }
            )
        );
        addInteraction(
            Fluids.LAVA, new InteractionInformation(
                AllFluids.CHOCOLATE, fluidState -> {
                if (fluidState.isStill()) {
                    return Blocks.OBSIDIAN.getDefaultState();
                } else {
                    return AllBlocks.SCORIA.getDefaultState();
                }
            }
            )
        );
    }

    /**
     * Holds the interaction data for a given source type on when to succeed
     * and what to perform.
     *
     * @param predicate   a test to see whether an interaction can occur
     * @param interaction the interaction to perform
     */
    public record InteractionInformation(HasFluidInteraction predicate, FluidInteraction interaction) {
        /**
         * Constructor which checks the surroundings fluids for a specific type
         * and then transforms the source state into a block.
         *
         * @param type  the type of the fluid that must be surrounding the source
         * @param state the state of the block replacing the source
         */
        public InteractionInformation(Fluid type, BlockState state) {
            this(type, fluidState -> state);
        }

        /**
         * Constructor which transforms the source state into a block.
         *
         * @param predicate a test to see whether an interaction can occur
         * @param state     the state of the block replacing the source
         */
        public InteractionInformation(HasFluidInteraction predicate, BlockState state) {
            this(predicate, fluidState -> state);
        }

        /**
         * Constructor which checks the surroundings fluids for a specific type
         * and then transforms the source state into a block.
         *
         * @param type     the type of the fluid that must be surrounding the source
         * @param getState a function to transform the source fluid into a block state
         */
        public InteractionInformation(Fluid type, Function<FluidState, BlockState> getState) {
            this(
                (level, currentPos, relativePos, currentState) -> {
                    FluidState state = level.getFluidState(relativePos);
                    Fluid fluid = state.getFluid();
                    if (!fluid.isStill(state) && fluid instanceof FlowableFluid flowableFluid) {
                        fluid = flowableFluid.getStill();
                    }
                    return fluid == type;
                }, getState
            );
        }

        /**
         * Constructor which transforms the source state into a block.
         *
         * @param predicate a test to see whether an interaction can occur
         * @param getState  a function to transform the source fluid into a block state
         */
        public InteractionInformation(HasFluidInteraction predicate, Function<FluidState, BlockState> getState) {
            this(
                predicate, (level, currentPos, relativePos, currentState) -> {
                    level.setBlockState(currentPos, getState.apply(currentState));
                    level.syncWorldEvent(1501, currentPos, 0);
                }
            );
        }
    }

    /**
     * An interface which tests whether a source fluid can interact with its
     * surroundings.
     */
    @FunctionalInterface
    public interface HasFluidInteraction {
        /**
         * Returns whether the interaction can occur.
         *
         * @param level        the level the interaction takes place in
         * @param currentPos   the position of the source
         * @param relativePos  a position surrounding the source
         * @param currentState the state of the fluid surrounding the source
         * @return {@code true} if an interaction can occur, {@code false} otherwise
         */
        boolean test(World level, BlockPos currentPos, BlockPos relativePos, FluidState currentState);
    }

    /**
     * An interface which performs an interaction for a source.
     */
    @FunctionalInterface
    public interface FluidInteraction {
        /**
         * Performs the interaction between the source and the surrounding data.
         *
         * @param level        the level the interaction takes place in
         * @param currentPos   the position of the source
         * @param relativePos  a position surrounding the source
         * @param currentState the state of the fluid surrounding the source
         */
        void interact(World level, BlockPos currentPos, BlockPos relativePos, FluidState currentState);
    }
}