package com.zurrtum.create.content.redstone.nixieTube;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.utility.DynamicComponent;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class NixieTubeBlockEntity extends SmartBlockEntity {
    private int redstoneStrength;
    private Optional<Component> customText;
    private int nixieIndex;
    private Couple<String> displayedStrings;
    private boolean keepAlive;

    private WeakReference<SignalBlockEntity> cachedSignalTE;
    public SignalState signalState;

    public NixieTubeBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.NIXIE_TUBE, pos, state);
        customText = Optional.empty();
        redstoneStrength = 0;
        cachedSignalTE = new WeakReference<>(null);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level.isClientSide())
            return;
        signalState = null;
        SignalBlockEntity signalBlockEntity = cachedSignalTE.get();

        if (signalBlockEntity == null || signalBlockEntity.isRemoved()) {
            Direction facing = NixieTubeBlock.getFacing(getBlockState());
            BlockEntity blockEntity = level.getBlockEntity(worldPosition.relative(facing.getOpposite()));
            if (blockEntity instanceof SignalBlockEntity signal) {
                signalState = signal.getState();
                cachedSignalTE = new WeakReference<>(signal);
            }
            return;
        }

        signalState = signalBlockEntity.getState();
    }

    @Override
    public void initialize() {
        if (level.isClientSide())
            updateDisplayedStrings();
    }

    //

    public boolean reactsToRedstone() {
        return customText.isEmpty();
    }

    @Nullable
    public Couple<String> getDisplayedStrings() {
        return displayedStrings;
    }

    public MutableComponent getFullText() {
        return customText.map(Component::copy).orElse(Component.literal("" + redstoneStrength));
    }

    public void updateRedstoneStrength(int signalStrength) {
        clearCustomText();
        redstoneStrength = signalStrength;
        DisplayLinkBlock.notifyGatherers(level, worldPosition);
        notifyUpdate();
    }

    public void displayCustomText(Component text, int nixiePositionInRow) {
        if (text == null)
            return;
        if (customText.filter(d -> d.equals(text)).isPresent())
            return;

        customText = Optional.ofNullable(DynamicComponent.parseCustomText(level, worldPosition, text));
        nixieIndex = nixiePositionInRow;
        DisplayLinkBlock.notifyGatherers(level, worldPosition);
        notifyUpdate();
    }

    public void displayEmptyText(int nixiePositionInRow) {
        displayCustomText(CommonComponents.EMPTY, nixiePositionInRow);
    }

    public void updateDisplayedStrings() {
        if (signalState != null)
            return;
        customText.map(Component::getString).ifPresentOrElse(
            fullText -> displayedStrings = Couple.create(charOrEmpty(fullText, nixieIndex * 2), charOrEmpty(fullText, nixieIndex * 2 + 1)),
            () -> displayedStrings = Couple.create(redstoneStrength < 10 ? "0" : "1", String.valueOf(redstoneStrength % 10))
        );
    }

    public void clearCustomText() {
        nixieIndex = 0;
        customText = Optional.empty();
    }

    public int getRedstoneStrength() {
        return redstoneStrength;
    }

    //

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);

        view.read("CustomText", ComponentSerialization.CODEC).ifPresentOrElse(
            text -> {
                customText = Optional.of(text);
                nixieIndex = view.getIntOr("CustomTextIndex", 0);
            }, () -> redstoneStrength = view.getIntOr("RedstoneStrength", 0)
        );
        if (clientPacket || isVirtual())
            updateDisplayedStrings();
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);

        customText.ifPresentOrElse(
            text -> {
                view.putInt("CustomTextIndex", nixieIndex);
                view.store("CustomText", ComponentSerialization.CODEC, text);
            }, () -> view.putInt("RedstoneStrength", redstoneStrength)
        );
    }

    private String charOrEmpty(String string, int index) {
        return string.length() <= index ? " " : string.substring(index, index + 1);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }

    @Override
    @SuppressWarnings("deprecation")
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        BlockState state = level.getBlockState(pos);
        if (getType().isValid(state)) {
            keepAlive = true;
            setBlockState(state);
        } else {
            super.preRemoveSideEffects(pos, oldState);
        }
    }

    @Override
    public void setRemoved() {
        if (keepAlive) {
            keepAlive = false;
            level.getChunk(worldPosition).setBlockEntity(this);
        } else {
            super.setRemoved();
        }
    }
}
