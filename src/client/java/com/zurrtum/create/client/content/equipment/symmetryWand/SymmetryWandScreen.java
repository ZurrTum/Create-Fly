package com.zurrtum.create.client.content.equipment.symmetryWand;

import com.google.common.collect.ImmutableList;
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
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import org.joml.Vector3f;

import java.util.List;

public class SymmetryWandScreen extends AbstractSimiScreen {

    private final AllGuiTextures background = AllGuiTextures.WAND_OF_SYMMETRY;

    private ScrollInput areaType;
    private Label labelType;
    private ScrollInput areaAlign;
    private Label labelAlign;
    private IconButton confirmButton;
    private ElementWidget renderedItem;
    private ElementWidget renderedBlock;

    private final Text mirrorType = CreateLang.translateDirect("gui.symmetryWand.mirrorType");
    private final Text orientation = CreateLang.translateDirect("gui.symmetryWand.orientation");

    private SymmetryMirror currentElement;
    private final ItemStack wand;
    private final Hand hand;

    public SymmetryWandScreen(ItemStack wand, Hand hand) {
        currentElement = SymmetryWandItem.getMirror(wand);
        if (currentElement instanceof EmptyMirror) {
            currentElement = new PlaneMirror(Vec3d.ZERO);
        }
        this.hand = hand;
        this.wand = wand;
    }

    public static List<Text> getMirrors() {
        return ImmutableList.of(
            CreateLang.translateDirect("symmetry.mirror.plane"),
            CreateLang.translateDirect("symmetry.mirror.doublePlane"),
            CreateLang.translateDirect("symmetry.mirror.triplePlane")
        );
    }

    public static List<Text> getAlignToolTips(SymmetryMirror element) {
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

        labelType = new Label(x + 51, y + 28, ScreenTexts.EMPTY).colored(0xFFFFFFFF).withShadow();
        labelAlign = new Label(x + 51, y + 50, ScreenTexts.EMPTY).colored(0xFFFFFFFF).withShadow();

        int state = currentElement instanceof TriplePlaneMirror ? 2 : currentElement instanceof CrossPlaneMirror ? 1 : 0;
        areaType = new SelectionScrollInput(x + 45, y + 21, 109, 18).forOptions(getMirrors()).titled(mirrorType.copyContentOnly())
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

        addDrawableChild(labelAlign);
        addDrawableChild(areaType);
        addDrawableChild(labelType);

        confirmButton = new IconButton(x + background.getWidth() - 33, y + background.getHeight() - 24, AllIcons.I_CONFIRM);
        confirmButton.withCallback(this::close);
        addDrawableChild(confirmButton);

        renderedItem = new ElementWidget(x + 140, y - 4).showingElement(GuiGameElement.of(wand).rotate(-70, 20, 20).scale(4).padding(100));
        addDrawableChild(renderedItem);

        renderedBlock = new ElementWidget(x + 23, y + 24).showingElement(GuiGameElement.of(SymmetryHandlerClient.getModel(currentElement))
            .transform(this::transformBlock));
        addDrawableChild(renderedBlock);
    }

    @Override
    public void close() {
        super.close();
        renderedItem.getRenderElement().clear();
        renderedBlock.getRenderElement().clear();
    }

    private void initAlign(SymmetryMirror element, int x, int y) {
        if (areaAlign != null)
            remove(areaAlign);

        areaAlign = new SelectionScrollInput(x + 45, y + 43, 109, 18).forOptions(getAlignToolTips(element)).titled(orientation.copyContentOnly())
            .writingTo(labelAlign).setState(element.getOrientationIndex()).calling(index -> {
                element.setOrientation(index);
                ((GuiGameElement.GuiPartialRenderBuilder) renderedBlock.getRenderElement()).markDirty();
            });

        addDrawableChild(areaAlign);
    }

    @Override
    protected void renderWindow(DrawContext graphics, int mouseX, int mouseY, float partialTicks) {
        int x = guiLeft;
        int y = guiTop;

        background.render(graphics, x, y);
        graphics.drawText(
            textRenderer,
            wand.getName(),
            x + (background.getWidth() - textRenderer.getWidth(wand.getName())) / 2,
            y + 4,
            0xFF592424,
            false
        );
    }

    private void transformBlock(MatrixStack ms, float p) {
        ms.translate(0.1875F, 0.9375f, 0);
        ms.multiply(RotationAxis.of(new Vector3f(.3f, 1f, 0f)).rotationDegrees(-22.5f));
        ms.scale(1, -1, 1);
        SymmetryHandlerClient.applyModelTransform(currentElement, ms);
    }

    @Override
    public void removed() {
        SymmetryWandItem.configureSettings(wand, currentElement);
        client.player.networkHandler.sendPacket(new ConfigureSymmetryWandPacket(hand, currentElement));
    }

}