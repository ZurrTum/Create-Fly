package com.zurrtum.create.client.content.equipment.zapper.terrainzapper;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllSpecialTextures;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.Brush;
import com.zurrtum.create.infrastructure.component.PlacementOptions;
import com.zurrtum.create.infrastructure.component.TerrainBrushes;
import com.zurrtum.create.infrastructure.component.TerrainTools;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.RaycastContext.ShapeType;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Supplier;

public class WorldshaperRenderHandler {

    private static Supplier<Collection<BlockPos>> renderedPositions;

    public static void tick(MinecraftClient mc) {
        gatherSelectedBlocks(mc);
        if (renderedPositions == null)
            return;

        Outliner.getInstance().showCluster("terrainZapper", renderedPositions.get()).colored(0xbfbfbf).disableLineNormals().lineWidth(1 / 32f)
            .withFaceTexture(AllSpecialTextures.CHECKERED);
    }

    protected static void gatherSelectedBlocks(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        ItemStack heldMain = player.getMainHandStack();
        ItemStack heldOff = player.getOffHandStack();
        boolean zapperInMain = heldMain.isOf(AllItems.WORLDSHAPER);
        boolean zapperInOff = heldOff.isOf(AllItems.WORLDSHAPER);

        if (zapperInMain) {
            if (!heldMain.contains(AllDataComponents.SHAPER_SWAP) || !zapperInOff) {
                createBrushOutline(player, heldMain);
                return;
            }
        }

        if (zapperInOff) {
            createBrushOutline(player, heldOff);
            return;
        }

        renderedPositions = null;
    }

    public static void createBrushOutline(ClientPlayerEntity player, ItemStack zapper) {
        if (!zapper.contains(AllDataComponents.SHAPER_BRUSH_PARAMS)) {
            renderedPositions = null;
            return;
        }

        Brush brush = zapper.getOrDefault(AllDataComponents.SHAPER_BRUSH, TerrainBrushes.Cuboid).get();
        PlacementOptions placement = zapper.getOrDefault(AllDataComponents.SHAPER_PLACEMENT_OPTIONS, PlacementOptions.Merged);
        TerrainTools tool = zapper.getOrDefault(AllDataComponents.SHAPER_TOOL, TerrainTools.Fill);
        BlockPos params = zapper.get(AllDataComponents.SHAPER_BRUSH_PARAMS);
        brush.set(params.getX(), params.getY(), params.getZ());

        Vec3d start = player.getPos().add(0, player.getStandingEyeHeight(), 0);
        Vec3d rotationVector = player.getRotationVector();
        Vec3d range = rotationVector.multiply(128);
        World world = player.getEntityWorld();
        BlockHitResult raytrace = world.raycast(new RaycastContext(start, start.add(range), ShapeType.OUTLINE, FluidHandling.NONE, player));
        if (raytrace == null || raytrace.getType() == Type.MISS) {
            renderedPositions = null;
            return;
        }

        BlockPos pos = raytrace.getBlockPos().add(brush.getOffset(rotationVector, raytrace.getSide(), placement));
        renderedPositions = () -> brush.addToGlobalPositions(world, pos, raytrace.getSide(), new ArrayList<>(), tool);
    }

}