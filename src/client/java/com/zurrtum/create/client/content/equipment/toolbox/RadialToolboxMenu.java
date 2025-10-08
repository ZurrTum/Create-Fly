package com.zurrtum.create.client.content.equipment.toolbox;

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
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.MouseInput;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

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
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        float fade = MathHelper.clamp((ticksOpen + AnimationTickHolder.getPartialTicks()) / 10f, 1 / 512f, 1);

        hoveredSlot = -1;
        Window window = client.getWindow();
        float hoveredX = mouseX - window.getScaledWidth() / 2;
        float hoveredY = mouseY - window.getScaledHeight() / 2;

        float distance = hoveredX * hoveredX + hoveredY * hoveredY;
        if (distance > 25 && distance < 10000)
            hoveredSlot = (MathHelper.floor((AngleHelper.deg(MathHelper.atan2(hoveredY, hoveredX)) + 360 + 180 - 22.5f)) % 360) / 45;
        boolean renderCenterSlot = state == State.SELECT_ITEM_UNEQUIP;
        if (scrollMode && distance > 150)
            scrollMode = false;
        if (renderCenterSlot && distance <= 150)
            hoveredSlot = UNEQUIP;

        Matrix3x2fStack ms = graphics.getMatrices();
        ms.pushMatrix();
        ms.translate(width / 2, height / 2);
        Text tip = null;

        if (state == State.DETACH) {

            tip = CreateLang.translateDirect("toolbox.outOfRange");
            if (hoveredX > -20 && hoveredX < 20 && hoveredY > -80 && hoveredY < -20)
                hoveredSlot = UNEQUIP;

            ms.pushMatrix();
            AllGuiTextures.TOOLBELT_INACTIVE_SLOT.render(graphics, -12, -12);
            graphics.drawItem(AllItems.BROWN_TOOLBOX.getDefaultStack(), -9, -9);

            ms.translate(0, -40 + (10 * (1 - fade) * (1 - fade)));
            AllGuiTextures.TOOLBELT_SLOT.render(graphics, -12, -12);
            ms.translate(-0.5F, 0.5F);
            AllIcons.I_DISABLE.render(graphics, -9, -9);
            ms.translate(0.5F, -0.5F);
            if (!scrollMode && hoveredSlot == UNEQUIP) {
                AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -13, -13);
                tip = CreateLang.translateDirect("toolbox.detach").formatted(Formatting.GOLD);
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
                tip = CreateLang.translateDirect(state == State.SELECT_BOX ? "toolbox.depositAll" : "toolbox.depositBox").formatted(Formatting.GOLD);
            }
            ms.popMatrix();

            for (int slot = 0; slot < 8; slot++) {
                ms.pushMatrix();
                ms.rotate(MathHelper.RADIANS_PER_DEGREE * (slot * 45 - 45));
                ms.translate(0, -40 + (10 * (1 - fade) * (1 - fade)));
                ms.rotate(MathHelper.RADIANS_PER_DEGREE * (-slot * 45 + 45));
                ms.translate(-12, -12);

                if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
                    ToolboxInventory inv = selectedBox.inventory;
                    ItemStack stackInSlot = inv.filters.get(slot);

                    if (!stackInSlot.isEmpty()) {
                        boolean empty = inv.getStack(slot * STACKS_PER_COMPARTMENT).isEmpty();

                        (empty ? AllGuiTextures.TOOLBELT_INACTIVE_SLOT : AllGuiTextures.TOOLBELT_SLOT).render(graphics, 0, 0);
                        graphics.drawItem(stackInSlot, 3, 3);

                        if (slot == (scrollMode ? scrollSlot : hoveredSlot) && !empty) {
                            AllGuiTextures.TOOLBELT_SLOT_HIGHLIGHT.render(graphics, -1, -1);
                            tip = stackInSlot.getName();
                        }
                    } else
                        AllGuiTextures.TOOLBELT_EMPTY_SLOT.render(graphics, 0, 0);

                } else if (state == State.SELECT_BOX) {

                    if (slot < toolboxes.size()) {
                        AllGuiTextures.TOOLBELT_SLOT.render(graphics, 0, 0);
                        ToolboxBlockEntity toolboxBlockEntity = toolboxes.get(slot);
                        ItemStack stack = toolboxBlockEntity.getCachedState().getBlock().asItem().getDefaultStack();
                        graphics.drawItem(stack, 3, 3);

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
                    tip = CreateLang.translateDirect("toolbox.unequip", client.player.getMainHandStack().getName()).formatted(Formatting.GOLD);
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
                int l = textRenderer.getWidth(tip);
                graphics.drawText(textRenderer, tip, Math.round(-l / 2f), -4, k1 | k, false);
                ms.popMatrix();
            }
        }

    }

    @Override
    public void renderBackground(DrawContext pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
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
                toolboxes.forEach(be -> client.player.networkHandler.sendPacket(new ToolboxDisposeAllPacket(be.getPos())));
            else
                client.player.networkHandler.sendPacket(new ToolboxDisposeAllPacket(selectedBox.getPos()));
            return;
        }

        if (state == State.SELECT_BOX)
            return;

        if (state == State.DETACH) {
            if (selected == UNEQUIP)
                client.player.networkHandler.sendPacket(new ToolboxEquipPacket(null, selected, client.player.getInventory().getSelectedSlot()));
            return;
        }

        if (selected == UNEQUIP)
            client.player.networkHandler.sendPacket(new ToolboxEquipPacket(
                selectedBox.getPos(),
                selected,
                client.player.getInventory().getSelectedSlot()
            ));

        if (selected < 0)
            return;
        ToolboxInventory inv = selectedBox.inventory;
        ItemStack stackInSlot = inv.filters.get(selected);
        if (stackInSlot.isEmpty())
            return;
        if (inv.getStack(selected * STACKS_PER_COMPARTMENT).isEmpty())
            return;

        client.player.networkHandler.sendPacket(new ToolboxEquipPacket(
            selectedBox.getPos(),
            selected,
            client.player.getInventory().getSelectedSlot()
        ));
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        Window window = client.getWindow();
        double hoveredX = pMouseY - window.getScaledWidth() / 2;
        double hoveredY = pMouseY - window.getScaledHeight() / 2;
        double distance = hoveredX * hoveredX + hoveredY * hoveredY;
        if (distance <= 150) {
            scrollMode = true;
            scrollSlot = (((int) (scrollSlot - pScrollY)) + 8) % 8;
            for (int i = 0; i < 10; i++) {

                if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
                    ToolboxInventory inv = selectedBox.inventory;
                    ItemStack stackInSlot = inv.filters.get(scrollSlot);
                    if (!stackInSlot.isEmpty() && !inv.getStack(scrollSlot * STACKS_PER_COMPARTMENT).isEmpty())
                        break;
                }

                if (state == State.SELECT_BOX)
                    if (scrollSlot < toolboxes.size())
                        break;

                if (state == State.DETACH)
                    break;

                scrollSlot -= MathHelper.sign(pScrollY);
                scrollSlot = (scrollSlot + 8) % 8;
            }
            return true;
        }

        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int selected = scrollMode ? scrollSlot : hoveredSlot;
        int button = click.button();

        if (button == 0) {
            if (selected == DEPOSIT) {
                close();
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
                    close();
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
                    client.player.networkHandler.sendPacket(new ToolboxEquipPacket(
                        selectedBox.getPos(),
                        selected,
                        client.player.getInventory().getSelectedSlot()
                    ));
                    state = State.SELECT_BOX;
                    return true;
                }

                close();
                ToolboxHandlerClient.COOLDOWN = 2;
                return true;
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(int code, int scanCode, int modifiers) {
        KeyBinding[] hotbarBinds = client.options.hotbarKeys;
        for (int i = 0; i < hotbarBinds.length && i < 8; i++) {
            if (hotbarBinds[i].matchesKey(code, scanCode)) {

                if (state == State.SELECT_ITEM || state == State.SELECT_ITEM_UNEQUIP) {
                    ToolboxInventory inv = selectedBox.inventory;
                    ItemStack stackInSlot = inv.filters.get(i);
                    if (stackInSlot.isEmpty() || inv.getStack(i * STACKS_PER_COMPARTMENT).isEmpty())
                        return false;
                }

                if (state == State.SELECT_BOX)
                    if (i >= toolboxes.size())
                        return false;

                scrollMode = true;
                scrollSlot = i;
                mouseClicked(new Click(0, 0, new MouseInput(0, 0)), false);
                return true;
            }
        }

        return super.keyPressed(code, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int code, int scanCode, int modifiers) {
        InputUtil.Key mouseKey = InputUtil.fromKeyCode(code, scanCode);
        if (mouseKey == AllKeys.TOOLBELT.boundKey) {
            close();
            return true;
        }
        return super.keyReleased(code, scanCode, modifiers);
    }

    public enum State {
        SELECT_BOX,
        SELECT_ITEM,
        SELECT_ITEM_UNEQUIP,
        DETACH
    }

}
