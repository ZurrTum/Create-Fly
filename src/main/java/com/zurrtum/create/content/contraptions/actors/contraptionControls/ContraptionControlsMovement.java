package com.zurrtum.create.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.catnip.animation.LerpedFloat.Chaser;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.data.IntAttached;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import com.zurrtum.create.content.contraptions.elevator.ElevatorContraption;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

public class ContraptionControlsMovement extends MovementBehaviour {

    @Override
    public ItemStack canBeDisabledVia(MovementContext context) {
        return null;
    }

    @Override
    public void startMoving(MovementContext context) {
        if (context.contraption instanceof ElevatorContraption && context.blockEntityData != null)
            context.blockEntityData.remove("Filter");
    }

    @Override
    public void stopMoving(MovementContext context) {
        ItemStack filter = getFilter(context);
        if (filter != null)
            context.blockEntityData.putBoolean(
                "Disabled",
                context.contraption.isActorTypeDisabled(filter) || context.contraption.isActorTypeDisabled(ItemStack.EMPTY)
            );
    }

    public static boolean isSameFilter(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() && stack2.isEmpty())
            return true;
        return ItemStack.areItemsAndComponentsEqual(stack1, stack2);
    }

    public static ItemStack getFilter(MovementContext ctx) {
        NbtCompound blockEntityData = ctx.blockEntityData;
        if (blockEntityData == null)
            return null;
        RegistryOps<NbtElement> ops = ctx.world.getRegistryManager().getOps(NbtOps.INSTANCE);
        return blockEntityData.get("Filter", ItemStack.OPTIONAL_CODEC, ops).orElse(ItemStack.EMPTY);
    }

    public static boolean isDisabledInitially(MovementContext ctx) {
        return ctx.blockEntityData != null && ctx.blockEntityData.getBoolean("Disabled", false);
    }

    @Override
    public void tick(MovementContext ctx) {
        if (!ctx.world.isClient())
            return;

        Contraption contraption = ctx.contraption;
        BlockEntity blockEntity = AllClientHandle.INSTANCE.getBlockEntityClientSide(contraption, ctx.localPos);

        if (!(contraption instanceof ElevatorContraption ec)) {
            if (!(blockEntity instanceof ContraptionControlsBlockEntity cbe))
                return;
            ItemStack filter = getFilter(ctx);
            int value = contraption.isActorTypeDisabled(filter) || contraption.isActorTypeDisabled(ItemStack.EMPTY) ? 4 * 45 : 0;
            cbe.indicator.setValue(value);
            cbe.indicator.updateChaseTarget(value);
            cbe.tickAnimations();
            return;
        }

        if (!(ctx.temporaryData instanceof ElevatorFloorSelection))
            ctx.temporaryData = new ElevatorFloorSelection();

        ElevatorFloorSelection efs = (ElevatorFloorSelection) ctx.temporaryData;
        tickFloorSelection(efs, ec);

        if (!(blockEntity instanceof ContraptionControlsBlockEntity cbe))
            return;

        cbe.tickAnimations();

        int currentY = (int) Math.round(contraption.entity.getY() + ec.getContactYOffset());
        boolean atTargetY = ec.clientYTarget == currentY;

        LerpedFloat indicator = cbe.indicator;
        float currentIndicator = indicator.getChaseTarget();
        boolean below = atTargetY ? currentIndicator > 0 : ec.clientYTarget <= currentY;

        if (currentIndicator == 0 && !atTargetY) {
            int startingPoint = below ? 181 : -181;
            indicator.setValue(startingPoint);
            indicator.updateChaseTarget(startingPoint);
            cbe.tickAnimations();
            return;
        }

        int currentStage = MathHelper.floor(((currentIndicator % 360) + 360) % 360);
        if (!atTargetY || currentStage / 45 != 0) {
            float increment = currentStage / 45 == (below ? 4 : 3) ? 2.25f : 33.75f;
            indicator.chase(currentIndicator + (below ? increment : -increment), 45f, Chaser.LINEAR);
            return;
        }

        indicator.setValue(0);
        indicator.updateChaseTarget(0);
    }

    public static void tickFloorSelection(ElevatorFloorSelection efs, ElevatorContraption ec) {
        if (ec.namesList.isEmpty()) {
            efs.currentShortName = "X";
            efs.currentLongName = "No Floors";
            efs.currentIndex = 0;
            efs.targetYEqualsSelection = true;
            return;
        }

        efs.currentIndex = MathHelper.clamp(efs.currentIndex, 0, ec.namesList.size() - 1);
        IntAttached<Couple<String>> entry = ec.namesList.get(efs.currentIndex);
        efs.currentTargetY = entry.getFirst();
        efs.currentShortName = entry.getSecond().getFirst();
        efs.currentLongName = entry.getSecond().getSecond();
        efs.targetYEqualsSelection = efs.currentTargetY == ec.clientYTarget;

        if (ec.isTargetUnreachable(efs.currentTargetY))
            efs.currentLongName = Text.translatable("create.contraption.controls.floor_unreachable").getString();
    }

    public static class ElevatorFloorSelection {
        public int currentIndex = 0;
        public int currentTargetY = 0;
        public boolean targetYEqualsSelection = true;
        public String currentShortName = "";
        public String currentLongName = "";
    }

}
