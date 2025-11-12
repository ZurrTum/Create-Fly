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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AttributeFilterScreen extends AbstractFilterScreen<AttributeFilterMenu> {

    private static final String PREFIX = "gui.attribute_filter.";

    private final Component addDESC = CreateLang.translateDirect(PREFIX + "add_attribute");
    private final Component addInvertedDESC = CreateLang.translateDirect(PREFIX + "add_inverted_attribute");

    private final Component allowDisN = CreateLang.translateDirect(PREFIX + "allow_list_disjunctive");
    private final Component allowDisDESC = CreateLang.translateDirect(PREFIX + "allow_list_disjunctive.description");
    private final Component allowConN = CreateLang.translateDirect(PREFIX + "allow_list_conjunctive");
    private final Component allowConDESC = CreateLang.translateDirect(PREFIX + "allow_list_conjunctive.description");
    private final Component denyN = CreateLang.translateDirect(PREFIX + "deny_list");
    private final Component denyDESC = CreateLang.translateDirect(PREFIX + "deny_list.description");

    private final Component referenceH = CreateLang.translateDirect(PREFIX + "add_reference_item");
    private final Component noSelectedT = CreateLang.translateDirect(PREFIX + "no_selected_attributes");
    private final Component selectedT = CreateLang.translateDirect(PREFIX + "selected_attributes");

    private IconButton whitelistDis, whitelistCon, blacklist;
    private IconButton add;
    private IconButton addInverted;

    private ItemStack lastItemScanned = ItemStack.EMPTY;
    private final List<ItemAttribute> attributesOfItem = new ArrayList<>();
    private final List<Component> selectedAttributes = new ArrayList<>();
    private SelectionScrollInput attributeSelector;
    private Label attributeSelectorLabel;

    public AttributeFilterScreen(AttributeFilterMenu menu, Inventory inv, Component title) {
        super(menu, inv, title, AllGuiTextures.ATTRIBUTE_FILTER);
    }

    public static AttributeFilterScreen create(
        Minecraft mc,
        MenuType<ItemStack> type,
        int syncId,
        Inventory inventory,
        Component title,
        RegistryFriendlyByteBuf extraData
    ) {
        return type.create(AttributeFilterScreen::new, syncId, inventory, title, getStack(extraData));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (getChildAt(mouseX, mouseY).filter(element -> element.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)).isPresent()) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    protected void init() {
        setWindowOffset(-11, 7);
        super.init();

        whitelistDis = new IconButton(leftPos + 38, topPos + 61, AllIcons.I_WHITELIST_OR);
        whitelistDis.withCallback(() -> {
            menu.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_DISJ;
            sendOptionUpdate(Option.WHITELIST);
        });
        whitelistDis.setToolTip(allowDisN);
        whitelistCon = new IconButton(leftPos + 56, topPos + 61, AllIcons.I_WHITELIST_AND);
        whitelistCon.withCallback(() -> {
            menu.whitelistMode = AttributeFilterWhitelistMode.WHITELIST_CONJ;
            sendOptionUpdate(Option.WHITELIST2);
        });
        whitelistCon.setToolTip(allowConN);
        blacklist = new IconButton(leftPos + 74, topPos + 61, AllIcons.I_WHITELIST_NOT);
        blacklist.withCallback(() -> {
            menu.whitelistMode = AttributeFilterWhitelistMode.BLACKLIST;
            sendOptionUpdate(Option.BLACKLIST);
        });
        blacklist.setToolTip(denyN);

        addRenderableWidgets(blacklist, whitelistCon, whitelistDis);

        addRenderableWidget(add = new IconButton(leftPos + 182, topPos + 26, AllIcons.I_ADD));
        addRenderableWidget(addInverted = new IconButton(leftPos + 200, topPos + 26, AllIcons.I_ADD_INVERTED_ATTRIBUTE));
        add.withCallback(() -> {
            handleAddedAttibute(false);
        });
        add.setToolTip(addDESC);
        addInverted.withCallback(() -> {
            handleAddedAttibute(true);
        });
        addInverted.setToolTip(addInvertedDESC);

        handleIndicators();

        attributeSelectorLabel = new Label(leftPos + 43, topPos + 31, CommonComponents.EMPTY).colored(0xFFF3EBDE).withShadow();
        attributeSelector = new SelectionScrollInput(leftPos + 39, topPos + 26, 137, 18);
        attributeSelector.forOptions(Arrays.asList(CommonComponents.EMPTY));
        attributeSelector.removeCallback();
        referenceItemChanged(menu.ghostInventory.getItem(0));

        addRenderableWidget(attributeSelector);
        addRenderableWidget(attributeSelectorLabel);

        selectedAttributes.clear();
        selectedAttributes.add((menu.selectedAttributes.isEmpty() ? noSelectedT : selectedT).plainCopy().withStyle(ChatFormatting.YELLOW));
        menu.selectedAttributes.forEach(at -> {
            selectedAttributes.add(Component.literal("- ").append(at.attribute().format(at.inverted())).withStyle(ChatFormatting.GRAY));
        });
    }

    private void referenceItemChanged(ItemStack stack) {
        HolderLookup.Provider registries = minecraft.level.registryAccess();
        lastItemScanned = stack;

        if (stack.isEmpty()) {
            attributeSelector.active = false;
            attributeSelector.visible = false;
            attributeSelectorLabel.text = referenceH.plainCopy().withStyle(ChatFormatting.ITALIC);
            add.active = false;
            addInverted.active = false;
            attributeSelector.calling(s -> {
            });
            return;
        }

        add.active = true;

        addInverted.active = true;
        attributeSelector.titled(CreateLang.text(stack.getHoverName().getString() + "...").color(ScrollInput.HEADER_RGB.getRGB()).component());
        attributesOfItem.clear();
        for (ItemAttributeType type : CreateRegistries.ITEM_ATTRIBUTE_TYPE)
            attributesOfItem.addAll(type.getAllAttributes(stack, minecraft.level));
        List<Component> options = attributesOfItem.stream().map(a -> a.format(false)).collect(Collectors.toList());
        attributeSelector.forOptions(options);
        attributeSelector.active = true;
        attributeSelector.visible = true;
        attributeSelector.setState(0);
        attributeSelector.calling(i -> {
            attributeSelectorLabel.setTextAndTrim(options.get(i), true, 112);
            ItemAttribute selected = attributesOfItem.get(i);
            for (ItemAttributeEntry existing : menu.selectedAttributes) {
                CompoundTag testTag = ItemAttribute.saveStatic(existing.attribute(), registries);
                CompoundTag testTag2 = ItemAttribute.saveStatic(selected, registries);
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
    public void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        ItemStack stack = menu.ghostInventory.getItem(1);
        graphics.renderItemDecorations(font, stack, leftPos + 16, topPos + 62, String.valueOf(selectedAttributes.size() - 1));

        super.renderForeground(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        ItemStack stackInSlot = menu.ghostInventory.getItem(0);
        if (!ItemStack.matches(stackInSlot, lastItemScanned))
            referenceItemChanged(stackInSlot);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (menu.getCarried().isEmpty() && hoveredSlot != null && hoveredSlot.hasItem()) {
            if (hoveredSlot.index == 37) {
                graphics.setComponentTooltipForNextFrame(font, selectedAttributes, mouseX, mouseY);
                return;
            }
            graphics.setTooltipForNextFrame(font, hoveredSlot.getItem(), mouseX, mouseY);
        }
        super.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected List<IconButton> getTooltipButtons() {
        return Arrays.asList(blacklist, whitelistCon, whitelistDis);
    }

    @Override
    protected List<MutableComponent> getTooltipDescriptions() {
        return Arrays.asList(denyDESC.plainCopy(), allowConDESC.plainCopy(), allowDisDESC.plainCopy());
    }

    protected boolean handleAddedAttibute(boolean inverted) {
        int index = attributeSelector.getState();
        if (index >= attributesOfItem.size())
            return false;
        add.active = false;
        addInverted.active = false;
        ItemAttribute itemAttribute = attributesOfItem.get(index);
        CompoundTag tag = ItemAttribute.saveStatic(itemAttribute, minecraft.level.registryAccess());
        minecraft.player.connection.send(new FilterScreenPacket(inverted ? Option.ADD_INVERTED_TAG : Option.ADD_TAG, tag));
        menu.appendSelectedAttribute(itemAttribute, inverted);
        if (menu.selectedAttributes.size() == 1)
            selectedAttributes.set(0, selectedT.plainCopy().withStyle(ChatFormatting.YELLOW));
        selectedAttributes.add(Component.literal("- ").append(itemAttribute.format(inverted)).withStyle(ChatFormatting.GRAY));
        return true;
    }

    @Override
    protected void contentsCleared() {
        selectedAttributes.clear();
        selectedAttributes.add(noSelectedT.plainCopy().withStyle(ChatFormatting.YELLOW));
        if (!lastItemScanned.isEmpty()) {
            add.active = true;
            addInverted.active = true;
        }
    }

    @Override
    protected boolean isButtonEnabled(IconButton button) {
        if (button == blacklist)
            return menu.whitelistMode != AttributeFilterWhitelistMode.BLACKLIST;
        if (button == whitelistCon)
            return menu.whitelistMode != AttributeFilterWhitelistMode.WHITELIST_CONJ;
        if (button == whitelistDis)
            return menu.whitelistMode != AttributeFilterWhitelistMode.WHITELIST_DISJ;
        return true;
    }

}
