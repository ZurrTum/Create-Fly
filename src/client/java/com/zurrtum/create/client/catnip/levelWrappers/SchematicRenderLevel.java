package com.zurrtum.create.client.catnip.levelWrappers;

import com.zurrtum.create.catnip.levelWrappers.SchematicLevel;
import com.zurrtum.create.client.flywheel.lib.model.baked.VirtualBlockGetter;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.CardinalLighting;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;

public class SchematicRenderLevel extends SchematicLevel implements BlockAndTintGetter {
    public SchematicRenderLevel(Level original) {
        super(original);
    }

    public SchematicRenderLevel(BlockPos anchor, Level original) {
        super(anchor, original);
    }

    @Override
    public CardinalLighting cardinalLighting() {
        return VirtualBlockGetter.FULL_LIGHTING;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return resolver.getColor(getBiome(pos).value(), pos.getX(), pos.getZ());
    }
}
