package com.zurrtum.create.client.content.redstone.thresholdSwitch;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.AbstractSimiWidget;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags;
import com.zurrtum.create.client.ponder.foundation.ui.PonderTagScreen;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchBlockEntity.ThresholdType;
import com.zurrtum.create.content.redstone.thresholdSwitch.ThresholdSwitchObservable;
import com.zurrtum.create.infrastructure.fluids.BucketFluidInventory;
import com.zurrtum.create.infrastructure.packet.c2s.ConfigureThresholdSwitchPacket;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

import java.util.ArrayList;
import java.util.List;

public class ThresholdSwitchScreen extends AbstractSimiScreen {

    private ScrollInput offBelow;
    private ScrollInput onAbove;
    private SelectionScrollInput inStacks;

    private IconButton confirmButton;
    private IconButton flipSignals;

    private final Text invertSignal = CreateLang.translateDirect("gui.threshold_switch.invert_signal");
    private ElementWidget renderedItem;
    private ElementWidget torchPower;
    private ElementWidget torchOff;

    private AllGuiTextures background;
    private ThresholdSwitchBlockEntity blockEntity;
    private int lastModification;

    public ThresholdSwitchScreen(ThresholdSwitchBlockEntity be) {
        super(CreateLang.translateDirect("gui.threshold_switch.title"));
        background = AllGuiTextures.THRESHOLD_SWITCH;
        this.blockEntity = be;
        lastModification = -1;
    }

    @Override
    protected void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        setWindowOffset(-20, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        inStacks = (SelectionScrollInput) new SelectionScrollInput(x + 100, y + 23, 52, 42).forOptions(List.of(
            CreateLang.translateDirect(
                "schedule.condition.threshold.items"), CreateLang.translateDirect("schedule.condition.threshold.stacks")
        )).titled(CreateLang.translateDirect("schedule.condition.threshold.item_measure")).setState(blockEntity.inStacks ? 1 : 0);

        offBelow = new ScrollInput(x + 48, y + 47, 1, 18).withRange(blockEntity.getMinLevel(), blockEntity.getMaxLevel() + 1 - getValueStep())
            .titled(CreateLang.translateDirect("gui.threshold_switch.lower_threshold")).calling(state -> {
                lastModification = 0;
                int valueStep = getValueStep();

                if (onAbove.getState() / valueStep == 0 && state / valueStep == 0)
                    return;

                if (onAbove.getState() / valueStep <= state / valueStep) {
                    onAbove.setState((state + valueStep) / valueStep * valueStep);
                    onAbove.onChanged();
                }
            }).withStepFunction(sc -> sc.shift ? 10 * getValueStep() : getValueStep()).setState(blockEntity.offWhenBelow);

        onAbove = new ScrollInput(x + 48, y + 23, 1, 18).withRange(blockEntity.getMinLevel() + getValueStep(), blockEntity.getMaxLevel() + 1)
            .titled(CreateLang.translateDirect("gui.threshold_switch.upper_threshold")).calling(state -> {
                lastModification = 0;
                int valueStep = getValueStep();

                if (offBelow.getState() / valueStep == 0 && state / valueStep == 0)
                    return;

                if (offBelow.getState() / valueStep >= state / valueStep) {
                    offBelow.setState((state - valueStep) / valueStep * valueStep);
                    offBelow.onChanged();
                }
            }).withStepFunction(sc -> sc.shift ? 10 * getValueStep() : getValueStep()).setState(blockEntity.onWhenAbove);

        onAbove.onChanged();
        offBelow.onChanged();

        addDrawableChild(onAbove);
        addDrawableChild(offBelow);
        addDrawableChild(inStacks);

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::close);
        addDrawableChild(confirmButton);

        flipSignals = new IconButton(x + background.getWidth() - 62, y + background.getHeight() - 24, AllIcons.I_FLIP);
        flipSignals.withCallback(() -> send(!blockEntity.isInverted()));
        flipSignals.setToolTip(invertSignal);
        addDrawableChild(flipSignals);

        renderedItem = new ElementWidget(
            x + background.getWidth() + 6,
            y + background.getHeight() - 56
        ).showingElement(GuiGameElement.of(AllItems.THRESHOLD_SWITCH.getDefaultStack()).scale(5));
        addDrawableChild(renderedItem);

        BlockState torch = Blocks.REDSTONE_TORCH.getDefaultState();
        torchPower = new ElementWidget(x + 22, y + 19).showingElement(GuiGameElement.of(torch).rotate(-22.5F, 45, 0).scale(1.25F));
        addDrawableChild(torchPower);
        torchOff = new ElementWidget(x + 22, y + 43).showingElement(GuiGameElement.of(torch.with(RedstoneTorchBlock.LIT, false)).rotate(-22.5F, 45, 0)
            .scale(1.25F));
        addDrawableChild(torchOff);

        updateInputBoxes();
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
        torchPower.getRenderElement().clear();
        torchOff.getRenderElement().clear();
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        int itemX = guiLeft + 13;
        int itemY = guiTop + 80;
        double mouseX = click.x();
        double mouseY = click.y();
        if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
            ScreenOpener.open(new PonderTagScreen(AllCreatePonderTags.THRESHOLD_SWITCH_TARGETS));
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        graphics.drawText(textRenderer, title, x + background.getWidth() / 2 - textRenderer.getWidth(title) / 2, y + 4, 0xFF592424, false);

        ThresholdType typeOfCurrentTarget = blockEntity.getTypeOfCurrentTarget();
        boolean forItems = typeOfCurrentTarget == ThresholdType.ITEM;
        AllGuiTextures inputBg = forItems ? AllGuiTextures.THRESHOLD_SWITCH_ITEMCOUNT_INPUTS : AllGuiTextures.THRESHOLD_SWITCH_MISC_INPUTS;

        inputBg.render(graphics, x + 44, y + 21);
        inputBg.render(graphics, x + 44, y + 21 + 24);

        int valueStep = 1;
        boolean stacks = inStacks.getState() == 1;
        if (typeOfCurrentTarget == ThresholdType.FLUID)
            valueStep = BucketFluidInventory.CAPACITY;

        if (forItems) {
            Text suffix = inStacks.getState() == 0 ? CreateLang.translateDirect("schedule.condition.threshold.items") : CreateLang.translateDirect(
                "schedule.condition.threshold.stacks");
            valueStep = inStacks.getState() == 0 ? 1 : 64;
            graphics.drawText(textRenderer, suffix, x + 105, y + 28, 0xFFFFFFFF, true);
            graphics.drawText(textRenderer, suffix, x + 105, y + 28 + 24, 0xFFFFFFFF, true);

        }

        graphics.drawText(
            textRenderer,
            Text.literal("≥ " + (typeOfCurrentTarget == ThresholdType.UNSUPPORTED ? "" : forItems ? onAbove.getState() / valueStep : format(
                blockEntity,
                onAbove.getState() / valueStep,
                stacks
            ).getString())),
            x + 53,
            y + 28,
            0xFFFFFFFF,
            true
        );
        graphics.drawText(
            textRenderer,
            Text.literal("≤ " + (typeOfCurrentTarget == ThresholdType.UNSUPPORTED ? "" : forItems ? offBelow.getState() / valueStep : format(
                blockEntity,
                offBelow.getState() / valueStep,
                stacks
            ).getString())),
            x + 53,
            y + 28 + 24,
            0xFFFFFFFF,
            true
        );

        int itemX = x + 13;
        int itemY = y + 80;

        ItemStack displayItem = blockEntity.getDisplayItemForScreen();
        graphics.drawItem(displayItem.isEmpty() ? new ItemStack(Items.BARRIER) : displayItem, itemX, itemY);

        int torchX = x + 23;
        int torchY = y + 24;

        boolean highlightTopRow = blockEntity.isInverted() ^ blockEntity.isPowered();
        AllGuiTextures.THRESHOLD_SWITCH_CURRENT_STATE.render(graphics, torchX - 3, torchY - 4 + (highlightTopRow ? 0 : 24));
        if (blockEntity.isInverted()) {
            torchPower.setY(y + 43);
            torchOff.setY(y + 19);
        } else {
            torchPower.setY(y + 19);
            torchOff.setY(y + 43);
        }

        if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
            ArrayList<Text> list = new ArrayList<>();
            if (displayItem.isEmpty()) {
                list.add(CreateLang.translateDirect("gui.threshold_switch.not_attached"));
                list.add(CreateLang.translateDirect("display_link.view_compatible").formatted(Formatting.DARK_GRAY));
                graphics.drawTooltip(textRenderer, list, mouseX, mouseY);
                return;
            }

            list.add(displayItem.getName());
            if (typeOfCurrentTarget == ThresholdType.UNSUPPORTED) {
                list.add(CreateLang.translateDirect("gui.threshold_switch.incompatible").formatted(Formatting.GRAY));
                list.add(CreateLang.translateDirect("display_link.view_compatible").formatted(Formatting.DARK_GRAY));
                graphics.drawTooltip(textRenderer, list, mouseX, mouseY);
                return;
            }

            CreateLang.translate("gui.threshold_switch.currently", format(blockEntity, blockEntity.currentLevel / valueStep, stacks))
                .style(Formatting.DARK_AQUA).addTo(list);

            if (blockEntity.currentMinLevel / valueStep == 0)
                CreateLang.translate("gui.threshold_switch.range_max", format(blockEntity, blockEntity.currentMaxLevel / valueStep, stacks))
                    .style(Formatting.GRAY).addTo(list);
            else
                CreateLang.translate(
                    "gui.threshold_switch.range",
                    blockEntity.currentMinLevel / valueStep,
                    format(blockEntity, blockEntity.currentMaxLevel / valueStep, stacks)
                ).style(Formatting.GRAY).addTo(list);

            list.add(CreateLang.translateDirect("display_link.view_compatible").formatted(Formatting.DARK_GRAY));

            graphics.drawTooltip(textRenderer, list, mouseX, mouseY);
            return;
        }

        for (boolean power : Iterate.trueAndFalse) {
            int thisTorchY = power ? torchY : torchY + 26;
            if (mouseX >= torchX && mouseX < torchX + 16 && mouseY >= thisTorchY && mouseY < thisTorchY + 16) {
                graphics.drawTooltip(
                    textRenderer,
                    List.of(CreateLang.translate(power ^ blockEntity.isInverted() ? "gui.threshold_switch.power_on_when" : "gui.threshold_switch.power_off_when")
                        .color(AbstractSimiWidget.HEADER_RGB).component()),
                    mouseX,
                    mouseY
                );
                return;
            }
        }
    }

    public static MutableText format(ThresholdSwitchBlockEntity be, int value, boolean stacks) {
        ThresholdType type = be.getTypeOfCurrentTarget();
        if (type == ThresholdType.CUSTOM)
            if (be.getWorld().getBlockEntity(be.getTargetPos()) instanceof ThresholdSwitchObservable tso)
                return tso.format(value);

        String suffix = type == ThresholdType.ITEM ? stacks ? "schedule.condition.threshold.stacks" : "schedule.condition.threshold.items" : "schedule.condition.threshold.buckets";
        return CreateLang.text(value + " ").add(CreateLang.translate(suffix)).component();
    }

    @Override
    public void tick() {
        super.tick();

        if (lastModification >= 0)
            lastModification++;

        if (lastModification >= 20) {
            lastModification = -1;
            send(blockEntity.isInverted());
        }

        if (inStacks == null)
            return;

        updateInputBoxes();
    }

    private void updateInputBoxes() {
        ThresholdType typeOfCurrentTarget = blockEntity.getTypeOfCurrentTarget();
        boolean forItems = typeOfCurrentTarget == ThresholdType.ITEM;
        final int valueStep = getValueStep();
        inStacks.active = inStacks.visible = forItems;
        onAbove.setWidth(forItems ? 48 : 103);
        offBelow.setWidth(forItems ? 48 : 103);

        onAbove.visible = typeOfCurrentTarget != ThresholdType.UNSUPPORTED;
        offBelow.visible = typeOfCurrentTarget != ThresholdType.UNSUPPORTED;

        int min = blockEntity.currentMinLevel + valueStep;
        int max = blockEntity.currentMaxLevel;
        onAbove.withRange(min, max + 1);
        int roundedState = MathHelper.clamp((onAbove.getState() / valueStep) * valueStep, min, max);
        if (roundedState != onAbove.getState()) {
            onAbove.setState(roundedState);
            onAbove.onChanged();
        }

        min = blockEntity.currentMinLevel;
        max = blockEntity.currentMaxLevel - valueStep;
        offBelow.withRange(min, max + 1);
        roundedState = MathHelper.clamp((offBelow.getState() / valueStep) * valueStep, min, max);
        if (roundedState != offBelow.getState()) {
            offBelow.setState(roundedState);
            offBelow.onChanged();
        }
    }

    private int getValueStep() {
        boolean stacks = inStacks.getState() == 1;
        int valueStep = 1;
        if (blockEntity.getTypeOfCurrentTarget() == ThresholdType.FLUID)
            valueStep = BucketFluidInventory.CAPACITY;
        else if (stacks)
            valueStep = 64;
        return valueStep;
    }

    @Override
    public void removed() {
        send(blockEntity.isInverted());
    }

    protected void send(boolean invert) {
        client.player.networkHandler.sendPacket(new ConfigureThresholdSwitchPacket(
            blockEntity.getPos(),
            offBelow.getState(),
            onAbove.getState(),
            invert,
            inStacks.getState() == 1
        ));
    }

}
