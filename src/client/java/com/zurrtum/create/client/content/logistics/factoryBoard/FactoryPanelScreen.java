package com.zurrtum.create.client.content.logistics.factoryBoard;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.content.logistics.AddressEditBox;
import com.zurrtum.create.client.content.trains.station.NoShadowFontWrapper;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.box.PackageStyles;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelConnection;
import com.zurrtum.create.content.logistics.factoryBoard.FactoryPanelPosition;
import com.zurrtum.create.content.logistics.factoryBoard.ServerFactoryPanelBehaviour;
import com.zurrtum.create.infrastructure.packet.c2s.FactoryPanelConfigurationPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zurrtum.create.client.foundation.gui.AllGuiTextures.*;

public class FactoryPanelScreen extends AbstractSimiScreen {
    private ElementWidget renderedBlock;
    private ElementWidget renderedItem;

    private AddressEditBox addressBox;
    private IconButton confirmButton;
    private IconButton deleteButton;
    private IconButton newInputButton;
    private IconButton relocateButton;
    private IconButton activateCraftingButton;
    private ScrollInput promiseExpiration;
    private ServerFactoryPanelBehaviour behaviour;
    private boolean restocker;
    private boolean sendReset;
    private boolean sendRedstoneReset;

    private BigItemStack outputConfig;
    private List<BigItemStack> inputConfig;
    private List<FactoryPanelConnection> connections;

    private boolean craftingActive;

    public FactoryPanelScreen(ServerFactoryPanelBehaviour behaviour) {
        this.behaviour = behaviour;
        restocker = behaviour.panelBE().restocker;
        craftingActive = !behaviour.activeCraftingArrangement.isEmpty();
        updateConfigs(MinecraftClient.getInstance().world);
    }

    private void updateConfigs(ClientWorld world) {
        connections = new ArrayList<>(behaviour.targetedBy.values());
        outputConfig = new BigItemStack(behaviour.getFilter(), behaviour.recipeOutput);
        inputConfig = connections.stream().map(c -> {
            ServerFactoryPanelBehaviour b = ServerFactoryPanelBehaviour.at(world, c.from);
            return b == null ? new BigItemStack(ItemStack.EMPTY, 0) : new BigItemStack(b.getFilter(), c.amount);
        }).toList();

        if (behaviour.craftingList == null) {
            craftingActive = false;
        }
    }

    @Override
    protected void init() {
        int sizeX = FACTORY_GAUGE_BOTTOM.getWidth();
        int sizeY = (restocker ? FACTORY_GAUGE_RESTOCK : FACTORY_GAUGE_RECIPE).getHeight() + FACTORY_GAUGE_BOTTOM.getHeight();

        setWindowSize(sizeX, sizeY);
        super.init();
        clearChildren();

        int x = guiLeft;
        int y = guiTop;

        if (addressBox == null) {
            String frogAddress = behaviour.getFrogAddress();
            addressBox = new AddressEditBox(this, new NoShadowFontWrapper(textRenderer), x + 36, y + windowHeight - 51, 108, 10, false, frogAddress);
            addressBox.setText(behaviour.recipeAddress);
            addressBox.setEditableColor(0xFF555555);
        }
        addressBox.setX(x + 36);
        addressBox.setY(y + windowHeight - 51);
        addDrawableChild(addressBox);

        confirmButton = new IconButton(x + sizeX - 33, y + sizeY - 25, AllIcons.I_CONFIRM);
        confirmButton.withCallback(() -> client.setScreen(null));
        confirmButton.setToolTip(CreateLang.translate("gui.factory_panel.save_and_close").component());
        addDrawableChild(confirmButton);

        deleteButton = new IconButton(x + sizeX - 55, y + sizeY - 25, AllIcons.I_TRASH);
        deleteButton.withCallback(() -> {
            sendReset = true;
            client.setScreen(null);
        });
        deleteButton.setToolTip(CreateLang.translate("gui.factory_panel.reset").component());
        addDrawableChild(deleteButton);

        promiseExpiration = new ScrollInput(x + 97, y + windowHeight - 24, 28, 16).withRange(-1, 31)
            .titled(CreateLang.translate("gui.factory_panel.promises_expire_title").component());
        promiseExpiration.setState(behaviour.promiseClearingInterval);
        addDrawableChild(promiseExpiration);

        newInputButton = new IconButton(x + 31, y + 47, AllIcons.I_ADD);
        newInputButton.withCallback(() -> {
            FactoryPanelConnectionHandler.startConnection(behaviour);
            client.setScreen(null);
        });
        newInputButton.setToolTip(CreateLang.translate("gui.factory_panel.connect_input").component());

        relocateButton = new IconButton(x + 31, y + 67, AllIcons.I_MOVE_GAUGE);
        relocateButton.withCallback(() -> {
            FactoryPanelConnectionHandler.startRelocating(behaviour);
            client.setScreen(null);
        });
        relocateButton.setToolTip(CreateLang.translate("gui.factory_panel.relocate").component());

        if (!restocker) {
            addDrawableChild(newInputButton);
            addDrawableChild(relocateButton);
        }

        activateCraftingButton = null;
        if (behaviour.craftingList != null) {
            activateCraftingButton = new IconButton(x + 31, y + 27, AllIcons.I_3x3);
            activateCraftingButton.green = craftingActive;
            activateCraftingButton.withCallback(() -> {
                craftingActive = !craftingActive;
                clearRenderedElements();
                init();
                if (craftingActive) {
                    outputConfig.count = behaviour.craftingList.getFirst().count;
                }
            });
            activateCraftingButton.setToolTip(CreateLang.translate("gui.factory_panel.activate_crafting").component());
            addDrawableChild(activateCraftingButton);
        }

        // ITEM PREVIEW
        int previewY = restocker ? 0 : 60;
        renderedBlock = new ElementWidget(x + 195, y + 55 + previewY).showingElement(GuiGameElement.of(AllItems.FACTORY_GAUGE.getDefaultStack())
            .scale(4));
        addDrawableChild(renderedBlock);

        if (!behaviour.getFilter().isEmpty()) {
            renderedItem = new ElementWidget(x + 214, y + 68 + previewY).showingElement(GuiGameElement.of(behaviour.getFilter()).scale(1.625F));
            addDrawableChild(renderedItem);
        } else {
            renderedItem = null;
        }
    }

    @Override
    public void close() {
        super.close();
        clearRenderedElements();
    }

    private void clearRenderedElements() {
        renderedBlock.getRenderElement().clear();
        if (renderedItem != null) {
            renderedItem.getRenderElement().clear();
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (inputConfig.size() != behaviour.targetedBy.size()) {
            updateConfigs(client.world);
            clearRenderedElements();
            init();
        }
        if (activateCraftingButton != null)
            activateCraftingButton.green = craftingActive;
        addressBox.tick();
        promiseExpiration.titled(CreateLang.translate(promiseExpiration.getState() == -1 ? "gui.factory_panel.promises_do_not_expire" : "gui.factory_panel.promises_expire_title")
            .component());
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        // BG
        AllGuiTextures bg = restocker ? FACTORY_GAUGE_RESTOCK : FACTORY_GAUGE_RECIPE;
        if (restocker)
            FACTORY_GAUGE_RECIPE.render(graphics, x, y - 16);
        bg.render(graphics, x, y);
        FACTORY_GAUGE_BOTTOM.render(graphics, x, y + bg.getHeight());
        y = guiTop;

        // RECIPE
        int slot = 0;
        if (craftingActive) {
            List<BigItemStack> list = behaviour.craftingList;
            for (int i = 1, size = list.size(); i < size; i++) {
                renderInputItem(graphics, slot++, list.get(i), mouseX, mouseY);
            }
        } else {
            for (BigItemStack itemStack : inputConfig)
                renderInputItem(graphics, slot++, itemStack, mouseX, mouseY);
            if (inputConfig.isEmpty()) {
                int inputX = guiLeft + (restocker ? 88 : 68 + (slot % 3 * 20));
                int inputY = guiTop + (restocker ? 12 : 28) + (slot / 3 * 20);
                if (!restocker && mouseY > inputY && mouseY < inputY + 60 && mouseX > inputX && mouseX < inputX + 60)
                    graphics.drawTooltip(
                        textRenderer, List.of(
                            CreateLang.translate("gui.factory_panel.unconfigured_input").color(ScrollInput.HEADER_RGB).component(),
                            CreateLang.translate("gui.factory_panel.unconfigured_input_tip").style(Formatting.GRAY).component(),
                            CreateLang.translate("gui.factory_panel.unconfigured_input_tip_1").style(Formatting.GRAY).component()
                        ), mouseX, mouseY
                    );
            }
        }

        if (restocker)
            renderInputItem(graphics, slot, new BigItemStack(behaviour.getFilter(), 1), mouseX, mouseY);

        if (!restocker) {
            int outputX = x + 160;
            int outputY = y + 48;
            graphics.drawItem(outputConfig.stack, outputX, outputY);
            graphics.drawStackOverlay(textRenderer, behaviour.getFilter(), outputX, outputY, outputConfig.count + "");

            if (mouseX >= outputX - 1 && mouseX < outputX - 1 + 18 && mouseY >= outputY - 1 && mouseY < outputY - 1 + 18) {
                MutableText c1 = CreateLang.translate(
                    "gui.factory_panel.expected_output",
                    CreateLang.itemName(outputConfig.stack).add(CreateLang.text(" x" + outputConfig.count)).string()
                ).color(ScrollInput.HEADER_RGB).component();
                MutableText c2 = CreateLang.translate("gui.factory_panel.expected_output_tip").style(Formatting.GRAY).component();
                MutableText c3 = CreateLang.translate("gui.factory_panel.expected_output_tip_1").style(Formatting.GRAY).component();
                MutableText c4 = CreateLang.translate("gui.factory_panel.expected_output_tip_2").style(Formatting.DARK_GRAY).style(Formatting.ITALIC)
                    .component();
                graphics.drawTooltip(textRenderer, craftingActive ? List.of(c1, c2, c3) : List.of(c1, c2, c3, c4), mouseX, mouseY);
            }
        }

        Matrix3x2fStack ms = graphics.getMatrices();
        ms.pushMatrix();

        // ADDRESS
        if (addressBox.isHovered() && !addressBox.isFocused())
            showAddressBoxTooltip(graphics, mouseX, mouseY);

        // TITLE
        Text title = CreateLang.translate(restocker ? "gui.factory_panel.title_as_restocker" : "gui.factory_panel.title_as_recipe").component();
        graphics.drawText(textRenderer, title, x + 97 - textRenderer.getWidth(title) / 2, y + (restocker ? -12 : 4), 0xFF3D3C48, false);

        // REDSTONE LINKS
        if (!behaviour.targetedByLinks.isEmpty()) {
            ItemStack asStack = AllItems.REDSTONE_LINK.getDefaultStack();
            int itemX = x + 9;
            int itemY = y + windowHeight - 24;
            AllGuiTextures.FROGPORT_SLOT.render(graphics, itemX - 1, itemY - 1);
            graphics.drawItem(asStack, itemX, itemY);

            if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
                List<Text> linkTip = List.of(
                    CreateLang.translate("gui.factory_panel.has_link_connections").color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.translate("gui.factory_panel.left_click_disconnect").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                );
                graphics.drawTooltip(textRenderer, linkTip, mouseX, mouseY);
            }
        }

        // PROMISES
        int state = promiseExpiration.getState();
        graphics.drawText(
            textRenderer,
            CreateLang.text(state == -1 ? " /" : state == 0 ? "30s" : state + "m").component(),
            promiseExpiration.getX() + 3,
            promiseExpiration.getY() + 4,
            0xffeeeeee,
            true
        );

        ItemStack asStack = PackageStyles.getDefaultBox();
        int itemX = x + 68;
        int itemY = y + windowHeight - 24;
        graphics.drawItem(asStack, itemX, itemY);
        int promised = behaviour.getPromised();
        graphics.drawStackOverlay(textRenderer, asStack, itemX, itemY, promised + "");

        if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
            List<Text> promiseTip;

            if (promised == 0) {
                promiseTip = List.of(
                    CreateLang.translate("gui.factory_panel.no_open_promises").color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.translate(restocker ? "gui.factory_panel.restocker_promises_tip" : "gui.factory_panel.recipe_promises_tip")
                        .style(Formatting.GRAY).component(),
                    CreateLang.translate(restocker ? "gui.factory_panel.restocker_promises_tip_1" : "gui.factory_panel.recipe_promises_tip_1")
                        .style(Formatting.GRAY).component(),
                    CreateLang.translate("gui.factory_panel.promise_prevents_oversending").style(Formatting.GRAY).component()
                );
            } else {
                promiseTip = List.of(
                    CreateLang.translate("gui.factory_panel.promised_items").color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.text(behaviour.getFilter().getName().getString() + " x" + promised).component(),
                    CreateLang.translate("gui.factory_panel.left_click_reset").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                );
            }

            graphics.drawTooltip(textRenderer, promiseTip, mouseX, mouseY);
        }

        ms.popMatrix();
    }

    //

    private void renderInputItem(DrawContext graphics, int slot, BigItemStack itemStack, int mouseX, int mouseY) {
        int inputX = guiLeft + (restocker ? 88 : 68 + (slot % 3 * 20));
        int inputY = guiTop + (restocker ? 12 : 28) + (slot / 3 * 20);

        graphics.drawItem(itemStack.stack, inputX, inputY);
        if (!craftingActive && !restocker && !itemStack.stack.isEmpty())
            graphics.drawStackOverlay(textRenderer, itemStack.stack, inputX, inputY, itemStack.count + "");

        if (mouseX < inputX - 2 || mouseX >= inputX - 2 + 20 || mouseY < inputY - 2 || mouseY >= inputY - 2 + 20)
            return;

        if (craftingActive) {
            graphics.drawTooltip(
                textRenderer, List.of(
                    CreateLang.translate("gui.factory_panel.crafting_input").color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.translate("gui.factory_panel.crafting_input_tip").style(Formatting.GRAY).component(),
                    CreateLang.translate("gui.factory_panel.crafting_input_tip_1").style(Formatting.GRAY).component()
                ), mouseX, mouseY
            );
            return;
        }

        if (itemStack.stack.isEmpty()) {
            graphics.drawTooltip(
                textRenderer, List.of(
                    CreateLang.translate("gui.factory_panel.empty_panel").color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.translate("gui.factory_panel.left_click_disconnect").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                ), mouseX, mouseY
            );
            return;
        }

        if (restocker) {
            graphics.drawTooltip(
                textRenderer, List.of(
                    CreateLang.translate("gui.factory_panel.sending_item", CreateLang.itemName(itemStack.stack).string())
                        .color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.translate("gui.factory_panel.sending_item_tip").style(Formatting.GRAY).component(),
                    CreateLang.translate("gui.factory_panel.sending_item_tip_1").style(Formatting.GRAY).component()
                ), mouseX, mouseY
            );
            return;
        }

        graphics.drawTooltip(
            textRenderer, List.of(
                CreateLang.translate(
                    "gui.factory_panel.sending_item",
                    CreateLang.itemName(itemStack.stack).add(CreateLang.text(" x" + itemStack.count)).string()
                ).color(ScrollInput.HEADER_RGB).component(),
                CreateLang.translate("gui.factory_panel.scroll_to_change_amount").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component(),
                CreateLang.translate("gui.factory_panel.left_click_disconnect").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
            ), mouseX, mouseY
        );
    }

    private void showAddressBoxTooltip(DrawContext graphics, int mouseX, int mouseY) {
        if (addressBox.getText().isBlank()) {
            if (restocker) {
                graphics.drawTooltip(
                    textRenderer, List.of(
                        CreateLang.translate("gui.factory_panel.restocker_address").color(ScrollInput.HEADER_RGB).component(),
                        CreateLang.translate("gui.factory_panel.restocker_address_tip").style(Formatting.GRAY).component(),
                        CreateLang.translate("gui.factory_panel.restocker_address_tip_1").style(Formatting.GRAY).component(),
                        CreateLang.translate("gui.schedule.lmb_edit").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                    ), mouseX, mouseY
                );

            } else {
                graphics.drawTooltip(
                    textRenderer, List.of(
                        CreateLang.translate("gui.factory_panel.recipe_address").color(ScrollInput.HEADER_RGB).component(),
                        CreateLang.translate("gui.factory_panel.recipe_address_tip").style(Formatting.GRAY).component(),
                        CreateLang.translate("gui.factory_panel.recipe_address_tip_1").style(Formatting.GRAY).component(),
                        CreateLang.translate("gui.schedule.lmb_edit").style(Formatting.DARK_GRAY).style(Formatting.ITALIC).component()
                    ), mouseX, mouseY
                );
            }
        } else
            graphics.drawTooltip(
                textRenderer, List.of(
                    CreateLang.translate(restocker ? "gui.factory_panel.restocker_address_given" : "gui.factory_panel.recipe_address_given")
                        .color(ScrollInput.HEADER_RGB).component(),
                    CreateLang.text("'" + addressBox.getText() + "'").style(Formatting.GRAY).component()
                ), mouseX, mouseY
            );
    }

    //

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        if (getFocused() != null && !getFocused().isMouseOver(mouseX, mouseY))
            setFocused(null);

        int x = guiLeft;
        int y = guiTop;

        // Remove connections
        if (!craftingActive)
            for (int i = 0; i < connections.size(); i++) {
                int inputX = x + 68 + (i % 3 * 20);
                int inputY = y + 28 + (i / 3 * 20);
                if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
                    sendIt(connections.get(i).from, false);
                    playButtonSound();
                    return true;
                }
            }

        // Clear promises
        int itemX = x + 68;
        int itemY = y + windowHeight - 24;
        if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
            sendIt(null, true);
            playButtonSound();
            return true;
        }

        // remove redstone connections
        itemX = x + 9;
        itemY = y + windowHeight - 24;
        if (mouseX >= itemX && mouseX < itemX + 16 && mouseY >= itemY && mouseY < itemY + 16) {
            sendRedstoneReset = true;
            sendIt(null, false);
            playButtonSound();
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    public void playButtonSound() {
        client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.25f));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int x = guiLeft;
        int y = guiTop;

        if (addressBox.mouseScrolled(mouseX, mouseY, scrollX, scrollY))
            return true;

        if (craftingActive)
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        for (int i = 0; i < inputConfig.size(); i++) {
            int inputX = x + 68 + (i % 3 * 20);
            int inputY = y + 26 + (i / 3 * 20);
            if (mouseX >= inputX && mouseX < inputX + 16 && mouseY >= inputY && mouseY < inputY + 16) {
                BigItemStack itemStack = inputConfig.get(i);
                if (itemStack.stack.isEmpty())
                    return true;
                itemStack.count = MathHelper.clamp((int) (itemStack.count + Math.signum(scrollY) * (AllKeys.hasShiftDown() ? 10 : 1)), 1, 64);
                return true;
            }
        }

        if (!restocker) {
            int outputX = x + 160;
            int outputY = y + 48;
            if (mouseX >= outputX && mouseX < outputX + 16 && mouseY >= outputY && mouseY < outputY + 16) {
                BigItemStack itemStack = outputConfig;
                itemStack.count = MathHelper.clamp((int) (itemStack.count + Math.signum(scrollY) * (AllKeys.hasShiftDown() ? 10 : 1)), 1, 64);
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public void removed() {
        sendIt(null, false);
        super.removed();
    }

    private void sendIt(@Nullable FactoryPanelPosition toRemove, boolean clearPromises) {
        Map<FactoryPanelPosition, Integer> inputs = new HashMap<>();

        if (inputConfig.size() == connections.size()) {
            for (int i = 0; i < inputConfig.size(); i++) {
                BigItemStack stackInConfig = inputConfig.get(i);
                inputs.put(
                    connections.get(i).from,
                    craftingActive ? (int) behaviour.craftingList.stream().skip(1)
                        .filter(b -> !b.stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(b.stack, stackInConfig.stack))
                        .count() : stackInConfig.count
                );
            }
        }

        List<ItemStack> craftingArrangement = craftingActive ? behaviour.craftingList.stream().skip(1).map(b -> b.stack).toList() : List.of();

        FactoryPanelPosition pos = behaviour.getPanelPosition();
        int promiseExp = promiseExpiration.getState();
        String address = addressBox.getText();

        FactoryPanelConfigurationPacket packet = new FactoryPanelConfigurationPacket(
            pos,
            address,
            inputs,
            craftingArrangement,
            outputConfig.count,
            promiseExp,
            toRemove,
            clearPromises,
            sendReset,
            sendRedstoneReset
        );
        client.player.networkHandler.sendPacket(packet);
    }

}
