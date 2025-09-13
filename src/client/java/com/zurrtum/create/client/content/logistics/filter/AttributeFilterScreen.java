package com.zurrtum.create.client.content.logistics.filter;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.filter.AttributeFilterMenu;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.foundation.gui.menu.MenuType;
import com.zurrtum.create.infrastructure.component.AttributeFilterWhitelistMode;
import com.zurrtum.create.infrastructure.component.ItemAttributeEntry;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket;
import com.zurrtum.create.infrastructure.packet.c2s.FilterScreenPacket.Option;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeFilterScreen extends AbstractFilterScreen<AttributeFilterMenu> {

    private static final String PREFIX = "gui.attribute_filter.";

    private final Text addDESC = CreateLang.translateDirect(PREFIX + "add_attribute");
    private final Text addInvertedDESC = CreateLang.translateDirect(PREFIX + "add_inverted_attribute");

    private final Text allowDisN = CreateLang.translateDirect(PREFIX + "allow_list_disjunctive");
    private final Text allowDisDESC = CreateLang.translateDirect(PREFIX + "allow_list_disjunctive.description");
    private final Text allowConN = CreateLang.translateDirect(PREFIX + "allow_list_conjunctive");
    private final Text allowConDESC = CreateLang.translateDirect(PREFIX + "allow_list_conjunctive.description");
    private final Text denyN = CreateLang.translateDirect(PREFIX + "deny_list");
    private final Text denyDESC = CreateLang.translateDirect(PREFIX + "deny_list.description");

    private final Text referenceH = CreateLang.translateDirect(PREFIX + "add_reference_item");
    private final Text noSelectedT = CreateLang.translateDirect(PREFIX + "no_selected_attributes");
    private final Text selectedT = CreateLang.translateDirect(PREFIX + "selected_attributes");

    private IconButton whitelistDis, whitelistCon, blacklist;
    private IconButton add;
    private IconButton addInverted;

    private ItemStack lastItemScanned = ItemStack.EMPTY;
    private final List<ItemAttribute> attributesOfItem = new ArrayList<>();
    private final List<Text> selectedAttributes = new ArrayList<>();
    private SelectionScrollInput attributeSelector;
    private Label attributeSelectorLabel;

    public AttributeFilterScreen(AttributeFilterMenu menu, PlayerInventory inv, Text title) {
        super(menu, inv, title, AllGuiTextures.ATTRIBUTE_FILTER);
    }

    public static AttributeFilterScreen create(
        MinecraftClient mc,
        MenuType<ItemStack> type,
        int syncId,
        PlayerInventory inventory,
        Text title,
        RegistryByteBuf extraData
    ) {
        return type.create(AttributeFilterScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (hoveredElement(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 7);
        super.init();

        whitelistDis = new IconButton(x + 38, y + 61, AllIcons.I_WHITELIST_OR);
        whitelistDis.withCallback(() -> {
            handler.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_DISJ;
            sendOptionUpdate(Option.WHITELIST);
        });
        whitelistDis.setToolTip(allowDisN);
        whitelistCon = new IconButton(x + 56, y + 61, AllIcons.I_WHITELIST_AND);
        whitelistCon.withCallback(() -> {
            handler.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_CONJ;
            sendOptionUpdate(Option.WHITELIST2);
        });
        whitelistCon.setToolTip(allowConN);
        blacklist = new IconButton(x + 74, y + 61, AllIcons.I_WHITELIST_NOT);
        blacklist.withCallback(() -> {
            handler.whitelistMode = AttributeFilterWhitelistMode.BLACKLIST;
            sendOptionUpdate(Option.BLACKLIST);
        });
        blacklist.setToolTip(denyN);

        addRenderableWidgets(blacklist, whitelistCon, whitelistDis);

        addDrawableChild(add = new IconButton(x + 182, y + 26, AllIcons.I_ADD));
        addDrawableChild(addInverted = new IconButton(x + 200, y + 26, AllIcons.I_ADD_INVERTED_ATTRIBUTE));
        add.withCallback(() -> {
            handleAddedAttibute(false);
        });
        add.setToolTip(addDESC);
        addInverted.withCallback(() -> {
            handleAddedAttibute(true);
        });
        addInverted.setToolTip(addInvertedDESC);

        handleIndicators();

        attributeSelectorLabel = new Label(x + 43, y + 31, ScreenTexts.EMPTY).colored(0xFFF3EBDE).withShadow();
        attributeSelector = new SelectionScrollInput(x + 39, y + 26, 137, 18);
        attributeSelector.forOptions(Arrays.asList(ScreenTexts.EMPTY));
        attributeSelector.removeCallback();
        referenceItemChanged(handler.ghostInventory.getStack(0));

        addDrawableChild(attributeSelector);
        addDrawableChild(attributeSelectorLabel);

        selectedAttributes.clear();
        selectedAttributes.add((handler.selectedAttributes.isEmpty() ? noSelectedT : selectedT).copyContentOnly().formatted(Formatting.YELLOW));
        handler.selectedAttributes.forEach(at -> {
            selectedAttributes.add(Text.literal("- ").append(at.attribute().format(at.inverted())).formatted(Formatting.GRAY));
        });
    }

    private void referenceItemChanged(ItemStack stack) {
        RegistryWrapper.WrapperLookup registries = client.world.getRegistryManager();
        lastItemScanned = stack;

        if (stack.isEmpty()) {
            attributeSelector.active = false;
            attributeSelector.visible = false;
            attributeSelectorLabel.text = referenceH.copyContentOnly().formatted(Formatting.ITALIC);
            add.active = false;
            addInverted.active = false;
            attributeSelector.calling(s -> {
            });
            return;
        }

        add.active = true;

        addInverted.active = true;
        attributeSelector.titled(CreateLang.text(stack.getName().getString() + "...").color(ScrollInput.HEADER_RGB.getRGB()).component());
        attributesOfItem.clear();
        for (ItemAttributeType type : CreateRegistries.ITEM_ATTRIBUTE_TYPE)
            attributesOfItem.addAll(type.getAllAttributes(stack, client.world));
        List<Text> options = attributesOfItem.stream().map(a -> a.format(false)).collect(Collectors.toList());
        attributeSelector.forOptions(options);
        attributeSelector.active = true;
        attributeSelector.visible = true;
        attributeSelector.setState(0);
        attributeSelector.calling(i -> {
            attributeSelectorLabel.setTextAndTrim(options.get(i), true, 112);
            ItemAttribute selected = attributesOfItem.get(i);
            for (ItemAttributeEntry existing : handler.selectedAttributes) {
                NbtCompound testTag = ItemAttribute.saveStatic(existing.attribute(), registries);
                NbtCompound testTag2 = ItemAttribute.saveStatic(selected, registries);
                if (testTag.equals(testTag2)) {
                    add.active = false;
                    addInverted.active = false;
                    return;
                }
            }
            add.active = true;
            addInverted.active = true;
        });
        attributeSelector.onChanged();
    }

    @Override
    public void renderForeground(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        ItemStack stack = handler.ghostInventory.getStack(1);
        graphics.drawStackOverlay(textRenderer, stack, x + 16, y + 62, String.valueOf(selectedAttributes.size() - 1));

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void handledScreenTick() {
        super.handledScreenTick();
        ItemStack stackInSlot = handler.ghostInventory.getStack(0);
        if (!ItemStack.areEqual(stackInSlot, lastItemScanned))
            referenceItemChanged(stackInSlot);
    }

    @Override
    protected void drawMouseoverTooltip(DrawContext graphics, int mouseX, int mouseY) {
        if (handler.getCursorStack().isEmpty() && focusedSlot != null && focusedSlot.hasStack()) {
            if (focusedSlot.id == 37) {
                graphics.drawTooltip(textRenderer, selectedAttributes, mouseX, mouseY);
                return;
            }
            graphics.drawItemTooltip(textRenderer, focusedSlot.getStack(), mouseX, mouseY);
        }
        super.drawMouseoverTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected List<IconButton> getTooltipButtons() {
        return Arrays.asList(blacklist, whitelistCon, whitelistDis);
    }

    @Override
    protected List<MutableText> getTooltipDescriptions() {
        return Arrays.asList(denyDESC.copyContentOnly(), allowConDESC.copyContentOnly(), allowDisDESC.copyContentOnly());
    }

    protected boolean handleAddedAttibute(boolean inverted) {
        int index = attributeSelector.getState();
        if (index >= attributesOfItem.size())
            return false;
        add.active = false;
        addInverted.active = false;
        ItemAttribute itemAttribute = attributesOfItem.get(index);
        NbtCompound tag = ItemAttribute.saveStatic(itemAttribute, client.world.getRegistryManager());
        client.player.networkHandler.sendPacket(new FilterScreenPacket(inverted ? Option.ADD_INVERTED_TAG : Option.ADD_TAG, tag));
        handler.appendSelectedAttribute(itemAttribute, inverted);
        if (handler.selectedAttributes.size() == 1)
            selectedAttributes.set(0, selectedT.copyContentOnly().formatted(Formatting.YELLOW));
        selectedAttributes.add(Text.literal("- ").append(itemAttribute.format(inverted)).formatted(Formatting.GRAY));
        return true;
    }

    @Override
    protected void contentsCleared() {
        selectedAttributes.clear();
        selectedAttributes.add(noSelectedT.copyContentOnly().formatted(Formatting.YELLOW));
        if (!lastItemScanned.isEmpty()) {
            add.active = true;
            addInverted.active = true;
        }
    }

    @Override
    protected boolean isButtonEnabled(IconButton button) {
        if (button == blacklist)
            return handler.whitelistMode != AttributeFilterWhitelistMode.BLACKLIST;
        if (button == whitelistCon)
            return handler.whitelistMode != AttributeFilterWhitelistMode.WHITELIST_CONJ;
        if (button == whitelistDis)
            return handler.whitelistMode != AttributeFilterWhitelistMode.WHITELIST_DISJ;
        return true;
    }

}
