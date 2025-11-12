package com.zurrtum.create.client.content.contraptions.wrench;


import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllKeys;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.gui.AbstractSimiScreen;
import com.zurrtum.create.client.catnip.gui.UIRenderHelper;
import com.zurrtum.create.client.catnip.gui.element.RenderElement;
import com.zurrtum.create.client.catnip.gui.render.DirectionIndicatorRenderState;
import com.zurrtum.create.client.catnip.gui.render.EntityBlockRenderState;
import com.zurrtum.create.client.catnip.gui.render.EntityBlockRenderer;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.ponder.enums.PonderGuiTextures;
import com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock;
import com.zurrtum.create.content.kinetics.base.HorizontalAxisKineticBlock;
import com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock;
import com.zurrtum.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.zurrtum.create.content.kinetics.transmission.sequencer.SequencedGearshiftBlock;
import com.zurrtum.create.content.redstone.DirectedDirectionalBlock;
import com.zurrtum.create.infrastructure.packet.c2s.RadialWrenchMenuSubmitPacket;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;

import java.util.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HopperBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

public class RadialWrenchMenu extends AbstractSimiScreen {

    public static final Map<Property<?>, String> VALID_PROPERTIES = new HashMap<>();

    static {
        registerRotationProperty(RotatedPillarKineticBlock.AXIS, "Axis");
        registerRotationProperty(DirectionalKineticBlock.FACING, "Facing");
        registerRotationProperty(HorizontalAxisKineticBlock.HORIZONTAL_AXIS, "Axis");
        registerRotationProperty(HorizontalKineticBlock.HORIZONTAL_FACING, "Facing");
        registerRotationProperty(HopperBlock.FACING, "Facing");
        registerRotationProperty(DirectedDirectionalBlock.TARGET, "Target");

        registerRotationProperty(SequencedGearshiftBlock.VERTICAL, "Vertical");
    }

    public static final Set<ResourceLocation> BLOCK_BLACKLIST = new HashSet<>();

    static {
        registerBlacklistedBlock(BuiltInRegistries.BLOCK.getKey(AllBlocks.LARGE_WATER_WHEEL));
        registerBlacklistedBlock(BuiltInRegistries.BLOCK.getKey(AllBlocks.WATER_WHEEL_STRUCTURAL));
    }

    public static void registerRotationProperty(Property<?> property, String label) {
        if (VALID_PROPERTIES.containsKey(property))
            return;

        VALID_PROPERTIES.put(property, label);
    }

    public static void registerBlacklistedBlock(ResourceLocation location) {
        if (BLOCK_BLACKLIST.contains(location))
            return;

        BLOCK_BLACKLIST.add(location);
    }

    private final BlockState state;
    private final BlockPos pos;
    private final BlockEntity blockEntity;
    private final Level level;
    private final NonVisualizationLevel nonVisualizationLevel;
    private final List<Map.Entry<Property<?>, String>> propertiesForState;
    private final int innerRadius = 50;
    private final int outerRadius = 110;

    private int selectedPropertyIndex = 0;
    private List<BlockState> allStates = List.of();
    private String propertyLabel = "";
    private int ticksOpen;
    private int selectedStateIndex = 0;

    private final RenderElement iconScroll = RenderElement.of(PonderGuiTextures.ICON_SCROLL);
    private final RenderElement iconUp = RenderElement.of(AllIcons.I_PRIORITY_HIGH);
    private final RenderElement iconDown = RenderElement.of(AllIcons.I_PRIORITY_LOW);

    public static Optional<RadialWrenchMenu> tryCreateFor(BlockState state, BlockPos pos, Level level) {
        if (BLOCK_BLACKLIST.contains(BuiltInRegistries.BLOCK.getKey(state.getBlock())))
            return Optional.empty();

        var propertiesForState = VALID_PROPERTIES.entrySet().stream().filter(entry -> state.hasProperty(entry.getKey())).toList();

        if (propertiesForState.isEmpty())
            return Optional.empty();

        return Optional.of(new RadialWrenchMenu(state, pos, level, propertiesForState));
    }

    private RadialWrenchMenu(BlockState state, BlockPos pos, Level level, List<Map.Entry<Property<?>, String>> properties) {
        this.state = state;
        this.pos = pos;
        this.level = level;
        this.nonVisualizationLevel = new NonVisualizationLevel(level);
        this.blockEntity = level.getBlockEntity(pos);
        this.propertiesForState = properties;

        initForSelectedProperty();
    }

    private void initForSelectedProperty() {
        Map.Entry<Property<?>, String> entry = propertiesForState.get(selectedPropertyIndex);

        allStates = new ArrayList<>();
        //allStates.add(state);
        cycleAllPropertyValues(state, entry.getKey(), allStates);

        propertyLabel = entry.getValue();
    }

    private void cycleAllPropertyValues(BlockState state, Property<?> property, List<BlockState> states) {
        Optional<? extends Comparable<?>> first = property.getPossibleValues().stream().findFirst();
        if (first.isEmpty())
            return;

        int offset = 0;
        int safety = 100;
        while (safety-- > 0) {
            if (state.getValue(property).equals(first.get())) {
                offset = 99 - safety;
                break;
            }

            state = state.cycle(property);
        }

        safety = 100;
        while (safety-- > 0) {
            if (states.contains(state))
                break;

            states.add(state);

            state = state.cycle(property);
        }

        offset = Mth.clamp(offset, 0, states.size() - 1);
        selectedStateIndex = (offset == 0) ? 0 : (states.size() - offset);
    }

    @Override
    public void tick() {
        ticksOpen++;
        if (minecraft != null && !level.getBlockState(pos).is(state.getBlock())) {
            minecraft.setScreen(null);
        }
        super.tick();
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        int x = this.width / 2;
        int y = this.height / 2;

        Matrix3x2fStack ms = graphics.pose();

        ms.pushMatrix();
        ms.translate(x, y);

        int mouseOffsetX = mouseX - this.width / 2;
        int mouseOffsetY = mouseY - this.height / 2;

        if (Mth.length(mouseOffsetX, mouseOffsetY) > innerRadius - 5) {
            double theta = Mth.atan2(mouseOffsetX, mouseOffsetY);

            float sectorSize = 360f / allStates.size();

            selectedStateIndex = (int) Math.floor(((-AngleHelper.deg(Mth.atan2(
                mouseOffsetX,
                mouseOffsetY
            )) + 180 + sectorSize / 2) % 360) / sectorSize);

            renderDirectionIndicator(graphics, theta);
        }

        renderRadialSectors(graphics);

        UIRenderHelper.streak(graphics, 0, 0, 0, 32, 65, Color.BLACK.setAlpha(0.8f));
        UIRenderHelper.streak(graphics, 180, 0, 0, 32, 65, Color.BLACK.setAlpha(0.8f));

        if (selectedPropertyIndex > 0) {
            iconScroll.at(-14, -46).render(graphics);
            iconUp.at(-1, -46).render(graphics);
            graphics.drawCenteredString(
                font,
                propertiesForState.get(selectedPropertyIndex - 1).getValue(),
                0,
                -30,
                UIRenderHelper.COLOR_TEXT.getFirst().getRGB()
            );
        }

        if (selectedPropertyIndex < propertiesForState.size() - 1) {
            iconScroll.at(-14, 30).render(graphics);
            iconDown.at(-1, 30).render(graphics);
            graphics.drawCenteredString(
                font,
                propertiesForState.get(selectedPropertyIndex + 1).getValue(),
                0,
                22,
                UIRenderHelper.COLOR_TEXT.getFirst().getRGB()
            );
        }

        graphics.drawCenteredString(font, "Currently", 0, -13, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
        graphics.drawCenteredString(font, "Changing:", 0, -3, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());
        graphics.drawCenteredString(font, propertyLabel, 0, 7, UIRenderHelper.COLOR_TEXT.getFirst().getRGB());

        ms.popMatrix();

    }

    private void renderRadialSectors(GuiGraphics graphics) {
        int sectors = allStates.size();
        if (sectors < 2)
            return;

        Matrix3x2fStack poseStack = graphics.pose();
        LocalPlayer player = minecraft.player;
        if (player == null)
            return;

        float sectorAngle = 360f / sectors;
        int sectorWidth = outerRadius - innerRadius;

        poseStack.pushMatrix();

        for (int i = 0; i < sectors; i++) {
            Color innerColor = Color.WHITE.setAlpha(0.05f);
            Color outerColor = Color.WHITE.setAlpha(0.3f);
            BlockState blockState = allStates.get(i);
            Property<?> property = propertiesForState.get(selectedPropertyIndex).getKey();

            poseStack.pushMatrix();

            if (i == selectedStateIndex) {
                innerColor.mixWith(new Color(0.8f, 0.8f, 0.2f, 0.2f), 0.5f);
                outerColor.mixWith(new Color(0.8f, 0.8f, 0.2f, 0.6f), 0.5f);

                UIRenderHelper.drawRadialSector(
                    graphics,
                    outerRadius + 2,
                    outerRadius + 3,
                    -(sectorAngle / 2 + 90),
                    sectorAngle,
                    outerColor,
                    outerColor
                );
            }

            UIRenderHelper.drawRadialSector(graphics, innerRadius, outerRadius, -(sectorAngle / 2 + 90), sectorAngle, innerColor, outerColor);
            Color c = innerColor.copy().setAlpha(0.5f);
            UIRenderHelper.drawRadialSector(graphics, innerRadius - 3, innerRadius - 2, -(sectorAngle / 2 + 90), sectorAngle, c, c);

            poseStack.translate(0, -(sectorWidth / 2f + innerRadius));
            poseStack.rotate(Mth.DEG_TO_RAD * (-i * sectorAngle));

            graphics.guiRenderState.submitPicturesInPictureState(EntityBlockRenderState.create(
                i,
                graphics,
                nonVisualizationLevel,
                pos,
                blockEntity,
                blockState,
                -21,
                -21,
                1.5f,
                17,
                player.getXRot(),
                player.getYRot() + 180,
                0
            ));

            if (i == selectedStateIndex) {
                graphics.drawCenteredString(
                    font,
                    blockState.getValue(property).toString(),
                    0,
                    15,
                    UIRenderHelper.COLOR_TEXT.getFirst().getRGB()
                );
            }

            poseStack.popMatrix();

            poseStack.pushMatrix();

            poseStack.rotate(Mth.DEG_TO_RAD * sectorAngle / 2);

            poseStack.translate(0, -innerRadius - 20);

            UIRenderHelper.angledGradient(graphics, -90, 0, 0, 0.5f, sectorWidth - 10, Color.WHITE.setAlpha(0.5f), Color.WHITE.setAlpha(0.15f));
            UIRenderHelper.angledGradient(graphics, 90, 0, 0, 0.5f, 25, Color.WHITE.setAlpha(0.5f), Color.WHITE.setAlpha(0.15f));
            poseStack.popMatrix();

            poseStack.rotate(Mth.DEG_TO_RAD * sectorAngle);
        }

        poseStack.popMatrix();

    }

    private void renderDirectionIndicator(GuiGraphics graphics, double theta) {
        Matrix3x2fStack poseStack = graphics.pose();
        poseStack.pushMatrix();
        poseStack.rotate((float) -theta);
        poseStack.translate(0, innerRadius + 3);
        graphics.guiRenderState.submitGuiElement(new DirectionIndicatorRenderState(new Matrix3x2f(poseStack), 0.8f, 0.8f, 0.8f));
        poseStack.popMatrix();
    }

    private void submitChange() {
        BlockState selectedState = allStates.get(selectedStateIndex);
        if (selectedState != state) {
            minecraft.player.connection.send(new RadialWrenchMenuSubmitPacket(pos, selectedState));
        }
        onClose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Color color = BACKGROUND_COLOR.scaleAlpha(Math.min(1, (ticksOpen + AnimationTickHolder.getPartialTicks()) / 20f));

        guiGraphics.fillGradient(0, 0, this.width, this.height, color.getRGB(), color.getRGB());
    }

    @Override
    public boolean keyReleased(KeyEvent input) {
        if (AllKeys.ROTATE_MENU.matches(input)) {
            submitChange();
            return true;
        }
        return super.keyReleased(input);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        int button = click.button();
        if (button == 0) {
            submitChange();
            return true;
        } else if (button == 1) {
            onClose();
            return true;
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (propertiesForState.size() < 2)
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);

        int indexDelta = (int) Math.round(Math.signum(-scrollY));

        int newIndex = selectedPropertyIndex + indexDelta;
        if (newIndex < 0)
            return false;

        if (newIndex >= propertiesForState.size())
            return false;

        selectedPropertyIndex = newIndex;
        initForSelectedProperty();

        return true;
    }

    @Override
    public void removed() {
        RadialWrenchHandler.COOLDOWN = 2;
        for (int i = 0, size = allStates.size(); i < size; i++) {
            EntityBlockRenderer.clear(i);
        }

        super.removed();
    }
}
