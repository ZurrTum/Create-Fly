package com.zurrtum.create.content.redstone.nixieTube;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkBlock;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity;
import com.zurrtum.create.content.trains.signal.SignalBlockEntity.SignalState;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.utility.DynamicComponent;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Optional;

public class NixieTubeBlockEntity extends SmartBlockEntity {
    private int redstoneStrength;
    private Optional<Text> customText;
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
        if (!world.isClient())
            return;
        signalState = null;
        SignalBlockEntity signalBlockEntity = cachedSignalTE.get();

        if (signalBlockEntity == null || signalBlockEntity.isRemoved()) {
            Direction facing = NixieTubeBlock.getFacing(getCachedState());
            BlockEntity blockEntity = world.getBlockEntity(pos.offset(facing.getOpposite()));
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
        if (world.isClient())
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

    public MutableText getFullText() {
        return customText.map(Text::copy).orElse(Text.literal("" + redstoneStrength));
    }

    public void updateRedstoneStrength(int signalStrength) {
        clearCustomText();
        redstoneStrength = signalStrength;
        DisplayLinkBlock.notifyGatherers(world, pos);
        notifyUpdate();
    }

    public void displayCustomText(Text text, int nixiePositionInRow) {
        if (text == null)
            return;
        if (customText.filter(d -> d.equals(text)).isPresent())
            return;

        customText = Optional.ofNullable(DynamicComponent.parseCustomText(world, pos, text));
        nixieIndex = nixiePositionInRow;
        DisplayLinkBlock.notifyGatherers(world, pos);
        notifyUpdate();
    }

    public void displayEmptyText(int nixiePositionInRow) {
        displayCustomText(ScreenTexts.EMPTY, nixiePositionInRow);
    }

    public void updateDisplayedStrings() {
        if (signalState != null)
            return;
        customText.map(Text::getString).ifPresentOrElse(
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
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);

        view.read("CustomText", TextCodecs.CODEC).ifPresentOrElse(
            text -> {
                customText = Optional.of(text);
                nixieIndex = view.getInt("CustomTextIndex", 0);
            }, () -> redstoneStrength = view.getInt("RedstoneStrength", 0)
        );
        if (clientPacket || isVirtual())
            updateDisplayedStrings();
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);

        customText.ifPresentOrElse(
            text -> {
                view.putInt("CustomTextIndex", nixieIndex);
                view.put("CustomText", TextCodecs.CODEC, text);
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
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        BlockState state = world.getBlockState(pos);
        if (getType().supports(state)) {
            keepAlive = true;
            setCachedState(state);
        } else {
            super.onBlockReplaced(pos, oldState);
        }
    }

    @Override
    public void markRemoved() {
        if (keepAlive) {
            keepAlive = false;
            world.getChunk(pos).setBlockEntity(this);
        } else {
            super.markRemoved();
        }
    }
}
