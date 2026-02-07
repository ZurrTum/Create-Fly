package com.zurrtum.create.content.kinetics.press;

import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.processing.basin.BasinBlock;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class PressingBehaviour extends BeltProcessingBehaviour {

    public static final int CYCLE = 240;
    public static final int ENTITY_SCAN = 10;

    public List<ItemStack> particleItems = new ArrayList<>();

    public PressingBehaviourSpecifics specifics;
    public int prevRunningTicks;
    public int runningTicks;
    public boolean running;
    public boolean finished;
    public Mode mode;

    int entityScanCooldown;

    public interface PressingBehaviourSpecifics {
        boolean tryProcessInBasin(boolean simulate);

        boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList);

        boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate);

        boolean canProcessInBulk();

        void onPressingCompleted();

        int getParticleAmount();

        float getKineticSpeed();
    }

    public <T extends SmartBlockEntity & PressingBehaviourSpecifics> PressingBehaviour(T be) {
        super(be);
        this.specifics = be;
        mode = Mode.WORLD;
        entityScanCooldown = ENTITY_SCAN;
        whenItemEnters((s, i) -> BeltPressingCallbacks.onItemReceived(s, i, this));
        whileItemHeld((s, i) -> BeltPressingCallbacks.whenItemHeld(s, i, this));
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        running = view.getBoolean("Running", false);
        mode = Mode.values()[view.getInt("Mode", 0)];
        finished = view.getBoolean("Finished", false);
        prevRunningTicks = runningTicks = view.getInt("Ticks", 0);
        super.read(view, clientPacket);

        if (clientPacket) {
            view.read("ParticleItems", CreateCodecs.ITEM_LIST_CODEC).ifPresent(particleItems::addAll);
            spawnParticles();
        }
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putBoolean("Running", running);
        view.putInt("Mode", mode.ordinal());
        view.putBoolean("Finished", finished);
        view.putInt("Ticks", runningTicks);
        super.write(view, clientPacket);

        if (clientPacket) {
            view.put("ParticleItems", CreateCodecs.ITEM_LIST_CODEC, particleItems);
            particleItems.clear();
        }
    }

    public float getRenderedHeadOffset(float partialTicks) {
        if (!running)
            return 0;
        int runningTicks = Math.abs(this.runningTicks);
        float ticks = MathHelper.lerp(partialTicks, prevRunningTicks, runningTicks);
        if (runningTicks < (CYCLE * 2) / 3)
            return (float) MathHelper.clamp(Math.pow(ticks / CYCLE * 2, 3), 0, 1);
        return MathHelper.clamp((CYCLE - ticks) / CYCLE * 3, 0, 1);
    }

    public void start(Mode mode) {
        this.mode = mode;
        running = true;
        prevRunningTicks = 0;
        runningTicks = 0;
        particleItems.clear();
        blockEntity.sendData();
    }

    public boolean inWorld() {
        return mode == Mode.WORLD;
    }

    public boolean onBasin() {
        return mode == Mode.BASIN;
    }

    @Override
    public void tick() {
        super.tick();

        World level = getWorld();
        BlockPos worldPosition = getPos();

        if (!running || level == null) {
            if (level != null && !level.isClient()) {

                if (specifics.getKineticSpeed() == 0)
                    return;
                if (entityScanCooldown > 0)
                    entityScanCooldown--;
                if (entityScanCooldown <= 0) {
                    entityScanCooldown = ENTITY_SCAN;

                    if (BlockEntityBehaviour.get(level, worldPosition.down(2), TransportedItemStackHandlerBehaviour.TYPE) != null)
                        return;
                    if (BasinBlock.isBasin(level, worldPosition.down(2)))
                        return;

                    for (ItemEntity itemEntity : level.getNonSpectatingEntities(ItemEntity.class, new Box(worldPosition.down()).contract(.125f))) {
                        if (!itemEntity.isAlive() || !itemEntity.isOnGround())
                            continue;
                        if (!specifics.tryProcessInWorld(itemEntity, true))
                            continue;
                        start(Mode.WORLD);
                        return;
                    }
                }

            }
            return;
        }

        if (level.isClient() && runningTicks == -CYCLE / 2) {
            prevRunningTicks = CYCLE / 2;
            return;
        }

        if (runningTicks == CYCLE / 2 && specifics.getKineticSpeed() != 0) {
            if (inWorld())
                applyInWorld();
            if (onBasin())
                applyOnBasin();

            if (level.getBlockState(worldPosition.down(2)).getSoundGroup() == BlockSoundGroup.WOOL)
                AllSoundEvents.MECHANICAL_PRESS_ACTIVATION_ON_BELT.playOnServer(level, worldPosition);
            else
                AllSoundEvents.MECHANICAL_PRESS_ACTIVATION.playOnServer(
                    level,
                    worldPosition,
                    .5f,
                    .75f + (Math.abs(specifics.getKineticSpeed()) / 1024f)
                );

            if (!level.isClient())
                blockEntity.sendData();
        }

        if (!level.isClient() && runningTicks > CYCLE) {
            finished = true;
            running = false;
            particleItems.clear();
            specifics.onPressingCompleted();
            blockEntity.sendData();
            return;
        }

        prevRunningTicks = runningTicks;
        runningTicks += getRunningTickSpeed();
        if (prevRunningTicks < CYCLE / 2 && runningTicks >= CYCLE / 2) {
            runningTicks = CYCLE / 2;
            // Pause the ticks until a packet is received
            if (level.isClient() && !blockEntity.isVirtual())
                runningTicks = -(CYCLE / 2);
        }
    }

    protected void applyOnBasin() {
        World level = getWorld();
        if (level.isClient())
            return;
        particleItems.clear();
        if (specifics.tryProcessInBasin(false))
            blockEntity.sendData();
    }

    protected void applyInWorld() {
        World level = getWorld();
        BlockPos worldPosition = getPos();
        Box bb = new Box(worldPosition.down(1));
        boolean bulk = specifics.canProcessInBulk();

        particleItems.clear();

        if (level.isClient())
            return;

        for (Entity entity : level.getOtherEntities(null, bb)) {
            if (!(entity instanceof ItemEntity itemEntity))
                continue;
            if (!entity.isAlive() || !entity.isOnGround())
                continue;

            entityScanCooldown = 0;
            if (specifics.tryProcessInWorld(itemEntity, false))
                blockEntity.sendData();
            if (!bulk)
                break;
        }
    }

    public int getRunningTickSpeed() {
        float speed = specifics.getKineticSpeed();
        if (speed == 0)
            return 0;
        return (int) MathHelper.lerp(MathHelper.clamp(Math.abs(speed) / 512f, 0, 1), 1, 60);
    }

    protected void spawnParticles() {
        if (particleItems.isEmpty())
            return;

        BlockPos worldPosition = getPos();

        if (mode == Mode.BASIN)
            particleItems.forEach(stack -> makeCompactingParticleEffect(VecHelper.getCenterOf(worldPosition.down(2)), stack));
        if (mode == Mode.BELT)
            particleItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(worldPosition.down(2)).add(0, 8 / 16f, 0), stack));
        if (mode == Mode.WORLD)
            particleItems.forEach(stack -> makePressingParticleEffect(VecHelper.getCenterOf(worldPosition.down(1)).add(0, -1 / 4f, 0), stack));

        particleItems.clear();
    }

    public void makePressingParticleEffect(Vec3d pos, ItemStack stack) {
        makePressingParticleEffect(pos, stack, specifics.getParticleAmount());
    }

    public void makePressingParticleEffect(Vec3d pos, ItemStack stack, int amount) {
        World level = getWorld();
        if (level == null || !level.isClient())
            return;
        for (int i = 0; i < amount; i++) {
            Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, level.random, .125f).multiply(1, 0, 1);
            motion = motion.add(0, amount != 1 ? 0.125f : 1 / 16f, 0);
            level.addParticleClient(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), pos.x, pos.y - .25f, pos.z, motion.x, motion.y, motion.z);
        }
    }

    public void makeCompactingParticleEffect(Vec3d pos, ItemStack stack) {
        World level = getWorld();
        if (level == null || !level.isClient())
            return;
        for (int i = 0; i < 20; i++) {
            Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, level.random, .175f).multiply(1, 0, 1);
            level.addParticleClient(new ItemStackParticleEffect(ParticleTypes.ITEM, stack), pos.x, pos.y, pos.z, motion.x, motion.y + .25f, motion.z);
        }
    }

    public enum Mode {
        WORLD(1),
        BELT(19f / 16f),
        BASIN(22f / 16f);

        public float headOffset;

        Mode(float headOffset) {
            this.headOffset = headOffset;
        }
    }

}
