package com.zurrtum.create.content.equipment.zapper.terrainzapper;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.equipment.zapper.ZapperItem;
import com.zurrtum.create.infrastructure.component.PlacementOptions;
import com.zurrtum.create.infrastructure.component.PlacementPatterns;
import com.zurrtum.create.infrastructure.component.TerrainBrushes;
import com.zurrtum.create.infrastructure.component.TerrainTools;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class WorldshaperItem extends ZapperItem {

    public WorldshaperItem(Properties properties) {
        super(properties);
    }

    @Override
    protected void openHandgunGUI(ItemStack item, InteractionHand hand) {
        AllClientHandle.INSTANCE.openWorldshaperScreen(item, hand);
    }

    @Override
    protected int getZappingRange(ItemStack stack) {
        return 128;
    }

    @Override
    protected int getCooldownDelay(ItemStack item) {
        return 2;
    }

    @Override
    public Component validateUsage(ItemStack item) {
        if (!item.has(AllDataComponents.SHAPER_BRUSH_PARAMS))
            return Component.translatable("create.terrainzapper.shiftRightClickToSet");
        return super.validateUsage(item);
    }

    @Override
    protected boolean canActivateWithoutSelectedBlock(ItemStack stack) {
        TerrainTools tool = stack.getOrDefault(AllDataComponents.SHAPER_TOOL, TerrainTools.Fill);
        return !tool.requiresSelectedBlock();
    }

    @Override
    protected boolean activate(Level level, Player player, ItemStack stack, BlockState stateToUse, BlockHitResult raytrace, CompoundTag data) {

        BlockPos targetPos = raytrace.getBlockPos();
        List<BlockPos> affectedPositions = new ArrayList<>();

        Brush brush = stack.getOrDefault(AllDataComponents.SHAPER_BRUSH, TerrainBrushes.Cuboid).get();
        BlockPos params = stack.get(AllDataComponents.SHAPER_BRUSH_PARAMS);
        PlacementOptions option = stack.getOrDefault(AllDataComponents.SHAPER_PLACEMENT_OPTIONS, PlacementOptions.Merged);
        TerrainTools tool = stack.getOrDefault(AllDataComponents.SHAPER_TOOL, TerrainTools.Fill);

        brush.set(params.getX(), params.getY(), params.getZ());
        targetPos = targetPos.offset(brush.getOffset(player.getLookAngle(), raytrace.getDirection(), option));
        brush.addToGlobalPositions(level, targetPos, raytrace.getDirection(), affectedPositions, tool);
        PlacementPatterns.applyPattern(affectedPositions, stack, level.random);
        brush.redirectTool(tool).run(level, affectedPositions, raytrace.getDirection(), stateToUse, data, player);

        return true;
    }

    public static void configureSettings(
        ItemStack stack,
        PlacementPatterns pattern,
        TerrainBrushes brush,
        int brushParamX,
        int brushParamY,
        int brushParamZ,
        TerrainTools tool,
        PlacementOptions placement
    ) {
        stack.set(AllDataComponents.PLACEMENT_PATTERN, pattern);
        stack.set(AllDataComponents.SHAPER_BRUSH, brush);
        stack.set(AllDataComponents.SHAPER_BRUSH_PARAMS, new BlockPos(brushParamX, brushParamY, brushParamZ));
        stack.set(AllDataComponents.SHAPER_TOOL, tool);
        stack.set(AllDataComponents.SHAPER_PLACEMENT_OPTIONS, placement);
    }
}