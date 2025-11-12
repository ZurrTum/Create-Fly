package com.zurrtum.create.content.contraptions.pulley;

import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.TranslatingContraption;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class PulleyContraption extends TranslatingContraption {

    int initialOffset;

    @Override
    public ContraptionType getType() {
        return AllContraptionTypes.PULLEY;
    }

    public PulleyContraption() {
    }

    public PulleyContraption(int initialOffset) {
        this.initialOffset = initialOffset;
    }

    @Override
    public boolean assemble(Level world, BlockPos pos) throws AssemblyException {
        if (!searchMovedStructure(world, pos, null))
            return false;
        startMoving(world);
        return true;
    }

    @Override
    protected boolean isAnchoringBlockAt(BlockPos pos) {
        if (pos.getX() != anchor.getX() || pos.getZ() != anchor.getZ())
            return false;
        int y = pos.getY();
        if (y <= anchor.getY() || y > anchor.getY() + initialOffset + 1)
            return false;
        return true;
    }

    @Override
    public void write(ValueOutput view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.putInt("InitialOffset", initialOffset);
    }

    @Override
    public void read(Level world, ValueInput view, boolean spawnData) {
        initialOffset = view.getIntOr("InitialOffset", 0);
        super.read(world, view, spawnData);
    }

    public int getInitialOffset() {
        return initialOffset;
    }
}
