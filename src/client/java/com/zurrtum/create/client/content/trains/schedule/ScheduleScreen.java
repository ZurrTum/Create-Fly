package com.zurrtum.create.client.content.trains.schedule;

import com.google.common.collect.ImmutableList;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSchedules;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.AllScheduleRenders;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.ModularGuiLine;
import com.zurrtum.create.client.foundation.gui.ModularGuiLineBuilder;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Indicator;
import com.zurrtum.create.client.foundation.gui.widget.Indicator.State;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.trains.GlobalRailwayManager;
import com.zurrtum.create.content.trains.graph.EdgePointType;
import com.zurrtum.create.content.trains.graph.TrackGraph;
import com.zurrtum.create.content.trains.schedule.Schedule;
import com.zurrtum.create.content.trains.schedule.ScheduleDataEntry;
import com.zurrtum.create.content.trains.schedule.ScheduleEntry;
import com.zurrtum.create.content.trains.schedule.ScheduleMenu;
import com.zurrtum.create.content.trains.schedule.condition.ScheduleWaitCondition;
import com.zurrtum.create.content.trains.schedule.destination.DestinationInstruction;
import com.zurrtum.create.content.trains.schedule.destination.ScheduleInstruction;
import com.zurrtum.create.content.trains.station.GlobalStation;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.packet.c2s.GhostItemSubmitPacket;
import com.zurrtum.create.infrastructure.packet.c2s.ScheduleEditPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.screen.slot.Slot;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.zurrtum.create.Create.LOGGER;

public class ScheduleScreen extends AbstractSimiContainerScreen<ScheduleMenu> {
    private static final int CARD_HEADER = 22;
    private static final int CARD_WIDTH = 195;

    private List<Rect2i> extraAreas = Collections.emptyList();

    private List<LerpedFloat> horizontalScrolls = new ArrayList<>();
    private LerpedFloat scroll = LerpedFloat.linear().startWithValue(0);
    private ElementWidget renderedItem;

    private Schedule schedule;

    private IconButton confirmButton;
    private IconButton cyclicButton;
    private Indicator cyclicIndicator;

    private IconButton resetProgress;
    private IconButton skipProgress;

    private ScheduleInstruction editingDestination;
    private ScheduleWaitCondition editingCondition;
    private SelectionScrollInput scrollInput;
    private Label scrollInputLabel;
    private IconButton editorConfirm, editorDelete;
    private ModularGuiLine editorSubWidgets;
    private Consumer<Boolean> onEditorClose;

    private DestinationSuggestions destinationSuggestions;

    public ScheduleScreen(ScheduleMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title);
        schedule = new Schedule();
        NbtCompound tag = handler.contentHolder.get(AllDataComponents.TRAIN_SCHEDULE);
        if (tag != null && !tag.isEmpty()) {
            try (ErrorReporter.Logging logging = new ErrorReporter.Logging(() -> "ScheduleScreen", LOGGER)) {
                ReadView view = NbtReadView.create(logging, handler.player.getRegistryManager(), tag);
                schedule = Schedule.read(view);
            }
        }
        handler.slotsActive = false;
        editorSubWidgets = new ModularGuiLine();
    }

    public static ScheduleScreen create(
        MinecraftClient mc,
        MenuType<ItemStack> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        return type.create(ScheduleScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    protected void init() {
        AllGuiTextures bg = AllGuiTextures.SCHEDULE;
        setWindowSize(bg.getWidth(), bg.getHeight());
        super.init();
        clearChildren();

        confirmButton = new IconButton(x + bg.getWidth() - 42, y + bg.getHeight() - 30, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> client.player.closeHandledScreen());
        addDrawableChild(confirmButton);

        cyclicIndicator = new Indicator(x + 21, y + 196, ScreenTexts.EMPTY);
        cyclicIndicator.state = schedule.cyclic ? State.ON : State.OFF;

        List<Text> tip = new ArrayList<>();
        tip.add(CreateLang.translateDirect("schedule.loop"));
        tip.add(CreateLang.translateDirect("gui.schematicannon.optionDisabled").formatted(Formatting.RED));
        tip.add(CreateLang.translateDirect("schedule.loop1").formatted(Formatting.GRAY));
        tip.add(CreateLang.translateDirect("schedule.loop2").formatted(Formatting.GRAY));

        List<Text> tipEnabled = new ArrayList<>(tip);
        tipEnabled.set(1, CreateLang.translateDirect("gui.schematicannon.optionEnabled").formatted(Formatting.DARK_GREEN));

        cyclicButton = new IconButton(x + 21, y + 196, AllIcons.I_REFRESH);
        cyclicButton.withCallback(() -> {
            schedule.cyclic = !schedule.cyclic;
            cyclicButton.green = schedule.cyclic;
            cyclicButton.getToolTip().clear();
            cyclicButton.getToolTip().addAll(schedule.cyclic ? tipEnabled : tip);
        });
        cyclicButton.green = schedule.cyclic;
        cyclicButton.getToolTip().clear();
        cyclicButton.getToolTip().addAll(schedule.cyclic ? tipEnabled : tip);

        addDrawableChild(cyclicButton);

        resetProgress = new IconButton(x + 45, y + 196, AllIcons.I_PRIORITY_VERY_HIGH);
        resetProgress.withCallback(() -> {
            schedule.savedProgress = 0;
            resetProgress.active = false;
        });
        resetProgress.active = schedule.savedProgress > 0 && !schedule.entries.isEmpty();
        resetProgress.setToolTip(CreateLang.translateDirect("schedule.reset"));
        addDrawableChild(resetProgress);

        skipProgress = new IconButton(x + 63, y + 196, AllIcons.I_PRIORITY_LOW);
        skipProgress.withCallback(() -> {
            schedule.savedProgress++;
            schedule.savedProgress %= schedule.entries.size();
            resetProgress.active = schedule.savedProgress > 0;
        });
        skipProgress.active = schedule.entries.size() > 1;
        skipProgress.setToolTip(CreateLang.translateDirect("schedule.skip"));
        addDrawableChild(skipProgress);

        stopEditing();
        extraAreas = ImmutableList.of(new Rect2i(x + bg.getWidth(), y + bg.getHeight() - 56, 48, 48));
        horizontalScrolls.clear();
        for (int i = 0; i < schedule.entries.size(); i++)
            horizontalScrolls.add(LerpedFloat.linear().startWithValue(0));

        renderedItem = new ElementWidget(x + AllGuiTextures.SCHEDULE.getWidth(), y + AllGuiTextures.SCHEDULE.getHeight() - 56).showingElement(
            GuiGameElement.of(handler.contentHolder).scale(3));
        addDrawableChild(renderedItem);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
    }

    public static <T> List<MutableText> getTypeOptions(List<Pair<Identifier, T>> list) {
        String langSection = list.equals(AllSchedules.INSTRUCTION_TYPES) ? "instruction." : "condition.";
        return list.stream().map(Pair::getFirst).map(rl -> rl.getNamespace() + ".schedule." + langSection + rl.getPath()).map(Text::translatable)
            .toList();
    }

    @SuppressWarnings("unchecked")
    protected <T extends ScheduleDataEntry> void startEditing(IScheduleInput<T> field, T input, Consumer<Boolean> onClose, boolean allowDeletion) {
        onEditorClose = onClose;
        confirmButton.visible = false;
        cyclicButton.visible = false;
        cyclicIndicator.visible = false;
        skipProgress.visible = false;
        resetProgress.visible = false;

        scrollInput = new SelectionScrollInput(x + 56, y + 65, 143, 16);
        scrollInputLabel = new Label(x + 59, y + 69, ScreenTexts.EMPTY).withShadow();
        editorConfirm = new IconButton(x + 56 + 168, y + 65 + 22, AllIcons.I_CONFIRM);
        if (allowDeletion)
            editorDelete = new IconButton(x + 56 - 45, y + 65 + 22, AllIcons.I_TRASH);
        handler.slotsActive = true;
        handler.targetSlotsActive = field.slotsTargeted();

        for (int i = 0; i < field.slotsTargeted(); i++) {
            ItemStack item = field.getItem(input, i);
            handler.ghostInventory.setStack(i, item);
            client.player.networkHandler.sendPacket(new GhostItemSubmitPacket(item, i));
        }

        if (input instanceof ScheduleInstruction instruction) {
            int startIndex = 0;
            for (int i = 0; i < AllSchedules.INSTRUCTION_TYPES.size(); i++)
                if (AllSchedules.INSTRUCTION_TYPES.get(i).getFirst().equals(instruction.getId()))
                    startIndex = i;
            editingDestination = instruction;
            updateEditorSubwidgets((IScheduleInput<ScheduleInstruction>) field, editingDestination);
            scrollInput.forOptions(getTypeOptions(AllSchedules.INSTRUCTION_TYPES)).titled(CreateLang.translateDirect("schedule.instruction_type"))
                .writingTo(scrollInputLabel).calling(index -> {
                    Pair<Identifier, Function<Identifier, ? extends ScheduleInstruction>> pair = AllSchedules.INSTRUCTION_TYPES.get(index);
                    ScheduleInstruction newlyCreated = pair.getSecond().apply(pair.getFirst());
                    if (editingDestination.getId().equals(newlyCreated.getId()))
                        return;
                    editingDestination = newlyCreated;
                    updateEditorSubwidgets(AllScheduleRenders.get(newlyCreated), editingDestination);
                }).setState(startIndex);
        }

        if (input instanceof ScheduleWaitCondition cond) {
            int startIndex = 0;
            for (int i = 0; i < AllSchedules.CONDITION_TYPES.size(); i++)
                if (AllSchedules.CONDITION_TYPES.get(i).getFirst().equals(cond.getId()))
                    startIndex = i;
            editingCondition = cond;
            updateEditorSubwidgets((IScheduleInput<ScheduleWaitCondition>) field, editingCondition);
            scrollInput.forOptions(getTypeOptions(AllSchedules.CONDITION_TYPES)).titled(CreateLang.translateDirect("schedule.condition_type"))
                .writingTo(scrollInputLabel).calling(index -> {
                    Pair<Identifier, Function<Identifier, ? extends ScheduleWaitCondition>> pair = AllSchedules.CONDITION_TYPES.get(index);
                    ScheduleWaitCondition newlyCreated = pair.getSecond().apply(pair.getFirst());
                    if (editingCondition.getId().equals(newlyCreated.getId()))
                        return;
                    editingCondition = newlyCreated;
                    updateEditorSubwidgets(AllScheduleRenders.get(newlyCreated), editingCondition);
                }).setState(startIndex);
        }

        addDrawableChild(scrollInput);
        addDrawableChild(scrollInputLabel);
        addDrawableChild(editorConfirm);
        if (allowDeletion)
            addDrawableChild(editorDelete);
    }

    private void onDestinationEdited(String text) {
        if (destinationSuggestions != null)
            destinationSuggestions.refresh();
    }

    protected void stopEditing() {
        confirmButton.visible = true;
        cyclicButton.visible = true;
        cyclicIndicator.visible = true;
        skipProgress.visible = true;
        resetProgress.visible = true;

        if (editingCondition == null && editingDestination == null)
            return;

        destinationSuggestions = null;

        remove(scrollInput);
        remove(scrollInputLabel);
        remove(editorConfirm);
        remove(editorDelete);

        ScheduleDataEntry input = editingCondition == null ? editingDestination : editingCondition;
        IScheduleInput<ScheduleDataEntry> editing = AllScheduleRenders.get(input);
        for (int i = 0; i < editing.slotsTargeted(); i++) {
            editing.setItem(input, i, handler.ghostInventory.getStack(i));
            client.player.networkHandler.sendPacket(new GhostItemSubmitPacket(ItemStack.EMPTY, i));
        }

        editorSubWidgets.saveValues(input.getData());
        editorSubWidgets.forEach(this::remove);
        editorSubWidgets.clear();

        editingCondition = null;
        editingDestination = null;
        editorConfirm = null;
        editorDelete = null;
        handler.slotsActive = false;
        renderedItem.getRenderElement().clear();
        init();
    }

    protected <T extends ScheduleDataEntry> void updateEditorSubwidgets(IScheduleInput<T> field, T input) {
        destinationSuggestions = null;
        handler.targetSlotsActive = field.slotsTargeted();

        editorSubWidgets.forEach(this::remove);
        editorSubWidgets.clear();
        field.initConfigurationWidgets(input, new ModularGuiLineBuilder(textRenderer, editorSubWidgets, x + 77, y + 92).speechBubble());
        editorSubWidgets.loadValues(input.getData(), this::addDrawableChild, this::addDrawable);

        if (!(input instanceof DestinationInstruction destinationInstruction))
            return;

        editorSubWidgets.forEach(e -> {
            if (!(e instanceof TextFieldWidget destinationBox))
                return;
            destinationSuggestions = new DestinationSuggestions(
                client,
                this,
                destinationBox,
                textRenderer,
                getViableStations(destinationInstruction),
                false,
                y + 33
            );
            destinationSuggestions.setWindowActive(true);
            destinationSuggestions.refresh();
            destinationBox.setChangedListener(this::onDestinationEdited);
        });
    }

    private List<IntAttached<String>> getViableStations(DestinationInstruction field) {
        GlobalRailwayManager railwayManager = Create.RAILWAYS;
        Set<TrackGraph> viableGraphs = new HashSet<>(railwayManager.trackNetworks.values());

        for (ScheduleEntry entry : schedule.entries) {
            if (!(entry.instruction instanceof DestinationInstruction destination))
                continue;
            if (destination == field)
                continue;
            String filter = destination.getFilterForRegex();
            if (filter.isBlank())
                continue;
            Graphs:
            for (Iterator<TrackGraph> iterator = viableGraphs.iterator(); iterator.hasNext(); ) {
                TrackGraph trackGraph = iterator.next();
                for (GlobalStation station : trackGraph.getPoints(EdgePointType.STATION)) {
                    if (station.name.matches(filter))
                        continue Graphs;
                }
                iterator.remove();
            }
        }

        if (viableGraphs.isEmpty())
            viableGraphs = new HashSet<>(railwayManager.trackNetworks.values());

        Vec3d position = client.player.getPos();
        Set<String> visited = new HashSet<>();

        return viableGraphs.stream().flatMap(g -> g.getPoints(EdgePointType.STATION).stream()).filter(station -> station.blockEntityPos != null)
            .filter(station -> visited.add(station.name))
            .map(station -> IntAttached.with((int) Vec3d.ofBottomCenter(station.blockEntityPos).distanceTo(position), station.name)).toList();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        scroll.tickChaser();
        for (LerpedFloat lerpedFloat : horizontalScrolls)
            lerpedFloat.tickChaser();

        if (destinationSuggestions != null)
            destinationSuggestions.tick();

        schedule.savedProgress = schedule.entries.isEmpty() ? 0 : MathHelper.clamp(schedule.savedProgress, 0, schedule.entries.size() - 1);
        resetProgress.active = schedule.savedProgress > 0;
        skipProgress.active = schedule.entries.size() > 1;
    }

    @Override
    public void render(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        partialTicks = client.getRenderTickCounter().getTickProgress(false);

        if (handler.slotsActive)
            super.render(graphics, mouseX, mouseY, partialTicks);
        else {
            for (Drawable widget : drawables)
                widget.render(graphics, mouseX, mouseY, partialTicks);
            renderForeground(graphics, mouseX, mouseY, partialTicks);
        }
    }

    protected void renderSchedule(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        Matrix3x2fStack matrixStack = graphics.getMatrices();
        UIRenderHelper.drawStretched(graphics, x + 33, y + 16, 3, 173, AllGuiTextures.SCHEDULE_STRIP_DARK);

        int yOffset = 25;
        List<ScheduleEntry> entries = schedule.entries;
        float scrollOffset = -scroll.getValue(partialTicks);

        graphics.enableScissor(x, y + 16, x + 236, y + 189);

        for (int i = 0; i <= entries.size(); i++) {

            if (schedule.savedProgress == i && !schedule.entries.isEmpty()) {
                matrixStack.pushMatrix();
                float expectedY = scrollOffset + y + yOffset + 4;
                float actualY = MathHelper.clamp(expectedY, y + 18, y + 170);
                matrixStack.translate(0, actualY);
                (expectedY == actualY ? AllGuiTextures.SCHEDULE_POINTER : AllGuiTextures.SCHEDULE_POINTER_OFFSCREEN).render(graphics, x, 0);
                matrixStack.popMatrix();
            }

            matrixStack.pushMatrix();
            matrixStack.translate(0, scrollOffset);
            if (i == 0 || entries.size() == 0)
                UIRenderHelper.drawStretched(graphics, x + 33, y + 16, 3, 10, AllGuiTextures.SCHEDULE_STRIP_LIGHT);

            if (i == entries.size()) {
                if (i > 0)
                    yOffset += 9;
                AllGuiTextures.SCHEDULE_STRIP_END.render(graphics, x + 29, y + yOffset);
                AllGuiTextures.SCHEDULE_CARD_NEW.render(graphics, x + 43, y + yOffset);
                matrixStack.popMatrix();
                break;
            }

            ScheduleEntry scheduleEntry = entries.get(i);
            int cardY = yOffset;
            int cardHeight = renderScheduleEntry(graphics, scheduleEntry, cardY, mouseX, mouseY, partialTicks);
            yOffset += cardHeight;

            if (i + 1 < entries.size()) {
                AllGuiTextures.SCHEDULE_STRIP_DOTTED.render(graphics, x + 29, y + yOffset - 3);
                yOffset += 10;
            }

            matrixStack.popMatrix();

            if (!scheduleEntry.instruction.supportsConditions())
                continue;

            float h = cardHeight - 26;
            float y1 = cardY + 24 + scrollOffset;
            float y2 = y1 + h;
            if (y2 > 189)
                h -= y2 - 189;
            if (y1 < 16) {
                float correction = 16 - y1;
                y1 += correction;
                h -= correction;
            }

            if (h <= 0)
                continue;

            graphics.enableScissor(x + 43, 0, x + 204, 400);
            matrixStack.pushMatrix();
            matrixStack.translate(0, scrollOffset);
            renderScheduleConditions(graphics, scheduleEntry, cardY, mouseX, mouseY, partialTicks, cardHeight, i);
            matrixStack.popMatrix();
            graphics.disableScissor();

            if (isConditionAreaScrollable(scheduleEntry)) {
                matrixStack.pushMatrix();
                matrixStack.translate(0, scrollOffset);
                int center = (cardHeight - 8 + CARD_HEADER) / 2;
                float chaseTarget = horizontalScrolls.get(i).getChaseTarget();
                if (!MathHelper.approximatelyEquals(chaseTarget, 0))
                    AllGuiTextures.SCHEDULE_SCROLL_LEFT.render(graphics, x + 40, y + cardY + center);
                if (!MathHelper.approximatelyEquals(chaseTarget, scheduleEntry.conditions.size() - 1))
                    AllGuiTextures.SCHEDULE_SCROLL_RIGHT.render(graphics, x + 203, y + cardY + center);
                matrixStack.popMatrix();
            }
        }

        graphics.disableScissor();

        graphics.fillGradient(x + 16, y + 16, x + 16 + 220, y + 16 + 10, 0x77000000, 0x00000000);
        graphics.fillGradient(x + 16, y + 179, x + 16 + 220, y + 179 + 10, 0x00000000, 0x77000000);
    }

    public int renderScheduleEntry(DrawContext graphics, ScheduleEntry entry, int yOffset, int mouseX, int mouseY, float partialTicks) {
        AllGuiTextures light = AllGuiTextures.SCHEDULE_CARD_LIGHT;
        AllGuiTextures medium = AllGuiTextures.SCHEDULE_CARD_MEDIUM;
        AllGuiTextures dark = AllGuiTextures.SCHEDULE_CARD_DARK;

        int cardWidth = CARD_WIDTH;
        int cardHeader = CARD_HEADER;
        int maxRows = 0;
        for (List<ScheduleWaitCondition> list : entry.conditions)
            maxRows = Math.max(maxRows, list.size());
        ScheduleInstruction instruction = entry.instruction;
        boolean supportsConditions = instruction.supportsConditions();
        int cardHeight = cardHeader + (supportsConditions ? 24 + maxRows * 18 : 4);

        Matrix3x2fStack matrixStack = graphics.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(x + 25, y + yOffset);

        UIRenderHelper.drawStretched(graphics, 0, 1, cardWidth, cardHeight - 2, light);
        UIRenderHelper.drawStretched(graphics, 1, 0, cardWidth - 2, cardHeight, light);
        UIRenderHelper.drawStretched(graphics, 1, 1, cardWidth - 2, cardHeight - 2, dark);
        UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeight - 4, medium);
        UIRenderHelper.drawStretched(graphics, 2, 2, cardWidth - 4, cardHeader, supportsConditions ? light : medium);

        AllGuiTextures.SCHEDULE_CARD_REMOVE.render(graphics, cardWidth - 14, 2);
        AllGuiTextures.SCHEDULE_CARD_DUPLICATE.render(graphics, cardWidth - 14, cardHeight - 14);

        int i = schedule.entries.indexOf(entry);
        if (i > 0)
            AllGuiTextures.SCHEDULE_CARD_MOVE_UP.render(graphics, cardWidth, cardHeader - 14);
        if (i < schedule.entries.size() - 1)
            AllGuiTextures.SCHEDULE_CARD_MOVE_DOWN.render(graphics, cardWidth, cardHeader);

        UIRenderHelper.drawStretched(graphics, 8, 0, 3, cardHeight + 10, AllGuiTextures.SCHEDULE_STRIP_LIGHT);
        (supportsConditions ? AllGuiTextures.SCHEDULE_STRIP_TRAVEL : AllGuiTextures.SCHEDULE_STRIP_ACTION).render(graphics, 4, 6);

        if (supportsConditions)
            AllGuiTextures.SCHEDULE_STRIP_WAIT.render(graphics, 4, 28);

        IScheduleInput<ScheduleInstruction> scheduleInput = AllScheduleRenders.get(instruction);
        Pair<ItemStack, Text> destination = scheduleInput.getSummary(instruction);
        renderInput(graphics, destination, 26, 5, false, 100);
        scheduleInput.renderSpecialIcon(instruction, graphics, 30, 5);

        matrixStack.popMatrix();

        return cardHeight;
    }

    public void renderScheduleConditions(
        DrawContext graphics,
        ScheduleEntry entry,
        int yOffset,
        int mouseX,
        int mouseY,
        float partialTicks,
        int cardHeight,
        int entryIndex
    ) {
        int cardWidth = CARD_WIDTH;
        int cardHeader = CARD_HEADER;

        Matrix3x2fStack matrixStack = graphics.getMatrices();
        matrixStack.pushMatrix();
        matrixStack.translate(x + 25, y + yOffset);
        int xOffset = 26;
        float scrollOffset = getConditionScroll(entry, partialTicks, entryIndex);

        matrixStack.pushMatrix();
        matrixStack.translate(-scrollOffset, 0);

        for (List<ScheduleWaitCondition> list : entry.conditions) {
            int maxWidth = getConditionColumnWidth(list);
            for (int i = 0; i < list.size(); i++) {
                ScheduleWaitCondition scheduleWaitCondition = list.get(i);
                IScheduleInput<ScheduleWaitCondition> scheduleInput = AllScheduleRenders.get(scheduleWaitCondition);
                Math.max(maxWidth, renderInput(graphics, scheduleInput.getSummary(scheduleWaitCondition), xOffset, 29 + i * 18, i != 0, maxWidth));
                scheduleInput.renderSpecialIcon(scheduleWaitCondition, graphics, xOffset + 4, 29 + i * 18);
            }

            AllGuiTextures.SCHEDULE_CONDITION_APPEND.render(graphics, xOffset + (maxWidth - 10) / 2, 29 + list.size() * 18);
            xOffset += maxWidth + 10;
        }

        AllGuiTextures.SCHEDULE_CONDITION_NEW.render(graphics, xOffset - 3, 29);
        matrixStack.popMatrix();

        if (xOffset + 16 > cardWidth - 26) {
            matrixStack.rotate(MathHelper.RADIANS_PER_DEGREE * -90);
            graphics.fillGradient(-cardHeight + 2, 18, -2 - cardHeader, 28, 0x44000000, 0x00000000);
            graphics.fillGradient(-cardHeight + 2, cardWidth - 26, -2 - cardHeader, cardWidth - 16, 0x00000000, 0x44000000);
        }

        matrixStack.popMatrix();
    }

    private boolean isConditionAreaScrollable(ScheduleEntry entry) {
        int xOffset = 26;
        for (List<ScheduleWaitCondition> list : entry.conditions)
            xOffset += getConditionColumnWidth(list) + 10;
        return xOffset + 16 > CARD_WIDTH - 26;
    }

    private float getConditionScroll(ScheduleEntry entry, float partialTicks, int entryIndex) {
        float scrollOffset = 0;
        float scrollIndex = horizontalScrolls.get(entryIndex).getValue(partialTicks);
        for (List<ScheduleWaitCondition> list : entry.conditions) {
            int maxWidth = getConditionColumnWidth(list);
            float partialOfThisColumn = Math.min(1, scrollIndex);
            scrollOffset += (maxWidth + 10) * partialOfThisColumn;
            scrollIndex -= partialOfThisColumn;
        }
        return scrollOffset;
    }

    private int getConditionColumnWidth(List<ScheduleWaitCondition> list) {
        int maxWidth = 0;
        for (ScheduleWaitCondition scheduleWaitCondition : list) {
            IScheduleInput<ScheduleWaitCondition> scheduleInput = AllScheduleRenders.get(scheduleWaitCondition);
            maxWidth = Math.max(maxWidth, getFieldSize(32, scheduleInput.getSummary(scheduleWaitCondition)));
        }
        return maxWidth;
    }

    protected int renderInput(DrawContext graphics, Pair<ItemStack, Text> pair, int x, int y, boolean clean, int minSize) {
        ItemStack stack = pair.getFirst();
        Text text = pair.getSecond();
        boolean hasItem = !stack.isEmpty();
        int fieldSize = Math.min(getFieldSize(minSize, pair), 150);
        Matrix3x2fStack matrixStack = graphics.getMatrices();
        matrixStack.pushMatrix();

        AllGuiTextures left = clean ? AllGuiTextures.SCHEDULE_CONDITION_LEFT_CLEAN : AllGuiTextures.SCHEDULE_CONDITION_LEFT;
        AllGuiTextures middle = AllGuiTextures.SCHEDULE_CONDITION_MIDDLE;
        AllGuiTextures item = AllGuiTextures.SCHEDULE_CONDITION_ITEM;
        AllGuiTextures right = AllGuiTextures.SCHEDULE_CONDITION_RIGHT;

        matrixStack.translate(x, y);
        UIRenderHelper.drawStretched(graphics, 0, 0, fieldSize, 16, middle);
        left.render(graphics, clean ? 0 : -3, 0);
        right.render(graphics, fieldSize - 2, 0);
        if (hasItem) {
            item.render(graphics, 3, 0);
            if (stack.getItem() != Items.STRUCTURE_VOID)
                graphics.drawItem(stack, 4, 0);
        }

        if (text != null)
            graphics.drawText(textRenderer, textRenderer.trimToWidth(text, 120).getString(), hasItem ? 28 : 8, 4, 0xff_f2f2ee, true);

        matrixStack.popMatrix();
        return fieldSize;
    }

    private Text clickToEdit = CreateLang.translateDirect("gui.schedule.lmb_edit").formatted(Formatting.DARK_GRAY, Formatting.ITALIC);
    private Text rClickToDelete = CreateLang.translateDirect("gui.schedule.rmb_remove").formatted(Formatting.DARK_GRAY, Formatting.ITALIC);

    public boolean action(@Nullable DrawContext graphics, double mouseX, double mouseY, int click) {
        if (editingCondition != null || editingDestination != null)
            return false;

        Text empty = ScreenTexts.EMPTY;

        int mx = (int) mouseX;
        int my = (int) mouseY;
        int x = mx - this.x - 25;
        int y = my - this.y - 25;
        if (x < 0 || x >= 205)
            return false;
        if (y < 0 || y >= 173)
            return false;
        y += scroll.getValue(0);

        List<ScheduleEntry> entries = schedule.entries;
        for (int i = 0; i < entries.size(); i++) {
            ScheduleEntry entry = entries.get(i);
            int maxRows = 0;
            for (List<ScheduleWaitCondition> list : entry.conditions)
                maxRows = Math.max(maxRows, list.size());
            ScheduleInstruction instruction = entry.instruction;
            int cardHeight = CARD_HEADER + (instruction.supportsConditions() ? 24 + maxRows * 18 : 4);

            if (y >= cardHeight + 5) {
                y -= cardHeight + 10;
                if (y < 0)
                    return false;
                continue;
            }

            IScheduleInput<ScheduleInstruction> input = AllScheduleRenders.get(instruction);
            int fieldSize = getFieldSize(100, input.getSummary(instruction));
            if (x > 25 && x <= 25 + fieldSize && y > 4 && y <= 20) {
                List<Text> components = new ArrayList<>();
                components.addAll(input.getTitleAs(instruction, "instruction"));
                components.add(empty);
                components.add(clickToEdit);
                renderActionTooltip(graphics, components, mx, my);
                if (click == 0)
                    startEditing(
                        input, instruction, confirmed -> {
                            if (confirmed)
                                entry.instruction = editingDestination;
                        }, false
                    );
                return true;
            }

            if (x > 180 && x <= 192) {
                if (y > 0 && y <= 14) {
                    renderActionTooltip(graphics, ImmutableList.of(CreateLang.translateDirect("gui.schedule.remove_entry")), mx, my);
                    if (click == 0) {
                        entries.remove(entry);
                        renderedItem.getRenderElement().clear();
                        init();
                    }
                    return true;
                }
                if (y > cardHeight - 14) {
                    renderActionTooltip(graphics, ImmutableList.of(CreateLang.translateDirect("gui.schedule.duplicate")), mx, my);
                    if (click == 0) {
                        entries.add(entries.indexOf(entry), entry.clone(client.world.getRegistryManager()));
                        renderedItem.getRenderElement().clear();
                        init();
                    }
                    return true;
                }
            }

            if (x > 194) {
                if (y > 7 && y <= 20 && i > 0) {
                    renderActionTooltip(graphics, ImmutableList.of(CreateLang.translateDirect("gui.schedule.move_up")), mx, my);
                    if (click == 0) {
                        entries.remove(entry);
                        entries.add(i - 1, entry);
                        renderedItem.getRenderElement().clear();
                        init();
                    }
                    return true;
                }
                if (y > 20 && y <= 33 && i < entries.size() - 1) {
                    renderActionTooltip(graphics, ImmutableList.of(CreateLang.translateDirect("gui.schedule.move_down")), mx, my);
                    if (click == 0) {
                        entries.remove(entry);
                        entries.add(i + 1, entry);
                        renderedItem.getRenderElement().clear();
                        init();
                    }
                    return true;
                }
            }

            int center = (cardHeight - 8 + CARD_HEADER) / 2;
            if (y > center - 1 && y <= center + 7 && isConditionAreaScrollable(entry)) {
                float chaseTarget = horizontalScrolls.get(i).getChaseTarget();
                if (x > 12 && x <= 19 && !MathHelper.approximatelyEquals(chaseTarget, 0)) {
                    if (click == 0)
                        horizontalScrolls.get(i).chase(chaseTarget - 1, 0.5f, Chaser.EXP);
                    return true;
                }
                if (x > 177 && x <= 184 && !MathHelper.approximatelyEquals(chaseTarget, entry.conditions.size() - 1)) {
                    if (click == 0)
                        horizontalScrolls.get(i).chase(chaseTarget + 1, 0.5f, Chaser.EXP);
                    return true;
                }
            }

            x -= 18;
            y -= 28;
            if (x < 0 || y < 0 || x > 160)
                return false;
            x += getConditionScroll(entry, 0, i) - 8;

            List<List<ScheduleWaitCondition>> columns = entry.conditions;
            for (int j = 0; j < columns.size(); j++) {
                List<ScheduleWaitCondition> conditions = columns.get(j);
                if (x < 0)
                    return false;
                int w = getConditionColumnWidth(conditions);
                if (x >= w) {
                    x -= w + 10;
                    continue;
                }

                int row = y / 18;
                if (row < conditions.size() && row >= 0) {
                    boolean canRemove = conditions.size() > 1 || columns.size() > 1;
                    List<Text> components = new ArrayList<>();
                    components.add(CreateLang.translateDirect("schedule.condition_type").formatted(Formatting.GRAY));
                    ScheduleWaitCondition condition = conditions.get(row);
                    IScheduleInput<ScheduleWaitCondition> scheduleInput = AllScheduleRenders.get(condition);
                    components.addAll(scheduleInput.getTitleAs(condition, "condition"));
                    components.add(empty);
                    components.add(clickToEdit);
                    if (canRemove)
                        components.add(rClickToDelete);
                    renderActionTooltip(graphics, components, mx, my);
                    if (canRemove && click == 1) {
                        conditions.remove(row);
                        if (conditions.isEmpty())
                            columns.remove(conditions);
                    }
                    if (click == 0)
                        startEditing(
                            scheduleInput, condition, confirmed -> {
                                conditions.remove(row);
                                if (confirmed) {
                                    conditions.add(row, editingCondition);
                                    return;
                                }
                                if (conditions.isEmpty())
                                    columns.remove(conditions);
                            }, canRemove
                        );
                    return true;
                }

                if (y > 18 * conditions.size() && y <= 18 * conditions.size() + 10 && x >= w / 2 - 5 && x < w / 2 + 5) {
                    renderActionTooltip(graphics, ImmutableList.of(CreateLang.translateDirect("gui.schedule.add_condition")), mx, my);
                    if (click == 0) {
                        ScheduleWaitCondition condition = AllSchedules.createScheduleWaitCondition(AllSchedules.DELAY);
                        IScheduleInput<ScheduleWaitCondition> scheduleInput = AllScheduleRenders.get(condition);
                        startEditing(
                            scheduleInput, condition, confirmed -> {
                                if (confirmed)
                                    conditions.add(editingCondition);
                            }, true
                        );
                    }
                    return true;
                }

                return false;
            }

            if (x < 0 || x > 15 || y > 20)
                return false;

            renderActionTooltip(graphics, ImmutableList.of(CreateLang.translateDirect("gui.schedule.alternative_condition")), mx, my);
            if (click == 0) {
                ScheduleWaitCondition condition = AllSchedules.createScheduleWaitCondition(AllSchedules.DELAY);
                IScheduleInput<ScheduleWaitCondition> scheduleInput = AllScheduleRenders.get(condition);
                startEditing(
                    scheduleInput, condition, confirmed -> {
                        if (!confirmed)
                            return;
                        ArrayList<ScheduleWaitCondition> conditions = new ArrayList<>();
                        conditions.add(editingCondition);
                        columns.add(conditions);
                    }, true
                );
            }
            return true;
        }

        if (x < 18 || x > 33 || y > 14)
            return false;

        renderActionTooltip(graphics, ImmutableList.of(CreateLang.translateDirect("gui.schedule.add_entry")), mx, my);
        if (click == 0) {
            ScheduleInstruction instruction = AllSchedules.createScheduleInstruction(AllSchedules.DESTINATION);
            IScheduleInput<ScheduleInstruction> scheduleInput = AllScheduleRenders.get(instruction);
            startEditing(
                scheduleInput, instruction, confirmed -> {
                    if (!confirmed)
                        return;

                    ScheduleEntry entry = new ScheduleEntry();
                    ScheduleWaitCondition condition = AllSchedules.createScheduleWaitCondition(AllSchedules.DELAY);
                    ArrayList<ScheduleWaitCondition> initialConditions = new ArrayList<>();
                    initialConditions.add(condition);
                    entry.instruction = editingDestination;
                    entry.conditions.add(initialConditions);
                    schedule.entries.add(entry);
                }, true
            );
        }
        return true;
    }

    private void renderActionTooltip(@Nullable DrawContext graphics, List<Text> tooltip, int mx, int my) {
        if (graphics != null)
            graphics.drawTooltip(textRenderer, tooltip, Optional.empty(), mx, my);
    }

    private int getFieldSize(int minSize, Pair<ItemStack, Text> pair) {
        ItemStack stack = pair.getFirst();
        Text text = pair.getSecond();
        boolean hasItem = !stack.isEmpty();
        return Math.max((text == null ? 0 : textRenderer.getWidth(text)) + (hasItem ? 20 : 0) + 16, minSize);
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        if (destinationSuggestions != null && destinationSuggestions.mouseClicked((int) pMouseX, (int) pMouseY, pButton))
            return true;
        if (editorConfirm != null && editorConfirm.isMouseOver(pMouseX, pMouseY) && onEditorClose != null) {
            onEditorClose.accept(true);
            stopEditing();
            return true;
        }
        if (editorDelete != null && editorDelete.isMouseOver(pMouseX, pMouseY) && onEditorClose != null) {
            onEditorClose.accept(false);
            stopEditing();
            return true;
        }
        if (action(null, pMouseX, pMouseY, pButton))
            return true;

        return super.mouseClicked(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        if (destinationSuggestions != null && destinationSuggestions.keyPressed(pKeyCode, pScanCode, pModifiers))
            return true;
        if (editingCondition == null && editingDestination == null)
            return super.keyPressed(pKeyCode, pScanCode, pModifiers);
        boolean hitEnter = getFocused() instanceof TextFieldWidget && (pKeyCode == 257 || pKeyCode == 335);
        boolean hitE = getFocused() == null || client.options.inventoryKey.matchesKey(pKeyCode, pScanCode);
        if (hitEnter) {
            onEditorClose.accept(true);
            stopEditing();
            return true;
        } else if (hitE) {
            return false;
        }
        return super.keyPressed(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        if (destinationSuggestions != null && destinationSuggestions.mouseScrolled(MathHelper.clamp(pScrollY, -1.0D, 1.0D)))
            return true;
        if (editingCondition != null || editingDestination != null)
            return hoveredElement(pMouseX, pMouseY).filter(element -> element.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY)).isPresent();

        if (hasShiftDown()) {
            List<ScheduleEntry> entries = schedule.entries;
            int y = (int) (pMouseY - this.y - 25 + scroll.getValue());
            for (int i = 0; i < entries.size(); i++) {
                ScheduleEntry entry = entries.get(i);
                int maxRows = 0;
                for (List<ScheduleWaitCondition> list : entry.conditions)
                    maxRows = Math.max(maxRows, list.size());
                int cardHeight = CARD_HEADER + 24 + maxRows * 18;

                if (y >= cardHeight) {
                    y -= cardHeight + 9;
                    if (y < 0)
                        break;
                    continue;
                }

                if (!isConditionAreaScrollable(entry))
                    break;
                if (y < 24)
                    break;
                if (pMouseX < x + 25)
                    break;
                if (pMouseX > x + 205)
                    break;
                float chaseTarget = horizontalScrolls.get(i).getChaseTarget();
                if (pScrollY > 0 && !MathHelper.approximatelyEquals(chaseTarget, 0)) {
                    horizontalScrolls.get(i).chase(chaseTarget - 1, 0.5f, Chaser.EXP);
                    return true;
                }
                if (pScrollY < 0 && !MathHelper.approximatelyEquals(chaseTarget, entry.conditions.size() - 1)) {
                    horizontalScrolls.get(i).chase(chaseTarget + 1, 0.5f, Chaser.EXP);
                    return true;
                }
                return false;
            }
        }

        float chaseTarget = scroll.getChaseTarget();
        float max = 40 - 173;
        for (ScheduleEntry scheduleEntry : schedule.entries) {
            int maxRows = 0;
            for (List<ScheduleWaitCondition> list : scheduleEntry.conditions)
                maxRows = Math.max(maxRows, list.size());
            max += CARD_HEADER + 24 + maxRows * 18 + 10;
        }
        if (max > 0) {
            chaseTarget -= pScrollY * 12;
            chaseTarget = MathHelper.clamp(chaseTarget, 0, max);
            scroll.chase((int) chaseTarget, 0.7f, Chaser.EXP);
        } else
            scroll.chase(0, 0.7f, Chaser.EXP);

        return super.mouseScrolled(pMouseX, pMouseY, pScrollX, pScrollY);
    }

    @Override
    protected void renderForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        Matrix3x2fStack matrixStack = graphics.getMatrices();
        if (destinationSuggestions != null) {
            matrixStack.pushMatrix();
            destinationSuggestions.render(graphics, mouseX, mouseY);
            matrixStack.popMatrix();
        }

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);

        action(graphics, mouseX, mouseY, -1);

        if (editingCondition == null && editingDestination == null)
            return;

        int x = this.x + 53;
        int y = this.y + 87;
        if (mouseX < x || mouseY < y || mouseX >= x + 120 || mouseY >= y + 18)
            return;

        ScheduleDataEntry entry = editingCondition == null ? editingDestination : editingCondition;
        IScheduleInput<ScheduleDataEntry> rendered = AllScheduleRenders.get(entry);

        for (int i = 0; i < Math.max(1, rendered.slotsTargeted()); i++) {
            List<Text> secondLineTooltip = rendered.getSecondLineTooltip(i);
            if (secondLineTooltip == null)
                continue;
            Slot slot = handler.getSlot(36 + i);
            if (slot == null || !slot.getStack().isEmpty())
                continue;
            if (mouseX < this.x + slot.x || mouseX > this.x + slot.x + 18)
                continue;
            if (mouseY < this.y + slot.y || mouseY > this.y + slot.y + 18)
                continue;
            renderActionTooltip(graphics, secondLineTooltip, mouseX, mouseY);
        }
    }

    @Override
    protected void drawBackground(DrawContext graphics, float pPartialTick, int pMouseX, int pMouseY) {
        pPartialTick = AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter());
        AllGuiTextures.SCHEDULE.render(graphics, x, y);
        OrderedText formattedcharsequence = title.asOrderedText();
        int center = x + (AllGuiTextures.SCHEDULE.getWidth() - 8) / 2;
        graphics.drawText(textRenderer, formattedcharsequence, center - textRenderer.getWidth(formattedcharsequence) / 2, y + 4, 0xFF505050, false);
        renderSchedule(graphics, pMouseX, pMouseY, pPartialTick);

        if (editingCondition == null && editingDestination == null)
            return;

        graphics.fillGradient(0, 0, this.width, this.height, -1072689136, -804253680);
        AllGuiTextures.SCHEDULE_EDITOR.render(graphics, x - 2, y + 40);
        AllGuiTextures.PLAYER_INVENTORY.render(graphics, x + 38, y + 122);
        graphics.drawText(textRenderer, playerInventoryTitle, x + 46, y + 128, 0xFF505050, false);

        formattedcharsequence = editingCondition == null ? CreateLang.translateDirect("schedule.instruction.editor")
            .asOrderedText() : CreateLang.translateDirect("schedule.condition.editor").asOrderedText();
        graphics.drawText(
            textRenderer,
            formattedcharsequence,
            (center - textRenderer.getWidth(formattedcharsequence) / 2),
            y + 44,
            0xFF505050,
            false
        );

        ScheduleDataEntry entry = editingCondition == null ? editingDestination : editingCondition;
        IScheduleInput<ScheduleDataEntry> rendered = AllScheduleRenders.get(entry);

        for (int i = 0; i < rendered.slotsTargeted(); i++)
            AllGuiTextures.SCHEDULE_EDITOR_ADDITIONAL_SLOT.render(graphics, x + 53 + 20 * i, y + 87);

        if (rendered.slotsTargeted() == 0 && !rendered.renderSpecialIcon(entry, graphics, x + 54, y + 88)) {
            Pair<ItemStack, Text> summary = rendered.getSummary(entry);
            ItemStack icon = summary.getFirst();
            if (icon.isEmpty())
                icon = rendered.getSecondLineIcon();
            if (icon.isEmpty())
                AllGuiTextures.SCHEDULE_EDITOR_INACTIVE_SLOT.render(graphics, x + 53, y + 87);
            else
                graphics.drawItem(icon, x + 54, y + 88);
        }

        Matrix3x2fStack pPoseStack = graphics.getMatrices();
        pPoseStack.pushMatrix();
        pPoseStack.translate(0, y + 87);
        editorSubWidgets.renderWidgetBG(x + 77, graphics);
        pPoseStack.popMatrix();
    }

    @Override
    public void removed() {
        super.removed();
        client.player.networkHandler.sendPacket(new ScheduleEditPacket(schedule));
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

}
