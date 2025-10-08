package com.zurrtum.create.client.content.equipment.clipboard;

import com.google.common.collect.Lists;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.clipboard.ClipboardOverrides;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import com.zurrtum.create.infrastructure.component.ClipboardType;
import com.zurrtum.create.infrastructure.packet.c2s.ClipboardEditPacket;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.font.TextHandler;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.PageTurnWidget;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.SelectionManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClipboardScreen extends AbstractSimiScreen {
    public ItemStack item;
    public BlockPos targetedBlock;

    List<List<ClipboardEntry>> pages;
    List<ClipboardEntry> currentEntries;
    int editingIndex;
    int frameTick;
    PageTurnWidget forward;
    PageTurnWidget backward;
    int currentPage;
    long lastClickTime;
    int lastIndex = -1;

    int hoveredEntry;
    boolean hoveredCheck;
    boolean readonly;

    DisplayCache displayCache = DisplayCache.EMPTY;
    SelectionManager editContext;

    IconButton closeBtn;
    IconButton clearBtn;

    private int targetSlot;

    public ClipboardScreen(int targetSlot, ItemStack item, @Nullable BlockPos pos) {
        this.targetSlot = targetSlot;
        this.targetedBlock = pos;
        reopenWith(item);
    }

    public void reopenWith(ItemStack clipboard) {
        item = clipboard;
        pages = ClipboardEntry.readAll(item);
        if (pages.isEmpty())
            pages.add(new ArrayList<>());
        if (clearBtn == null) {
            currentPage = item.getOrDefault(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE, 0);
            currentPage = MathHelper.clamp(currentPage, 0, pages.size() - 1);
        }
        currentEntries = pages.get(currentPage);
        boolean startEmpty = currentEntries.isEmpty();
        if (startEmpty)
            currentEntries.add(new ClipboardEntry(false, Text.empty()));
        editingIndex = 0;
        editContext = new SelectionManager(
            this::getCurrentEntryText,
            this::setCurrentEntryText,
            this::getClipboard,
            this::setClipboard,
            this::validateTextForEntry
        );
        editingIndex = startEmpty ? 0 : -1;
        readonly = item.contains(AllDataComponents.CLIPBOARD_READ_ONLY);
        if (readonly)
            editingIndex = -1;
        if (clearBtn != null)
            init();
    }

    @Override
    protected void init() {
        setWindowSize(256, 256);
        super.init();
        clearDisplayCache();

        int x = guiLeft;
        int y = guiTop - 8;

        clearChildren();
        clearBtn = new IconButton(x + 234, y + 153, AllIcons.I_CLEAR_CHECKED).withCallback(() -> {
            editingIndex = -1;
            currentEntries.removeIf(ce -> ce.checked);
            if (currentEntries.isEmpty())
                currentEntries.add(new ClipboardEntry(false, Text.empty()));
            sendIfEditingBlock();
        });
        clearBtn.setToolTip(CreateLang.translateDirect("gui.clipboard.erase_checked"));
        closeBtn = new IconButton(x + 234, y + 175, AllIcons.I_PRIORITY_VERY_LOW).withCallback(() -> client.setScreen(null));
        closeBtn.setToolTip(CreateLang.translateDirect("station.close"));
        addDrawableChild(closeBtn);
        addDrawableChild(clearBtn);

        forward = new PageTurnWidget(x + 176, y + 229, true, $ -> changePage(true), true);
        backward = new PageTurnWidget(x + 53, y + 229, false, $ -> changePage(false), true);
        addDrawableChild(forward);
        addDrawableChild(backward);

        forward.visible = currentPage < 50 && (!readonly || currentPage + 1 < pages.size());
        backward.visible = currentPage > 0;
    }

    private int getNumPages() {
        return pages.size();
    }

    public void tick() {
        super.tick();
        frameTick++;

        if (targetedBlock != null) {
            if (!client.player.getBlockPos().isWithinDistance(targetedBlock, 10)) {
                removed();
                return;
            }
            if (!client.world.getBlockState(targetedBlock).isOf(AllBlocks.CLIPBOARD)) {
                removed();
                return;
            }
        }

        Window window = client.getWindow();
        int mx = (int) (client.mouse.getX() * (double) window.getScaledWidth() / (double) window.getWidth());
        int my = (int) (client.mouse.getY() * (double) window.getScaledHeight() / (double) window.getHeight());

        mx -= guiLeft + 35;
        my -= guiTop + 41;

        hoveredCheck = false;
        hoveredEntry = -1;

        if (mx > 0 && mx < 183 && my > 0 && my < 190) {
            hoveredCheck = mx < 20;
            int totalHeight = 0;
            for (int i = 0; i < currentEntries.size(); i++) {
                ClipboardEntry clipboardEntry = currentEntries.get(i);
                String text = clipboardEntry.text.getString();
                totalHeight += Math.max(12, textRenderer.wrapLines(Text.literal(text), clipboardEntry.icon.isEmpty() ? 150 : 130).size() * 9 + 3);

                if (totalHeight > my) {
                    hoveredEntry = i;
                    return;
                }
            }
            hoveredEntry = currentEntries.size();
        }
    }

    private String getCurrentEntryText() {
        return currentEntries.get(editingIndex).text.getString();
    }

    private void setCurrentEntryText(String text) {
        currentEntries.get(editingIndex).text = Text.literal(text);
        sendIfEditingBlock();
    }

    private void setClipboard(String p_98148_) {
        if (client != null)
            SelectionManager.setClipboard(client, p_98148_);
    }

    private String getClipboard() {
        return client != null ? SelectionManager.getClipboard(client) : "";
    }

    private boolean validateTextForEntry(String newText) {
        int totalHeight = 0;
        for (int i = 0; i < currentEntries.size(); i++) {
            ClipboardEntry clipboardEntry = currentEntries.get(i);
            String text = i == editingIndex ? newText : clipboardEntry.text.getString();
            totalHeight += Math.max(12, textRenderer.wrapLines(Text.literal(text), 150).size() * 9 + 3);
        }
        return totalHeight < 185;
    }

    private int yOffsetOfEditingEntry() {
        int totalHeight = 0;
        for (int i = 0; i < currentEntries.size(); i++) {
            if (i == editingIndex)
                break;
            ClipboardEntry clipboardEntry = currentEntries.get(i);
            totalHeight += Math.max(12, textRenderer.wrapLines(clipboardEntry.text, 150).size() * 9 + 3);
        }
        return totalHeight;
    }

    private void changePage(boolean next) {
        int previously = currentPage;
        currentPage = MathHelper.clamp(currentPage + (next ? 1 : -1), 0, 50);
        if (currentPage == previously)
            return;
        editingIndex = -1;
        if (pages.size() <= currentPage) {
            if (readonly) {
                currentPage = previously;
                return;
            }
            pages.add(new ArrayList<>());
        }
        currentEntries = pages.get(currentPage);
        if (currentEntries.isEmpty()) {
            currentEntries.add(new ClipboardEntry(false, Text.empty()));
            if (!readonly) {
                editingIndex = 0;
                editContext.putCursorAtEnd();
                clearDisplayCacheAfterChange();
            }
        }

        forward.visible = currentPage < 50 && (!readonly || currentPage + 1 < pages.size());
        backward.visible = currentPage > 0;

        if (next)
            return;
        if (pages.get(currentPage + 1).stream().allMatch(ce -> ce.text.getString().isBlank()))
            pages.remove(currentPage + 1);
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop - 8;

        AllGuiTextures.CLIPBOARD.render(graphics, x, y);
        graphics.drawText(textRenderer, Text.translatable("book.pageIndicator", currentPage + 1, getNumPages()), x + 150, y + 9, 0x43ffffff, false);

        for (int i = 0; i < currentEntries.size(); i++) {
            ClipboardEntry clipboardEntry = currentEntries.get(i);
            boolean checked = clipboardEntry.checked;
            int iconOffset = clipboardEntry.icon.isEmpty() ? 0 : 16;

            MutableText text = clipboardEntry.text;
            String string = text.getString();
            boolean isAddress = string.startsWith("#") && !string.substring(1).isBlank();

            if (isAddress) {
                (checked ? AllGuiTextures.CLIPBOARD_ADDRESS_INACTIVE : AllGuiTextures.CLIPBOARD_ADDRESS).render(graphics, x + 44, y + 50);
                text = Text.literal(string.substring(1).stripLeading());
            } else {
                graphics.drawText(textRenderer, "\u25A1", x + 45, y + 51, checked ? 0x668D7F6B : 0xff8D7F6B, false);
                if (checked)
                    graphics.drawText(textRenderer, "\u2714", x + 45, y + 50, 0xff31B25D, false);
            }

            List<OrderedText> split = textRenderer.wrapLines(text, 150 - iconOffset);
            if (split.isEmpty()) {
                y += 12;
                continue;
            }

            if (!clipboardEntry.icon.isEmpty())
                graphics.drawItem(clipboardEntry.icon, x + 54, y + 50);

            for (OrderedText sequence : split) {
                if (i != editingIndex)
                    graphics.drawText(
                        textRenderer,
                        sequence,
                        x + 58 + iconOffset,
                        y + 50,
                        checked ? isAddress ? 0x668D7F6B : 0xff31B25D : 0xff311A00,
                        false
                    );
                y += 9;
            }
            y += 3;
        }

        if (editingIndex == -1)
            return;

        setFocused(null);
        DisplayCache cache = getDisplayCache();

        for (LineInfo line : cache.lines)
            graphics.drawText(textRenderer, line.asComponent, line.x, line.y, 0xff311A00, false);

        renderHighlight(graphics, cache.selection);
        renderCursor(graphics, cache.cursor, cache.cursorAtEnd);
    }

    @Override
    public void removed() {
        pages.forEach(list -> list.removeIf(ce -> ce.text.getString().isBlank()));
        pages.removeIf(List::isEmpty);

        for (int i = 0; i < pages.size(); i++)
            if (pages.get(i) == currentEntries) {
                item.set(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE, i);
                if (i == 0)
                    item.remove(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE);
            }

        send();

        super.removed();
    }

    private void sendIfEditingBlock() {
        ClientPlayNetworkHandler handler = client.player.networkHandler;
        if (handler.getPlayerList().size() > 1 && targetedBlock != null)
            send();
    }

    private void send() {
        ClipboardEntry.saveAll(pages, item);
        ClipboardOverrides.switchTo(ClipboardType.WRITTEN, item);

        if (pages.isEmpty()) {
            item.remove(AllDataComponents.CLIPBOARD_PAGES);
            item.remove(AllDataComponents.CLIPBOARD_PREVIOUSLY_OPENED_PAGE);
            item.remove(AllDataComponents.CLIPBOARD_READ_ONLY);
            item.remove(AllDataComponents.CLIPBOARD_TYPE);
            item.remove(AllDataComponents.CLIPBOARD_COPIED_VALUES);
        }

        client.player.networkHandler.sendPacket(new ClipboardEditPacket(targetSlot, item.getComponentChanges(), targetedBlock));
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pScrollX, double pScrollY) {
        changePage(pScrollY < 0);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int pKeyCode = input.key();
        if (pKeyCode == 266) {
            backward.onPress(input);
            return true;
        }
        if (pKeyCode == 267) {
            forward.onPress(input);
            return true;
        }
        if (editingIndex != -1 && pKeyCode != 256) {
            keyPressedWhileEditing(input);
            clearDisplayCache();
            return true;
        }
        if (super.keyPressed(input))
            return true;
        return true;
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (super.charTyped(input))
            return true;
        if (!input.isValidChar())
            return false;
        if (editingIndex == -1)
            return false;
        editContext.insert(input.asString());
        clearDisplayCache();
        return true;
    }

    private boolean keyPressedWhileEditing(KeyInput input) {
        if (input.isSelectAll()) {
            editContext.selectAll();
            return true;
        } else if (input.isCopy()) {
            editContext.copy();
            return true;
        } else if (input.isPaste()) {
            editContext.paste();
            return true;
        } else if (input.isCut()) {
            editContext.cut();
            return true;
        } else {
            switch (input.key()) {
                case 257:
                case 335:
                    if (input.hasShift()) {
                        editContext.insert("\n");
                        return true;
                    } else if (!input.hasCtrl()) {
                        if (currentEntries.size() <= editingIndex + 1 || !currentEntries.get(editingIndex + 1).text.getString().isEmpty())
                            currentEntries.add(editingIndex + 1, new ClipboardEntry(false, Text.empty()));
                        editingIndex += 1;
                        editContext.putCursorAtEnd();
                        if (validateTextForEntry(" "))
                            return true;
                        currentEntries.remove(editingIndex);
                        editingIndex -= 1;
                        editContext.putCursorAtEnd();
                        return true;
                    }
                    editingIndex = -1;
                    return true;
                case 259:
                    if (currentEntries.get(editingIndex).text.getString().isEmpty() && currentEntries.size() > 1) {
                        currentEntries.remove(editingIndex);
                        editingIndex = Math.max(0, editingIndex - 1);
                        editContext.putCursorAtEnd();
                        return true;
                    } else if (input.hasCtrl()) {
                        int prevPos = editContext.getSelectionStart();
                        editContext.moveCursorPastWord(-1);
                        if (prevPos != editContext.getSelectionStart())
                            editContext.delete(prevPos - editContext.getSelectionStart());
                        return true;
                    }
                    editContext.delete(-1);
                    return true;
                case 261:
                    if (input.hasCtrl()) {
                        int prevPos = editContext.getSelectionStart();
                        editContext.moveCursorPastWord(1);
                        if (prevPos != editContext.getSelectionStart())
                            editContext.delete(prevPos - editContext.getSelectionStart());
                        return true;
                    }
                    editContext.delete(1);
                    return true;
                case 262:
                    if (input.hasCtrl()) {
                        editContext.moveCursorPastWord(1, input.hasShift());
                        return true;
                    }
                    editContext.moveCursor(1, input.hasShift());
                    return true;
                case 263:
                    if (input.hasCtrl()) {
                        editContext.moveCursorPastWord(-1, input.hasShift());
                        return true;
                    }
                    editContext.moveCursor(-1, input.hasShift());
                    return true;
                case 264:
                    keyDown(input);
                    return true;
                case 265:
                    keyUp(input);
                    return true;
                case 268:
                    keyHome(input);
                    return true;
                case 269:
                    keyEnd(input);
                    return true;
                default:
                    return false;
            }
        }
    }

    private void keyUp(KeyInput input) {
        changeLine(input, -1);
    }

    private void keyDown(KeyInput input) {
        changeLine(input, 1);
    }

    private void changeLine(KeyInput input, int pYChange) {
        int i = editContext.getSelectionStart();
        int j = getDisplayCache().changeLine(i, pYChange);
        editContext.moveCursorTo(j, input.hasShift());
    }

    private void keyHome(KeyInput input) {
        int i = editContext.getSelectionStart();
        int j = getDisplayCache().findLineStart(i);
        editContext.moveCursorTo(j, input.hasShift());
    }

    private void keyEnd(KeyInput input) {
        DisplayCache cache = getDisplayCache();
        int i = editContext.getSelectionStart();
        int j = cache.findLineEnd(i);
        editContext.moveCursorTo(j, input.hasShift());
    }

    private void renderCursor(DrawContext graphics, Pos2i pCursorPos, boolean pIsEndOfText) {
        if (frameTick / 6 % 2 != 0)
            return;
        pCursorPos = convertLocalToScreen(pCursorPos);
        if (!pIsEndOfText) {
            graphics.fill(pCursorPos.x, pCursorPos.y - 1, pCursorPos.x + 1, pCursorPos.y + 9, -16777216);
        } else {
            graphics.drawText(textRenderer, "_", pCursorPos.x, pCursorPos.y, 0xFF000000, false);
        }
    }

    private void renderHighlight(DrawContext graphics, Rect2i[] pSelected) {
        for (Rect2i rect2i : pSelected) {
            int i = rect2i.getX();
            int j = rect2i.getY();
            int k = i + rect2i.getWidth();
            int l = j + rect2i.getHeight();
            graphics.drawSelection(i, j, k, l);
        }
    }

    private Pos2i convertScreenToLocal(Pos2i pScreenPos) {
        return new Pos2i(pScreenPos.x - (width - 192) / 2 - 36 + 10, pScreenPos.y - 32 - 24 - yOffsetOfEditingEntry() - guiTop + 14);
    }

    private Pos2i convertLocalToScreen(Pos2i pLocalScreenPos) {
        return new Pos2i(pLocalScreenPos.x + (width - 192) / 2 + 36 - 10, pLocalScreenPos.y + 32 + 24 + yOffsetOfEditingEntry() + guiTop - 14);
    }

    public boolean mouseClicked(Click click, boolean doubled) {
        if (super.mouseClicked(click, doubled))
            return true;
        if (click.button() != 0)
            return true;

        if (hoveredEntry != -1) {
            if (hoveredCheck) {
                editingIndex = -1;
                if (hoveredEntry < currentEntries.size()) {
                    currentEntries.get(hoveredEntry).checked ^= true;
                    if (currentEntries.get(hoveredEntry).checked == true)
                        client.getSoundManager().play(PositionedSoundInstance.master(
                            AllSoundEvents.CLIPBOARD_CHECKMARK.getMainEvent(),
                            0.95f + (float) Math.random() * 0.05f
                        ));
                    else
                        client.getSoundManager().play(PositionedSoundInstance.master(
                            AllSoundEvents.CLIPBOARD_ERASE.getMainEvent(),
                            0.90f + (float) Math.random() * 0.2f
                        ));
                }
                sendIfEditingBlock();
                return true;
            }

            if (hoveredEntry != editingIndex && !readonly) {
                editingIndex = hoveredEntry;
                if (hoveredEntry >= currentEntries.size()) {
                    currentEntries.add(new ClipboardEntry(false, Text.empty()));
                    if (!validateTextForEntry(" ")) {
                        currentEntries.remove(hoveredEntry);
                        editingIndex = -1;
                        return true;
                    }
                }
                clearDisplayCacheAfterChange();
            }
        }

        if (editingIndex == -1)
            return false;

        double pMouseX = click.x();
        double pMouseY = click.y();
        if (pMouseX < guiLeft + 50 || pMouseX > guiLeft + 220 || pMouseY < guiTop + 30 || pMouseY > guiTop + 230) {
            setFocused(null);
            clearDisplayCache();
            editingIndex = -1;
            return false;
        }

        long i = Util.getMeasuringTimeMs();
        DisplayCache cache = getDisplayCache();
        int j = cache.getIndexAtPosition(textRenderer, convertScreenToLocal(new Pos2i((int) pMouseX, (int) pMouseY)));
        if (j >= 0) {
            if (j == lastIndex && i - lastClickTime < 250L) {
                if (!editContext.isSelecting()) {
                    selectWord(j);
                } else {
                    editContext.selectAll();
                }
            } else {
                editContext.moveCursorTo(j, click.hasShift());
            }

            clearDisplayCache();
        }

        lastIndex = j;
        lastClickTime = i;
        return true;
    }

    private void selectWord(int pIndex) {
        String s = getCurrentEntryText();
        editContext.setSelection(TextHandler.moveCursorByWords(s, -1, pIndex, false), TextHandler.moveCursorByWords(s, 1, pIndex, false));
    }

    public boolean mouseDragged(Click click, double pDragX, double pDragY) {
        if (super.mouseDragged(click, pDragX, pDragY))
            return true;
        if (click.button() != 0)
            return true;
        if (editingIndex == -1)
            return false;

        DisplayCache cache = getDisplayCache();
        int i = cache.getIndexAtPosition(textRenderer, convertScreenToLocal(new Pos2i((int) click.x(), (int) click.y())));
        editContext.moveCursorTo(i, true);
        clearDisplayCache();
        return true;
    }

    private DisplayCache getDisplayCache() {
        if (displayCache == null)
            displayCache = rebuildDisplayCache();
        return displayCache;
    }

    private void clearDisplayCache() {
        displayCache = null;
    }

    private void clearDisplayCacheAfterChange() {
        editContext.putCursorAtEnd();
        clearDisplayCache();
    }

    private DisplayCache rebuildDisplayCache() {
        String current = getCurrentEntryText();
        boolean address = current.startsWith("#") && !current.substring(1).isBlank();
        int offset = 0;

        if (address) {
            String stripped = current.substring(1).stripLeading();
            offset = current.length() - stripped.length();
            current = stripped;
        }

        if (current.isEmpty())
            return DisplayCache.EMPTY;

        String s = current;
        int i = editContext.getSelectionStart();
        int j = editContext.getSelectionEnd();
        i = MathHelper.clamp(i - offset, 0, s.length());
        j = MathHelper.clamp(j - offset, 0, s.length());

        IntList intlist = new IntArrayList();
        List<LineInfo> list = Lists.newArrayList();
        MutableInt mutableint = new MutableInt();
        MutableBoolean mutableboolean = new MutableBoolean();
        TextHandler stringsplitter = textRenderer.getTextHandler();
        stringsplitter.wrapLines(
            s, 150, Style.EMPTY, true, (p_98132_, p_98133_, p_98134_) -> {
                int k3 = mutableint.getAndIncrement();
                String s2 = s.substring(p_98133_, p_98134_);
                mutableboolean.setValue(s2.endsWith("\n"));
                String s3 = StringUtils.stripEnd(s2, " \n");
                int l3 = k3 * 9;
                Pos2i pos1 = convertLocalToScreen(new Pos2i(0, l3));
                intlist.add(p_98133_);
                list.add(new LineInfo(p_98132_, s3, pos1.x, pos1.y));
            }
        );

        int[] aint = intlist.toIntArray();
        boolean flag = i == s.length();
        Pos2i pos;
        if (flag && mutableboolean.isTrue()) {
            pos = new Pos2i(0, list.size() * 9);
        } else {
            int k = findLineFromPos(aint, i);
            int l = textRenderer.getWidth(s.substring(aint[k], i));
            pos = new Pos2i(l, k * 9);
        }

        List<Rect2i> list1 = Lists.newArrayList();
        if (i != j) {
            int l2 = Math.min(i, j);
            int i1 = Math.max(i, j);
            int j1 = findLineFromPos(aint, l2);
            int k1 = findLineFromPos(aint, i1);
            if (j1 == k1) {
                int l1 = j1 * 9;
                int i2 = aint[j1];
                list1.add(createPartialLineSelection(s, stringsplitter, l2, i1, l1, i2));
            } else {
                int i3 = j1 + 1 > aint.length ? s.length() : aint[j1 + 1];
                list1.add(createPartialLineSelection(s, stringsplitter, l2, i3, j1 * 9, aint[j1]));

                for (int j3 = j1 + 1; j3 < k1; ++j3) {
                    int j2 = j3 * 9;
                    String s1 = s.substring(aint[j3], aint[j3 + 1]);
                    int k2 = (int) stringsplitter.getWidth(s1);
                    list1.add(createSelection(new Pos2i(0, j2), new Pos2i(k2, j2 + 9)));
                }

                list1.add(createPartialLineSelection(s, stringsplitter, aint[k1], i1, k1 * 9, aint[k1]));
            }
        }

        return new DisplayCache(s, pos, flag, aint, list.toArray(new LineInfo[0]), list1.toArray(new Rect2i[0]));
    }

    static int findLineFromPos(int[] pLineStarts, int pFind) {
        int i = Arrays.binarySearch(pLineStarts, pFind);
        return i < 0 ? -(i + 2) : i;
    }

    private Rect2i createPartialLineSelection(String pInput, TextHandler pSplitter, int p_98122_, int p_98123_, int p_98124_, int p_98125_) {
        String s = pInput.substring(p_98125_, p_98122_);
        String s1 = pInput.substring(p_98125_, p_98123_);
        Pos2i firstPos = new Pos2i((int) pSplitter.getWidth(s), p_98124_);
        Pos2i secondPos = new Pos2i((int) pSplitter.getWidth(s1), p_98124_ + 9);
        return createSelection(firstPos, secondPos);
    }

    private Rect2i createSelection(Pos2i pCorner1, Pos2i pCorner2) {
        Pos2i firstPos = convertLocalToScreen(pCorner1);
        Pos2i secondPos = convertLocalToScreen(pCorner2);
        int i = Math.min(firstPos.x, secondPos.x);
        int j = Math.max(firstPos.x, secondPos.x);
        int k = Math.min(firstPos.y, secondPos.y);
        int l = Math.max(firstPos.y, secondPos.y);
        return new Rect2i(i, k, j - i, l - k);
    }

    static class DisplayCache {
        static final DisplayCache EMPTY = new DisplayCache(
            "",
            new Pos2i(0, 0),
            true,
            new int[]{0},
            new LineInfo[]{new LineInfo(Style.EMPTY, "", 0, 0)},
            new Rect2i[0]
        );
        private final String fullText;
        final Pos2i cursor;
        final boolean cursorAtEnd;
        private final int[] lineStarts;
        final LineInfo[] lines;
        final Rect2i[] selection;

        public DisplayCache(String pFullText, Pos2i pCursor, boolean pCursorAtEnd, int[] pLineStarts, LineInfo[] pLines, Rect2i[] pSelection) {
            fullText = pFullText;
            cursor = pCursor;
            cursorAtEnd = pCursorAtEnd;
            lineStarts = pLineStarts;
            lines = pLines;
            selection = pSelection;
        }

        public int getIndexAtPosition(TextRenderer pFont, Pos2i pCursorPosition) {
            int i = pCursorPosition.y / 9;
            if (i < 0)
                return 0;
            if (i >= lines.length)
                return fullText.length();
            LineInfo line = lines[i];
            return lineStarts[i] + pFont.getTextHandler().getTrimmedLength(line.contents, pCursorPosition.x, line.style);
        }

        public int changeLine(int pXChange, int pYChange) {
            int i = findLineFromPos(lineStarts, pXChange);
            int j = i + pYChange;
            int k;
            if (0 <= j && j < lineStarts.length) {
                int l = pXChange - lineStarts[i];
                int i1 = lines[j].contents.length();
                k = lineStarts[j] + Math.min(l, i1);
            } else {
                k = pXChange;
            }

            return k;
        }

        public int findLineStart(int pLine) {
            int i = findLineFromPos(lineStarts, pLine);
            return lineStarts[i];
        }

        public int findLineEnd(int pLine) {
            int i = findLineFromPos(lineStarts, pLine);
            return lineStarts[i] + lines[i].contents.length();
        }
    }

    static class LineInfo {
        final Style style;
        final String contents;
        final Text asComponent;
        final int x;
        final int y;

        public LineInfo(Style pStyle, String pContents, int pX, int pY) {
            style = pStyle;
            contents = pContents;
            x = pX;
            y = pY;
            asComponent = Text.literal(pContents).setStyle(pStyle);
        }
    }

    static class Pos2i {
        public final int x;
        public final int y;

        Pos2i(int pX, int pY) {
            x = pX;
            y = pY;
        }
    }

}
