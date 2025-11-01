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
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.List;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.*;

public class ToolboxHandlerClient {
    static int COOLDOWN = 0;

    public static void clientTick() {
        if (COOLDOWN > 0 && !AllKeys.TOOLBELT.wasPressed() && (AllKeys.TOOLBELT.boundKey != AllKeys.TOOL_MENU.boundKey || !AllKeys.TOOL_MENU.wasPressed())) {
            COOLDOWN--;
        }
    }

    public static boolean onPickItem(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null)
            return false;
        World level = player.getEntityWorld();
        HitResult hitResult = mc.crosshairTarget;

        if (hitResult == null || hitResult.getType() == HitResult.Type.MISS)
            return false;
        if (player.isCreative())
            return false;

        ItemStack result = ItemStack.EMPTY;
        List<ToolboxBlockEntity> toolboxes = ToolboxHandler.getNearest(player.getEntityWorld(), player, 8);

        if (toolboxes.isEmpty())
            return false;

        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = ((BlockHitResult) hitResult).getBlockPos();
            BlockState state = level.getBlockState(pos);
            if (state.isAir())
                return false;
            result = state.getPickStack(level, pos, true);

        } else if (hitResult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult) hitResult).getEntity();
            result = entity.getPickBlockStack();
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
                if (!ItemStack.areEqual(inSlot, result))
                    continue;

                player.networkHandler.sendPacket(new ToolboxEquipPacket(toolboxBlockEntity.getPos(), comp, player.getInventory().getSelectedSlot()));
                return true;
            }

        }

        return false;
    }

    public static boolean onKeyInput(MinecraftClient mc, KeyInput input) {
        if (!AllKeys.TOOLBELT.matchesKey(input))
            return false;
        if (mc.interactionManager == null || mc.interactionManager.getCurrentGameMode() == GameMode.SPECTATOR)
            return false;
        if (COOLDOWN > 0)
            return false;
        ClientPlayerEntity player = mc.player;
        if (player == null)
            return false;
        World level = player.getEntityWorld();

        List<ToolboxBlockEntity> toolboxes = ToolboxHandler.getNearest(player.getEntityWorld(), player, 8);
        toolboxes.sort(Comparator.comparing(ToolboxBlockEntity::getUniqueId));

        NbtCompound compound = AllSynchedDatas.TOOLBOX.get(player);

        String slotKey = String.valueOf(player.getInventory().getSelectedSlot());
        boolean equipped = compound.contains(slotKey);

        if (equipped) {
            NbtCompound slotCompound = compound.getCompoundOrEmpty(slotKey);
            BlockPos pos = slotCompound.get("Pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
            double max = ToolboxHandler.getMaxRange(player);
            boolean canReachToolbox = ToolboxHandler.distance(player.getEntityPos(), pos) < max * max;

            if (canReachToolbox) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof ToolboxBlockEntity) {
                    RadialToolboxMenu screen = new RadialToolboxMenu(
                        toolboxes,
                        RadialToolboxMenu.State.SELECT_ITEM_UNEQUIP,
                        (ToolboxBlockEntity) blockEntity
                    );
                    screen.prevSlot(slotCompound.getInt("Slot", 0));
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

    public static void renderOverlay(MinecraftClient mc, DrawContext guiGraphics) {
        int width = guiGraphics.getScaledWindowWidth();
        int height = guiGraphics.getScaledWindowHeight();
        int x = width / 2 - 90;
        int y = height - 23;

        PlayerEntity player = mc.player;
        NbtCompound compound = AllSynchedDatas.TOOLBOX.get(player);
        if (compound.isEmpty())
            return;

        int selectedSlot = player.getInventory().getSelectedSlot();
        for (int slot = 0; slot < 9; slot++) {
            String key = String.valueOf(slot);
            if (!compound.contains(key))
                continue;
            BlockPos pos = compound.getCompoundOrEmpty(key).get("Pos", BlockPos.CODEC).orElse(BlockPos.ORIGIN);
            double max = ToolboxHandler.getMaxRange(player);
            boolean selected = slot == selectedSlot;
            int offset = selected ? 1 : 0;
            AllGuiTextures texture = ToolboxHandler.distance(
                player.getEntityPos(),
                pos
            ) < max * max ? selected ? TOOLBELT_SELECTED_ON : TOOLBELT_HOTBAR_ON : selected ? TOOLBELT_SELECTED_OFF : TOOLBELT_HOTBAR_OFF;
            texture.render(guiGraphics, x + 20 * slot - offset, y + offset);
        }
    }

}
