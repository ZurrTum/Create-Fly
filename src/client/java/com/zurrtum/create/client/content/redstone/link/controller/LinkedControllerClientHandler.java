package com.zurrtum.create.client.content.redstone.link.controller;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.catnip.lang.FontHelper.Palette;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.foundation.item.TooltipHelper;
import com.zurrtum.create.client.foundation.utility.ControlsUtil;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.model.LinkedControllerModel;
import com.zurrtum.create.content.redstone.link.ServerLinkBehaviour;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.c2s.LinkedControllerBindPacket;
import com.zurrtum.create.infrastructure.packet.c2s.LinkedControllerInputPacket;
import com.zurrtum.create.infrastructure.packet.c2s.LinkedControllerStopLecternPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.client.gui.tooltip.TooltipComponent;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix3x2fStack;
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
    private static BlockPos lecternPos;
    private static BlockPos selectedLocation = BlockPos.ORIGIN;
    private static int packetCooldown;

    public static void toggleBindMode(ClientPlayerEntity player, BlockPos location) {
        if (MODE == Mode.IDLE) {
            MODE = Mode.BIND;
            selectedLocation = location;
        } else {
            MODE = Mode.IDLE;
            onReset(player);
        }
    }

    public static void toggle(ClientPlayerEntity player) {
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

    public static boolean deactivateInLectern(ClientPlayerEntity player) {
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

    protected static void onReset(ClientPlayerEntity player) {
        ControlsUtil.getControls().forEach(kb -> kb.setPressed(ControlsUtil.isActuallyPressed(kb)));
        packetCooldown = 0;
        selectedLocation = BlockPos.ORIGIN;

        if (inLectern())
            player.networkHandler.sendPacket(new LinkedControllerStopLecternPacket(lecternPos));
        lecternPos = null;

        if (!currentlyPressed.isEmpty())
            player.networkHandler.sendPacket(new LinkedControllerInputPacket(currentlyPressed, false));
        currentlyPressed.clear();

        LinkedControllerModel.resetButtons();
        if (player.isUsingItem()) {
            player.stopUsingItem();
        }
    }

    private static void updateUsingItem(ClientPlayerEntity player, Hand hand) {
        if (player.isUsingItem()) {
            if (player.getActiveHand() != hand) {
                player.stopUsingItem();
                player.setCurrentHand(hand);
            }
        } else {
            player.setCurrentHand(hand);
        }
    }

    public static void tick(MinecraftClient mc) {
        LinkedControllerModel.tick(mc);

        if (MODE == Mode.IDLE)
            return;
        if (packetCooldown > 0)
            packetCooldown--;

        ClientPlayerEntity player = mc.player;
        ClientWorld world = mc.world;
        Hand hand = null;
        ItemStack heldItem = player.getMainHandStack();

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
            if (heldItem.isOf(AllItems.LINKED_CONTROLLER)) {
                hand = Hand.MAIN_HAND;
            } else {
                heldItem = player.getOffHandStack();
                if (heldItem.isOf(AllItems.LINKED_CONTROLLER)) {
                    hand = Hand.OFF_HAND;
                } else {
                    MODE = Mode.IDLE;
                    onReset(player);
                    return;
                }
            }
        }

        if (mc.currentScreen != null || InputUtil.isKeyPressed(mc.getWindow(), GLFW.GLFW_KEY_ESCAPE)) {
            MODE = Mode.IDLE;
            onReset(player);
            return;
        }

        List<KeyBinding> controls = ControlsUtil.getControls();
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
                player.networkHandler.sendPacket(new LinkedControllerInputPacket(releasedKeys, false, lecternPos));
                AllSoundEvents.CONTROLLER_CLICK.playAt(player.getEntityWorld(), player.getBlockPos(), 1f, .5f, true);
            }

            // Newly Pressed Keys
            if (!newKeys.isEmpty()) {
                player.networkHandler.sendPacket(new LinkedControllerInputPacket(newKeys, true, lecternPos));
                packetCooldown = PACKET_RATE;
                AllSoundEvents.CONTROLLER_CLICK.playAt(player.getEntityWorld(), player.getBlockPos(), 1f, .75f, true);
            }

            // Keepalive Pressed Keys
            if (packetCooldown == 0) {
                if (!pressedKeys.isEmpty()) {
                    player.networkHandler.sendPacket(new LinkedControllerInputPacket(pressedKeys, true, lecternPos));
                    packetCooldown = PACKET_RATE;
                }
            }
            if (hand != null) {
                updateUsingItem(player, hand);
            }
        } else {
            VoxelShape shape = world.getBlockState(selectedLocation).getOutlineShape(world, selectedLocation);
            if (!shape.isEmpty())
                Outliner.getInstance().showAABB("controller", shape.getBoundingBox().offset(selectedLocation)).colored(0xB73C2D).lineWidth(1 / 16f);

            if (newKeys.isEmpty()) {
                if (hand != null) {
                    updateUsingItem(player, hand);
                }
            } else {
                for (Integer integer : newKeys) {
                    ServerLinkBehaviour linkBehaviour = BlockEntityBehaviour.get(world, selectedLocation, ServerLinkBehaviour.TYPE);
                    if (linkBehaviour != null) {
                        player.networkHandler.sendPacket(new LinkedControllerBindPacket(integer, selectedLocation));
                        CreateLang.translate("linked_controller.key_bound", controls.get(integer).getBoundKeyLocalizedText().getString())
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
        controls.forEach(kb -> kb.setPressed(false));
    }

    public static void renderOverlay(MinecraftClient mc, DrawContext guiGraphics) {
        if (MODE != Mode.BIND)
            return;
        int width1 = guiGraphics.getScaledWindowWidth();
        int height1 = guiGraphics.getScaledWindowHeight();

        Matrix3x2fStack poseStack = guiGraphics.getMatrices();
        poseStack.pushMatrix();

        Object[] keys = new Object[6];
        List<KeyBinding> controls = ControlsUtil.getControls();
        for (int i = 0; i < controls.size(); i++) {
            KeyBinding keyBinding = controls.get(i);
            keys[i] = keyBinding.getBoundKeyLocalizedText().getString();
        }

        List<Text> list = new ArrayList<>();
        list.add(CreateLang.translateDirect("linked_controller.bind_mode").formatted(Formatting.GOLD));
        list.addAll(TooltipHelper.cutTextComponent(CreateLang.translateDirect("linked_controller.press_keybind", keys), Palette.ALL_GRAY));

        int width = 0;
        int height = list.size() * mc.textRenderer.fontHeight;
        for (Text iTextComponent : list)
            width = Math.max(width, mc.textRenderer.getWidth(iTextComponent));
        int x = (width1 / 3) - width / 2;
        int y = height1 - height - 24;

        // TODO
        guiGraphics.drawTooltipImmediately(
            mc.textRenderer,
            list.stream().map(Text::asOrderedText).map(TooltipComponent::of).collect(Collectors.toList()),
            x,
            y,
            HoveredTooltipPositioner.INSTANCE,
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
