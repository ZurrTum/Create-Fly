package com.zurrtum.create.api.behaviour.display;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDisplayTargets;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.WorldAccess;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class DisplayTarget {
    public static final SimpleRegistry<Block, DisplayTarget> BY_BLOCK = SimpleRegistry.create();
    public static final SimpleRegistry<BlockEntityType<?>, DisplayTarget> BY_BLOCK_ENTITY = SimpleRegistry.create();

    public abstract void acceptText(int line, List<MutableText> text, DisplayLinkContext context);

    public abstract DisplayTargetStats provideStats(DisplayLinkContext context);

    public Text getLineOptionText(int line) {
        return Text.translatable("create.display_target.line", line + 1);
    }

    public static void reserve(int line, DisplayHolder target, DisplayLinkContext context) {
        if (line == 0)
            return;

        target.updateLine(line, context.blockEntity().getPos());
    }

    public boolean isReserved(int line, DisplayHolder target, DisplayLinkContext context) {
        BlockPos reserved = target.getLine(line);
        if (reserved == null) {
            return false;
        }

        if (!reserved.equals(context.blockEntity().getPos()) && context.level().getBlockState(reserved).isOf(AllBlocks.DISPLAY_LINK))
            return true;

        target.removeLine(line);
        return false;
    }

    public boolean requiresComponentSanitization() {
        return false;
    }

    /**
     * Get the DisplayTarget with the given ID, accounting for legacy names.
     */
    @Nullable
    public static DisplayTarget get(@Nullable Identifier id) {
        if (id == null)
            return null;
        return CreateRegistries.DISPLAY_TARGET.get(id);
    }

    /**
     * Get the DisplayTarget applicable to the given location, or null if there isn't one.
     */
    @Nullable
    public static DisplayTarget get(WorldAccess level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        DisplayTarget byBlock = BY_BLOCK.get(state);
        // block takes priority if present, it's more granular
        if (byBlock != null)
            return byBlock;

        BlockEntity be = level.getBlockEntity(pos);
        if (be == null)
            return null;

        DisplayTarget byBe = BY_BLOCK_ENTITY.get(be.getType());
        if (byBe != null)
            return byBe;

        // special case: modded signs are common
        return be instanceof SignBlockEntity ? AllDisplayTargets.SIGN : null;
    }

    public Box getMultiblockBounds(WorldAccess level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getOutlineShape(level, pos);
        if (shape.isEmpty())
            return new Box(pos);
        return shape.getBoundingBox().offset(pos);
    }
}
