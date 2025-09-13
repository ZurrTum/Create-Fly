package com.zurrtum.create.client.content.kinetics.mechanicalArm;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmBlockEntity;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint;
import com.zurrtum.create.content.kinetics.mechanicalArm.ArmInteractionPoint.Mode;
import com.zurrtum.create.infrastructure.packet.c2s.ArmPlacementPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class ArmInteractionPointHandler {

    static List<ArmInteractionPoint> currentSelection = new ArrayList<>();
    static ItemStack currentItem;

    static long lastBlockPos = -1;


    public static ActionResult rightClickingBlocksSelectsThem(World world, ClientPlayerEntity player, Hand hand, BlockHitResult hit) {
        if (currentItem == null)
            return null;
        if (player != null && player.isSpectator())
            return null;

        BlockPos pos = hit.getBlockPos();
        ArmInteractionPoint selected = getSelected(pos);
        BlockState state = world.getBlockState(pos);

        if (selected == null) {
            ArmInteractionPoint point = ArmInteractionPoint.create(world, pos, state);
            if (point == null)
                return null;
            selected = point;
            put(point);
        }

        selected.cycleMode();
        if (player != null) {
            Mode mode = selected.getMode();
            Text text = Text.translatable(mode.getTranslationKey(), CreateLang.blockName(state).style(Formatting.WHITE).component())
                .withColor(mode.getColor());
            player.sendMessage(text, true);
        }

        return ActionResult.SUCCESS;
    }

    public static boolean leftClickingBlocksDeselectsThem(BlockPos pos) {
        if (currentItem == null)
            return false;
        return remove(pos);
    }

    public static void flushSettings(ClientPlayerEntity player, BlockPos pos) {
        if (currentSelection == null)
            return;

        int removed = 0;
        for (Iterator<ArmInteractionPoint> iterator = currentSelection.iterator(); iterator.hasNext(); ) {
            ArmInteractionPoint point = iterator.next();
            if (point.getPos().isWithinDistance(pos, ArmBlockEntity.getRange()))
                continue;
            iterator.remove();
            removed++;
        }

        if (removed > 0) {
            CreateLang.builder().translate("mechanical_arm.points_outside_range", removed).style(Formatting.RED).sendStatus(player);
        } else {
            int inputs = 0;
            int outputs = 0;
            for (ArmInteractionPoint armInteractionPoint : currentSelection) {
                if (armInteractionPoint.getMode() == Mode.DEPOSIT)
                    outputs++;
                else
                    inputs++;
            }
            if (inputs + outputs > 0)
                CreateLang.builder().translate("mechanical_arm.summary", inputs, outputs).style(Formatting.WHITE).sendStatus(player);
        }

        player.networkHandler.sendPacket(new ArmPlacementPacket(currentSelection, pos));
        currentSelection.clear();
        currentItem = null;
    }

    public static void tick(MinecraftClient mc) {
        PlayerEntity player = mc.player;

        if (player == null)
            return;

        ItemStack heldItemMainhand = player.getMainHandStack();
        if (!heldItemMainhand.isOf(AllItems.MECHANICAL_ARM)) {
            currentItem = null;
        } else {
            if (heldItemMainhand != currentItem) {
                currentSelection.clear();
                currentItem = heldItemMainhand;
            }

            drawOutlines(currentSelection);
        }

        checkForWrench(mc, heldItemMainhand);
    }

    private static void checkForWrench(MinecraftClient mc, ItemStack heldItem) {
        if (!heldItem.isOf(AllItems.WRENCH)) {
            return;
        }

        HitResult objectMouseOver = mc.crosshairTarget;
        if (!(objectMouseOver instanceof BlockHitResult result)) {
            return;
        }

        BlockPos pos = result.getBlockPos();

        BlockEntity be = mc.world.getBlockEntity(pos);
        if (!(be instanceof ArmBlockEntity)) {
            lastBlockPos = -1;
            currentSelection.clear();
            return;
        }

        if (lastBlockPos == -1 || lastBlockPos != pos.asLong()) {
            currentSelection.clear();
            ArmBlockEntity arm = (ArmBlockEntity) be;
            arm.inputs.forEach(ArmInteractionPointHandler::put);
            arm.outputs.forEach(ArmInteractionPointHandler::put);
            lastBlockPos = pos.asLong();
        }

        if (lastBlockPos != -1) {
            drawOutlines(currentSelection);
        }
    }

    private static void drawOutlines(Collection<ArmInteractionPoint> selection) {
        for (Iterator<ArmInteractionPoint> iterator = selection.iterator(); iterator.hasNext(); ) {
            ArmInteractionPoint point = iterator.next();

            if (!point.isValid()) {
                iterator.remove();
                continue;
            }

            World level = point.getLevel();
            BlockPos pos = point.getPos();
            BlockState state = level.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(level, pos);
            if (shape.isEmpty())
                continue;

            int color = point.getMode().getColor();
            Outliner.getInstance().showAABB(point, shape.getBoundingBox().offset(pos)).colored(color).lineWidth(1 / 16f);
        }
    }

    private static void put(ArmInteractionPoint point) {
        currentSelection.add(point);
    }

    private static boolean remove(BlockPos pos) {
        ArmInteractionPoint result = getSelected(pos);
        if (result != null) {
            currentSelection.remove(result);
            return true;
        }
        return false;
    }

    private static ArmInteractionPoint getSelected(BlockPos pos) {
        for (ArmInteractionPoint point : currentSelection)
            if (point.getPos().equals(pos))
                return point;
        return null;
    }
}
