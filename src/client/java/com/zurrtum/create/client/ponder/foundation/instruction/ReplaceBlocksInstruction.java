package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.function.UnaryOperator;

public class ReplaceBlocksInstruction extends WorldModifyInstruction {

    private final UnaryOperator<BlockState> stateToUse;
    private final boolean replaceAir;
    private final boolean spawnParticles;

    public ReplaceBlocksInstruction(Selection selection, UnaryOperator<BlockState> stateToUse, boolean replaceAir, boolean spawnParticles) {
        super(selection);
        this.stateToUse = stateToUse;
        this.replaceAir = replaceAir;
        this.spawnParticles = spawnParticles;
    }

    @Override
    protected void runModification(Selection selection, PonderScene scene) {
        PonderLevel level = scene.getWorld();
        selection.forEach(pos -> {
            if (!level.getBounds().contains(pos))
                return;
            BlockState prevState = level.getBlockState(pos);
            if (!replaceAir && prevState == Blocks.AIR.getDefaultState())
                return;
            if (spawnParticles)
                level.addBlockDestroyEffects(pos, prevState);
            level.setBlockState(pos, stateToUse.apply(prevState));
        });
    }

    @Override
    protected boolean needsRedraw() {
        return true;
    }

}