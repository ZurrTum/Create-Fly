package com.zurrtum.create.client.content.equipment.zapper.terrainzapper;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.content.equipment.zapper.ZapperScreen;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.AllIcons;
import com.zurrtum.create.client.foundation.gui.widget.*;
import com.zurrtum.create.client.foundation.gui.widget.Indicator.State;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.content.equipment.zapper.ConfigureZapperPacket;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.Brush;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.CylinderBrush;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.DynamicBrush;
import com.zurrtum.create.content.equipment.zapper.terrainzapper.SphereBrush;
import com.zurrtum.create.infrastructure.component.PlacementOptions;
import com.zurrtum.create.infrastructure.component.TerrainBrushes;
import com.zurrtum.create.infrastructure.component.TerrainTools;
import com.zurrtum.create.infrastructure.packet.c2s.ConfigureWorldshaperPacket;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class WorldshaperScreen extends ZapperScreen {

    protected final Text placementSection = CreateLang.translateDirect("gui.terrainzapper.placement");
    protected final Text toolSection = CreateLang.translateDirect("gui.terrainzapper.tool");
    protected final List<Text> brushOptions = CreateLang.translatedOptions(
        "gui.terrainzapper.brush",
        "cuboid",
        "sphere",
        "cylinder",
        "surface",
        "cluster"
    );

    protected List<IconButton> toolButtons;
    protected List<IconButton> placementButtons;

    protected ScrollInput brushInput;
    protected Label brushLabel;
    protected List<ScrollInput> brushParams = new ArrayList<>(3);
    protected List<Label> brushParamLabels = new ArrayList<>(3);
    protected IconButton followDiagonals;
    protected IconButton acrossMaterials;
    protected Indicator followDiagonalsIndicator;
    protected Indicator acrossMaterialsIndicator;

    protected TerrainBrushes currentBrush;
    protected int[] currentBrushParams = new int[]{1, 1, 1};
    protected boolean currentFollowDiagonals;
    protected boolean currentAcrossMaterials;
    protected TerrainTools currentTool;
    protected PlacementOptions currentPlacement;

    public WorldshaperScreen(ItemStack zapper, Hand hand) {
        super(AllGuiTextures.TERRAINZAPPER, zapper, hand);
        fontColor = 0xFF767676;
        title = zapper.getName();

        currentBrush = zapper.getOrDefault(AllDataComponents.SHAPER_BRUSH, TerrainBrushes.Cuboid);
        if (zapper.contains(AllDataComponents.SHAPER_BRUSH_PARAMS)) {
            BlockPos paramsData = zapper.get(AllDataComponents.SHAPER_BRUSH_PARAMS);
            currentBrushParams[0] = paramsData.getX();
            currentBrushParams[1] = paramsData.getY();
            currentBrushParams[2] = paramsData.getZ();
            if (currentBrushParams[1] == 0) {
                currentFollowDiagonals = true;
            }
            if (currentBrushParams[2] == 0) {
                currentAcrossMaterials = true;
            }
        }
        currentTool = zapper.getOrDefault(AllDataComponents.SHAPER_TOOL, TerrainTools.Fill);
        currentPlacement = zapper.getOrDefault(AllDataComponents.SHAPER_PLACEMENT_OPTIONS, PlacementOptions.Merged);
    }

    @Override
    protected void init() {
        super.init();

        int x = guiLeft;
        int y = guiTop;

        brushLabel = new Label(x + 61, y + 25, ScreenTexts.EMPTY).withShadow();
        brushInput = new SelectionScrollInput(x + 56, y + 20, 77, 18).forOptions(brushOptions)
            .titled(CreateLang.translateDirect("gui.terrainzapper.brush")).writingTo(brushLabel).calling(brushIndex -> {
                currentBrush = TerrainBrushes.values()[brushIndex];
                initBrushParams(x, y);
            });

        brushInput.setState(currentBrush.ordinal());

        addDrawableChild(brushLabel);
        addDrawableChild(brushInput);

        initBrushParams(x, y);
    }

    public AllIcons getIcon(TerrainTools tool) {
        return switch (tool) {
            case Fill -> AllIcons.I_FILL;
            case Place -> AllIcons.I_PLACE;
            case Replace -> AllIcons.I_REPLACE;
            case Clear -> AllIcons.I_CLEAR;
            case Overlay -> AllIcons.I_OVERLAY;
            case Flatten -> AllIcons.I_FLATTEN;
        };
    }

    public AllIcons getIcon(PlacementOptions option) {
        return switch (option) {
            case Merged -> AllIcons.I_CENTERED;
            case Attached -> AllIcons.I_ATTACHED;
            case Inserted -> AllIcons.I_INSERTED;
        };
    }

    public static Text getParamLabel(Brush brush, int paramIndex) {
        return switch (brush) {
            case DynamicBrush b -> CreateLang.translateDirect("generic.range");
            case SphereBrush b -> CreateLang.translateDirect("generic.radius");
            case CylinderBrush b ->
                paramIndex == 0 ? CreateLang.translateDirect("generic.radius") : CreateLang.translateDirect(paramIndex == 1 ? "generic.height" : "generic.length");
            default -> CreateLang.translateDirect(paramIndex == 0 ? "generic.width" : paramIndex == 1 ? "generic.height" : "generic.length");
        };
    }

    protected void initBrushParams(int x, int y) {
        Brush currentBrush = this.currentBrush.get();

        // Brush Params

        removeWidgets(brushParamLabels);
        removeWidgets(brushParams);

        brushParamLabels.clear();
        brushParams.clear();

        for (int index = 0; index < 3; index++) {
            Label label = new Label(x + 65 + 20 * index, y + 45, ScreenTexts.EMPTY).withShadow();

            final int finalIndex = index;
            ScrollInput input = new ScrollInput(x + 56 + 20 * index, y + 40, 18, 18).withRange(
                currentBrush.getMin(index),
                currentBrush.getMax(index) + 1
            ).writingTo(label).titled(getParamLabel(currentBrush, index).copyContentOnly()).calling(state -> {
                currentBrushParams[finalIndex] = state;
                label.setX(x + 65 + 20 * finalIndex - textRenderer.getWidth(label.text) / 2);
            });
            input.setState(currentBrushParams[index]);
            input.onChanged();

            if (index >= currentBrush.amtParams) {
                input.visible = false;
                label.visible = false;
                input.active = false;
            }

            brushParamLabels.add(label);
            brushParams.add(input);
        }

        addRenderableWidgets(brushParamLabels);
        addRenderableWidgets(brushParams);

        // Connectivity Options

        if (followDiagonals != null) {
            remove(followDiagonals);
            remove(followDiagonalsIndicator);
            remove(acrossMaterials);
            remove(acrossMaterialsIndicator);
            followDiagonals = null;
            followDiagonalsIndicator = null;
            acrossMaterials = null;
            acrossMaterialsIndicator = null;
        }

        if (currentBrush.hasConnectivityOptions()) {
            int x1 = x + 7 + 4 * 18;
            int y1 = y + 79;
            followDiagonalsIndicator = new Indicator(x1, y1 - 6, ScreenTexts.EMPTY);
            followDiagonals = new IconButton(x1, y1, AllIcons.I_FOLLOW_DIAGONAL);
            x1 += 18;
            acrossMaterialsIndicator = new Indicator(x1, y1 - 6, ScreenTexts.EMPTY);
            acrossMaterials = new IconButton(x1, y1, AllIcons.I_FOLLOW_MATERIAL);

            followDiagonals.withCallback(() -> {
                followDiagonalsIndicator.state = followDiagonalsIndicator.state == State.OFF ? State.ON : State.OFF;
                currentFollowDiagonals = !currentFollowDiagonals;
            });
            followDiagonals.setToolTip(CreateLang.translateDirect("gui.terrainzapper.searchDiagonal"));
            acrossMaterials.withCallback(() -> {
                acrossMaterialsIndicator.state = acrossMaterialsIndicator.state == State.OFF ? State.ON : State.OFF;
                currentAcrossMaterials = !currentAcrossMaterials;
            });
            acrossMaterials.setToolTip(CreateLang.translateDirect("gui.terrainzapper.searchFuzzy"));
            addDrawableChild(followDiagonals);
            addDrawableChild(followDiagonalsIndicator);
            addDrawableChild(acrossMaterials);
            addDrawableChild(acrossMaterialsIndicator);
            if (currentFollowDiagonals)
                followDiagonalsIndicator.state = State.ON;
            if (currentAcrossMaterials)
                acrossMaterialsIndicator.state = State.ON;
        }

        // Tools

        if (toolButtons != null)
            removeWidgets(toolButtons);

        TerrainTools[] toolValues = currentBrush.getSupportedTools();
        toolButtons = new ArrayList<>(toolValues.length);
        for (int id = 0; id < toolValues.length; id++) {
            TerrainTools tool = toolValues[id];
            IconButton toolButton = new IconButton(x + 7 + id * 18, y + 79, getIcon(tool));
            toolButton.withCallback(() -> {
                toolButtons.forEach(b -> b.green = false);
                toolButton.green = true;
                currentTool = tool;
            });
            toolButton.setToolTip(CreateLang.translateDirect("gui.terrainzapper.tool." + tool.translationKey));
            toolButtons.add(toolButton);
        }

        int toolIndex = -1;
        for (int i = 0; i < toolValues.length; i++)
            if (currentTool == toolValues[i])
                toolIndex = i;
        if (toolIndex == -1) {
            currentTool = toolValues[0];
            toolIndex = 0;
        }
        toolButtons.get(toolIndex).green = true;

        addRenderableWidgets(toolButtons);

        // Placement Options

        if (placementButtons != null)
            removeWidgets(placementButtons);

        if (currentBrush.hasPlacementOptions()) {
            PlacementOptions[] placementValues = PlacementOptions.values();
            placementButtons = new ArrayList<>(placementValues.length);
            for (int id = 0; id < placementValues.length; id++) {
                PlacementOptions option = placementValues[id];
                IconButton placementButton = new IconButton(x + 136 + id * 18, y + 79, getIcon(option));
                placementButton.withCallback(() -> {
                    placementButtons.forEach(b -> b.green = false);
                    placementButton.green = true;
                    currentPlacement = option;
                });
                placementButton.setToolTip(CreateLang.translateDirect("gui.terrainzapper.placement." + option.translationKey));
                placementButtons.add(placementButton);
            }

            placementButtons.get(currentPlacement.ordinal()).green = true;

            addRenderableWidgets(placementButtons);
        }
    }

    @Override
    protected void drawOnBackground(DrawContext graphics, int x, int y) {
        super.drawOnBackground(graphics, x, y);

        Brush currentBrush = this.currentBrush.get();
        for (int index = 2; index >= currentBrush.amtParams; index--)
            AllGuiTextures.TERRAINZAPPER_INACTIVE_PARAM.render(graphics, x + 56 + 20 * index, y + 40);

        graphics.drawText(textRenderer, toolSection, x + 7, y + 69, fontColor, false);
        if (currentBrush.hasPlacementOptions())
            graphics.drawText(textRenderer, placementSection, x + 136, y + 69, fontColor, false);
    }

    @Override
    protected ConfigureZapperPacket getConfigurationPacket() {
        int brushParamX = currentBrushParams[0];
        int brushParamY = followDiagonalsIndicator != null ? followDiagonalsIndicator.state == State.ON ? 0 : 1 : currentBrushParams[1];
        int brushParamZ = acrossMaterialsIndicator != null ? acrossMaterialsIndicator.state == State.ON ? 0 : 1 : currentBrushParams[2];
        return new ConfigureWorldshaperPacket(
            hand,
            currentPattern,
            currentBrush,
            brushParamX,
            brushParamY,
            brushParamZ,
            currentTool,
            currentPlacement
        );
    }

}