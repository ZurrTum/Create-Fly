package com.zurrtum.create.client.content.logistics.stockTicker;

import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.content.logistics.AddressEditBox;
import com.zurrtum.create.client.content.trains.station.NoShadowFontWrapper;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.menu.AbstractSimiContainerScreen;
import com.zurrtum.create.client.foundation.gui.render.BlazeBurnerRenderState;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.contraptions.actors.seat.SeatEntity;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.stockTicker.PackageOrder;
import com.zurrtum.create.content.logistics.stockTicker.StockKeeperRequestMenu;
import com.zurrtum.create.content.logistics.stockTicker.StockTickerBlockEntity;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock.HeatLevel;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.component.ClipboardEntry;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts.CraftingEntry;
import com.zurrtum.create.infrastructure.packet.c2s.LogisticalStockRequestPacket;
import com.zurrtum.create.infrastructure.packet.c2s.PackageOrderRequestPacket;
import com.zurrtum.create.infrastructure.packet.c2s.StockKeeperCategoryHidingPacket;
import com.zurrtum.create.infrastructure.packet.c2s.StockKeeperLockPacket;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.Rect2i;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item.TooltipContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.function.Function;

public class StockKeeperRequestScreen extends AbstractSimiContainerScreen<StockKeeperRequestMenu> {
    public static class CategoryEntry {
        boolean hidden;
        String name;
        int y;
        int targetBECategory;

        public CategoryEntry(int targetBECategory, String name, int y) {
            this.targetBECategory = targetBECategory;
            this.name = name;
            hidden = false;
            this.y = y;
        }
    }

    private static final AllGuiTextures NUMBERS = AllGuiTextures.NUMBERS;
    private static final AllGuiTextures HEADER = AllGuiTextures.STOCK_KEEPER_REQUEST_HEADER;
    private static final AllGuiTextures BODY = AllGuiTextures.STOCK_KEEPER_REQUEST_BODY;
    private static final AllGuiTextures FOOTER = AllGuiTextures.STOCK_KEEPER_REQUEST_FOOTER;

    StockTickerBlockEntity blockEntity;
    public LerpedFloat itemScroll = LerpedFloat.linear().startWithValue(0);

    final int rows = 9;
    final int cols = 9;
    final int rowHeight = 20;
    final int colWidth = 20;
    final Couple<Integer> noneHovered = Couple.create(-1, -1);
    int itemsX;
    int itemsY;
    int orderY;
    int lockX;
    int lockY;
    int windowWidth;
    int windowHeight;

    public TextFieldWidget searchBox;
    public AddressEditBox addressBox;

    int emptyTicks = 0;
    int successTicks = 0;

    public List<List<BigItemStack>> currentItemSource;
    public List<List<BigItemStack>> displayedItems = new ArrayList<>();
    public List<CategoryEntry> categories = new ArrayList<>();

    public List<BigItemStack> itemsToOrder = new ArrayList<>();
    public List<CraftableBigItemStack> recipesToOrder = new ArrayList<>();

    WeakReference<LivingEntity> stockKeeper = new WeakReference<>(null);
    WeakReference<BlazeBurnerBlockEntity> blaze = new WeakReference<>(null);

    boolean encodeRequester; // Redstone requesters
    ItemStack itemToProgram;
    List<List<ClipboardEntry>> clipboardItem;

    private boolean isAdmin;
    private boolean isLocked;
    private boolean scrollHandleActive;
    private boolean ignoreTextInput;

    public boolean refreshSearchNextTick = false;
    public boolean moveToTopNextTick = false;
    private List<Rect2i> extraAreas = Collections.emptyList();

    private final Set<Integer> hiddenCategories;
    private InventorySummary forcedEntries = new InventorySummary();
    private boolean canRequestCraftingPackage = false;

    public StockKeeperRequestScreen(StockKeeperRequestMenu container, PlayerInventory inv, Text title) {
        super(container, inv, title);
        blockEntity = container.contentHolder;
        blockEntity.lastClientsideStockSnapshot = null;
        blockEntity.ticksSinceLastUpdate = 15;
        handler.screenReference = this;
        hiddenCategories = new HashSet<>(blockEntity.hiddenCategoriesByPlayer.getOrDefault(handler.player.getUuid(), List.of()));

        itemToProgram = handler.player.getMainHandStack();
        encodeRequester = itemToProgram.isIn(AllItemTags.TABLE_CLOTHS) || itemToProgram.isOf(AllItems.REDSTONE_REQUESTER);

        if (itemToProgram.isOf(AllItems.CLIPBOARD)) {
            clipboardItem = ClipboardEntry.readAll(itemToProgram);
            boolean anyItems = false;
            for (List<ClipboardEntry> list : clipboardItem) {
                for (ClipboardEntry entry : list) {
                    if (!entry.icon.isEmpty()) {
                        anyItems = true;
                        break;
                    }
                }
            }
            if (!anyItems)
                clipboardItem = null;
        }

        // Find the keeper for rendering
        for (int yOffset : Iterate.zeroAndOne) {
            for (Direction side : Iterate.horizontalDirections) {
                BlockPos seatPos = blockEntity.getPos().down(yOffset).offset(side);
                for (SeatEntity seatEntity : blockEntity.getWorld().getNonSpectatingEntities(SeatEntity.class, new Box(seatPos))) {
                    if (!seatEntity.getPassengerList().isEmpty() && seatEntity.getPassengerList().getFirst() instanceof LivingEntity keeper) {
                        stockKeeper = new WeakReference<>(keeper);
                    }
                }
                if (yOffset == 0 && blockEntity.getWorld().getBlockEntity(seatPos) instanceof BlazeBurnerBlockEntity bbbe) {
                    blaze = new WeakReference<>(bbbe);
                    return;
                }
            }
        }
    }

    public static StockKeeperRequestScreen create(
        MinecraftClient mc,
        MenuType<StockTickerBlockEntity> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        StockKeeperRequestScreen screen = type.create(StockKeeperRequestScreen::new, syncId, inventory, title, getBlockEntity(mc, extraData));
        if (screen == null) {
            return null;
        }
        screen.isAdmin = extraData.readBoolean();
        screen.isLocked = extraData.readBoolean();
        return screen;
    }

    @Override
    protected void init() {
        int appropriateHeight = MinecraftClient.getInstance().getWindow().getScaledHeight() - 10;
        appropriateHeight -= MathHelper.floorMod(appropriateHeight - HEADER.getHeight() - FOOTER.getHeight(), BODY.getHeight());
        appropriateHeight = Math.min(appropriateHeight, HEADER.getHeight() + FOOTER.getHeight() + BODY.getHeight() * 17);

        setWindowSize(windowWidth = 226, windowHeight = appropriateHeight);
        super.init();
        clearChildren();

        itemsX = x + (windowWidth - cols * colWidth) / 2 + 1;
        itemsY = y + 33;
        orderY = y + windowHeight - 72;
        lockX = x + 186;
        lockY = y + 18;

        MutableText searchLabel = CreateLang.translateDirect("gui.stock_keeper.search_items");
        searchBox = new TextFieldWidget(new NoShadowFontWrapper(textRenderer), x + 71, y + 22, 100, 9, searchLabel);
        searchBox.setMaxLength(50);
        searchBox.setDrawsBackground(false);
        searchBox.setEditableColor(0xFF4A2D31);
        addSelectableChild(searchBox);

        boolean initial = addressBox == null;
        String previouslyUsedAddress = initial ? blockEntity.previouslyUsedAddress : addressBox.getText();
        addressBox = new AddressEditBox(this, new NoShadowFontWrapper(textRenderer), x + 27, y + windowHeight - 36, 92, 10, true);
        addressBox.setEditableColor(0xFF714A40);
        addressBox.setText(previouslyUsedAddress);
        addDrawableChild(addressBox);

        extraAreas = new ArrayList<>();
        int leftHeight = 40;
        int rightHeight = 50;

        LivingEntity keeper = stockKeeper.get();
        if (keeper != null && keeper.isAlive())
            leftHeight = (int) (Math.max(0, keeper.getBoundingBox().getLengthY()) * 50);

        extraAreas.add(new Rect2i(0, y + windowHeight - 15 - leftHeight, x, height));
        if (encodeRequester)
            extraAreas.add(new Rect2i(x + windowWidth, y + windowHeight - 15 - rightHeight, rightHeight + 10, rightHeight));

        if (initial) {
            playUiSound(SoundEvents.BLOCK_WOOD_HIT, 0.5f, 1.5f);
            playUiSound(SoundEvents.ITEM_BOOK_PAGE_TURN, 1, 1);
            //TODO
            //            syncJEI();
        }
    }

    private void refreshSearchResults(boolean scrollBackUp) {
        displayedItems = Collections.emptyList();
        if (scrollBackUp)
            itemScroll.startWithValue(0);

        if (currentItemSource == null) {
            clampScrollBar();
            return;
        }

        if (isSchematicListMode()) {
            clampScrollBar();
            requestSchematicList();
            return;
        }

        categories = new ArrayList<>();
        for (int i = 0; i < blockEntity.categories.size(); i++) {
            ItemStack stack = blockEntity.categories.get(i);
            CategoryEntry entry = new CategoryEntry(i, stack.isEmpty() ? "" : stack.getName().getString(), 0);
            entry.hidden = hiddenCategories.contains(i);
            categories.add(entry);
        }

        CategoryEntry unsorted = new CategoryEntry(-1, CreateLang.translate("gui.stock_keeper.unsorted_category").string(), 0);
        unsorted.hidden = hiddenCategories.contains(-1);
        categories.add(unsorted);

        String valueWithPrefix = searchBox.getText();
        boolean anyItemsInCategory = false;

        // Nothing is being filtered out
        if (valueWithPrefix.isBlank()) {
            displayedItems = new ArrayList<>(currentItemSource);

            int categoryY = 0;
            for (int categoryIndex = 0; categoryIndex < currentItemSource.size(); categoryIndex++) {
                categories.get(categoryIndex).y = categoryY;
                List<BigItemStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
                if (displayedItemsInCategory.isEmpty())
                    continue;
                if (categoryIndex < currentItemSource.size() - 1)
                    anyItemsInCategory = true;

                categoryY += rowHeight;
                if (!categories.get(categoryIndex).hidden)
                    categoryY += Math.ceil(displayedItemsInCategory.size() / (float) cols) * rowHeight;
            }

            if (!anyItemsInCategory)
                categories.clear();

            clampScrollBar();
            updateCraftableAmounts();
            return;
        }

        // Filter by search string
        boolean modSearch = false;
        boolean tagSearch = false;
        if ((modSearch = valueWithPrefix.startsWith("@")) || (tagSearch = valueWithPrefix.startsWith("#")))
            valueWithPrefix = valueWithPrefix.substring(1);
        final String value = valueWithPrefix.toLowerCase(Locale.ROOT);

        displayedItems = new ArrayList<>();
        currentItemSource.forEach($ -> displayedItems.add(new ArrayList<>()));

        int categoryY = 0;
        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
            List<BigItemStack> category = currentItemSource.get(categoryIndex);
            categories.get(categoryIndex).y = categoryY;

            if (displayedItems.size() <= categoryIndex)
                break;

            List<BigItemStack> displayedItemsInCategory = displayedItems.get(categoryIndex);
            for (BigItemStack entry : category) {
                ItemStack stack = entry.stack;

                if (modSearch) {
                    if (Registries.ITEM.getId(stack.getItem()).getNamespace().contains(value)) {
                        displayedItemsInCategory.add(entry);
                    }
                    continue;
                }

                if (tagSearch) {
                    if (stack.streamTags().anyMatch(key -> key.id().toString().contains(value)))
                        displayedItemsInCategory.add(entry);
                    continue;
                }

                if (stack.getName().getString().toLowerCase(Locale.ROOT).contains(value) || Registries.ITEM.getId(stack.getItem()).getPath()
                    .contains(value)) {
                    displayedItemsInCategory.add(entry);
                    continue;
                }
            }

            if (displayedItemsInCategory.isEmpty())
                continue;
            if (categoryIndex < currentItemSource.size() - 1)
                anyItemsInCategory = true;

            categoryY += rowHeight;

            if (!categories.get(categoryIndex).hidden)
                categoryY += Math.ceil(displayedItemsInCategory.size() / (float) cols) * rowHeight;
        }

        if (!anyItemsInCategory)
            categories.clear();

        clampScrollBar();
        updateCraftableAmounts();
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        addressBox.tick();

        if (!forcedEntries.isEmpty()) {
            InventorySummary summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
            for (BigItemStack stack : forcedEntries.getStacks()) {
                int limitedAmount = -stack.count - 1;
                int actualAmount = summary.getCountOf(stack.stack);
                if (actualAmount <= limitedAmount)
                    forcedEntries.erase(stack.stack);
            }
        }

        boolean allEmpty = true;
        for (List<BigItemStack> list : displayedItems)
            allEmpty &= list.isEmpty();
        if (allEmpty)
            emptyTicks++;
        else
            emptyTicks = 0;

        if (successTicks > 0 && itemsToOrder.isEmpty())
            successTicks++;
        else
            successTicks = 0;

        List<List<BigItemStack>> clientStockSnapshot = blockEntity.getClientStockSnapshot();
        if (clientStockSnapshot != currentItemSource) {
            currentItemSource = clientStockSnapshot;
            refreshSearchResults(false);
            revalidateOrders();
        }

        if (refreshSearchNextTick) {
            refreshSearchNextTick = false;
            refreshSearchResults(moveToTopNextTick);
        }

        itemScroll.tickChaser();

        if (Math.abs(itemScroll.getValue() - itemScroll.getChaseTarget()) < 1 / 16f)
            itemScroll.setValue(itemScroll.getChaseTarget());

        if (blockEntity.ticksSinceLastUpdate > 15) {
            blockEntity.resetTicksSinceLastUpdate();
            client.player.networkHandler.sendPacket(new LogisticalStockRequestPacket(blockEntity.getPos()));
        }

        LivingEntity keeper = stockKeeper.get();
        BlazeBurnerBlockEntity blazeKeeper = blaze.get();
        if ((keeper == null || !keeper.isAlive()) && (blazeKeeper == null || blazeKeeper.isRemoved()))
            client.player.closeHandledScreen();
    }

    @Override
    protected void drawBackground(DrawContext graphics, float partialTicks, int mouseX, int mouseY) {
        if (this != client.currentScreen)
            return; // stencil buffer does not cooperate with ponders gui fade out

        partialTicks = AnimationTickHolder.getPartialTicksUI(client.getRenderTickCounter());
        Matrix3x2fStack ms = graphics.getMatrices();
        float currentScroll = itemScroll.getValue(partialTicks);
        Couple<Integer> hoveredSlot = getHoveredSlot(mouseX, mouseY);

        int x = this.x;
        int y = this.y;

        // BG
        HEADER.render(graphics, x - 15, y);
        y += HEADER.getHeight();
        for (int i = 0; i < (windowHeight - HEADER.getHeight() - FOOTER.getHeight()) / BODY.getHeight(); i++) {
            BODY.render(graphics, x - 15, y);
            y += BODY.getHeight();
        }
        FOOTER.render(graphics, x - 15, y);
        y = this.y;

        // Render text input hints
        if (addressBox.getText().isBlank() && !addressBox.isFocused()) {
            graphics.drawText(
                textRenderer,
                CreateLang.translate("gui.stock_keeper.package_adress").style(Formatting.ITALIC).component(),
                addressBox.getX(),
                addressBox.getY(),
                0xff_CDBCA8,
                false
            );
        }

        // Render keeper
        int entitySizeOffset = 0;
        LivingEntity keeper = stockKeeper.get();
        if (keeper != null && keeper.isAlive()) {
            ms.pushMatrix();
            entitySizeOffset = (int) (Math.max(0, keeper.getBoundingBox().getLengthX() - 1) * 50);
            int entitySizeOffsetY = (int) (Math.max(0, keeper.getBoundingBox().getLengthY() - 1) * 25);
            int entityX = x - 35 - entitySizeOffset;
            int entityY = y + windowHeight - 47 - entitySizeOffsetY;
            InventoryScreen.drawEntity(
                graphics,
                entityX - 100,
                entityY - 100,
                entityX + 100,
                entityY + 100,
                50,
                0,
                mouseX,
                MathHelper.clamp(mouseY, entityY - 50, entityY + 10),
                keeper
            );
            ms.popMatrix();
        }

        BlazeBurnerBlockEntity keeperBE = blaze.get();
        if (keeperBE != null && !keeperBE.isRemoved()) {
            int entityX = x - 69;
            int entityY = y + windowHeight - 85;
            World world = client.world;
            BlockState block = keeperBE.getCachedState();
            HeatLevel heatLevel = keeperBE.getHeatLevelForRender();
            float animation = keeperBE.headAnimation.getValue(partialTicks) * .175f;
            boolean drawGoggles = keeperBE.goggles;
            int hashCode = keeperBE.hashCode();
            graphics.state.addSpecialElement(new BlazeBurnerRenderState(
                new Matrix3x2f(ms),
                entityX,
                entityY,
                world,
                block,
                heatLevel,
                animation,
                drawGoggles,
                hashCode
            ));
        }

        // Render static item icons
        if (encodeRequester) {
            ms.pushMatrix();
            ms.scale(3.5f, 3.5f);
            graphics.drawItem(itemToProgram, x + windowWidth + 5, y + windowHeight - 70);
            ms.popMatrix();
        }

        // Render ordered items
        for (int index = 0; index < cols; index++) {
            if (itemsToOrder.size() <= index)
                break;

            BigItemStack entry = itemsToOrder.get(index);
            boolean isStackHovered = index == hoveredSlot.getSecond() && hoveredSlot.getFirst() == -1;

            ms.pushMatrix();
            ms.translate(itemsX + index * colWidth, orderY);
            renderItemEntry(graphics, entry, isStackHovered, true);
            ms.popMatrix();
        }

        if (itemsToOrder.size() > 9) {
            graphics.drawText(
                textRenderer,
                Text.literal("[+" + (itemsToOrder.size() - 9) + "]"),
                x + windowWidth - 40,
                orderY + 21,
                0xFFF8F8EC,
                true
            );
        }

        boolean justSent = itemsToOrder.isEmpty() && successTicks > 0;
        if (isConfirmHovered(mouseX, mouseY) && !justSent)
            AllGuiTextures.STOCK_KEEPER_REQUEST_SEND_HOVER.render(graphics, x + windowWidth - 81, y + windowHeight - 41);

        MutableText headerTitle = CreateLang.translate("gui.stock_keeper.title").component();
        graphics.drawText(textRenderer, headerTitle, x + windowWidth / 2 - textRenderer.getWidth(headerTitle) / 2, y + 4, 0xFF714A40, false);
        MutableText component = CreateLang.translate(encodeRequester ? "gui.stock_keeper.configure" : "gui.stock_keeper.send").component();

        if (justSent) {
            float alpha = MathHelper.clamp((successTicks + partialTicks - 5f) / 5f, 0f, 1f);
            ms.pushMatrix();
            ms.translate(alpha * alpha * 50, 0);
            if (successTicks < 10)
                graphics.drawText(
                    textRenderer,
                    component,
                    x + windowWidth - 42 - textRenderer.getWidth(component) / 2,
                    y + windowHeight - 35,
                    new Color(0x252525).setAlpha(1 - alpha * alpha).getRGB(),
                    false
                );
            ms.popMatrix();

        } else {
            graphics.drawText(
                textRenderer,
                component,
                x + windowWidth - 42 - textRenderer.getWidth(component) / 2,
                y + windowHeight - 35,
                0xFF252525,
                false
            );
        }

        // Request just sent
        if (justSent) {
            Text msg = CreateLang.translateDirect("gui.stock_keeper.request_sent");
            float alpha = MathHelper.clamp((successTicks + partialTicks - 10f) / 5f, 0f, 1f);
            int msgX = x + windowWidth / 2 - (textRenderer.getWidth(msg) + 10) / 2;
            int msgY = orderY + 5;
            if (alpha > 0) {
                int c3 = new Color(0x8C5D4B).setAlpha(alpha).getRGB();
                int w = textRenderer.getWidth(msg) + 14;
                AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_L.render(graphics, msgX - 8, msgY - 4);
                UIRenderHelper.drawStretched(graphics, msgX, msgY - 4, w, 16, AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_M);
                AllGuiTextures.STOCK_KEEPER_REQUEST_BANNER_R.render(graphics, msgX + textRenderer.getWidth(msg) + 10, msgY - 4);
                graphics.drawText(textRenderer, msg, msgX + 5, msgY, c3, false);
            }
        }

        int itemWindowX = x + 21;
        int itemWindowX2 = itemWindowX + 184;
        int itemWindowY = y + 17;
        int itemWindowY2 = y + windowHeight - 80;

        graphics.enableScissor(itemWindowX - 5, itemWindowY, itemWindowX2 + 10, itemWindowY2);

        ms.pushMatrix();
        ms.translate(0, -currentScroll * rowHeight);

        // BG
        for (int sliceY = -2; sliceY < getMaxScroll() * rowHeight + windowHeight - 72; sliceY += AllGuiTextures.STOCK_KEEPER_REQUEST_BG.getHeight()) {
            if (sliceY - currentScroll * rowHeight < -20)
                continue;
            if (sliceY - currentScroll * rowHeight > windowHeight - 72)
                continue;
            AllGuiTextures.STOCK_KEEPER_REQUEST_BG.render(graphics, x + 22, y + sliceY + 18);
        }

        // Search bar
        AllGuiTextures.STOCK_KEEPER_REQUEST_SEARCH.render(graphics, x + 42, searchBox.getY() - 5);
        searchBox.render(graphics, mouseX, mouseY, partialTicks);
        if (searchBox.getText().isBlank() && !searchBox.isFocused())
            graphics.drawText(
                textRenderer,
                searchBox.getMessage(),
                x + windowWidth / 2 - textRenderer.getWidth(searchBox.getMessage()) / 2,
                searchBox.getY(),
                0xff4A2D31,
                false
            );

        // Something isnt right
        boolean allEmpty = true;
        for (List<BigItemStack> list : displayedItems)
            allEmpty &= list.isEmpty();
        if (allEmpty) {
            Text msg = getTroubleshootingMessage();
            float alpha = MathHelper.clamp((emptyTicks - 10f) / 5f, 0f, 1f);
            if (alpha > 0) {
                List<OrderedText> split = textRenderer.wrapLines(msg, 160);
                for (int i = 0; i < split.size(); i++) {
                    OrderedText sequence = split.get(i);
                    int lineWidth = textRenderer.getWidth(sequence);
                    graphics.drawText(
                        textRenderer,
                        sequence,
                        x + windowWidth / 2 - lineWidth / 2 + 1,
                        itemsY + 20 + 1 + i * (textRenderer.fontHeight + 1),
                        new Color(0x4A2D31).setAlpha(alpha).getRGB(),
                        false
                    );
                    graphics.drawText(
                        textRenderer,
                        sequence,
                        x + windowWidth / 2 - lineWidth / 2,
                        itemsY + 20 + i * (textRenderer.fontHeight + 1),
                        new Color(0xF8F8EC).setAlpha(alpha).getRGB(),
                        false
                    );
                }
            }
        }

        // Items
        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
            List<BigItemStack> category = displayedItems.get(categoryIndex);
            CategoryEntry categoryEntry = categories.isEmpty() ? null : categories.get(categoryIndex);
            int categoryY = categories.isEmpty() ? 0 : categoryEntry.y;
            if (category.isEmpty())
                continue;

            if (!categories.isEmpty()) {
                (categoryEntry.hidden ? AllGuiTextures.STOCK_KEEPER_CATEGORY_HIDDEN : AllGuiTextures.STOCK_KEEPER_CATEGORY_SHOWN).render(
                    graphics,
                    itemsX,
                    itemsY + categoryY + 6
                );
                graphics.drawText(textRenderer, categoryEntry.name, itemsX + 10, itemsY + categoryY + 8, 0xFF4A2D31, false);
                graphics.drawText(textRenderer, categoryEntry.name, itemsX + 9, itemsY + categoryY + 7, 0xFFF8F8EC, false);
                if (categoryEntry.hidden)
                    continue;
            }

            for (int index = 0; index < category.size(); index++) {
                int pY = itemsY + categoryY + (categories.isEmpty() ? 4 : rowHeight) + (index / cols) * rowHeight;
                float cullY = pY - currentScroll * rowHeight;

                if (cullY < y)
                    continue;
                if (cullY > y + windowHeight - 72)
                    break;

                boolean isStackHovered = index == hoveredSlot.getSecond() && categoryIndex == hoveredSlot.getFirst();
                BigItemStack entry = category.get(index);

                ms.pushMatrix();
                ms.translate(itemsX + (index % cols) * colWidth, pY);
                renderItemEntry(graphics, entry, isStackHovered, false);
                ms.popMatrix();
            }
        }

        // Render lock option
        if (isAdmin)
            (isLocked ? AllGuiTextures.STOCK_KEEPER_REQUEST_LOCKED : AllGuiTextures.STOCK_KEEPER_REQUEST_UNLOCKED).render(graphics, lockX, lockY);

        ms.popMatrix();
        graphics.disableScissor();

        // Scroll bar
        int windowH = windowHeight - 92;
        int totalH = getMaxScroll() * rowHeight + windowH;
        int barSize = Math.max(5, MathHelper.floor((float) windowH / totalH * (windowH - 2)));
        if (barSize < windowH - 2) {
            int barX = itemsX + cols * colWidth;
            int barY = y + 15;
            ms.pushMatrix();
            ms.translate(0, (currentScroll * rowHeight) / totalH * (windowH - 2));
            AllGuiTextures pad = AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_PAD;
            graphics.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                pad.location,
                barX,
                barY,
                pad.getStartX(),
                pad.getStartY(),
                pad.getWidth(),
                barSize,
                pad.getWidth(),
                pad.getHeight(),
                256,
                256
            );
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_TOP.render(graphics, barX, barY);
            if (barSize > 16)
                AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_MID.render(graphics, barX, barY + barSize / 2 - 4);
            AllGuiTextures.STOCK_KEEPER_REQUEST_SCROLL_BOT.render(graphics, barX, barY + barSize - 5);
            ms.popMatrix();
        }

        // Render JEI imported
        if (!recipesToOrder.isEmpty()) {
            int jeiX = x + (windowWidth - colWidth * recipesToOrder.size()) / 2 + 1;
            int jeiY = orderY - 31;
            ms.pushMatrix();
            ms.translate(jeiX, jeiY);
            int xoffset = -3;
            AllGuiTextures.STOCK_KEEPER_REQUEST_BLUEPRINT_LEFT.render(graphics, xoffset, -3);
            xoffset += 10;
            for (int i = 0; i <= (recipesToOrder.size() - 1) * 5; i++) {
                AllGuiTextures.STOCK_KEEPER_REQUEST_BLUEPRINT_MIDDLE.render(graphics, xoffset, -3);
                xoffset += 4;
            }
            AllGuiTextures.STOCK_KEEPER_REQUEST_BLUEPRINT_RIGHT.render(graphics, xoffset, -3);

            for (int index = 0; index < recipesToOrder.size(); index++) {
                CraftableBigItemStack craftableBigItemStack = recipesToOrder.get(index);
                boolean isStackHovered = index == hoveredSlot.getSecond() && -2 == hoveredSlot.getFirst();
                ms.pushMatrix();
                ms.translate(index * colWidth, 0);
                renderItemEntry(graphics, craftableBigItemStack, isStackHovered, true);
                ms.popMatrix();
            }

            ms.popMatrix();
        }
    }

    @Override
    protected void renderForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
        float currentScroll = itemScroll.getValue(partialTicks);
        Couple<Integer> hoveredSlot = getHoveredSlot(mouseX, mouseY);

        // Render tooltip of hovered item
        if (hoveredSlot != noneHovered) {
            int slot = hoveredSlot.getSecond();
            boolean recipeHovered = hoveredSlot.getFirst() == -2;
            boolean orderHovered = hoveredSlot.getFirst() == -1;
            BigItemStack entry = recipeHovered ? recipesToOrder.get(slot) : orderHovered ? itemsToOrder.get(slot) : displayedItems.get(hoveredSlot.getFirst())
                .get(slot);

            if (recipeHovered) {
                ArrayList<Text> lines = new ArrayList<>(entry.stack.getTooltip(
                    TooltipContext.create(client.world),
                    client.player,
                    TooltipType.BASIC
                ));
                if (!lines.isEmpty()) {
                    lines.set(0, CreateLang.translateDirect("gui.stock_keeper.craft", lines.getFirst().copy()));
                }
                graphics.drawTooltip(textRenderer, lines, mouseX, mouseY);
            } else {
                graphics.drawItemTooltip(textRenderer, entry.stack, mouseX, mouseY);
            }
        }

        // Render tooltip of lock option
        if (currentScroll < 1 && isAdmin && mouseX > lockX && mouseX <= lockX + 15 && mouseY > lockY && mouseY <= lockY + 15) {
            graphics.drawTooltip(
                textRenderer, List.of(
                    CreateLang.translate(isLocked ? "gui.stock_keeper.network_locked" : "gui.stock_keeper.network_open").component(),
                    CreateLang.translate("gui.stock_keeper.network_lock_tip").style(Formatting.GRAY).component(),
                    CreateLang.translate("gui.stock_keeper.network_lock_tip_1").style(Formatting.GRAY).component(),
                    CreateLang.translate("gui.stock_keeper.network_lock_tip_2").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                ), mouseX, mouseY
            );
        }

        // Render tooltip of address input
        if (addressBox.getText().isBlank() && !addressBox.isFocused() && addressBox.isHovered()) {
            graphics.drawTooltip(
                textRenderer, List.of(
                    CreateLang.translate("gui.factory_panel.restocker_address").color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.translate("gui.schedule.lmb_edit").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                ), mouseX, mouseY
            );
        }
    }

    private void renderItemEntry(DrawContext graphics, BigItemStack entry, boolean isStackHovered, boolean isRenderingOrders) {
        int customCount = entry.count;
        if (!isRenderingOrders) {
            BigItemStack order = getOrderForItem(entry.stack);
            if (entry.count < BigItemStack.INF) {
                int forcedCount = forcedEntries.getCountOf(entry.stack);
                if (forcedCount != 0)
                    customCount = Math.min(customCount, -forcedCount - 1);
                if (order != null)
                    customCount -= order.count;
                customCount = Math.max(0, customCount);
            }
            AllGuiTextures.STOCK_KEEPER_REQUEST_SLOT.render(graphics, 0, 0);
        }
        boolean craftable = entry instanceof CraftableBigItemStack;
        Matrix3x2fStack ms = graphics.getMatrices();
        ms.pushMatrix();

        float scaleFromHover = 1;
        if (isStackHovered)
            scaleFromHover += .075f;

        ms.translate((float) ((colWidth - 18) / 2.0), (float) ((rowHeight - 18) / 2.0));
        ms.translate((float) (18 / 2.0), (float) (18 / 2.0));
        ms.scale(scaleFromHover, scaleFromHover);
        ms.translate((float) (-18 / 2.0), (float) (-18 / 2.0));
        if (customCount != 0 || craftable)
            graphics.drawItem(entry.stack, 0, 0);
        ms.popMatrix();

        ms.pushMatrix();
        if (customCount != 0 || craftable)
            graphics.drawStackOverlay(textRenderer, entry.stack, 1, 1, "");
        if (customCount > 1 || craftable)
            drawItemCount(graphics, customCount);
        ms.popMatrix();
    }

    private void drawItemCount(DrawContext graphics, int customCount) {
        String text = customCount >= 1000000 ? (customCount / 1000000) + "m" : customCount >= 10000 ? (customCount / 1000) + "k" : customCount >= 1000 ? ((customCount * 10) / 1000) / 10f + "k" : customCount >= 100 ? customCount + "" : " " + customCount;

        if (customCount >= BigItemStack.INF)
            text = "+";

        if (text.isBlank())
            return;

        int x = (int) Math.floor(-text.length() * 2.5);
        for (char c : text.toCharArray()) {
            int index = c - '0';
            int xOffset = index * 6;
            int spriteWidth = NUMBERS.getWidth();

            switch (c) {
                case ' ':
                    x += 4;
                    continue;
                case '.':
                    spriteWidth = 3;
                    xOffset = 60;
                    break;
                case 'k':
                    xOffset = 64;
                    break;
                case 'm':
                    spriteWidth = 7;
                    xOffset = 70;
                    break;
                case '+':
                    spriteWidth = 9;
                    xOffset = 84;
                    break;
            }

            graphics.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                NUMBERS.location,
                14 + x,
                10,
                NUMBERS.getStartX() + xOffset,
                NUMBERS.getStartY(),
                spriteWidth,
                NUMBERS.getHeight(),
                256,
                256
            );
            x += spriteWidth - 1;
        }
    }

    @Nullable
    private BigItemStack getOrderForItem(ItemStack stack) {
        for (BigItemStack entry : itemsToOrder)
            if (ItemStack.areItemsAndComponentsEqual(stack, entry.stack))
                return entry;
        return null;
    }

    private void revalidateOrders() {
        Set<BigItemStack> invalid = new HashSet<>(itemsToOrder);
        InventorySummary summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
        if (currentItemSource == null || summary == null) {
            itemsToOrder.removeAll(invalid);
            return;
        }
        for (BigItemStack entry : itemsToOrder) {
            entry.count = Math.min(summary.getCountOf(entry.stack), entry.count);
            if (entry.count > 0)
                invalid.remove(entry);
        }

        itemsToOrder.removeAll(invalid);
    }

    private Couple<Integer> getHoveredSlot(int x, int y) {
        x += 1;
        if (x < itemsX || x >= itemsX + cols * colWidth || isSchematicListMode())
            return noneHovered;

        // Ordered item is hovered
        if (y >= orderY && y < orderY + rowHeight) {
            int col = (x - itemsX) / colWidth;
            if (itemsToOrder.size() <= col || col < 0)
                return noneHovered;
            return Couple.create(-1, col);
        }

        // Ordered recipe is hovered
        if (y >= orderY - 31 && y < orderY - 31 + rowHeight) {
            int jeiX = this.x + (windowWidth - colWidth * recipesToOrder.size()) / 2 + 1;
            int col = MathHelper.floorDiv(x - jeiX, colWidth);
            if (recipesToOrder.size() > col && col >= 0)
                return Couple.create(-2, col);
        }

        if (y < this.y + 16 || y > this.y + windowHeight - 80)
            return noneHovered;
        if (!itemScroll.settled())
            return noneHovered;

        int localY = y - itemsY;

        for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
            CategoryEntry entry = categories.isEmpty() ? new CategoryEntry(0, "", 0) : categories.get(categoryIndex);
            if (entry.hidden)
                continue;

            int row = MathHelper.floor((localY - (categories.isEmpty() ? 4 : rowHeight) - entry.y) / (float) rowHeight + itemScroll.getChaseTarget());

            int col = (x - itemsX) / colWidth;
            int slot = row * cols + col;

            if (slot < 0)
                return noneHovered;
            if (displayedItems.get(categoryIndex).size() <= slot)
                continue;

            return Couple.create(categoryIndex, slot);
        }

        return noneHovered;
    }

    @Nullable
    public ItemStack getHoveredItemStack(int mouseX, int mouseY) {
        Couple<Integer> hoveredSlot = getHoveredSlot(mouseX, mouseY);
        if (hoveredSlot == noneHovered) {
            return null;
        }
        int index = hoveredSlot.getSecond();
        boolean recipeHovered = hoveredSlot.getFirst() == -2;
        BigItemStack entry;
        if (recipeHovered) {
            entry = recipesToOrder.get(index);
        } else {
            if (hoveredSlot.getFirst() == -1) {
                entry = itemsToOrder.get(index);
            } else {
                entry = displayedItems.get(hoveredSlot.getFirst()).get(index);
            }
        }
        return entry.stack.copy();
    }

    private boolean isConfirmHovered(int mouseX, int mouseY) {
        int confirmX = x + 143;
        int confirmY = y + windowHeight - 39;
        int confirmW = 78;
        int confirmH = 18;

        if (mouseX < confirmX || mouseX >= confirmX + confirmW)
            return false;
        return mouseY >= confirmY && mouseY < confirmY + confirmH;
    }

    private Text getTroubleshootingMessage() {
        if (currentItemSource == null)
            return CreateLang.translate("gui.stock_keeper.checking_stocks").component();
        if (blockEntity.activeLinks == 0)
            return CreateLang.translate("gui.stock_keeper.no_packagers_linked").component();
        if (currentItemSource.isEmpty())
            return CreateLang.translate("gui.stock_keeper.inventories_empty").component();
        if (isSchematicListMode())
            return CreateLang.translate(itemsToOrder.isEmpty() ? "gui.stock_keeper.schematic_list.no_results" : "gui.stock_keeper.schematic_list.requesting")
                .component();
        return CreateLang.translate("gui.stock_keeper.no_search_results").component();
    }

    @Override
    public boolean mouseClicked(double pMouseX, double pMouseY, int pButton) {
        boolean lmb = pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT;
        boolean rmb = pButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT;

        // Search
        if (rmb && searchBox.isMouseOver(pMouseX, pMouseY)) {
            searchBox.setText("");
            refreshSearchNextTick = true;
            moveToTopNextTick = true;
            searchBox.setFocused(true);
            //TODO
            //            syncJEI();
            return true;
        }

        if (addressBox.isFocused()) {
            boolean result = addressBox.mouseClicked(pMouseX, pMouseY, pButton);
            if (addressBox.isHovered() || result)
                return result;
            addressBox.setFocused(false);
        }
        if (searchBox.isFocused()) {
            if (searchBox.isHovered())
                return searchBox.mouseClicked(pMouseX, pMouseY, pButton);
            searchBox.setFocused(false);
        }

        // Scroll bar
        int barX = itemsX + cols * colWidth - 1;
        if (getMaxScroll() > 0 && lmb && pMouseX > barX && pMouseX <= barX + 8 && pMouseY > y + 15 && pMouseY < y + windowHeight - 82) {
            scrollHandleActive = true;
            if (client.isWindowFocused())
                GLFW.glfwSetInputMode(client.getWindow().getHandle(), 208897, GLFW.GLFW_CURSOR_HIDDEN);
            return true;
        }

        Couple<Integer> hoveredSlot = getHoveredSlot((int) pMouseX, (int) pMouseY);

        // Lock
        if (isAdmin && itemScroll.getChaseTarget() == 0 && lmb && pMouseX > lockX && pMouseX <= lockX + 15 && pMouseY > lockY && pMouseY <= lockY + 15) {
            isLocked = !isLocked;
            client.player.networkHandler.sendPacket(new StockKeeperLockPacket(blockEntity.getPos(), isLocked));
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
            return true;
        }

        // Confirm
        if (lmb && isConfirmHovered((int) pMouseX, (int) pMouseY)) {
            sendIt();
            playUiSound(SoundEvents.UI_BUTTON_CLICK.value(), 1, 1);
            return true;
        }

        // Category hiding
        int localY = (int) (pMouseY - itemsY);
        if (itemScroll.settled() && lmb && !categories.isEmpty() && pMouseX >= itemsX && pMouseX < itemsX + cols * colWidth && pMouseY >= y + 16 && pMouseY <= y + windowHeight - 80) {
            for (int categoryIndex = 0; categoryIndex < displayedItems.size(); categoryIndex++) {
                CategoryEntry entry = categories.get(categoryIndex);
                if (MathHelper.floor((localY - entry.y) / (float) rowHeight + itemScroll.getChaseTarget()) != 0)
                    continue;
                if (displayedItems.get(categoryIndex).isEmpty())
                    continue;
                int indexOf = entry.targetBECategory;
                if (indexOf >= blockEntity.categories.size())
                    continue;

                if (!entry.hidden) {
                    hiddenCategories.add(indexOf);
                    playUiSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, 1f, 1.5f);
                } else {
                    hiddenCategories.remove(indexOf);
                    playUiSound(SoundEvents.ENTITY_ITEM_FRAME_ROTATE_ITEM, 1f, 0.675f);
                }

                refreshSearchNextTick = true;
                moveToTopNextTick = false;
                return true;
            }
        }

        if (hoveredSlot == noneHovered || !lmb && !rmb)
            return super.mouseClicked(pMouseX, pMouseY, pButton);

        // Items
        boolean orderClicked = hoveredSlot.getFirst() == -1;
        boolean recipeClicked = hoveredSlot.getFirst() == -2;
        BigItemStack entry = recipeClicked ? recipesToOrder.get(hoveredSlot.getSecond()) : orderClicked ? itemsToOrder.get(hoveredSlot.getSecond()) : displayedItems.get(
            hoveredSlot.getFirst()).get(hoveredSlot.getSecond());

        ItemStack itemStack = entry.stack;
        int transfer = hasShiftDown() ? itemStack.getMaxCount() : hasControlDown() ? 10 : 1;

        if (recipeClicked && entry instanceof CraftableBigItemStack cbis) {
            if (rmb && cbis.count == 0) {
                recipesToOrder.remove(cbis);
                return true;
            }
            requestCraftable(cbis, rmb ? -transfer : transfer);
            return true;
        }

        BigItemStack existingOrder = getOrderForItem(entry.stack);
        if (existingOrder == null) {
            if (itemsToOrder.size() >= cols || rmb)
                return true;
            itemsToOrder.add(existingOrder = new BigItemStack(itemStack.copyWithCount(1), 0));
            playUiSound(SoundEvents.BLOCK_WOOL_STEP, 0.75f, 1.2f);
            playUiSound(SoundEvents.BLOCK_BAMBOO_WOOD_STEP, 0.75f, 0.8f);
        }

        int current = existingOrder.count;

        if (rmb || orderClicked) {
            existingOrder.count = current - transfer;
            if (existingOrder.count <= 0) {
                itemsToOrder.remove(existingOrder);
                playUiSound(SoundEvents.BLOCK_WOOL_STEP, 0.75f, 1.8f);
                playUiSound(SoundEvents.BLOCK_BAMBOO_WOOD_STEP, 0.75f, 1.8f);
            }
            return true;
        }

        existingOrder.count = current + Math.min(transfer, entry.count - current);
        return true;
    }

    @Override
    public boolean mouseReleased(double pMouseX, double pMouseY, int pButton) {
        if (pButton == GLFW.GLFW_MOUSE_BUTTON_LEFT && scrollHandleActive) {
            scrollHandleActive = false;
            if (client.isWindowFocused())
                GLFW.glfwSetInputMode(client.getWindow().getHandle(), 208897, GLFW.GLFW_CURSOR_NORMAL);
        }
        return super.mouseReleased(pMouseX, pMouseY, pButton);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        Couple<Integer> hoveredSlot = getHoveredSlot((int) mouseX, (int) mouseY);
        boolean noHover = hoveredSlot == noneHovered;

        if (noHover || hoveredSlot.getFirst() >= 0 && !hasShiftDown() && getMaxScroll() != 0) {
            int maxScroll = getMaxScroll();
            int direction = (int) (Math.ceil(Math.abs(scrollY)) * -Math.signum(scrollY));
            float newTarget = MathHelper.clamp(Math.round(itemScroll.getChaseTarget() + direction), 0, maxScroll);
            itemScroll.chase(newTarget, 0.5, Chaser.EXP);
            return true;
        }

        boolean orderClicked = hoveredSlot.getFirst() == -1;
        boolean recipeClicked = hoveredSlot.getFirst() == -2;
        BigItemStack entry = recipeClicked ? recipesToOrder.get(hoveredSlot.getSecond()) : orderClicked ? itemsToOrder.get(hoveredSlot.getSecond()) : displayedItems.get(
            hoveredSlot.getFirst()).get(hoveredSlot.getSecond());

        boolean remove = scrollY < 0;
        int transfer = MathHelper.ceil(Math.abs(scrollY)) * (hasControlDown() ? 10 : 1);

        if (recipeClicked && entry instanceof CraftableBigItemStack cbis) {
            requestCraftable(cbis, remove ? -transfer : transfer);
            return true;
        }

        BigItemStack existingOrder = orderClicked ? entry : getOrderForItem(entry.stack);
        if (existingOrder == null) {
            if (itemsToOrder.size() >= cols || remove)
                return true;
            itemsToOrder.add(existingOrder = new BigItemStack(entry.stack.copyWithCount(1), 0));
            playUiSound(SoundEvents.BLOCK_WOOL_STEP, 0.75f, 1.2f);
            playUiSound(SoundEvents.BLOCK_BAMBOO_WOOD_STEP, 0.75f, 0.8f);
        }

        int current = existingOrder.count;

        if (remove) {
            existingOrder.count = current - transfer;
            if (existingOrder.count <= 0) {
                itemsToOrder.remove(existingOrder);
                playUiSound(SoundEvents.BLOCK_WOOL_STEP, 0.75f, 1.8f);
                playUiSound(SoundEvents.BLOCK_BAMBOO_WOOD_STEP, 0.75f, 1.8f);
            } else if (existingOrder.count != current)
                playUiSound(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 0.25f, 1.2f);
            return true;
        }

        existingOrder.count = current + Math.min(transfer, blockEntity.getLastClientsideStockSnapshotAsSummary().getCountOf(entry.stack) - current);

        if (existingOrder.count != current && current != 0)
            playUiSound(AllSoundEvents.SCROLL_VALUE.getMainEvent(), 0.25f, 1.2f);

        return true;
    }

    private void clampScrollBar() {
        int maxScroll = getMaxScroll();
        float prevTarget = itemScroll.getChaseTarget();
        float newTarget = MathHelper.clamp(prevTarget, 0, maxScroll);
        if (prevTarget != newTarget)
            itemScroll.startWithValue(newTarget);
    }

    private int getMaxScroll() {
        int visibleHeight = windowHeight - 84;
        int totalRows = 2;
        for (int i = 0; i < displayedItems.size(); i++) {
            List<BigItemStack> list = displayedItems.get(i);
            if (list.isEmpty())
                continue;
            totalRows++;
            if (categories.size() > i && categories.get(i).hidden)
                continue;
            totalRows += Math.ceil(list.size() / (float) cols);
        }
        return Math.max(0, (totalRows * rowHeight - visibleHeight + 50) / rowHeight);
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (pButton != GLFW.GLFW_MOUSE_BUTTON_LEFT || !scrollHandleActive)
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);

        Window window = client.getWindow();
        double scaleX = window.getScaledWidth() / (double) window.getWidth();
        double scaleY = window.getScaledHeight() / (double) window.getHeight();

        int windowH = windowHeight - 92;
        int totalH = getMaxScroll() * rowHeight + windowH;
        int barSize = Math.max(5, MathHelper.floor((float) windowH / totalH * (windowH - 2)));

        int minY = y + 15 + barSize / 2;
        int maxY = y + 15 + windowH - barSize / 2;

        if (barSize >= windowH - 2)
            return true;

        int barX = itemsX + cols * colWidth;
        double target = (pMouseY - y - 15 - barSize / 2.0) * totalH / (windowH - 2) / rowHeight;
        itemScroll.chase(MathHelper.clamp(target, 0, getMaxScroll()), 0.8, Chaser.EXP);

        if (client.isWindowFocused()) {
            double forceX = (barX + 2) / scaleX;
            double forceY = MathHelper.clamp(pMouseY, minY, maxY) / scaleY;
            GLFW.glfwSetCursorPos(window.getHandle(), forceX, forceY);
        }

        return true;
    }

    @Override
    public boolean charTyped(char pCodePoint, int pModifiers) {
        if (ignoreTextInput)
            return false;
        if (addressBox.isFocused() && addressBox.charTyped(pCodePoint, pModifiers))
            return true;
        String s = searchBox.getText();
        if (!searchBox.charTyped(pCodePoint, pModifiers))
            return false;
        if (!Objects.equals(s, searchBox.getText())) {
            refreshSearchNextTick = true;
            moveToTopNextTick = true;
            //TODO
            //            syncJEI();
        }
        return true;
    }

    @Override
    public boolean keyPressed(int pKeyCode, int pScanCode, int pModifiers) {
        ignoreTextInput = false;
        if (!addressBox.isFocused() && !searchBox.isFocused() && client.options.chatKey.matchesKey(pKeyCode, pScanCode)) {
            ignoreTextInput = true;
            searchBox.setFocused(true);
            return true;
        }

        if (pKeyCode == GLFW.GLFW_KEY_ENTER && searchBox.isFocused()) {
            searchBox.setFocused(false);
            return true;
        }

        if (pKeyCode == GLFW.GLFW_KEY_ENTER && hasShiftDown()) {
            sendIt();
            return true;
        }

        if (addressBox.isFocused() && addressBox.keyPressed(pKeyCode, pScanCode, pModifiers))
            return true;

        String s = searchBox.getText();
        if (!searchBox.keyPressed(pKeyCode, pScanCode, pModifiers)) {
            return searchBox.isFocused() && searchBox.isVisible() && pKeyCode != 256 || super.keyPressed(pKeyCode, pScanCode, pModifiers);
        }
        if (!Objects.equals(s, searchBox.getText())) {
            refreshSearchNextTick = true;
            moveToTopNextTick = true;
            //TODO
            //            syncJEI();
        }
        return true;
    }

    @Override
    public void removed() {
        BlockPos pos = blockEntity.getPos();
        ClientPlayNetworkHandler networkHandler = client.player.networkHandler;
        networkHandler.sendPacket(new PackageOrderRequestPacket(pos, PackageOrderWithCrafts.empty(), addressBox.getText(), false));
        networkHandler.sendPacket(new StockKeeperCategoryHidingPacket(pos, new ArrayList<>(hiddenCategories)));
        super.removed();
    }

    private void sendIt() {
        revalidateOrders();
        if (itemsToOrder.isEmpty())
            return;

        forcedEntries = new InventorySummary();
        InventorySummary summary = blockEntity.getLastClientsideStockSnapshotAsSummary();
        for (BigItemStack toOrder : itemsToOrder) {
            // momentarily cut the displayed stack size until the stock updates come in
            int countOf = summary.getCountOf(toOrder.stack);
            if (countOf == BigItemStack.INF)
                continue;
            forcedEntries.add(toOrder.stack.copy(), -1 - Math.max(0, countOf - toOrder.count));
        }

        PackageOrderWithCrafts order = PackageOrderWithCrafts.simple(itemsToOrder);

        if (canRequestCraftingPackage && !itemsToOrder.isEmpty() && !recipesToOrder.isEmpty()) {
            List<CraftingEntry> craftList = new ArrayList<>();
            for (CraftableBigItemStack cbis : recipesToOrder) {
                if (!cbis.input.crafting()) {
                    continue;
                }
                int craftedCount = 0;
                int targetCount = cbis.count / cbis.stack.getCount();
                List<BigItemStack> mutableOrder = BigItemStack.duplicateWrappers(itemsToOrder);

                while (craftedCount < targetCount) {
                    // Carefully split the ordered recipes based on what exactly will be used to craft them
                    PackageOrder pattern = cbis.input.getPattern(mutableOrder);
                    int maxCrafts = targetCount - craftedCount;
                    int availableCrafts = 0;

                    boolean itemsExhausted = false;
                    Outer:
                    while (availableCrafts < maxCrafts && !itemsExhausted) {
                        List<BigItemStack> previousSnapshot = BigItemStack.duplicateWrappers(mutableOrder);
                        itemsExhausted = true;
                        Pattern:
                        for (BigItemStack patternStack : pattern.stacks()) {
                            if (patternStack.stack.isEmpty())
                                continue;
                            for (BigItemStack ordered : mutableOrder) {
                                if (!ItemStack.areItemsAndComponentsEqual(ordered.stack, patternStack.stack))
                                    continue;
                                if (ordered.count == 0)
                                    continue;
                                ordered.count -= 1;
                                itemsExhausted = false;
                                continue Pattern;
                            }
                            mutableOrder = previousSnapshot;
                            break Outer;
                        }
                        availableCrafts++;
                    }

                    if (availableCrafts == 0)
                        break;

                    craftList.add(new CraftingEntry(pattern, availableCrafts));
                    craftedCount += availableCrafts;
                }

            }
            order = new PackageOrderWithCrafts(order.orderedStacks(), craftList);
        }

        client.player.networkHandler.sendPacket(new PackageOrderRequestPacket(blockEntity.getPos(), order, addressBox.getText(), encodeRequester));

        itemsToOrder = new ArrayList<>();
        recipesToOrder = new ArrayList<>();
        blockEntity.ticksSinceLastUpdate = 10;
        successTicks = 1;

        if (isSchematicListMode())
            client.player.closeHandledScreen();
    }

    @Override
    public boolean keyReleased(int pKeyCode, int pScanCode, int pModifiers) {
        ignoreTextInput = false;
        return super.keyReleased(pKeyCode, pScanCode, pModifiers);
    }

    @Override
    public List<Rect2i> getExtraAreas() {
        return extraAreas;
    }

    public boolean isSchematicListMode() {
        return clipboardItem != null;
    }

    public void requestSchematicList() {
        itemsToOrder.clear();
        InventorySummary availableItems = blockEntity.getLastClientsideStockSnapshotAsSummary();
        for (List<ClipboardEntry> list : clipboardItem) {
            for (ClipboardEntry entry : list) {
                ItemStack stack = entry.icon;
                int toOrder = Math.min(entry.itemAmount, availableItems.getCountOf(stack));
                if (toOrder == 0)
                    continue;
                itemsToOrder.add(new BigItemStack(stack, toOrder));
            }
        }
    }

    public void requestCraftable(CraftableBigItemStack cbis, int requestedDifference) {
        boolean takeOrdersAway = requestedDifference < 0;
        if (takeOrdersAway)
            requestedDifference = Math.max(-cbis.count, requestedDifference);
        if (requestedDifference == 0)
            return;

        InventorySummary availableItems = blockEntity.getLastClientsideStockSnapshotAsSummary();
        Function<ItemStack, Integer> countModifier = stack -> {
            BigItemStack ordered = getOrderForItem(stack);
            return ordered == null ? 0 : -ordered.count;
        };

        if (takeOrdersAway) {
            availableItems = new InventorySummary();
            for (BigItemStack ordered : itemsToOrder)
                availableItems.add(ordered.stack, ordered.count);
            countModifier = stack -> 0;
        }

        Pair<Integer, List<List<BigItemStack>>> craftingResult = maxCraftable(
            cbis,
            availableItems,
            countModifier,
            takeOrdersAway ? -1 : 9 - itemsToOrder.size()
        );
        int outputCount = cbis.stack.getCount();
        int adjustToRecipeAmount = MathHelper.ceil(Math.abs(requestedDifference) / (float) outputCount) * outputCount;
        int maxCraftable = Math.min(adjustToRecipeAmount, craftingResult.getFirst());

        if (maxCraftable == 0)
            return;

        cbis.count += takeOrdersAway ? -maxCraftable : maxCraftable;

        List<List<BigItemStack>> validEntriesByIngredient = craftingResult.getSecond();
        for (List<BigItemStack> list : validEntriesByIngredient) {
            int remaining = maxCraftable / outputCount;
            for (BigItemStack entry : list) {
                if (remaining <= 0)
                    break;

                int toTransfer = Math.min(remaining, entry.count);
                BigItemStack order = getOrderForItem(entry.stack);

                if (takeOrdersAway) {
                    if (order != null) {
                        order.count -= toTransfer;
                        if (order.count == 0)
                            itemsToOrder.remove(order);
                    }
                } else {
                    if (order == null)
                        itemsToOrder.add(order = new BigItemStack(entry.stack.copyWithCount(1), 0));
                    order.count += toTransfer;
                }

                remaining -= entry.count;
            }
        }

        updateCraftableAmounts();
    }

    private void updateCraftableAmounts() {
        InventorySummary usedItems = new InventorySummary();
        InventorySummary availableItems = new InventorySummary();

        for (BigItemStack ordered : itemsToOrder)
            availableItems.add(ordered.stack, ordered.count);

        for (CraftableBigItemStack cbis : recipesToOrder) {
            Pair<Integer, List<List<BigItemStack>>> craftingResult = maxCraftable(cbis, availableItems, stack -> -usedItems.getCountOf(stack), -1);
            int maxCraftable = craftingResult.getFirst();
            List<List<BigItemStack>> validEntriesByIngredient = craftingResult.getSecond();
            int outputCount = cbis.stack.getCount();

            // Only tweak amounts downward
            cbis.count = Math.min(cbis.count, maxCraftable);

            // Use ingredients up before checking next recipe
            for (List<BigItemStack> list : validEntriesByIngredient) {
                int remaining = cbis.count / outputCount;
                for (BigItemStack entry : list) {
                    if (remaining <= 0)
                        break;
                    usedItems.add(entry.stack, Math.min(remaining, entry.count));
                    remaining -= entry.count;
                }
            }
        }

        canRequestCraftingPackage = false;
        for (BigItemStack ordered : itemsToOrder)
            if (usedItems.getCountOf(ordered.stack) != ordered.count)
                return;
        canRequestCraftingPackage = true;
    }

    private Pair<Integer, List<List<BigItemStack>>> maxCraftable(
        CraftableBigItemStack cbis,
        InventorySummary summary,
        Function<ItemStack, Integer> countModifier,
        int newTypeLimit
    ) {
        List<List<BigItemStack>> validEntriesByIngredient = new ArrayList<>();
        List<BigItemStack> alreadyCreated = new ArrayList<>();

        for (Object2ObjectMap.Entry<List<ItemStack>, IntList> value : cbis.input.entrySet()) {
            List<ItemStack> ingredient = value.getKey();
            List<BigItemStack> valid = new ArrayList<>();
            for (List<BigItemStack> list : summary.getItemMap().values())
                Entries:for (BigItemStack entry : list) {
                    if (!CraftableInput.contains(ingredient, entry.stack))
                        continue;
                    for (BigItemStack visitedStack : alreadyCreated) {
                        if (!ItemStack.areItemsAndComponentsEqual(visitedStack.stack, entry.stack))
                            continue;
                        valid.add(visitedStack);
                        continue Entries;
                    }
                    BigItemStack asBis = new BigItemStack(entry.stack, summary.getCountOf(entry.stack) + countModifier.apply(entry.stack));
                    if (asBis.count > 0) {
                        valid.add(asBis);
                        alreadyCreated.add(asBis);
                    }
                }

            if (valid.isEmpty())
                return Pair.of(0, List.of());

            valid.sort((bis1, bis2) -> -Integer.compare(summary.getCountOf(bis1.stack), summary.getCountOf(bis2.stack)));
            for (int i = 0, size = value.getValue().size(); i < size; i++) {
                validEntriesByIngredient.add(valid);
            }
        }

        // Used new items may have to be trimmed
        if (newTypeLimit != -1) {
            int toRemove = (int) validEntriesByIngredient.stream().flatMap(Collection::stream).filter(entry -> getOrderForItem(entry.stack) == null)
                .distinct().count() - newTypeLimit;

            for (int i = 0; i < toRemove; i++)
                removeLeastEssentialItemStack(validEntriesByIngredient);
        }

        // Ingredients with shared items must divide counts
        validEntriesByIngredient = resolveIngredientAmounts(validEntriesByIngredient);

        // Determine the bottlenecking ingredient
        int minCount = Integer.MAX_VALUE;
        for (List<BigItemStack> list : validEntriesByIngredient) {
            int sum = 0;
            for (BigItemStack entry : list)
                sum += entry.count;
            minCount = Math.min(sum, minCount);
        }

        if (minCount == 0)
            return Pair.of(0, List.of());

        return Pair.of(minCount * cbis.stack.getCount(), validEntriesByIngredient);
    }

    private void removeLeastEssentialItemStack(List<List<BigItemStack>> validIngredients) {
        List<BigItemStack> longest = null;
        int most = 0;
        for (List<BigItemStack> list : validIngredients) {
            int count = (int) list.stream().filter(entry -> getOrderForItem(entry.stack) == null).count();
            if (longest != null && count <= most)
                continue;
            longest = list;
            most = count;
        }

        if (longest.isEmpty())
            return;

        BigItemStack chosen = null;
        for (int i = 0; i < longest.size(); i++) {
            BigItemStack entry = longest.get(longest.size() - 1 - i);
            if (getOrderForItem(entry.stack) != null)
                continue;
            chosen = entry;
            break;
        }

        for (List<BigItemStack> list : validIngredients)
            list.remove(chosen);
    }

    private List<List<BigItemStack>> resolveIngredientAmounts(List<List<BigItemStack>> validIngredients) {
        List<List<BigItemStack>> resolvedIngredients = new ArrayList<>();
        for (int i = 0; i < validIngredients.size(); i++)
            resolvedIngredients.add(new ArrayList<>());

        boolean everythingTaken = false;
        while (!everythingTaken) {
            everythingTaken = true;
            Ingredients:
            for (int i = 0; i < validIngredients.size(); i++) {
                List<BigItemStack> list = validIngredients.get(i);
                List<BigItemStack> resolvedList = resolvedIngredients.get(i);
                for (BigItemStack bigItemStack : list) {
                    if (bigItemStack.count == 0)
                        continue;

                    bigItemStack.count -= 1;
                    everythingTaken = false;

                    for (BigItemStack resolvedItemStack : resolvedList) {
                        if (resolvedItemStack.stack == bigItemStack.stack) {
                            resolvedItemStack.count++;
                            continue Ingredients;
                        }
                    }

                    resolvedList.add(new BigItemStack(bigItemStack.stack, 1));
                    continue Ingredients;
                }
            }
        }

        return resolvedIngredients;
    }

    //TODO
    //    private void syncJEI() {
    //        if (Mods.JEI.isLoaded() && AllConfigs.client().syncJeiSearch.get())
    //            CreateJEI.runtime.getIngredientFilter().setFilterText(searchBox.getValue());
    //    }

}
