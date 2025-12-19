package com.zurrtum.create.client.content.trains;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.client.catnip.placement.PlacementClient;
import com.zurrtum.create.client.content.contraptions.actors.trainControls.ControlsHandler;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.utility.ControlsUtil;
import com.zurrtum.create.content.contraptions.actors.trainControls.ControlsBlock;
import com.zurrtum.create.content.trains.entity.Carriage;
import com.zurrtum.create.content.trains.entity.CarriageContraptionEntity;
import com.zurrtum.create.content.trains.entity.Train;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.packet.c2s.HonkPacket;
import com.zurrtum.create.infrastructure.packet.c2s.TrainHUDUpdatePacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Matrix3x2fStack;

public class TrainHUD {
    static LerpedFloat displayedSpeed = LerpedFloat.linear();
    static LerpedFloat displayedThrottle = LerpedFloat.linear();
    static LerpedFloat displayedPromptSize = LerpedFloat.linear();

    static Double editedThrottle = null;
    static int hudPacketCooldown = 5;
    static int honkPacketCooldown = 5;

    public static Text currentPrompt;
    public static boolean currentPromptShadow;
    public static int promptKeepAlive = 0;

    static boolean usedToHonk;

    public static void tick(MinecraftClient mc) {
        if (promptKeepAlive > 0)
            promptKeepAlive--;
        else
            currentPrompt = null;

        displayedPromptSize.chase(currentPrompt != null ? mc.textRenderer.getWidth(currentPrompt) + 17 : 0, .5f, Chaser.EXP);
        displayedPromptSize.tickChaser();

        Carriage carriage = getCarriage();
        if (carriage == null)
            return;

        Train train = carriage.train;
        double value = Math.abs(train.speed) / (train.maxSpeed() * AllConfigs.server().trains.manualTrainSpeedModifier.getF());
        value = MathHelper.clamp(value + 0.05f, 0, 1);

        displayedSpeed.chase((int) (value * 18) / 18f, .5f, Chaser.EXP);
        displayedSpeed.tickChaser();
        displayedThrottle.chase(editedThrottle != null ? editedThrottle : train.throttle, .75f, Chaser.EXP);
        displayedThrottle.tickChaser();

        boolean isSprintKeyPressed = ControlsUtil.isActuallyPressed(mc.options.sprintKey);

        if (isSprintKeyPressed && honkPacketCooldown-- <= 0) {
            train.determineHonk(mc.world);
            if (train.lowHonk != null) {
                mc.player.networkHandler.sendPacket(new HonkPacket(train, true));
                honkPacketCooldown = 5;
                usedToHonk = true;
            }
        }

        if (!isSprintKeyPressed && usedToHonk) {
            mc.player.networkHandler.sendPacket(new HonkPacket(train, false));
            honkPacketCooldown = 0;
            usedToHonk = false;
        }

        if (editedThrottle == null)
            return;
        if (MathHelper.approximatelyEquals(editedThrottle, train.throttle)) {
            editedThrottle = null;
            hudPacketCooldown = 5;
            return;
        }

        if (hudPacketCooldown-- <= 0) {
            mc.player.networkHandler.sendPacket(new TrainHUDUpdatePacket(train, editedThrottle));
            hudPacketCooldown = 5;
        }
    }

    private static Carriage getCarriage() {
        if (!(ControlsHandler.getContraption() instanceof CarriageContraptionEntity cce))
            return null;
        return cce.getCarriage();
    }

    public static boolean renderOverlay(MinecraftClient mc, DrawContext guiGraphics, RenderTickCounter deltaTracker) {
        float partialTicks = deltaTracker.getTickProgress(false);
        if (!(ControlsHandler.getContraption() instanceof CarriageContraptionEntity cce))
            return false;
        Carriage carriage = cce.getCarriage();
        if (carriage == null)
            return false;
        Entity cameraEntity = mc.getCameraEntity();
        if (cameraEntity == null)
            return false;
        BlockPos localPos = ControlsHandler.getControlsPos();
        if (localPos == null)
            return false;

        Matrix3x2fStack poseStack = guiGraphics.getMatrices();
        poseStack.pushMatrix();
        poseStack.translate(guiGraphics.getScaledWindowWidth() / 2 - 91, guiGraphics.getScaledWindowHeight() - 29);

        // Speed, Throttle

        AllGuiTextures.TRAIN_HUD_FRAME.render(guiGraphics, -2, 1);
        AllGuiTextures.TRAIN_HUD_SPEED_BG.render(guiGraphics, 0, 0);

        int w = (int) (AllGuiTextures.TRAIN_HUD_SPEED.getWidth() * displayedSpeed.getValue(partialTicks));
        int h = AllGuiTextures.TRAIN_HUD_SPEED.getHeight();

        guiGraphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            AllGuiTextures.TRAIN_HUD_SPEED.location,
            0,
            0,
            AllGuiTextures.TRAIN_HUD_SPEED.getStartX(),
            AllGuiTextures.TRAIN_HUD_SPEED.getStartY(),
            w,
            h,
            256,
            256
        );

        int promptSize = (int) displayedPromptSize.getValue(partialTicks);
        if (promptSize > 1) {

            poseStack.pushMatrix();
            poseStack.translate(promptSize / -2f + 91, -27);

            AllGuiTextures.TRAIN_PROMPT_L.render(guiGraphics, -3, 0);
            AllGuiTextures.TRAIN_PROMPT_R.render(guiGraphics, promptSize, 0);
            guiGraphics.drawTexture(
                RenderPipelines.GUI_TEXTURED,
                AllGuiTextures.TRAIN_PROMPT.location,
                0,
                0,
                AllGuiTextures.TRAIN_PROMPT.getStartX() + (128 - promptSize / 2f),
                AllGuiTextures.TRAIN_PROMPT.getStartY(),
                promptSize,
                AllGuiTextures.TRAIN_PROMPT.getHeight(),
                256,
                256
            );

            poseStack.popMatrix();

            TextRenderer font = mc.textRenderer;
            if (currentPrompt != null && font.getWidth(currentPrompt) < promptSize - 10) {
                poseStack.pushMatrix();
                poseStack.translate(font.getWidth(currentPrompt) / -2f + 82, -27);
                guiGraphics.drawText(font, currentPrompt, 9, 4, 0xFF544D45, currentPromptShadow);
                poseStack.popMatrix();
            }
        }

        AllGuiTextures.TRAIN_HUD_DIRECTION.render(guiGraphics, 77, -20);

        w = (int) (AllGuiTextures.TRAIN_HUD_THROTTLE.getWidth() * (1 - displayedThrottle.getValue(partialTicks)));
        int invW = AllGuiTextures.TRAIN_HUD_THROTTLE.getWidth() - w;
        guiGraphics.drawTexture(
            RenderPipelines.GUI_TEXTURED,
            AllGuiTextures.TRAIN_HUD_THROTTLE.location,
            invW,
            0,
            AllGuiTextures.TRAIN_HUD_THROTTLE.getStartX() + invW,
            AllGuiTextures.TRAIN_HUD_THROTTLE.getStartY(),
            w,
            h,
            256,
            256
        );
        AllGuiTextures.TRAIN_HUD_THROTTLE_POINTER.render(guiGraphics, Math.max(1, AllGuiTextures.TRAIN_HUD_THROTTLE.getWidth() - w) - 3, -2);

        // Direction

        StructureBlockInfo info = cce.getContraption().getBlocks().get(localPos);
        Direction initialOrientation = cce.getInitialOrientation().rotateYCounterclockwise();
        boolean inverted = false;
        if (info != null && info.state().contains(ControlsBlock.FACING))
            inverted = !info.state().get(ControlsBlock.FACING).equals(initialOrientation);

        boolean reversing = ControlsHandler.currentlyPressed.contains(1);
        inverted ^= reversing;
        int angleOffset = (ControlsHandler.currentlyPressed.contains(2) ? -45 : 0) + (ControlsHandler.currentlyPressed.contains(3) ? 45 : 0);
        if (reversing)
            angleOffset *= -1;

        float snapSize = 22.5f;
        float diff = AngleHelper.getShortestAngleDiff(cameraEntity.getYaw(), cce.yaw) + (inverted ? -90 : 90);
        if (Math.abs(diff) < 60)
            diff = 0;

        float angle = diff + angleOffset;
        float snappedAngle = (snapSize * Math.round(angle / snapSize)) % 360f;

        poseStack.translate(91, -9);
        poseStack.scale(0.925f, 0.925f);
        PlacementClient.textured(guiGraphics, 0, 0, 1, snappedAngle);

        poseStack.popMatrix();
        return true;
    }

    public static boolean onScroll(double delta) {
        Carriage carriage = getCarriage();
        if (carriage == null)
            return false;

        double prevThrottle = editedThrottle == null ? carriage.train.throttle : editedThrottle;
        editedThrottle = MathHelper.clamp(prevThrottle + (delta > 0 ? 1 : -1) / 18f, 1 / 18f, 1);
        return true;
    }

}
