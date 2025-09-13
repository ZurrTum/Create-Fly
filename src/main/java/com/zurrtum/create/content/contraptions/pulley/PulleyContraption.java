package com.zurrtum.create.content.contraptions.pulley;

import com.zurrtum.create.AllContraptionTypes;
import com.zurrtum.create.api.contraption.ContraptionType;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.TranslatingContraption;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

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
    public boolean assemble(World world, BlockPos pos) throws AssemblyException {
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
    public void write(WriteView view, boolean spawnPacket) {
        super.write(view, spawnPacket);
        view.putInt("InitialOffset", initialOffset);
    }

    @Override
    public void read(World world, ReadView view, boolean spawnData) {
        initialOffset = view.getInt("InitialOffset", 0);
        super.read(world, view, spawnData);
    }

    public int getInitialOffset() {
        return initialOffset;
    }
}
