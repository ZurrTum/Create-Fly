package com.zurrtum.create.content.decoration.placard;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class PlacardBlockEntity extends SmartBlockEntity {

    ItemStack heldItem;
    int poweredTicks;

    public PlacardBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PLACARD, pos, state);
        heldItem = ItemStack.EMPTY;
        poweredTicks = 0;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        if (!heldItem.isEmpty()) {
            Block.popResource(level, pos, heldItem);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide())
            return;
        if (poweredTicks == 0)
            return;

        poweredTicks--;
        if (poweredTicks > 0)
            return;

        BlockState blockState = getBlockState();
        level.setBlock(worldPosition, blockState.setValue(PlacardBlock.POWERED, false), Block.UPDATE_ALL);
        PlacardBlock.updateNeighbours(blockState, level, worldPosition);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(ItemStack heldItem) {
        this.heldItem = heldItem;
        notifyUpdate();
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        view.putInt("PoweredTicks", poweredTicks);
        if (!heldItem.isEmpty()) {
            view.store("Item", ItemStack.CODEC, heldItem);
        }
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        int prevTicks = poweredTicks;
        poweredTicks = view.getIntOr("PoweredTicks", 0);
        heldItem = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        super.read(view, clientPacket);

        if (clientPacket && prevTicks < poweredTicks)
            spawnParticles();
    }

    private void spawnParticles() {
        BlockState blockState = getBlockState();
        if (!blockState.is(AllBlocks.PLACARD))
            return;

        DustParticleOptions pParticleData = new DustParticleOptions(0xff3300, 1);
        Vec3 centerOf = VecHelper.getCenterOf(worldPosition);
        Vec3 normal = Vec3.atLowerCornerOf(PlacardBlock.connectedDirection(blockState).getUnitVec3i());
        Vec3 offset = VecHelper.axisAlingedPlaneOf(normal);

        for (int i = 0; i < 10; i++) {
            Vec3 v = VecHelper.offsetRandomly(Vec3.ZERO, level.random, .5f).multiply(offset).normalize().scale(.45f).add(normal.scale(-.45f))
                .add(centerOf);
            level.addParticle(pParticleData, v.x, v.y, v.z, 0, 0, 0);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

}
