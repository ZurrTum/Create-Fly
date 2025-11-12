package com.zurrtum.create.client.content.equipment.toolbox;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxHandler;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import com.zurrtum.create.infrastructure.packet.c2s.ToolboxEquipPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.Comparator;
import java.util.List;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.*;

public class ToolboxHandlerClient {
    static int COOLDOWN = 0;

    public static void clientTick() {
        if (COOLDOWN > 0 && !AllKeys.TOOLBELT.consumeClick() && (AllKeys.TOOLBELT.key != AllKeys.TOOL_MENU.key || !AllKeys.TOOL_MENU.consumeClick())) {
            COOLDOWN--;
        }
    }

    public static boolean onPickItem(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null)
            return false;
        Level level = player.level();
        HitResult hitResult = mc.hitResult;

        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS)
            return false;
        if (player.isCreative())
            return false;

        ItemStack result = ItemStack.EMPTY;
        List<ToolboxBlockEntity> toolboxes = ToolboxHandler.getNearest(player.level(), player, 8);

        if (toolboxes.isEmpty())
            return false;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.isAir())
                return false;
            result = state.getCloneItemStack(level, pos, true);

        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitResult).getEntity();
            result = entity.getPickResult();
        }

        if (result.isEmpty())
            return false;

        for (ToolboxBlockEntity toolboxBlockEntity : toolboxes) {
            ToolboxInventory inventory = toolboxBlockEntity.inventory;
            for (int comp = 0; comp < 8; comp++) {
                ItemStack inSlot = inventory.takeFromCompartment(1, comp, true);
                if (inSlot.isEmpty())
                    continue;
                if (inSlot.getItem() != result.getItem())
                    continue;
                if (!ItemStack.matches(inSlot, result))
                    continue;

                player.connection.send(new ToolboxEquipPacket(toolboxBlockEntity.getBlockPos(), comp, player.getInventory().getSelectedSlot()));
                return true;
            }

        }

        return false;
    }

    public static boolean onKeyInput(Minecraft mc, KeyEvent input) {
        if (!AllKeys.TOOLBELT.matches(input))
            return false;
        if (mc.gameMode == null || mc.gameMode.getPlayerMode() == GameType.SPECTATOR)
            return false;
        if (COOLDOWN > 0)
            return false;
        LocalPlayer player = mc.player;
        if (player == null)
            return false;
        Level level = player.level();

        List<ToolboxBlockEntity> toolboxes = ToolboxHandler.getNearest(player.level(), player, 8);
        toolboxes.sort(Comparator.comparing(ToolboxBlockEntity::getUniqueId));

        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);

        String slotKey = String.valueOf(player.getInventory().getSelectedSlot());
        boolean equipped = compound.contains(slotKey);

        if (equipped) {
            CompoundTag slotCompound = compound.getCompoundOrEmpty(slotKey);
            BlockPos pos = slotCompound.read("Pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
            double max = ToolboxHandler.getMaxRange(player);
            boolean canReachToolbox = ToolboxHandler.distance(player.position(), pos) < max * max;

            if (canReachToolbox) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ToolboxBlockEntity) {
                    RadialToolboxMenu screen = new RadialToolboxMenu(
                        toolboxes,
                        RadialToolboxMenu.State.SELECT_ITEM_UNEQUIP,
                        (ToolboxBlockEntity) blockEntity
                    );
                    screen.prevSlot(slotCompound.getIntOr("Slot", 0));
                    ScreenOpener.open(screen);
                    return true;
                }
            }

            ScreenOpener.open(new RadialToolboxMenu(ImmutableList.of(), RadialToolboxMenu.State.DETACH, null));
            return true;
        }

        if (toolboxes.isEmpty())
            return false;

        if (toolboxes.size() == 1)
            ScreenOpener.open(new RadialToolboxMenu(toolboxes, RadialToolboxMenu.State.SELECT_ITEM, toolboxes.getFirst()));
        else
            ScreenOpener.open(new RadialToolboxMenu(toolboxes, RadialToolboxMenu.State.SELECT_BOX, null));
        return true;
    }

    public static void renderOverlay(Minecraft mc, GuiGraphics guiGraphics) {
        int width = guiGraphics.guiWidth();
        int height = guiGraphics.guiHeight();
        int x = width / 2 - 90;
        int y = height - 23;

        Player player = mc.player;
        CompoundTag compound = AllSynchedDatas.TOOLBOX.get(player);
        if (compound.isEmpty())
            return;

        int selectedSlot = player.getInventory().getSelectedSlot();
        for (int slot = 0; slot < 9; slot++) {
            String key = String.valueOf(slot);
            if (!compound.contains(key))
                continue;
            BlockPos pos = compound.getCompoundOrEmpty(key).read("Pos", BlockPos.CODEC).orElse(BlockPos.ZERO);
            double max = ToolboxHandler.getMaxRange(player);
            boolean selected = slot == selectedSlot;
            int offset = selected ? 1 : 0;
            AllGuiTextures texture = ToolboxHandler.distance(
                player.position(),
                pos
            ) < max * max ? selected ? TOOLBELT_SELECTED_ON : TOOLBELT_HOTBAR_ON : selected ? TOOLBELT_SELECTED_OFF : TOOLBELT_HOTBAR_OFF;
            texture.render(guiGraphics, x + 20 * slot - offset, y + offset);
        }
    }

}
