package com.zurrtum.create.client.foundation.blockEntity;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.infrastructure.packet.c2s.ValueSettingsPacket;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class ValueSettingsClient {
    public int interactHeldTicks = -1;
    public BlockPos interactHeldPos = null;
    public BehaviourType<? extends BlockEntityBehaviour<?>> interactHeldBehaviour = null;
    public Hand interactHeldHand = null;
    public Direction interactHeldFace = null;

    public List<MutableText> lastHoverTip;
    public int hoverTicks;
    public int hoverWarmup;

    public boolean cancelIfWarmupAlreadyStarted(BlockPos pos) {
        return interactHeldTicks != -1 && pos.equals(interactHeldPos);
    }

    public void startInteractionWith(BlockPos pos, BehaviourType<? extends BlockEntityBehaviour<?>> behaviourType, Hand hand, Direction side) {
        interactHeldTicks = 0;
        interactHeldPos = pos;
        interactHeldBehaviour = behaviourType;
        interactHeldHand = hand;
        interactHeldFace = side;
    }

    public void cancelInteraction() {
        interactHeldTicks = -1;
    }

    public void tick(MinecraftClient mc) {
        if (hoverWarmup > 0)
            hoverWarmup--;
        if (hoverTicks > 0)
            hoverTicks--;
        if (interactHeldTicks == -1)
            return;
        ClientPlayerEntity player = mc.player;

        if (!ValueSettingsInputHandler.canInteract(player) || player.getMainHandStack().isOf(AllItems.CLIPBOARD)) {
            cancelInteraction();
            return;
        }
        HitResult hitResult = mc.crosshairTarget;
        if (!(hitResult instanceof BlockHitResult blockHitResult) || !blockHitResult.getBlockPos().equals(interactHeldPos)) {
            cancelInteraction();
            return;
        }
        BlockEntityBehaviour<?> behaviour = BlockEntityBehaviour.get(mc.world, interactHeldPos, interactHeldBehaviour);
        if (!(behaviour instanceof ValueSettingsBehaviour valueSettingBehaviour) || valueSettingBehaviour.bypassesInput(player.getMainHandStack()) || !valueSettingBehaviour.testHit(
            blockHitResult.getPos())) {
            cancelInteraction();
            return;
        }
        if (!mc.options.useKey.isPressed()) {
            player.networkHandler.sendPacket(new ValueSettingsPacket(
                interactHeldPos,
                0,
                0,
                interactHeldHand,
                blockHitResult,
                interactHeldFace,
                false,
                valueSettingBehaviour.netId()
            ));
            valueSettingBehaviour.onShortInteract(player, interactHeldHand, interactHeldFace, blockHitResult);
            cancelInteraction();
            return;
        }

        if (interactHeldTicks > 3)
            player.handSwinging = false;
        if (interactHeldTicks++ < 5)
            return;
        ScreenOpener.open(new ValueSettingsScreen(
            interactHeldPos,
            valueSettingBehaviour.createBoard(player, blockHitResult),
            valueSettingBehaviour.getValueSettings(),
            valueSettingBehaviour::newSettingHovered,
            valueSettingBehaviour.netId()
        ));
        interactHeldTicks = -1;
    }

    public void showHoverTip(MinecraftClient mc, List<MutableText> tip) {
        if (mc.currentScreen != null)
            return;
        if (hoverWarmup < 6) {
            hoverWarmup += 2;
            return;
        } else
            hoverWarmup++;
        hoverTicks = hoverTicks == 0 ? 11 : Math.max(hoverTicks, 6);
        lastHoverTip = tip;
    }

    public void render(MinecraftClient mc, DrawContext guiGraphics) {
        if (!ValueSettingsInputHandler.canInteract(mc.player))
            return;
        if (hoverTicks == 0 || lastHoverTip == null)
            return;

        int x = guiGraphics.getScaledWindowWidth() / 2;
        int y = guiGraphics.getScaledWindowHeight() - 75 - lastHoverTip.size() * 12;
        float alpha = hoverTicks > 5 ? (11 - hoverTicks) / 5f : Math.min(1, hoverTicks / 5f);

        Color color = new Color(0xffffff);
        Color titleColor = new Color(0xFBDC7D);
        color.setAlpha(alpha);
        titleColor.setAlpha(alpha);

        for (int i = 0; i < lastHoverTip.size(); i++) {
            MutableText mutableComponent = lastHoverTip.get(i);
            guiGraphics.drawText(
                mc.textRenderer,
                mutableComponent,
                x - mc.textRenderer.getWidth(mutableComponent) / 2,
                y,
                (i == 0 ? titleColor : color).getRGB(),
                true
            );
            y += 12;
        }
    }

}
