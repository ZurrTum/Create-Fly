package com.zurrtum.create.content.decoration.placard;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.List;

public class PlacardBlockEntity extends SmartBlockEntity {

    ItemStack heldItem;
    int poweredTicks;

    public PlacardBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PLACARD, pos, state);
        heldItem = ItemStack.EMPTY;
        poweredTicks = 0;
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        if (!heldItem.isEmpty()) {
            Block.dropStack(world, pos, heldItem);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (world.isClient)
            return;
        if (poweredTicks == 0)
            return;

        poweredTicks--;
        if (poweredTicks > 0)
            return;

        BlockState blockState = getCachedState();
        world.setBlockState(pos, blockState.with(PlacardBlock.POWERED, false), Block.NOTIFY_ALL);
        PlacardBlock.updateNeighbours(blockState, world, pos);
    }

    public ItemStack getHeldItem() {
        return heldItem;
    }

    public void setHeldItem(ItemStack heldItem) {
        this.heldItem = heldItem;
        notifyUpdate();
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        view.putInt("PoweredTicks", poweredTicks);
        if (!heldItem.isEmpty()) {
            view.put("Item", ItemStack.CODEC, heldItem);
        }
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        int prevTicks = poweredTicks;
        poweredTicks = view.getInt("PoweredTicks", 0);
        heldItem = view.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        super.read(view, clientPacket);

        if (clientPacket && prevTicks < poweredTicks)
            spawnParticles();
    }

    private void spawnParticles() {
        BlockState blockState = getCachedState();
        if (!blockState.isOf(AllBlocks.PLACARD))
            return;

        DustParticleEffect pParticleData = new DustParticleEffect(0xff3300, 1);
        Vec3d centerOf = VecHelper.getCenterOf(pos);
        Vec3d normal = Vec3d.of(PlacardBlock.connectedDirection(blockState).getVector());
        Vec3d offset = VecHelper.axisAlingedPlaneOf(normal);

        for (int i = 0; i < 10; i++) {
            Vec3d v = VecHelper.offsetRandomly(Vec3d.ZERO, world.random, .5f).multiply(offset).normalize().multiply(.45f).add(normal.multiply(-.45f))
                .add(centerOf);
            world.addParticleClient(pParticleData, v.x, v.y, v.z, 0, 0, 0);
        }
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

}
