package com.zurrtum.create.content.kinetics.mechanicalArm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.Create;
import com.zurrtum.create.api.contraption.transformable.TransformableBlockEntity;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.kinetics.base.KineticBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.AllArmInteractionPointTypes.JukeboxPoint;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint.Mode;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.scrollValue.ServerScrollOptionBehaviour;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ArmBlockEntity extends KineticBlockEntity implements TransformableBlockEntity {
    public Codec<ArmInteractionPoint> pointCodec;

    // Server
    public List<ArmInteractionPoint> inputs;
    public List<ArmInteractionPoint> outputs;
    public NbtList interactionPointTag;

    // Both
    float chasedPointProgress;
    int chasedPointIndex;
    public ItemStack heldItem;
    public Phase phase;
    public boolean goggles;

    // Client
    ArmAngleTarget previousTarget;
    public LerpedFloat lowerArmAngle;
    public LerpedFloat upperArmAngle;
    public LerpedFloat baseAngle;
    public LerpedFloat headAngle;
    LerpedFloat clawAngle;
    float previousBaseAngle;
    boolean updateInteractionPoints;
    public int tooltipWarmup;

    protected ServerScrollOptionBehaviour<SelectionMode> selectionMode;
    protected int lastInputIndex = -1;
    protected int lastOutputIndex = -1;
    protected boolean redstoneLocked;

    public enum Phase implements StringIdentifiable {
        SEARCH_INPUTS,
        MOVE_TO_INPUT,
        SEARCH_OUTPUTS,
        MOVE_TO_OUTPUT,
        DANCING;

        public static final Codec<Phase> CODEC = StringIdentifiable.createCodec(Phase::values);

        @Override
        public String asString() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public ArmBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_ARM, pos, state);
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
        interactionPointTag = null;
        heldItem = ItemStack.EMPTY;
        phase = Phase.SEARCH_INPUTS;
        previousTarget = ArmAngleTarget.NO_TARGET;
        baseAngle = LerpedFloat.angular();
        baseAngle.startWithValue(previousTarget.baseAngle);
        lowerArmAngle = LerpedFloat.angular();
        lowerArmAngle.startWithValue(previousTarget.lowerArmAngle);
        upperArmAngle = LerpedFloat.angular();
        upperArmAngle.startWithValue(previousTarget.upperArmAngle);
        headAngle = LerpedFloat.angular();
        headAngle.startWithValue(previousTarget.headAngle);
        clawAngle = LerpedFloat.angular();
        previousBaseAngle = previousTarget.baseAngle;
        updateInteractionPoints = true;
        redstoneLocked = false;
        tooltipWarmup = 15;
        goggles = false;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        selectionMode = new ServerScrollOptionBehaviour<>(SelectionMode.class, this);
        behaviours.add(selectionMode);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(
            AllAdvancements.ARM_BLAZE_BURNER,
            AllAdvancements.ARM_MANY_TARGETS,
            AllAdvancements.MECHANICAL_ARM,
            AllAdvancements.MUSICAL_ARM
        );
    }

    @Override
    public void tick() {
        super.tick();
        initInteractionPoints();
        boolean targetReached = tickMovementProgress();

        if (tooltipWarmup > 0)
            tooltipWarmup--;
        if (chasedPointProgress < 1) {
            if (phase == Phase.MOVE_TO_INPUT) {
                ArmInteractionPoint point = getTargetedInteractionPoint();
                if (point != null)
                    point.keepAlive();
            }
            return;
        }
        if (world.isClient)
            return;

        if (phase == Phase.MOVE_TO_INPUT)
            collectItem();
        else if (phase == Phase.MOVE_TO_OUTPUT)
            depositItem();
        else if (phase == Phase.SEARCH_INPUTS || phase == Phase.DANCING)
            searchForItem();

        if (targetReached)
            lazyTick();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();

        if (world.isClient)
            return;
        if (chasedPointProgress < .5f)
            return;
        if (phase == Phase.SEARCH_INPUTS || phase == Phase.DANCING)
            checkForMusic();
        if (phase == Phase.SEARCH_OUTPUTS)
            searchForDestination();
    }

    private void checkForMusic() {
        boolean hasMusic = checkForMusicAmong(inputs) || checkForMusicAmong(outputs);
        if (hasMusic != (phase == Phase.DANCING)) {
            phase = hasMusic ? Phase.DANCING : Phase.SEARCH_INPUTS;
            markDirty();
            sendData();
        }
    }

    @Override
    protected Box createRenderBoundingBox() {
        return super.createRenderBoundingBox().expand(3);
    }

    private boolean checkForMusicAmong(List<ArmInteractionPoint> list) {
        for (ArmInteractionPoint armInteractionPoint : list) {
            if (!(armInteractionPoint instanceof JukeboxPoint))
                continue;
            BlockState state = world.getBlockState(armInteractionPoint.getPos());
            if (state.get(JukeboxBlock.HAS_RECORD, false))
                return true;
        }
        return false;
    }

    private boolean tickMovementProgress() {
        boolean targetReachedPreviously = chasedPointProgress >= 1;
        chasedPointProgress += Math.min(256, Math.abs(getSpeed())) / 1024f;
        if (chasedPointProgress > 1)
            chasedPointProgress = 1;
        if (!world.isClient)
            return !targetReachedPreviously && chasedPointProgress >= 1;

        ArmInteractionPoint targetedInteractionPoint = getTargetedInteractionPoint();
        ArmAngleTarget previousTarget = this.previousTarget;
        ArmAngleTarget target = targetedInteractionPoint == null ? ArmAngleTarget.NO_TARGET : targetedInteractionPoint.getTargetAngles(
            pos,
            isOnCeiling()
        );

        baseAngle.setValue(AngleHelper.angleLerp(
            chasedPointProgress,
            previousBaseAngle,
            target == ArmAngleTarget.NO_TARGET ? previousBaseAngle : target.baseAngle
        ));

        // Arm's angles first backup to resting position and then continue
        if (chasedPointProgress < .5f)
            target = ArmAngleTarget.NO_TARGET;
        else
            previousTarget = ArmAngleTarget.NO_TARGET;
        float progress = chasedPointProgress == 1 ? 1 : (chasedPointProgress % .5f) * 2;

        lowerArmAngle.setValue(MathHelper.lerp(progress, previousTarget.lowerArmAngle, target.lowerArmAngle));
        upperArmAngle.setValue(MathHelper.lerp(progress, previousTarget.upperArmAngle, target.upperArmAngle));
        headAngle.setValue(AngleHelper.angleLerp(progress, previousTarget.headAngle % 360, target.headAngle % 360));

        return false;
    }

    protected boolean isOnCeiling() {
        BlockState state = getCachedState();
        return hasWorld() && state.get(ArmBlock.CEILING, false);
    }

    @Override
    public void destroy() {
        super.destroy();
        if (!heldItem.isEmpty())
            Block.dropStack(world, pos, heldItem);
    }

    @Nullable
    private ArmInteractionPoint getTargetedInteractionPoint() {
        if (chasedPointIndex == -1)
            return null;
        if (phase == Phase.MOVE_TO_INPUT && chasedPointIndex < inputs.size())
            return inputs.get(chasedPointIndex);
        if (phase == Phase.MOVE_TO_OUTPUT && chasedPointIndex < outputs.size())
            return outputs.get(chasedPointIndex);
        return null;
    }

    protected void searchForItem() {
        if (redstoneLocked)
            return;

        boolean foundInput = false;
        // for round robin, we start looking after the last used index, for default we
        // start at 0;
        int startIndex = selectionMode.get() == SelectionMode.PREFER_FIRST ? 0 : lastInputIndex + 1;

        // if we enforce round robin, only look at the next input in the list,
        // otherwise, look at all inputs
        int scanRange = selectionMode.get() == SelectionMode.FORCED_ROUND_ROBIN ? lastInputIndex + 2 : inputs.size();
        if (scanRange > inputs.size())
            scanRange = inputs.size();

        InteractionPoints:
        for (int i = startIndex; i < scanRange; i++) {
            ArmInteractionPoint armInteractionPoint = inputs.get(i);
            if (!armInteractionPoint.isValid())
                continue;
            for (int j = 0; j < armInteractionPoint.getSlotCount(this); j++) {
                if (getDistributableAmount(armInteractionPoint, j) == 0)
                    continue;

                selectIndex(true, i);
                foundInput = true;
                break InteractionPoints;
            }
        }
        if (!foundInput && selectionMode.get() == SelectionMode.ROUND_ROBIN) {
            // if we didn't find an input, but don't want to enforce round robin, reset the
            // last index
            lastInputIndex = -1;
        }
        if (lastInputIndex == inputs.size() - 1) {
            // if we reached the last input in the list, reset the last index
            lastInputIndex = -1;
        }
    }

    protected void searchForDestination() {
        ItemStack held = heldItem.copy();

        boolean foundOutput = false;
        // for round robin, we start looking after the last used index, for default we
        // start at 0;
        int startIndex = selectionMode.get() == SelectionMode.PREFER_FIRST ? 0 : lastOutputIndex + 1;

        // if we enforce round robin, only look at the next index in the list,
        // otherwise, look at all
        int scanRange = selectionMode.get() == SelectionMode.FORCED_ROUND_ROBIN ? lastOutputIndex + 2 : outputs.size();
        if (scanRange > outputs.size())
            scanRange = outputs.size();

        for (int i = startIndex; i < scanRange; i++) {
            ArmInteractionPoint armInteractionPoint = outputs.get(i);
            if (!armInteractionPoint.isValid())
                continue;

            ItemStack remainder = armInteractionPoint.insert(this, held, true);
            if (ItemStack.areEqual(remainder, heldItem))
                continue;

            selectIndex(false, i);
            foundOutput = true;
            break;
        }

        if (!foundOutput && selectionMode.get() == SelectionMode.ROUND_ROBIN) {
            // if we didn't find an input, but don't want to enforce round robin, reset the
            // last index
            lastOutputIndex = -1;
        }
        if (lastOutputIndex == outputs.size() - 1) {
            // if we reached the last input in the list, reset the last index
            lastOutputIndex = -1;
        }
    }

    // input == true => select input, false => select output
    private void selectIndex(boolean input, int index) {
        phase = input ? Phase.MOVE_TO_INPUT : Phase.MOVE_TO_OUTPUT;
        chasedPointIndex = index;
        chasedPointProgress = 0;
        if (input)
            lastInputIndex = index;
        else
            lastOutputIndex = index;
        sendData();
        markDirty();
    }

    protected int getDistributableAmount(ArmInteractionPoint armInteractionPoint, int i) {
        ItemStack stack = armInteractionPoint.extract(this, i, true);
        ItemStack remainder = simulateInsertion(stack);
        if (ItemStack.areItemsEqual(stack, remainder)) {
            return stack.getCount() - remainder.getCount();
        } else {
            return stack.getCount();
        }
    }

    private ItemStack simulateInsertion(ItemStack stack) {
        for (ArmInteractionPoint armInteractionPoint : outputs) {
            if (armInteractionPoint.isValid())
                stack = armInteractionPoint.insert(this, stack, true);
            if (stack.isEmpty())
                break;
        }
        return stack;
    }

    protected void depositItem() {
        ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
        if (armInteractionPoint != null && armInteractionPoint.isValid()) {
            ItemStack toInsert = heldItem.copy();
            ItemStack remainder = armInteractionPoint.insert(this, toInsert, false);
            heldItem = remainder;

            if (armInteractionPoint instanceof JukeboxPoint && remainder.isEmpty())
                award(AllAdvancements.MUSICAL_ARM);
        }

        phase = heldItem.isEmpty() ? Phase.SEARCH_INPUTS : Phase.SEARCH_OUTPUTS;
        chasedPointProgress = 0;
        chasedPointIndex = -1;
        sendData();
        markDirty();

        if (!world.isClient)
            award(AllAdvancements.MECHANICAL_ARM);
    }

    protected void collectItem() {
        ArmInteractionPoint armInteractionPoint = getTargetedInteractionPoint();
        if (armInteractionPoint != null && armInteractionPoint.isValid())
            for (int i = 0; i < armInteractionPoint.getSlotCount(this); i++) {
                int amountExtracted = getDistributableAmount(armInteractionPoint, i);
                if (amountExtracted == 0)
                    continue;

                ItemStack prevHeld = heldItem;
                heldItem = armInteractionPoint.extract(this, i, amountExtracted, false);
                phase = Phase.SEARCH_OUTPUTS;
                chasedPointProgress = 0;
                chasedPointIndex = -1;
                sendData();
                markDirty();

                if (!ItemStack.areItemsEqual(heldItem, prevHeld))
                    world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, .125f, .5f + world.random.nextFloat() * .25f);
                return;
            }

        phase = Phase.SEARCH_INPUTS;
        chasedPointProgress = 0;
        chasedPointIndex = -1;
        sendData();
        markDirty();
    }

    public void redstoneUpdate() {
        if (world.isClient)
            return;
        boolean blockPowered = world.isReceivingRedstonePower(pos);
        if (blockPowered == redstoneLocked)
            return;
        redstoneLocked = blockPowered;
        sendData();
        if (!redstoneLocked)
            searchForItem();
    }

    @Override
    public void transform(BlockEntity be, StructureTransform transform) {
        if (interactionPointTag == null)
            return;

        for (NbtElement tag : interactionPointTag) {
            ArmInteractionPoint.transformPos((NbtCompound) tag, transform);
        }

        notifyUpdate();
    }

    // ClientLevel#hasChunk (and consequently #isAreaLoaded) always returns true,
    // so manually check the ChunkSource to avoid weird behavior on the client side
    protected boolean isAreaActuallyLoaded(BlockPos center, int range) {
        if (!world.isRegionLoaded(center.add(-range, -range, -range), center.add(range, range, range))) {
            return false;
        }
        if (world.isClient) {
            int minY = center.getY() - range;
            int maxY = center.getY() + range;
            if (maxY < world.getBottomY() || minY >= world.getTopYInclusive()) {
                return false;
            }

            int minX = center.getX() - range;
            int minZ = center.getZ() - range;
            int maxX = center.getX() + range;
            int maxZ = center.getZ() + range;

            int minChunkX = ChunkSectionPos.getSectionCoord(minX);
            int maxChunkX = ChunkSectionPos.getSectionCoord(maxX);
            int minChunkZ = ChunkSectionPos.getSectionCoord(minZ);
            int maxChunkZ = ChunkSectionPos.getSectionCoord(maxZ);

            ChunkManager chunkSource = world.getChunkManager();
            for (int chunkX = minChunkX; chunkX <= maxChunkX; ++chunkX) {
                for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; ++chunkZ) {
                    if (!chunkSource.isChunkLoaded(chunkX, chunkZ)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    protected void initInteractionPoints() {
        if (!updateInteractionPoints || interactionPointTag == null)
            return;
        if (!isAreaActuallyLoaded(pos, getRange() + 1))
            return;
        inputs.clear();
        outputs.clear();

        boolean hasBlazeBurner = false;
        if (pointCodec == null) {
            pointCodec = ArmInteractionPoint.getCodec(world, pos);
        }
        for (NbtElement tag : interactionPointTag) {
            ArmInteractionPoint point = decodePoint(tag);
            if (point == null)
                continue;
            BlockState state = world.getBlockState(point.pos);
            if (!point.type.canCreatePoint(world, point.pos, state)) {
                continue;
            }
            if (point.getMode() == Mode.DEPOSIT)
                outputs.add(point);
            else if (point.getMode() == Mode.TAKE)
                inputs.add(point);
            hasBlazeBurner |= point instanceof AllArmInteractionPointTypes.BlazeBurnerPoint;
        }

        if (!world.isClient) {
            if (outputs.size() >= 10)
                award(AllAdvancements.ARM_MANY_TARGETS);
            if (hasBlazeBurner)
                award(AllAdvancements.ARM_BLAZE_BURNER);
        }

        updateInteractionPoints = false;
        sendData();
        markDirty();
    }

    public void writeInteractionPoints(WriteView view) {
        if (pointCodec == null) {
            pointCodec = ArmInteractionPoint.getCodec(world, pos);
        }
        NbtList list;
        if (updateInteractionPoints && interactionPointTag != null) {
            list = interactionPointTag;
        } else {
            list = new NbtList();
            appendEncodedPoints(inputs, pointCodec, list);
            appendEncodedPoints(outputs, pointCodec, list);
        }
        view.put("InteractionPoints", CreateCodecs.NBT_LIST_CODEC, list);
    }

    public static void appendEncodedPoints(List<ArmInteractionPoint> points, Codec<ArmInteractionPoint> pointCodec, NbtList list) {
        for (ArmInteractionPoint point : points) {
            switch (pointCodec.encodeStart(NbtOps.INSTANCE, point)) {
                case DataResult.Success<NbtElement> success -> list.add(success.value());
                case DataResult.Error<NbtElement> error ->
                    Create.LOGGER.warn("Failed to append value '{}' to list 'InteractionPoints': {}", point, error.message());
            }
        }
    }

    public ArmInteractionPoint decodePoint(NbtElement tag) {
        return switch (pointCodec.parse(NbtOps.INSTANCE, tag)) {
            case DataResult.Success<ArmInteractionPoint> success -> success.value();
            case DataResult.Error<ArmInteractionPoint> error -> {
                Create.LOGGER.warn("Failed to decode value '{}' from field 'InteractionPoints': {}", tag, error.message());
                yield null;
            }
        };
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);

        writeInteractionPoints(view);

        view.put("Phase", Phase.CODEC, phase);
        view.putBoolean("Powered", redstoneLocked);
        view.putBoolean("Goggles", goggles);
        if (!heldItem.isEmpty()) {
            view.put("HeldItem", ItemStack.CODEC, heldItem);
        }
        view.putInt("TargetPointIndex", chasedPointIndex);
        view.putFloat("MovementProgress", chasedPointProgress);
    }

    @Override
    public void writeSafe(WriteView view) {
        super.writeSafe(view);

        writeInteractionPoints(view);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        int previousIndex = chasedPointIndex;
        Phase previousPhase = phase;
        NbtList interactionPointTagBefore = interactionPointTag;

        super.read(view, clientPacket);
        heldItem = view.read("HeldItem", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        phase = view.read("Phase", Phase.CODEC).orElse(Phase.SEARCH_INPUTS);
        chasedPointIndex = view.getInt("TargetPointIndex", 0);
        chasedPointProgress = view.getFloat("MovementProgress", 0);
        interactionPointTag = view.read("InteractionPoints", CreateCodecs.NBT_LIST_CODEC).orElseGet(NbtList::new);
        redstoneLocked = view.getBoolean("Powered", false);

        boolean hadGoggles = goggles;
        goggles = view.getBoolean("Goggles", false);

        if (!clientPacket)
            return;

        if (hadGoggles != goggles && world.isClient)
            AllClientHandle.INSTANCE.queueUpdate(this);

        boolean ceiling = isOnCeiling();
        if (interactionPointTagBefore == null || interactionPointTagBefore.size() != interactionPointTag.size())
            updateInteractionPoints = true;
        if (previousIndex != chasedPointIndex || (previousPhase != phase)) {
            ArmInteractionPoint previousPoint = null;
            if (previousPhase == Phase.MOVE_TO_INPUT && previousIndex < inputs.size())
                previousPoint = inputs.get(previousIndex);
            if (previousPhase == Phase.MOVE_TO_OUTPUT && previousIndex < outputs.size())
                previousPoint = outputs.get(previousIndex);
            previousTarget = previousPoint == null ? ArmAngleTarget.NO_TARGET : previousPoint.getTargetAngles(pos, ceiling);
            if (previousPoint != null)
                previousBaseAngle = previousTarget.baseAngle;

            ArmInteractionPoint targetedPoint = getTargetedInteractionPoint();
            if (targetedPoint != null)
                targetedPoint.updateCachedState();
        }
    }

    public static int getRange() {
        return AllConfigs.server().logistics.mechanicalArmRange.get();
    }

    public void setLevel(World level) {
        super.setWorld(level);
        for (ArmInteractionPoint input : inputs) {
            input.setLevel(level);
        }
        for (ArmInteractionPoint output : outputs) {
            output.setLevel(level);
        }
    }

    public enum SelectionMode {
        ROUND_ROBIN,
        FORCED_ROUND_ROBIN,
        PREFER_FIRST
    }
}
