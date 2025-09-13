package com.zurrtum.create.content.equipment.zapper.terrainzapper;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.content.equipment.zapper.ZapperItem;
import com.zurrtum.create.infrastructure.component.PlacementOptions;
import com.zurrtum.create.infrastructure.component.PlacementPatterns;
import com.zurrtum.create.infrastructure.component.TerrainBrushes;
import com.zurrtum.create.infrastructure.component.TerrainTools;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class WorldshaperItem extends ZapperItem {

    public WorldshaperItem(Settings properties) {
        super(properties);
    }

    @Override
    protected void openHandgunGUI(ItemStack item, Hand hand) {
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
    public Text validateUsage(ItemStack item) {
        if (!item.contains(AllDataComponents.SHAPER_BRUSH_PARAMS))
            return Text.translatable("create.terrainzapper.shiftRightClickToSet");
        return super.validateUsage(item);
    }

    @Override
    protected boolean canActivateWithoutSelectedBlock(ItemStack stack) {
        TerrainTools tool = stack.getOrDefault(AllDataComponents.SHAPER_TOOL, TerrainTools.Fill);
        return !tool.requiresSelectedBlock();
    }

    @Override
    protected boolean activate(World level, PlayerEntity player, ItemStack stack, BlockState stateToUse, BlockHitResult raytrace, NbtCompound data) {

        BlockPos targetPos = raytrace.getBlockPos();
        List<BlockPos> affectedPositions = new ArrayList<>();

        Brush brush = stack.getOrDefault(AllDataComponents.SHAPER_BRUSH, TerrainBrushes.Cuboid).get();
        BlockPos params = stack.get(AllDataComponents.SHAPER_BRUSH_PARAMS);
        PlacementOptions option = stack.getOrDefault(AllDataComponents.SHAPER_PLACEMENT_OPTIONS, PlacementOptions.Merged);
        TerrainTools tool = stack.getOrDefault(AllDataComponents.SHAPER_TOOL, TerrainTools.Fill);

        brush.set(params.getX(), params.getY(), params.getZ());
        targetPos = targetPos.add(brush.getOffset(player.getRotationVector(), raytrace.getSide(), option));
        brush.addToGlobalPositions(level, targetPos, raytrace.getSide(), affectedPositions, tool);
        PlacementPatterns.applyPattern(affectedPositions, stack, level.random);
        brush.redirectTool(tool).run(level, affectedPositions, raytrace.getSide(), stateToUse, data, player);

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