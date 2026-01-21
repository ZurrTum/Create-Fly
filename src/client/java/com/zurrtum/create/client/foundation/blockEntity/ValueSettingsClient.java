package com.zurrtum.create.client.foundation.blockEntity;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.catnip.gui.ScreenOpener;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueSettingsBehaviour;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.ValueSettingsInputHandler;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.infrastructure.packet.c2s.ValueSettingsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class ValueSettingsClient {
    public int interactHeldTicks = -1;
    public @Nullable BlockPos interactHeldPos = null;
    public @Nullable BehaviourType<? extends BlockEntityBehaviour<?>> interactHeldBehaviour = null;
    public @Nullable InteractionHand interactHeldHand = null;
    public @Nullable Direction interactHeldFace = null;

    public @Nullable List<MutableComponent> lastHoverTip;
    public int hoverTicks;
    public int hoverWarmup;

    public boolean cancelIfWarmupAlreadyStarted(BlockPos pos) {
        return interactHeldTicks != -1 && pos.equals(interactHeldPos);
    }

    public void startInteractionWith(
        BlockPos pos,
        BehaviourType<? extends BlockEntityBehaviour<?>> behaviourType,
        InteractionHand hand,
        Direction side
    ) {
        interactHeldTicks = 0;
        interactHeldPos = pos;
        interactHeldBehaviour = behaviourType;
        interactHeldHand = hand;
        interactHeldFace = side;
    }

    public void cancelInteraction() {
        interactHeldTicks = -1;
    }

    public void tick(Minecraft mc) {
        if (hoverWarmup > 0)
            hoverWarmup--;
        if (hoverTicks > 0)
            hoverTicks--;
        if (interactHeldTicks == -1)
            return;
        LocalPlayer player = mc.player;

        if (!ValueSettingsInputHandler.canInteract(player) || player.getMainHandItem().is(AllItems.CLIPBOARD)) {
            cancelInteraction();
            return;
        }
        HitResult hitResult = mc.hitResult;
        if (!(hitResult instanceof BlockHitResult blockHitResult) || !blockHitResult.getBlockPos().equals(interactHeldPos)) {
            cancelInteraction();
            return;
        }
        BlockEntityBehaviour<?> behaviour = BlockEntityBehaviour.get(mc.level, interactHeldPos, interactHeldBehaviour);
        if (!(behaviour instanceof ValueSettingsBehaviour valueSettingBehaviour) || valueSettingBehaviour.bypassesInput(player.getMainHandItem()) || !valueSettingBehaviour.testHit(
            blockHitResult.getLocation())) {
            cancelInteraction();
            return;
        }
        if (!mc.options.keyUse.isDown()) {
            player.connection.send(new ValueSettingsPacket(
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
            player.swinging = false;
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

    public void showHoverTip(Minecraft mc, List<MutableComponent> tip) {
        if (mc.screen != null)
            return;
        if (hoverWarmup < 6) {
            hoverWarmup += 2;
            return;
        } else
            hoverWarmup++;
        hoverTicks = hoverTicks == 0 ? 11 : Math.max(hoverTicks, 6);
        lastHoverTip = tip;
    }

    public void render(Minecraft mc, GuiGraphics guiGraphics) {
        if (!ValueSettingsInputHandler.canInteract(mc.player))
            return;
        if (hoverTicks == 0 || lastHoverTip == null)
            return;

        int x = guiGraphics.guiWidth() / 2;
        int y = guiGraphics.guiHeight() - 75 - lastHoverTip.size() * 12;
        float alpha = hoverTicks > 5 ? (11 - hoverTicks) / 5f : Math.min(1, hoverTicks / 5f);

        Color color = new Color(0xffffff);
        Color titleColor = new Color(0xFBDC7D);
        color.setAlpha(alpha);
        titleColor.setAlpha(alpha);

        for (int i = 0; i < lastHoverTip.size(); i++) {
            MutableComponent mutableComponent = lastHoverTip.get(i);
            guiGraphics.drawString(
                mc.font,
                mutableComponent,
                x - mc.font.width(mutableComponent) / 2,
                y,
                (i == 0 ? titleColor : color).getRGB(),
                true
            );
            y += 12;
        }
    }

}
