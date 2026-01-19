package com.zurrtum.create.client.content.equipment.toolbox;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.toolbox.ToolboxBlockEntity;
import com.zurrtum.create.content.equipment.toolbox.ToolboxInventory;
import com.zurrtum.create.infrastructure.packet.c2s.ToolboxDisposeAllPacket;
import com.zurrtum.create.infrastructure.packet.c2s.ToolboxEquipPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.zurrtum.create.content.equipment.toolbox.ToolboxInventory.STACKS_PER_COMPARTMENT;

public class RadialToolboxMenu extends AbstractSimiScreen {

    private State state;
    private int ticksOpen;
    private int hoveredSlot;
    private boolean scrollMode;
    private int scrollSlot = 0;
    private final List<ToolboxBlockEntity> toolboxes;
    private ToolboxBlockEntity selectedBox;

    private static final int DEPOSIT = -7;
    private static final int UNEQUIP = -5;

    public RadialToolboxMenu(List<ToolboxBlockEntity> toolboxes, State state, @Nullable ToolboxBlockEntity selectedBox) {
        this.toolboxes = toolboxes;
        this.state = state;
        hoveredSlot = -1;

        if (selectedBox != null)
            this.selectedBox = selectedBox;
    }

    public void prevSlot(int slot) {
        scrollSlot = slot;
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        float fade = Mth.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);

        hoveredSlot = -1;
        Window window = minecraft.getWindow();
        float hoveredX = mouseX - window.getGuiScaledWidth() / 2;
        float hoveredY = mouseY - window.getGuiScaledHeight() / 2;

        float distance = hoveredX * hoveredX + hoveredY * hoveredY;
        if (distance > 25 && distance < 10000)
            hoveredSlot = (Mth.floor((AngleHelper.deg(Mth.atan2(hoveredY, hoveredX)) + 360 + 180 - 22.5f)) % 360) / 45;
        boolean renderCenterSlot = state == State.SELECT_ITEM_UNEQUIP;
        if (scrollMode && distance > 150)
            scrollMode = false;
        if (renderCenterSlot && distance <= 150)
            hoveredSlot = UNEQUIP;

        Matrix3x2fStack ms = graphics.pose();
        ms.pushMatrix();
        ms.translate(width / 2, height / 2);
        Component tip = null;

        if (state == State.DETACH) {

            tip = CreateLang.translateDirect("toolbox.outOfRange");
            if (hoveredX > -20 && hoveredX < 20 && hoveredY > -80 && hoveredY < -20)
                hoveredSlot = UNEQUIP;

            ms.pushMatrix();
            AllGuiTextures.TOOLBELT_INACTIVE_SLOT.render(graphics, -12, -12);
            graphics.renderItem(AllItems.BROWN_TOOLBOX.getDefaultInstance(), -9, -9);

            ms.translate(0, -40 + (10 * (1 - fade) * (1 - fade)));
            AllGuiTextures.TOOLBELT_SLOT.render(graphics, -12, -12);
            ms.translate(-0.5F, 0.5F);
            AllIcons.I_DISABLE.render(graphics, -9, -9);
            ms.translate(0.5F, -0.5F);
            if (!scrollMode && hoveredSlot == UNEQUIP) {
                AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -13, -13);
                tip = CreateLang.translateDirect("toolbox.detach").withStyle(ChatFormatting.GOLD);
            }
            ms.popMatrix();

        } else {

            if (hoveredX > 60 && hoveredX < 100 && hoveredY > -20 && hoveredY < 20)
                hoveredSlot = DEPOSIT;

            ms.pushMatrix();
            ms.translate(80 + (-5 * (1 - fade) * (1 - fade)), 0);
            AllGuiTextures.TOOLBELT_SLOT.render(graphics, -12, -12);
            ms.translate(-0.5F, 0.5F);
            AllIcons.I_TOOLBOX.render(graphics, -9, -9);
            ms.translate(0.5F, -0.5F);
            if (!scrollMode && hoveredSlot == DEPOSIT) {
                AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -13, -13);
                tip = CreateLang.translateDirect(state == State.SELECT_BOX ? "toolbox.depositAll" : "toolbox.depositBox")
                    .withStyle(ChatFormatting.GOLD);
            }
            ms.popMatrix();

            for (int slot = 0; slot < 8; slot++) {
                ms.pushMatrix();
                ms.rotate(Mth.DEG_TO_RAD * (slot * 45 - 45));
                ms.translate(0, -40 + (10 * (1 - fade) * (1 - fade)));
                ms.rotate(Mth.DEG_TO_RAD * (-slot * 45 + 45));
                ms.translate(-12, -12);

                if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
                    ToolboxInventory inv = selectedBox.inventory;
                    ItemStack stackInSlot = inv.filters.get(slot);

                    if (!stackInSlot.isEmpty()) {
                        boolean empty = inv.getItem(slot * STACKS_PER_COMPARTMENT).isEmpty();

                        (empty ? AllGuiTextures.TOOLBELT_INACTIVE_SLOT : AllGuiTextures.TOOLBELT_SLOT).render(graphics, 0, 0);
                        graphics.renderItem(stackInSlot, 3, 3);

                        if (slot == (scrollMode ? scrollSlot : hoveredSlot) && !empty) {
                            AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -1, -1);
                            tip = stackInSlot.getHoverName();
                        }
                    } else
                        AllGuiTextures.TOOLBELT_EMPTY_SLOT.render(graphics, 0, 0);

                } else if (state == State.SELECT_BOX) {

                    if (slot < toolboxes.size()) {
                        AllGuiTextures.TOOLBELT_SLOT.render(graphics, 0, 0);
                        ToolboxBlockEntity toolboxBlockEntity = toolboxes.get(slot);
                        ItemStack stack = toolboxBlockEntity.getBlockState().getBlock().asItem().getDefaultInstance();
                        graphics.renderItem(stack, 3, 3);

                        if (slot == (scrollMode ? scrollSlot : hoveredSlot)) {
                            AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -1, -1);
                            tip = toolboxBlockEntity.getDisplayName();
                        }
                    } else
                        AllGuiTextures.TOOLBELT_EMPTY_SLOT.render(graphics, 0, 0);

                }

                ms.popMatrix();
            }

            if (renderCenterSlot) {
                ms.pushMatrix();
                AllGuiTextures.TOOLBELT_SLOT.render(graphics, -12, -12);
                (scrollMode ? AllIcons.I_REFRESH : AllIcons.I_FLIP).render(graphics, -9, -9);
                if (!scrollMode && UNEQUIP == hoveredSlot) {
                    AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -13, -13);
                    tip = CreateLang.translateDirect("toolbox.unequip", minecraft.player.getMainHandItem().getHoverName())
                        .withStyle(ChatFormatting.GOLD);
                }
                ms.popMatrix();
            }
        }
        ms.popMatrix();

        if (tip != null) {
            int i1 = (int) (fade * 255.0F);
            if (i1 > 255)
                i1 = 255;

            if (i1 > 8) {
                ms.pushMatrix();
                ms.translate((float) (width / 2), (float) (height - 68));
                int k1 = 16777215;
                int k = i1 << 24 & -16777216;
                int l = font.width(tip);
                graphics.drawString(font, tip, Math.round(-l / 2f), -4, k1 | k, false);
                ms.popMatrix();
            }
        }

    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        Color color = BACKGROUND_COLOR.scaleAlpha(Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f));

        pGuiGraphics.fillGradient(0, 0, this.width, this.height, color.getRGB(), color.getRGB());
    }

    @Override
    public void tick() {
        ticksOpen++;
        super.tick();
    }

    @Override
    public void removed() {
        super.removed();

        int selected = (scrollMode ? scrollSlot : hoveredSlot);

        if (selected == DEPOSIT) {
            if (state == State.DETACH)
                return;
            else if (state == State.SELECT_BOX)
                toolboxes.forEach(be -> minecraft.player.connection.send(new ToolboxDisposeAllPacket(be.getBlockPos())));
            else
                minecraft.player.connection.send(new ToolboxDisposeAllPacket(selectedBox.getBlockPos()));
            return;
        }

        if (state == State.SELECT_BOX)
            return;

        if (state == State.DETACH) {
            if (selected == UNEQUIP)
                minecraft.player.connection.send(new ToolboxEquipPacket(null, selected, minecraft.player.getInventory().getSelectedSlot()));
            return;
        }

        if (selected == UNEQUIP)
            minecraft.player.connection.send(new ToolboxEquipPacket(
                selectedBox.getBlockPos(),
                selected,
                minecraft.player.getInventory().getSelectedSlot()
            ));

        if (selected < 0)
            return;
        ToolboxInventory inv = selectedBox.inventory;
        ItemStack stackInSlot = inv.filters.get(selected);
        if (stackInSlot.isEmpty())
            return;
        if (inv.getItem(selected * STACKS_PER_COMPARTMENT).isEmpty())
            return;

        minecraft.player.connection.send(new ToolboxEquipPacket(
            selectedBox.getBlockPos(),
            selected,
            minecraft.player.getInventory().getSelectedSlot()
        ));
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        Window window = minecraft.getWindow();
        double hoveredX = pMouseY - window.getGuiScaledWidth() / 2;
        double hoveredY = pMouseY - window.getGuiScaledHeight() / 2;
        double distance = hoveredX * hoveredX + hoveredY * hoveredY;
        if (distance <= 150) {
            scrollMode = true;
            scrollSlot = (((int) (scrollSlot - pScrollY)) + 8) % 8;
            for (int i = 0; i < 10; i++) {

                if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
                    ToolboxInventory inv = selectedBox.inventory;
                    ItemStack stackInSlot = inv.filters.get(scrollSlot);
                    if (!stackInSlot.isEmpty() && !inv.getItem(scrollSlot * STACKS_PER_COMPARTMENT).isEmpty())
                        break;
                }

                if (state == State.SELECT_BOX)
                    if (scrollSlot < toolboxes.size())
                        break;

                if (state == State.DETACH)
                    break;

                scrollSlot -= Mth.sign(pScrollY);
                scrollSlot = (scrollSlot + 8) % 8;
            }
            return true;
        }

        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int selected = scrollMode ? scrollSlot : hoveredSlot;
        int button = click.button();

        if (button == 0) {
            if (selected == DEPOSIT) {
                onClose();
                ToolboxHandlerClient.COOLDOWN = 2;
                return true;
            }

            if (state == State.SELECT_BOX && selected >= 0 && selected < toolboxes.size()) {
                state = State.SELECT_ITEM;
                selectedBox = toolboxes.get(selected);
                return true;
            }

            if (state == State.DETACH || state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
                if (selected == UNEQUIP || selected >= 0) {
                    onClose();
                    ToolboxHandlerClient.COOLDOWN = 2;
                    return true;
                }
            }
        }

        if (button == 1) {
            if (state == State.SELECT_ITEM && toolboxes.size() > 1) {
                state = State.SELECT_BOX;
                return true;
            }

            if (state == State.SELECT_ITEM_UNEQUIP && selected == UNEQUIP) {
                if (toolboxes.size() > 1) {
                    minecraft.player.connection.send(new ToolboxEquipPacket(
                        selectedBox.getBlockPos(),
                        selected,
                        minecraft.player.getInventory().getSelectedSlot()
                    ));
                    state = State.SELECT_BOX;
                    return true;
                }

                onClose();
                ToolboxHandlerClient.COOLDOWN = 2;
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        KeyMapping[] hotbarBinds = minecraft.options.keyHotbarSlots;
        for (int i = 0; i < hotbarBinds.length && i < 8; i++) {
            if (hotbarBinds[i].matches(input)) {

                if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
                    ToolboxInventory inv = selectedBox.inventory;
                    ItemStack stackInSlot = inv.filters.get(i);
                    if (stackInSlot.isEmpty() || inv.getItem(i * STACKS_PER_COMPARTMENT).isEmpty())
                        return false;
                }

                if (state == State.SELECT_BOX)
                    if (i >= toolboxes.size())
                        return false;

                scrollMode = true;
                scrollSlot = i;
                mouseClicked(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);
                return true;
            }
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        InputConstants.Key mouseKey = InputConstants.getKey(input);
        if (mouseKey == AllKeys.TOOLBELT.key) {
            onClose();
            return true;
        }
        return super.keyReleased(input);
    }

    public enum State {
        SELECT_BOX,
        SELECT_ITEM,
        SELECT_ITEM_UNEQUIP,
        DETACH
    }

}
