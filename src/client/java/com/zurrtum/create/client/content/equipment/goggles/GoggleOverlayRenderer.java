package com.zurrtum.create.client.content.equipment.goggles;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.equipment.goggles.IProxyHoveringInformation;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.api.goggles.IHaveCustomOverlayIcon;
import com.zurrtum.create.client.api.goggles.IHaveGoggleInformation;
import com.zurrtum.create.client.api.goggles.IHaveHoveringInformation;
import com.zurrtum.create.client.catnip.gui.element.BoxElement;
import com.zurrtum.create.client.catnip.outliner.Outline;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.catnip.outliner.Outliner.OutlineEntry;
import com.zurrtum.create.client.content.contraptions.IDisplayAssemblyExceptions;
import com.zurrtum.create.client.content.trains.entity.TrainRelocatorClient;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueBox;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.tooltip.TooltipBehaviour;
import com.zurrtum.create.client.foundation.gui.RemovedGuiUtils;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.client.infrastructure.config.AllConfigs;
import com.zurrtum.create.client.infrastructure.config.CClient;
import com.zurrtum.create.compat.Mods;
import com.zurrtum.create.content.contraptions.piston.MechanicalPistonBlock;
import com.zurrtum.create.content.contraptions.piston.PistonExtensionPoleBlock;
import com.zurrtum.create.content.equipment.goggles.GogglesItem;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.StringVisitable;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GoggleOverlayRenderer {

    private static final Map<Object, OutlineEntry> outlines = Outliner.getInstance().getOutlines();

    public static int hoverTicks = 0;
    public static BlockPos lastHovered = null;

    public static void renderOverlay(MinecraftClient mc, DrawContext guiGraphics, RenderTickCounter deltaTracker) {
        HitResult objectMouseOver = mc.crosshairTarget;
        if (!(objectMouseOver instanceof BlockHitResult result)) {
            lastHovered = null;
            hoverTicks = 0;
            return;
        }

        for (OutlineEntry entry : outlines.values()) {
            if (!entry.isAlive())
                continue;
            Outline outline = entry.getOutline();
            if (outline instanceof ValueBox && !((ValueBox) outline).isPassive)
                return;
        }

        ClientWorld world = mc.world;
        BlockPos pos = result.getBlockPos();

        int prevHoverTicks = hoverTicks;
        hoverTicks++;
        lastHovered = pos;

        pos = proxiedOverlayPosition(world, pos);

        TooltipBehaviour<?> be = BlockEntityBehaviour.get(world, pos, TooltipBehaviour.TYPE);
        boolean wearingGoggles = GogglesItem.isWearingGoggles(mc.player);

        boolean isShifting = mc.player.isSneaking();

        boolean hasGoggleInformation = be instanceof IHaveGoggleInformation;
        boolean hasHoveringInformation = be instanceof IHaveHoveringInformation;

        boolean goggleAddedInformation = false;
        boolean hoverAddedInformation = false;

        ItemStack item = new ItemStack(AllItems.GOGGLES);
        List<Text> tooltip = new ArrayList<>();

        if (be instanceof IHaveCustomOverlayIcon customOverlayIcon)
            item = customOverlayIcon.getIcon(isShifting);

        if (hasGoggleInformation && wearingGoggles) {
            IHaveGoggleInformation gte = (IHaveGoggleInformation) be;
            goggleAddedInformation = gte.addToGoggleTooltip(tooltip, isShifting);
        }

        if (hasHoveringInformation) {
            if (!tooltip.isEmpty())
                tooltip.add(ScreenTexts.EMPTY);
            IHaveHoveringInformation hte = (IHaveHoveringInformation) be;
            hoverAddedInformation = hte.addToTooltip(tooltip, isShifting);

            if (goggleAddedInformation && !hoverAddedInformation)
                tooltip.remove(tooltip.size() - 1);
        }

        if (be instanceof IDisplayAssemblyExceptions) {
            boolean exceptionAdded = ((IDisplayAssemblyExceptions) be).addExceptionToTooltip(tooltip);
            if (exceptionAdded) {
                hasHoveringInformation = true;
                hoverAddedInformation = true;
            }
        }

        if (!hasHoveringInformation)
            if (hasHoveringInformation = hoverAddedInformation = TrainRelocatorClient.addToTooltip(tooltip))
                hoverTicks = prevHoverTicks + 1;

        // break early if goggle or hover returned false when present
        if ((hasGoggleInformation && !goggleAddedInformation) && (hasHoveringInformation && !hoverAddedInformation)) {
            hoverTicks = 0;
            return;
        }

        // check for piston poles if goggles are worn
        BlockState state = world.getBlockState(pos);
        if (wearingGoggles && state.isOf(AllBlocks.PISTON_EXTENSION_POLE)) {
            Direction[] directions = Iterate.directionsInAxis(state.get(PistonExtensionPoleBlock.FACING).getAxis());
            int poles = 1;
            boolean pistonFound = false;
            for (Direction dir : directions) {
                int attachedPoles = PistonExtensionPoleBlock.PlacementHelper.get().attachedPoles(world, pos, dir);
                poles += attachedPoles;
                pistonFound |= world.getBlockState(pos.offset(dir, attachedPoles + 1)).getBlock() instanceof MechanicalPistonBlock;
            }

            if (!pistonFound) {
                hoverTicks = 0;
                return;
            }
            if (!tooltip.isEmpty())
                tooltip.add(ScreenTexts.EMPTY);

            CreateLang.translate("gui.goggles.pole_length").text(" " + poles).forGoggles(tooltip);
        }

        if (tooltip.isEmpty()) {
            hoverTicks = 0;
            return;
        }

        Matrix3x2fStack poseStack = guiGraphics.getMatrices();
        poseStack.pushMatrix();

        int tooltipTextWidth = 0;
        for (StringVisitable textLine : tooltip) {
            int textLineWidth = mc.textRenderer.getWidth(textLine);
            if (textLineWidth > tooltipTextWidth)
                tooltipTextWidth = textLineWidth;
        }

        int tooltipHeight = 8;
        if (tooltip.size() > 1) {
            tooltipHeight += 2; // gap between title lines and next lines
            tooltipHeight += (tooltip.size() - 1) * 10;
        }

        int width = guiGraphics.getScaledWindowWidth();
        int height = guiGraphics.getScaledWindowHeight();

        CClient cfg = AllConfigs.client();
        int posX = width / 2 + cfg.overlayOffsetX.get();
        int posY = height / 2 + cfg.overlayOffsetY.get();

        posX = Math.min(posX, width - tooltipTextWidth - 20);
        posY = Math.min(posY, height - tooltipHeight - 20);

        float fade = MathHelper.clamp((hoverTicks + deltaTracker.getTickProgress(false)) / 24f, 0, 1);
        Boolean useCustom = cfg.overlayCustomColor.get();
        Color colorBackground = useCustom ? new Color(cfg.overlayBackgroundColor.get()) : BoxElement.COLOR_VANILLA_BACKGROUND.scaleAlpha(.75f);
        Color colorBorderTop = useCustom ? new Color(cfg.overlayBorderColorTop.get()) : BoxElement.COLOR_VANILLA_BORDER.getFirst().copy();
        Color colorBorderBot = useCustom ? new Color(cfg.overlayBorderColorBot.get()) : BoxElement.COLOR_VANILLA_BORDER.getSecond().copy();

        if (fade < 1) {
            poseStack.translate((float) (Math.pow(1 - fade, 3) * Math.signum(cfg.overlayOffsetX.get() + .5f) * 8), 0);
            colorBackground.scaleAlpha(fade);
            colorBorderTop.scaleAlpha(fade);
            colorBorderBot.scaleAlpha(fade);
        }

        if (!Mods.MODERNUI.isLoaded()) {
            // default tooltip rendering when modernUI is not loaded
            RemovedGuiUtils.drawHoveringText(
                guiGraphics,
                tooltip,
                posX,
                posY,
                width,
                height,
                -1,
                colorBackground.getRGB(),
                colorBorderTop.getRGB(),
                colorBorderBot.getRGB(),
                mc.textRenderer
            );
            guiGraphics.drawItem(item, posX + 10, posY - 16);

            poseStack.popMatrix();

            return;
        }

        /*
         * special handling for modernUI
         *
         * their tooltip handler causes the overlay to jiggle each frame,
         * if the mouse is moving, guiScale is anything but 1 and exactPositioning is enabled
         *
         * this is a workaround to fix this behavior
         */
        Mouse mouseHandler = mc.mouse;
        Window window = mc.getWindow();
        double guiScale = window.getScaleFactor();
        double cursorX = mouseHandler.getX();
        double cursorY = mouseHandler.getY();
        mouseHandler.x = Math.round(cursorX / guiScale) * guiScale;
        mouseHandler.y = Math.round(cursorY / guiScale) * guiScale;

        RemovedGuiUtils.drawHoveringText(
            guiGraphics,
            tooltip,
            posX,
            posY,
            width,
            height,
            -1,
            colorBackground.getRGB(),
            colorBorderTop.getRGB(),
            colorBorderBot.getRGB(),
            mc.textRenderer
        );

        guiGraphics.drawItem(item, posX + 10, posY - 16);

        mouseHandler.x = cursorX;
        mouseHandler.y = cursorY;
        poseStack.popMatrix();

    }

    public static BlockPos proxiedOverlayPosition(World level, BlockPos pos) {
        BlockState targetedState = level.getBlockState(pos);
        if (targetedState.getBlock() instanceof IProxyHoveringInformation proxy)
            return proxy.getInformationSource(level, pos, targetedState);
        return pos;
    }

}
