package com.zurrtum.create.content.kinetics.drill;

import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import com.zurrtum.create.infrastructure.fluids.FluidInteractionRegistry;
import com.zurrtum.create.infrastructure.fluids.FluidInteractionRegistry.FluidInteraction;
import com.zurrtum.create.infrastructure.fluids.FluidInteractionRegistry.HasFluidInteraction;
import com.zurrtum.create.infrastructure.fluids.FluidInteractionRegistry.InteractionInformation;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CobbleGenOptimisation {
    static CobbleGenLevel cachedLevel;

    public record CobbleGenBlockConfiguration(List<BlockState> statesAroundDrill) {
    }

    @Nullable
    public static CobbleGenBlockConfiguration getConfig(WorldAccess level, BlockPos drillPos, Direction drillDirection) {
        List<BlockState> list = new ArrayList<>();
        for (Direction side : Iterate.directions) {
            BlockPos relative = drillPos.offset(drillDirection).offset(side);
            if (level instanceof World l && !l.isPosLoaded(relative))
                return null;
            list.add(level.getBlockState(relative));
        }
        return new CobbleGenBlockConfiguration(list);
    }

    public static BlockState determineOutput(ServerWorld level, BlockPos pos, CobbleGenBlockConfiguration config) {
        Map<Fluid, List<InteractionInformation>> interactions = FluidInteractionRegistry.INTERACTIONS;
        Map<Fluid, Pair<Direction, FluidState>> presentFluidTypes = new HashMap<>();

        for (int i = 0; i < Iterate.directions.length; i++) {
            if (config.statesAroundDrill.size() <= i)
                break;
            FluidState fluidState = config.statesAroundDrill.get(i).getFluidState();
            if (!fluidState.isEmpty()) {
                Fluid fluid = fluidState.getFluid();
                if (interactions.get(fluid) != null)
                    presentFluidTypes.put(fluid, Pair.of(Iterate.directions[i], fluidState));
            }
        }

        FluidInteraction interaction = null;
        Pair<Direction, FluidState> affected = null;

        Search:
        for (Map.Entry<Fluid, Pair<Direction, FluidState>> type : presentFluidTypes.entrySet()) {
            List<InteractionInformation> list = interactions.get(type.getKey());
            FluidState state = FluidHelper.convertToFlowing(type.getValue().getSecond().getFluid()).getDefaultState();

            if (list == null)
                continue;
            for (Direction d : Iterate.horizontalDirections) {
                for (InteractionInformation information : list) {
                    if (d == type.getValue().getFirst())
                        continue;
                    BlockPos relative = pos.offset(d);
                    HasFluidInteraction predicate = information.predicate();
                    if (!predicate.test(level, pos, relative, state))
                        continue;
                    interaction = information.interaction();
                    affected = Pair.of(d, state);
                    break Search;
                }
            }
        }

        ServerWorld owLevel = level.getServer().getWorld(World.OVERWORLD);
        if (owLevel == null)
            owLevel = level;

        if (cachedLevel == null || cachedLevel.getLevel() != owLevel)
            cachedLevel = new CobbleGenLevel(level);

        BlockState result = Blocks.AIR.getDefaultState();
        if (interaction == null)
            return result;

        interaction.interact(cachedLevel, pos, pos.offset(affected.getFirst()), affected.getSecond());
        BlockState output = cachedLevel.blocksAdded.getOrDefault(pos, result);
        cachedLevel.clear();

        return output;
    }

    public static void invalidateWorld(WorldAccess world) {
        if (cachedLevel != null && cachedLevel.getLevel() == world)
            cachedLevel = null;
    }
}
