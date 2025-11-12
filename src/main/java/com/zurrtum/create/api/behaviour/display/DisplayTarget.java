package com.zurrtum.create.api.behaviour.display;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDisplayTargets;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.api.registry.SimpleRegistry;
import com.zurrtum.create.content.redstone.displayLink.DisplayLinkContext;
import com.zurrtum.create.content.redstone.displayLink.target.DisplayTargetStats;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class DisplayTarget {
    public static final SimpleRegistry<Block, DisplayTarget> BY_BLOCK = SimpleRegistry.create();
    public static final SimpleRegistry<BlockEntityType<?>, DisplayTarget> BY_BLOCK_ENTITY = SimpleRegistry.create();

    public abstract void acceptText(int line, List<MutableComponent> text, DisplayLinkContext context);

    public abstract DisplayTargetStats provideStats(DisplayLinkContext context);

    public Component getLineOptionText(int line) {
        return Component.translatable("create.display_target.line", line + 1);
    }

    public static void reserve(int line, DisplayHolder target, DisplayLinkContext context) {
        if (line == 0)
            return;

        target.updateLine(line, context.blockEntity().getBlockPos());
    }

    public boolean isReserved(int line, DisplayHolder target, DisplayLinkContext context) {
        BlockPos reserved = target.getLine(line);
        if (reserved == null) {
            return false;
        }

        if (!reserved.equals(context.blockEntity().getBlockPos()) && context.level().getBlockState(reserved).is(AllBlocks.DISPLAY_LINK))
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
        return CreateRegistries.DISPLAY_TARGET.getValue(id);
    }

    /**
     * Get the DisplayTarget applicable to the given location, or null if there isn't one.
     */
    @Nullable
    public static DisplayTarget get(LevelAccessor level, BlockPos pos) {
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

    public AABB getMultiblockBounds(LevelAccessor level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        if (shape.isEmpty())
            return new AABB(pos);
        return shape.bounds().move(pos);
    }
}
