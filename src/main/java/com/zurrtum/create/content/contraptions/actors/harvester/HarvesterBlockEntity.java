package com.zurrtum.create.content.contraptions.actors.harvester;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.foundation.blockEntity.CachedRenderBBBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public class HarvesterBlockEntity extends CachedRenderBBBlockEntity {

    // For simulations such as Ponder
    private float manuallyAnimatedSpeed;

    public HarvesterBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.HARVESTER, pos, state);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition);
    }

    public float getAnimatedSpeed() {
        return manuallyAnimatedSpeed;
    }

    public void setAnimatedSpeed(float speed) {
        manuallyAnimatedSpeed = speed;
    }

}
