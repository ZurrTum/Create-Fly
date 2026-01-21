package com.zurrtum.create.client.content.redstone.link.controller;

import com.mojang.blaze3d.platform.InputConstants;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.ControlsUtil;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.model.LinkedControllerModel;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.infrastructure.packet.c2s.LinkedControllerBindPacket;
import com.zurrtum.create.infrastructure.packet.c2s.LinkedControllerInputPacket;
import com.zurrtum.create.infrastructure.packet.c2s.LinkedControllerStopLecternPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class LinkedControllerClientHandler {
    public static Mode MODE = Mode.IDLE;
    public static int PACKET_RATE = 5;
    public static Collection<Integer> currentlyPressed = new HashSet<>();
    private static @Nullable BlockPos lecternPos;
    private static BlockPos selectedLocation = BlockPos.ZERO;
    private static int packetCooldown;

    public static void toggleBindMode(LocalPlayer player, BlockPos location) {
        if (MODE == Mode.IDLE) {
            MODE = Mode.BIND;
            selectedLocation = location;
        } else {
            MODE = Mode.IDLE;
            onReset(player);
        }
    }

    public static void toggle(LocalPlayer player) {
        if (MODE == Mode.IDLE) {
            MODE = Mode.ACTIVE;
            lecternPos = null;
        } else {
            MODE = Mode.IDLE;
            onReset(player);
        }
    }

    public static void activateInLectern(BlockPos lecternAt) {
        if (MODE == Mode.IDLE) {
            MODE = Mode.ACTIVE;
            lecternPos = lecternAt;
        }
    }

    public static boolean deactivateInLectern(LocalPlayer player) {
        if (MODE == Mode.ACTIVE && inLectern()) {
            MODE = Mode.IDLE;
            onReset(player);
            return true;
        }
        return false;
    }

    public static boolean inLectern() {
        return lecternPos != null;
    }

    protected static void onReset(LocalPlayer player) {
        ControlsUtil.getControls().forEach(kb -> kb.setDown(ControlsUtil.isActuallyPressed(kb)));
        packetCooldown = 0;
        selectedLocation = BlockPos.ZERO;

        if (lecternPos != null)
            player.connection.send(new LinkedControllerStopLecternPacket(lecternPos));
        lecternPos = null;

        if (!currentlyPressed.isEmpty())
            player.connection.send(new LinkedControllerInputPacket(currentlyPressed, false));
        currentlyPressed.clear();

        LinkedControllerModel.resetButtons();
        if (player.isUsingItem()) {
            player.stopUsingItem();
        }
    }

    private static void updateUsingItem(LocalPlayer player, InteractionHand hand) {
        if (player.isUsingItem()) {
            if (player.getUsedItemHand() != hand) {
                player.stopUsingItem();
                player.startUsingItem(hand);
            }
        } else {
            player.startUsingItem(hand);
        }
    }

    public static void tick(Minecraft mc) {
        LinkedControllerModel.tick(mc);

        if (MODE == Mode.IDLE)
            return;
        if (packetCooldown > 0)
            packetCooldown--;

        LocalPlayer player = mc.player;
        ClientLevel world = mc.level;
        InteractionHand hand = null;
        ItemStack heldItem = player.getMainHandItem();

        if (player.isSpectator()) {
            MODE = Mode.IDLE;
            onReset(player);
            return;
        }

        if (inLectern()) {
            if (AllBlocks.LECTERN_CONTROLLER.getBlockEntityOptional(world, lecternPos).map(be -> !be.isUsedBy(mc.player)).orElse(true)) {
                deactivateInLectern(player);
                return;
            }
        } else {
            if (heldItem.is(AllItems.LINKED_CONTROLLER)) {
                hand = InteractionHand.MAIN_HAND;
            } else {
                heldItem = player.getOffhandItem();
                if (heldItem.is(AllItems.LINKED_CONTROLLER)) {
                    hand = InteractionHand.OFF_HAND;
                } else {
                    MODE = Mode.IDLE;
                    onReset(player);
                    return;
                }
            }
        }

        if (mc.screen != null || InputConstants.isKeyDown(mc.getWindow(), GLFW.GLFW_KEY_ESCAPE)) {
            MODE = Mode.IDLE;
            onReset(player);
            return;
        }

        List<KeyMapping> controls = ControlsUtil.getControls();
        Collection<Integer> pressedKeys = new HashSet<>();
        for (int i = 0; i < controls.size(); i++) {
            if (ControlsUtil.isActuallyPressed(controls.get(i)))
                pressedKeys.add(i);
        }

        Collection<Integer> newKeys = new HashSet<>(pressedKeys);
        Collection<Integer> releasedKeys = currentlyPressed;
        newKeys.removeAll(releasedKeys);
        releasedKeys.removeAll(pressedKeys);

        if (MODE == Mode.ACTIVE) {
            // Released Keys
            if (!releasedKeys.isEmpty()) {
                player.connection.send(new LinkedControllerInputPacket(releasedKeys, false, lecternPos));
                AllSoundEvents.CONTROLLER_CLICK.playAt(player.level(), player.blockPosition(), 1f, .5f, true);
            }

            // Newly Pressed Keys
            if (!newKeys.isEmpty()) {
                player.connection.send(new LinkedControllerInputPacket(newKeys, true, lecternPos));
                packetCooldown = PACKET_RATE;
                AllSoundEvents.CONTROLLER_CLICK.playAt(player.level(), player.blockPosition(), 1f, .75f, true);
            }

            // Keepalive Pressed Keys
            if (packetCooldown == 0) {
                if (!pressedKeys.isEmpty()) {
                    player.connection.send(new LinkedControllerInputPacket(pressedKeys, true, lecternPos));
                    packetCooldown = PACKET_RATE;
                }
            }
            if (hand != null) {
                updateUsingItem(player, hand);
            }
        } else {
            VoxelShape shape = world.getBlockState(selectedLocation).getShape(world, selectedLocation);
            if (!shape.isEmpty())
                Outliner.getInstance().showAABB("controller", shape.bounds().move(selectedLocation)).colored(0xB73C2D).lineWidth(1 / 16f);

            if (newKeys.isEmpty()) {
                if (hand != null) {
                    updateUsingItem(player, hand);
                }
            } else {
                for (Integer integer : newKeys) {
                    ServerLinkBehaviour linkBehaviour = BlockEntityBehaviour.get(world, selectedLocation, ServerLinkBehaviour.TYPE);
                    if (linkBehaviour != null) {
                        player.connection.send(new LinkedControllerBindPacket(integer, selectedLocation));
                        CreateLang.translate("linked_controller.key_bound", controls.get(integer).getTranslatedKeyMessage().getString())
                            .sendStatus(mc.player);
                    }
                    MODE = Mode.IDLE;
                    break;
                }
                if (player.isUsingItem()) {
                    player.stopUsingItem();
                }
            }
        }

        currentlyPressed = pressedKeys;
        controls.forEach(kb -> kb.setDown(false));
    }

    public static void renderOverlay(Minecraft mc, GuiGraphics guiGraphics) {
        if (MODE != Mode.BIND)
            return;
        int width1 = guiGraphics.guiWidth();
        int height1 = guiGraphics.guiHeight();

        Matrix3x2fStack poseStack = guiGraphics.pose();
        poseStack.pushMatrix();

        Object[] keys = new Object[6];
        List<KeyMapping> controls = ControlsUtil.getControls();
        for (int i = 0; i < controls.size(); i++) {
            KeyMapping keyBinding = controls.get(i);
            keys[i] = keyBinding.getTranslatedKeyMessage().getString();
        }

        List<Component> list = new ArrayList<>();
        list.add(CreateLang.translateDirect("linked_controller.bind_mode").withStyle(ChatFormatting.GOLD));
        list.addAll(TooltipHelper.cutTextComponent(CreateLang.translateDirect("linked_controller.press_keybind", keys), Palette.ALL_GRAY));

        int width = 0;
        int height = list.size() * mc.font.lineHeight;
        for (Component iTextComponent : list)
            width = Math.max(width, mc.font.width(iTextComponent));
        int x = (width1 / 3) - width / 2;
        int y = height1 - height - 24;

        // TODO
        guiGraphics.renderTooltip(
            mc.font,
            list.stream().map(Component::getVisualOrderText).map(ClientTooltipComponent::create).collect(Collectors.toList()),
            x,
            y,
            DefaultTooltipPositioner.INSTANCE,
            null
        );

        poseStack.popMatrix();
    }

    public enum Mode {
        IDLE,
        ACTIVE,
        BIND
    }

}
