package com.zurrtum.create.content.logistics.packagePort.postbox;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.content.logistics.packagePort.PackagePortBlockEntity;
import com.zurrtum.create.content.trains.station.GlobalPackagePort;
import com.zurrtum.create.content.trains.station.GlobalStation;
import net.minecraft.block.BlockState;
import net.minecraft.item.BoneMealItem;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;

import java.lang.ref.WeakReference;

public class PostboxBlockEntity extends PackagePortBlockEntity {

    public WeakReference<GlobalStation> trackedGlobalStation;

    public LerpedFloat flag;
    public boolean forceFlag;

    private boolean sendParticles;

    public PostboxBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.PACKAGE_POSTBOX, pos, state);
        trackedGlobalStation = new WeakReference<>(null);
        flag = LerpedFloat.linear().startWithValue(0);
    }

    @Override
    public void tick() {
        super.tick();
        if (!world.isClient && !isVirtual()) {
            if (sendParticles)
                sendData();
            return;
        }

        float currentTarget = flag.getChaseTarget();
        if (currentTarget == 0 || flag.settled()) {
            int target = (inventory.isEmpty() && !forceFlag) ? 0 : 1;
            if (target != currentTarget) {
                flag.chase(target, 0.1f, Chaser.LINEAR);
                if (target == 1)
                    AllSoundEvents.CONTRAPTION_ASSEMBLE.playAt(world, pos, 1, 2, true);
            }
        }
        boolean settled = flag.getValue() > .15f;
        flag.tickChaser();
        if (currentTarget == 0 && settled != flag.getValue() > .15f)
            AllSoundEvents.CONTRAPTION_DISASSEMBLE.playAt(world, pos, 0.75f, 1.5f, true);

        if (sendParticles) {
            sendParticles = false;
            BoneMealItem.createParticles(world, pos, 40);
        }
    }

    @Override
    protected void onOpenChange(boolean open) {
        world.setBlockState(pos, getCachedState().with(PostboxBlock.OPEN, open));
        world.playSound(null, pos, open ? SoundEvents.BLOCK_BARREL_OPEN : SoundEvents.BLOCK_BARREL_CLOSE, SoundCategory.BLOCKS);
    }

    public void spawnParticles() {
        sendParticles = true;
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (clientPacket && sendParticles)
            view.putBoolean("Particles", true);
        sendParticles = false;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        sendParticles = clientPacket && view.getBoolean("Particles", false);
    }

    @Override
    public void markDirty() {
        saveOfflineBuffer();
        super.markDirty();
    }

    private void saveOfflineBuffer() {
        if (world == null || world.isClient) {
            return;
        }
        GlobalStation station = trackedGlobalStation.get();
        if (station == null) {
            return;
        }
        GlobalPackagePort globalPackagePort = station.connectedPorts.get(pos);
        if (globalPackagePort == null) {
            return;
        }
        globalPackagePort.saveOfflineBuffer(inventory);
    }
}