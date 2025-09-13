package com.zurrtum.create.api.behaviour.display;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayBoardTarget;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.zurrtum.create.content.trains.display.FlapDisplayBlockEntity;
import com.zurrtum.create.content.trains.display.FlapDisplayLayout;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class DisplaySource {
    public static final SimpleRegistry.Multi<Block, DisplaySource> BY_BLOCK = SimpleRegistry.Multi.create();
    public static final SimpleRegistry.Multi<BlockEntityType<?>, DisplaySource> BY_BLOCK_ENTITY = SimpleRegistry.Multi.create();

    public static final List<MutableText> EMPTY = ImmutableList.of(Text.empty());
    public static final MutableText EMPTY_LINE = Text.empty();
    public static final MutableText WHITESPACE = ScreenTexts.space();
    public Object attachRender;

    @SuppressWarnings("unchecked")
    public <T> T getAttachRender() {
        return (T) attachRender;
    }

    public abstract List<MutableText> provideText(DisplayLinkContext context, DisplayTargetStats stats);

    public void transferData(DisplayLinkContext context, DisplayTarget activeTarget, int line) {
        DisplayTargetStats stats = activeTarget.provideStats(context);

        if (activeTarget instanceof DisplayBoardTarget fddt) {
            List<List<MutableText>> flapDisplayText = provideFlapDisplayText(context, stats);
            fddt.acceptFlapText(line, flapDisplayText, context);
        }

        List<MutableText> text = provideText(context, stats);
        if (text.isEmpty())
            text = EMPTY;

        if (activeTarget.requiresComponentSanitization())
            for (MutableText component : text)
                if (NBTProcessors.textComponentHasClickEvent(component))
                    return; // Naughty

        activeTarget.acceptText(line, text, context);
    }

    public void onSignalReset(DisplayLinkContext context) {
    }

    public void populateData(DisplayLinkContext context) {
    }

    public int getPassiveRefreshTicks() {
        return 100;
    }

    public boolean shouldPassiveReset() {
        return true;
    }

    protected final Identifier getId() {
        return CreateRegistries.DISPLAY_SOURCE.getId(this);
    }

    protected String getTranslationKey() {
        return this.getId().getPath();
    }

    public Text getName() {
        return Text.translatable(this.getId().getNamespace() + ".display_source." + getTranslationKey());
    }

    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout, int lineIndex) {
        loadFlapDisplayLayout(context, flapDisplay, layout);
    }

    public void loadFlapDisplayLayout(DisplayLinkContext context, FlapDisplayBlockEntity flapDisplay, FlapDisplayLayout layout) {
        if (!layout.isLayout("Default"))
            layout.loadDefault(flapDisplay.getMaxCharCount());
    }

    public List<List<MutableText>> provideFlapDisplayText(DisplayLinkContext context, DisplayTargetStats stats) {
        return provideText(context, stats).stream().map(Arrays::asList).toList();
    }

    /**
     * Get the DisplaySource with the given ID, accounting for legacy names.
     */
    @Nullable
    public static DisplaySource get(@Nullable Identifier id) {
        if (id == null)
            return null;
        return CreateRegistries.DISPLAY_SOURCE.get(id);
    }

    /**
     * Get all DisplaySources applicable to the block at the given location, checking both the Block and BlockEntity.
     * Returns an empty list if none are present, not null.
     */
    public static List<DisplaySource> getAll(WorldAccess level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        List<DisplaySource> byBlock = BY_BLOCK.get(state);

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null)
            return byBlock;

        List<DisplaySource> byBe = BY_BLOCK_ENTITY.get(be.getType());

        if (byBlock.isEmpty()) {
            if (byBe.isEmpty()) {
                // none
                return List.of();
            } else {
                // only BlockEntity
                return byBe;
            }
        } else if (byBe.isEmpty()) {
            // only Block
            return byBlock;
        } else {
            // both present, combine
            List<DisplaySource> combined = new ArrayList<>(byBlock);
            combined.addAll(byBe);
            return combined;
        }
    }
}
