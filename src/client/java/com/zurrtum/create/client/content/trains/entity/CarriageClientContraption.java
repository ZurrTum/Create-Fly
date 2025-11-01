package com.zurrtum.create.client.content.trains.entity;

import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.entity.CarriageContraption;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class CarriageClientContraption extends ClientContraption {
    // Parallel array to renderedBlockEntityView. Marks BEs that are outside the portal.
    public final BitSet scratchBlockEntitiesOutsidePortal = new BitSet();

    public CarriageClientContraption(CarriageContraption contraption) {
        super(contraption);
    }

    @Override
    public RenderedBlocks getRenderedBlocks() {
        CarriageContraption contraption = (CarriageContraption) this.contraption;
        if (contraption.notInPortal())
            return super.getRenderedBlocks();

        Map<BlockPos, BlockState> values = new HashMap<>();
        contraption.getBlocks().forEach((pos, info) -> {
            if (contraption.withinVisible(pos)) {
                values.put(pos, info.state());
            } else if (contraption.atSeam(pos)) {
                values.put(pos, Blocks.PURPLE_STAINED_GLASS.getDefaultState());
            }
        });
        return new RenderedBlocks(pos -> values.getOrDefault(pos, Blocks.AIR.getDefaultState()), values.keySet());
    }

    @Override
    public BlockEntity readBlockEntity(World level, StructureBlockInfo info, boolean legacy) {
        if (info.state().getBlock() instanceof AbstractBogeyBlock<?> bogey && !bogey.captureBlockEntityForTrain())
            return null; // Bogeys are typically rendered by the carriage contraption, not the BE

        return super.readBlockEntity(level, info, legacy);
    }

    @Override
    public BitSet getAndAdjustShouldRenderBlockEntities() {
        CarriageContraption contraption = (CarriageContraption) this.contraption;
        if (contraption.notInPortal()) {
            return super.getAndAdjustShouldRenderBlockEntities();
        }

        scratchBlockEntitiesOutsidePortal.clear();
        scratchBlockEntitiesOutsidePortal.or(shouldRenderBlockEntities);

        for (var i = 0; i < renderedBlockEntityView.size(); i++) {
            var be = renderedBlockEntityView.get(i);
            if (contraption.isHiddenInPortal(be.getPos())) {
                scratchBlockEntitiesOutsidePortal.clear(i);
            }
        }

        return scratchBlockEntitiesOutsidePortal;
    }
}
