package com.zurrtum.create.content.decoration.bracket;

import com.zurrtum.create.content.contraptions.StructureTransform;
import com.zurrtum.create.content.schematics.requirement.ItemRequirement;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class BracketedBlockEntityBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {

    public static final BehaviourType<BracketedBlockEntityBehaviour> TYPE = new BehaviourType<>();

    private BlockState bracket;
    private boolean reRender;

    private Predicate<BlockState> pred;

    public BracketedBlockEntityBehaviour(SmartBlockEntity be) {
        this(be, state -> true);
    }

    public BracketedBlockEntityBehaviour(SmartBlockEntity be, Predicate<BlockState> pred) {
        super(be);
        this.pred = pred;
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public void applyBracket(BlockState state) {
        this.bracket = state;
        reRender = true;
        blockEntity.notifyUpdate();
        World world = getWorld();
        if (world.isClient())
            return;
        blockEntity.getCachedState().updateNeighbors(world, getPos(), 3);
    }

    public void transformBracket(StructureTransform transform) {
        if (isBracketPresent()) {
            BlockState transformedBracket = transform.apply(bracket);
            applyBracket(transformedBracket);
        }
    }

    @Nullable
    public BlockState removeBracket(boolean inOnReplacedContext) {
        if (bracket == null) {
            return null;
        }

        BlockState removed = this.bracket;
        World world = getWorld();
        if (!world.isClient())
            world.syncWorldEvent(WorldEvents.BLOCK_BROKEN, getPos(), Block.getRawIdFromState(bracket));
        this.bracket = null;
        reRender = true;
        if (inOnReplacedContext) {
            blockEntity.sendData();
            return removed;
        }
        blockEntity.notifyUpdate();
        if (world.isClient())
            return removed;
        blockEntity.getCachedState().updateNeighbors(world, getPos(), 3);
        return removed;
    }

    public boolean isBracketPresent() {
        return bracket != null;
    }

    public boolean isBracketValid(BlockState bracketState) {
        return bracketState.getBlock() instanceof BracketBlock;
    }

    @Nullable
    public BlockState getBracket() {
        return bracket;
    }

    public boolean canHaveBracket() {
        return pred.test(blockEntity.getCachedState());
    }

    @Override
    public ItemRequirement getRequiredItems() {
        if (!isBracketPresent()) {
            return ItemRequirement.NONE;
        }
        return ItemRequirement.of(bracket, null);
    }

    @Override
    public boolean isSafeNBT() {
        return true;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        if (isBracketPresent() && isBracketValid(bracket)) {
            view.put("Bracket", BlockState.CODEC, bracket);
        }
        if (clientPacket && reRender) {
            view.putBoolean("Redraw", true);
            reRender = false;
        }
        super.write(view, clientPacket);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        view.read("Bracket", BlockState.CODEC).ifPresent(state -> {
            bracket = null;
            if (isBracketValid(state))
                bracket = state;
        });
        if (clientPacket && view.getBoolean("Redraw", false))
            getWorld().updateListeners(getPos(), blockEntity.getCachedState(), blockEntity.getCachedState(), 16);
        super.read(view, clientPacket);
    }

}
