package com.zurrtum.create.client.content.equipment.symmetryWand;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.element.GuiGameElement;
import com.zurrtum.create.client.catnip.gui.widget.ElementWidget;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.IconButton;
import com.zurrtum.create.client.foundation.gui.widget.Label;
import com.zurrtum.create.client.foundation.gui.widget.ScrollInput;
import com.zurrtum.create.client.foundation.gui.widget.SelectionScrollInput;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.symmetryWand.SymmetryWandItem;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.CrossPlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.EmptyMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.PlaneMirror;
import com.zurrtum.create.content.equipment.symmetryWand.mirror.TriplePlaneMirror;
import com.zurrtum.create.infrastructure.component.SymmetryMirror;
import com.zurrtum.create.infrastructure.packet.c2s.ConfigureSymmetryWandPacket;
import org.joml.Vector3f;

import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class SymmetryWandScreen extends AbstractSimiScreen {

    private final AllGuiTextures background = AllGuiTextures.WAND_OF_SYMMETRY;

    private ScrollInput areaType;
    private Label labelType;
    private ScrollInput areaAlign;
    private Label labelAlign;
    private IconButton confirmButton;
    private ElementWidget renderedItem;
    private ElementWidget renderedBlock;

    private final Component mirrorType = CreateLang.translateDirect("gui.symmetryWand.mirrorType");
    private final Component orientation = CreateLang.translateDirect("gui.symmetryWand.orientation");

    private SymmetryMirror currentElement;
    private final ItemStack wand;
    private final InteractionHand hand;

    public SymmetryWandScreen(ItemStack wand, InteractionHand hand) {
        currentElement = SymmetryWandItem.getMirror(wand);
        if (currentElement instanceof EmptyMirror) {
            currentElement = new PlaneMirror(Vec3.ZERO);
        }
        this.hand = hand;
        this.wand = wand;
    }

    public static List<Component> getMirrors() {
        return ImmutableList.of(
            CreateLang.translateDirect("symmetry.mirror.plane"),
            CreateLang.translateDirect("symmetry.mirror.doublePlane"),
            CreateLang.translateDirect("symmetry.mirror.triplePlane")
        );
    }

    public static List<Component> getAlignToolTips(SymmetryMirror element) {
        return switch (element) {
            case PlaneMirror planeMirror ->
                ImmutableList.of(CreateLang.translateDirect("orientation.alongZ"), CreateLang.translateDirect("orientation.alongX"));
            case CrossPlaneMirror crossPlaneMirror ->
                ImmutableList.of(CreateLang.translateDirect("orientation.orthogonal"), CreateLang.translateDirect("orientation.diagonal"));
            case TriplePlaneMirror triplePlaneMirror -> ImmutableList.of(CreateLang.translateDirect("orientation.horizontal"));
            default -> ImmutableList.of();
        };
    }

    @Override
    public void init() {
        setWindowSize(background.getWidth(), background.getHeight());
        setWindowOffset(-20, 0);
        super.init();

        int x = guiLeft;
        int y = guiTop;

        labelType = new Label(x + 51, y + 28, CommonComponents.EMPTY).colored(0xFFFFFFFF).withShadow();
        labelAlign = new Label(x + 51, y + 50, CommonComponents.EMPTY).colored(0xFFFFFFFF).withShadow();

        int state = currentElement instanceof TriplePlaneMirror ? 2 : currentElement instanceof CrossPlaneMirror ? 1 : 0;
        areaType = new SelectionScrollInput(x + 45, y + 21, 109, 18).forOptions(getMirrors()).titled(mirrorType.plainCopy())
            .writingTo(labelType).setState(state);

        areaType.calling(position -> {
            switch (position) {
                case 0:
                    currentElement = new PlaneMirror(currentElement.getPosition());
                    break;
                case 1:
                    currentElement = new CrossPlaneMirror(currentElement.getPosition());
                    break;
                case 2:
                    currentElement = new TriplePlaneMirror(currentElement.getPosition());
                    break;
                default:
                    break;
            }
            initAlign(currentElement, x, y);
            ((GuiGameElement.GuiPartialRenderBuilder) renderedBlock.getRenderElement()).partial(SymmetryHandlerClient.getModel(currentElement));
        });

        initAlign(currentElement, x, y);

        addRenderableWidget(labelAlign);
        addRenderableWidget(areaType);
        addRenderableWidget(labelType);

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::onClose);
        addRenderableWidget(confirmButton);

        renderedItem = new ElementWidget(x + 140, y - 4).showingElement(GuiGameElement.of(wand).rotate(-70, 20, 20).scale(4).padding(100));
        addRenderableWidget(renderedItem);

        renderedBlock = new ElementWidget(x + 23, y + 24).showingElement(GuiGameElement.of(SymmetryHandlerClient.getModel(currentElement))
            .transform(this::transformBlock));
        addRenderableWidget(renderedBlock);
    }

    @Override
    public void onClose() {
        super.onClose();
        renderedItem.getRenderElement().clear();
        renderedBlock.getRenderElement().clear();
    }

    private void initAlign(SymmetryMirror element, int x, int y) {
        if (areaAlign != null)
            removeWidget(areaAlign);

        areaAlign = new SelectionScrollInput(x + 45, y + 43, 109, 18).forOptions(getAlignToolTips(element)).titled(orientation.plainCopy())
            .writingTo(labelAlign).setState(element.getOrientationIndex()).calling(index -> {
                element.setOrientation(index);
                ((GuiGameElement.GuiPartialRenderBuilder) renderedBlock.getRenderElement()).markDirty();
            });

        addRenderableWidget(areaAlign);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        graphics.drawString(
            font,
            wand.getHoverName(),
            x + (background.getWidth() - font.width(wand.getHoverName())) / 2,
            y + 4,
            0xFF592424,
            false
        );
    }

    private void transformBlock(PoseStack ms, float p) {
        ms.translate(0.1875F, 0.9375f, 0);
        ms.mulPose(Axis.of(new Vector3f(.3f, 1f, 0f)).rotationDegrees(-22.5f));
        ms.scale(1, -1, 1);
        SymmetryHandlerClient.applyModelTransform(currentElement, ms);
    }

    @Override
    public void removed() {
        SymmetryWandItem.configureSettings(wand, currentElement);
        minecraft.player.connection.send(new ConfigureSymmetryWandPacket(hand, currentElement));
    }

}