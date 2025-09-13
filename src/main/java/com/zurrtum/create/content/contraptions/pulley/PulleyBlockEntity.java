package com.zurrtum.create.content.contraptions.pulley;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.content.contraptions.AbstractContraptionEntity;
import com.zurrtum.create.content.contraptions.AssemblyException;
import com.zurrtum.create.content.contraptions.ContraptionCollider;
import com.zurrtum.create.content.contraptions.ControlledContraptionEntity;
import com.zurrtum.create.content.contraptions.piston.LinearActuatorBlockEntity;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchObservable;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.state.property.Properties;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PulleyBlockEntity extends LinearActuatorBlockEntity implements ThresholdSwitchObservable {

    protected int initialOffset;
    private float prevAnimatedOffset;

    protected BlockPos mirrorParent;
    protected List<BlockPos> mirrorChildren;
    public WeakReference<AbstractContraptionEntity> sharedMirrorContraption;

    public PulleyBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public PulleyBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.ROPE_PULLEY, pos, state);
    }

    @Override
    protected Box createRenderBoundingBox() {
        double expandY = -offset;
        if (sharedMirrorContraption != null) {
            AbstractContraptionEntity ace = sharedMirrorContraption.get();
            if (ace != null)
                expandY = ace.getY() - pos.getY();
        }
        return super.createRenderBoundingBox().expand(0, expandY, 0);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.PULLEY_MAXED);
    }

    @Override
    public void tick() {
        float prevOffset = offset;
        super.tick();

        if (world.isClient() && mirrorParent != null)
            if (sharedMirrorContraption == null || sharedMirrorContraption.get() == null || !sharedMirrorContraption.get().isAlive()) {
                sharedMirrorContraption = null;
                if (world.getBlockEntity(mirrorParent) instanceof PulleyBlockEntity pte && pte.movedContraption != null)
                    sharedMirrorContraption = new WeakReference<>(pte.movedContraption);
            }

        if (isVirtual())
            prevAnimatedOffset = offset;
        invalidateRenderBoundingBox();

        if (prevOffset < 200 && offset >= 200)
            award(AllAdvancements.PULLEY_MAXED);
    }

    @Override
    protected boolean isPassive() {
        return mirrorParent != null;
    }

    @Nullable
    public AbstractContraptionEntity getAttachedContraption() {
        return mirrorParent != null && sharedMirrorContraption != null ? sharedMirrorContraption.get() : movedContraption;
    }

    @Override
    protected void assemble() throws AssemblyException {
        if (!(world.getBlockState(pos).getBlock() instanceof PulleyBlock))
            return;
        if (speed == 0 && mirrorParent == null)
            return;
        int maxLength = AllConfigs.server().kinetics.maxRopeLength.get();
        int i = 1;
        while (i <= maxLength) {
            BlockPos ropePos = pos.down(i);
            BlockState ropeState = world.getBlockState(ropePos);
            if (!ropeState.isOf(AllBlocks.ROPE) && !ropeState.isOf(AllBlocks.PULLEY_MAGNET)) {
                break;
            }
            ++i;
        }
        offset = i - 1;
        if (offset >= getExtensionRange() && getSpeed() > 0)
            return;
        if (offset <= 0 && getSpeed() < 0)
            return;

        // Collect Construct
        if (!world.isClient && mirrorParent == null) {
            needsContraption = false;
            BlockPos anchor = pos.down(MathHelper.floor(offset + 1));
            initialOffset = MathHelper.floor(offset);
            PulleyContraption contraption = new PulleyContraption(initialOffset);
            boolean canAssembleStructure = contraption.assemble(world, anchor);

            if (canAssembleStructure) {
                Direction movementDirection = getSpeed() > 0 ? Direction.DOWN : Direction.UP;
                if (ContraptionCollider.isCollidingWithWorld(world, contraption, anchor.offset(movementDirection), movementDirection))
                    canAssembleStructure = false;
            }

            if (!canAssembleStructure && getSpeed() > 0)
                return;

            removeRopes();

            if (!contraption.getBlocks().isEmpty()) {
                contraption.removeBlocksFromWorld(world, BlockPos.ORIGIN);
                movedContraption = ControlledContraptionEntity.create(world, this, contraption);
                movedContraption.setPosition(anchor.getX(), anchor.getY(), anchor.getZ());
                world.spawnEntity(movedContraption);
                forceMove = true;
                needsContraption = true;

                if (contraption.containsBlockBreakers())
                    award(AllAdvancements.CONTRAPTION_ACTORS);

                for (BlockPos pos : contraption.createColliders(world, Direction.UP)) {
                    if (pos.getY() != 0)
                        continue;
                    pos = pos.add(anchor);
                    if (world.getBlockEntity(new BlockPos(pos.getX(), this.pos.getY(), pos.getZ())) instanceof PulleyBlockEntity pbe)
                        pbe.startMirroringOther(this.pos);
                }
            }
        }

        if (mirrorParent != null)
            removeRopes();

        clientOffsetDiff = 0;
        running = true;
        sendData();
    }

    private void removeRopes() {
        for (int i = ((int) offset); i > 0; i--) {
            BlockPos offset = pos.down(i);
            BlockState oldState = world.getBlockState(offset);
            world.setBlockState(offset, oldState.getFluidState().getBlockState(), Block.NOTIFY_LISTENERS | Block.MOVED);
        }
    }

    @Override
    public void disassemble() {
        if (!running && movedContraption == null && mirrorParent == null)
            return;
        offset = getGridOffset(offset);
        if (movedContraption != null)
            resetContraptionToOffset();

        if (!world.isClient) {
            if (shouldCreateRopes()) {
                if (offset > 0) {
                    BlockPos magnetPos = pos.down((int) offset);
                    FluidState ifluidstate = world.getFluidState(magnetPos);
                    if (world.getBlockState(magnetPos).getHardness(world, magnetPos) != -1) {

                        world.breakBlock(magnetPos, world.getBlockState(magnetPos).getCollisionShape(world, magnetPos).isEmpty());
                        world.setBlockState(
                            magnetPos,
                            AllBlocks.PULLEY_MAGNET.getDefaultState()
                                .with(Properties.WATERLOGGED, Boolean.valueOf(ifluidstate.getFluid() == Fluids.WATER)),
                            Block.NOTIFY_LISTENERS | Block.MOVED
                        );
                    }
                }

                boolean[] waterlog = new boolean[(int) offset];

                for (boolean destroyPass : Iterate.trueAndFalse) {
                    for (int i = 1; i <= ((int) offset) - 1; i++) {
                        BlockPos ropePos = pos.down(i);
                        if (world.getBlockState(ropePos).getHardness(world, ropePos) == -1)
                            continue;

                        if (destroyPass) {
                            FluidState ifluidstate = world.getFluidState(ropePos);
                            waterlog[i] = ifluidstate.getFluid() == Fluids.WATER;
                            world.breakBlock(ropePos, world.getBlockState(ropePos).getCollisionShape(world, ropePos).isEmpty());
                            continue;
                        }

                        world.setBlockState(
                            pos.down(i),
                            AllBlocks.ROPE.getDefaultState().with(Properties.WATERLOGGED, waterlog[i]),
                            Block.NOTIFY_LISTENERS | Block.MOVED
                        );
                    }
                }

            }

            if (movedContraption != null && mirrorParent == null)
                movedContraption.disassemble();
            notifyMirrorsOfDisassembly();
        }

        if (movedContraption != null)
            movedContraption.discard();

        movedContraption = null;
        initialOffset = 0;
        running = false;
        sendData();
    }

    protected boolean shouldCreateRopes() {
        return !removed;
    }

    @Override
    protected Vec3d toPosition(float offset) {
        if (movedContraption.getContraption() instanceof PulleyContraption contraption) {
            return Vec3d.of(contraption.anchor).add(0, contraption.getInitialOffset() - offset, 0);

        }
        return Vec3d.ZERO;
    }

    @Override
    protected void visitNewPosition() {
        super.visitNewPosition();
        if (world.isClient)
            return;
        if (movedContraption != null)
            return;
        if (getSpeed() <= 0)
            return;

        BlockPos posBelow = pos.down((int) (offset + getMovementSpeed()) + 1);
        BlockState state = world.getBlockState(posBelow);
        if (!BlockMovementChecks.isMovementNecessary(state, world, posBelow))
            return;
        if (BlockMovementChecks.isBrittle(state))
            return;

        disassemble();
        assembleNextTick = true;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        initialOffset = view.getInt("InitialOffset", 0);
        needsContraption = view.getBoolean("NeedsContraption", false);
        super.read(view, clientPacket);

        BlockPos prevMirrorParent = mirrorParent;
        mirrorParent = view.read("MirrorParent", BlockPos.CODEC).orElse(null);
        mirrorChildren = view.read("MirrorChildren", CreateCodecs.BLOCK_POS_LIST_CODEC).map(ArrayList::new).orElse(null);

        if (mirrorParent != null) {
            offset = 0;
            if (prevMirrorParent == null || !prevMirrorParent.equals(mirrorParent))
                sharedMirrorContraption = null;
        }

        if (mirrorParent == null)
            sharedMirrorContraption = null;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        view.putInt("InitialOffset", initialOffset);
        super.write(view, clientPacket);

        if (mirrorParent != null)
            view.put("MirrorParent", BlockPos.CODEC, mirrorParent);
        if (mirrorChildren != null)
            view.put("MirrorChildren", CreateCodecs.BLOCK_POS_LIST_CODEC, mirrorChildren);
    }

    public void startMirroringOther(BlockPos parent) {
        if (parent.equals(pos))
            return;
        if (!(world.getBlockEntity(parent) instanceof PulleyBlockEntity pbe))
            return;
        if (pbe.getType() != getType())
            return;
        if (pbe.mirrorChildren == null)
            pbe.mirrorChildren = new ArrayList<>();
        pbe.mirrorChildren.add(pos);
        pbe.notifyUpdate();

        mirrorParent = parent;
        try {
            assemble();
        } catch (AssemblyException e) {
        }
        notifyUpdate();
    }

    public void notifyMirrorsOfDisassembly() {
        if (mirrorChildren == null)
            return;
        for (BlockPos blockPos : mirrorChildren) {
            if (!(world.getBlockEntity(blockPos) instanceof PulleyBlockEntity pbe))
                continue;
            pbe.offset = offset;
            pbe.disassemble();
            pbe.mirrorParent = null;
            pbe.notifyUpdate();
        }
        mirrorChildren.clear();
        notifyUpdate();
    }

    @Override
    protected int getExtensionRange() {
        return Math.max(0, Math.min(AllConfigs.server().kinetics.maxRopeLength.get(), (pos.getY() - 1) - world.getBottomY()));
    }

    @Override
    protected int getInitialOffset() {
        return initialOffset;
    }

    @Override
    protected Vec3d toMotionVector(float speed) {
        return new Vec3d(0, -speed, 0);
    }

    @Override
    public float getInterpolatedOffset(float partialTicks) {
        if (isVirtual())
            return MathHelper.lerp(partialTicks, prevAnimatedOffset, offset);
        boolean moving = running && (movedContraption == null || !movedContraption.isStalled());
        return super.getInterpolatedOffset(moving ? partialTicks : 0.5f);
    }

    public void animateOffset(float forcedOffset) {
        offset = forcedOffset;
    }

    public BlockPos getMirrorParent() {
        return mirrorParent;
    }

    // Threshold switch

    @Override
    public int getCurrentValue() {
        return pos.getY() - (int) getInterpolatedOffset(.5f);
    }

    @Override
    public int getMinValue() {
        return world.getBottomY();
    }

    @Override
    public int getMaxValue() {
        return pos.getY();
    }

    @Override
    public MutableText format(int value) {
        return Text.translatable("create.gui.threshold_switch.pulley_y_level", value);
    }

}
