package com.zurrtum.create.content.redstone.nixieTube;

import com.mojang.serialization.Codec;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.compat.computercraft.AbstractComputerBehaviour;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.utility.DynamicComponent;
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
import org.jspecify.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Optional;

public class NixieTubeBlockEntity extends SmartBlockEntity {
    public static final class ComputerSignal {
        public static final class TubeDisplay {
            public static final int ENCODED_SIZE = 7;

            public byte r = 63, g = 63, b = 63;
            public byte blinkPeriod = 0, blinkOffTime = 0;
            public byte glowWidth = 1, glowHeight = 1;

            public void decode(byte[] data, int offset) {
                r = data[offset];
                g = data[offset + 1];
                b = data[offset + 2];
                blinkPeriod = data[offset + 3];
                blinkOffTime = data[offset + 4];
                glowWidth = data[offset + 5];
                glowHeight = data[offset + 6];
            }

            public void encode(byte[] data, int offset) {
                data[offset] = r;
                data[offset + 1] = g;
                data[offset + 2] = b;
                data[offset + 3] = blinkPeriod;
                data[offset + 4] = blinkOffTime;
                data[offset + 5] = glowWidth;
                data[offset + 6] = glowHeight;
            }
        }

        public TubeDisplay first = new TubeDisplay();
        public TubeDisplay second = new TubeDisplay();

        public void decode(byte[] encoded) {
            first.decode(encoded, 0);
            second.decode(encoded, TubeDisplay.ENCODED_SIZE);
        }

        public byte[] encode() {
            byte[] encoded = new byte[TubeDisplay.ENCODED_SIZE * 2];
            first.encode(encoded, 0);
            second.encode(encoded, TubeDisplay.ENCODED_SIZE);
            return encoded;
        }
    }

    private int redstoneStrength;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private Optional<Component> customText;
    private int nixieIndex;
    private @Nullable Couple<String> displayedStrings;

    public @Nullable ComputerSignal computerSignal;

    private WeakReference<@Nullable SignalBlockEntity> cachedSignalTE;
    public @Nullable SignalState signalState;

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

        if (AbstractComputerBehaviour.contains(this)) {
            if (level.isClientSide() && cachedSignalTE.get() != null) {
                cachedSignalTE = new WeakReference<>(null);
            }
            return;
        } else {
            computerSignal = null;
        }

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
        if (AbstractComputerBehaviour.contains(this)) {
            return false;
        }
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

    public void displayCustomText(@Nullable Component text, int nixiePositionInRow) {
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
        if (signalState != null || computerSignal != null)
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
        if (clientPacket || isVirtual()) {
            view.read("ComputerSignal", Codec.BYTE_BUFFER).ifPresentOrElse(
                t -> {
                    byte[] encodedComputerSignal = t.array();
                    if (computerSignal == null)
                        computerSignal = new ComputerSignal();
                    computerSignal.decode(encodedComputerSignal);
                }, () -> computerSignal = null
            );
            updateDisplayedStrings();
        }
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
        if (clientPacket && computerSignal != null) {
            view.store("ComputerSignal", Codec.BYTE_BUFFER, ByteBuffer.wrap(computerSignal.encode()));
        }
    }

    private String charOrEmpty(String string, int index) {
        return string.length() <= index ? " " : string.substring(index, index + 1);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
    }
}
