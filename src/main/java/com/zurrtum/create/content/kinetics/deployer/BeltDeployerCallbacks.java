package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.content.equipment.sandPaper.SandPaperPolishingRecipe;
import com.zurrtum.create.content.kinetics.belt.BeltHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.BeltProcessingBehaviour.ProcessingResult;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour;
import com.zurrtum.create.content.kinetics.belt.behaviour.TransportedItemStackHandlerBehaviour.TransportedResult;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.Mode;
import com.zurrtum.create.content.kinetics.deployer.DeployerBlockEntity.State;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.List;

import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class BeltDeployerCallbacks {

    public static ProcessingResult onItemReceived(TransportedItemStack s, TransportedItemStackHandlerBehaviour i, DeployerBlockEntity blockEntity) {

        if (blockEntity.getSpeed() == 0)
            return ProcessingResult.PASS;
        if (blockEntity.mode == Mode.PUNCH)
            return ProcessingResult.PASS;
        BlockState blockState = blockEntity.getCachedState();
        if (!blockState.contains(FACING) || blockState.get(FACING) != Direction.DOWN)
            return ProcessingResult.PASS;
        if (blockEntity.state != State.WAITING)
            return ProcessingResult.HOLD;
        if (blockEntity.redstoneLocked)
            return ProcessingResult.PASS;

        DeployerPlayer player = blockEntity.getPlayer();
        ItemStack held = player == null ? ItemStack.EMPTY : player.cast().getMainHandStack();

        if (held.isEmpty())
            return ProcessingResult.HOLD;
        if (blockEntity.getRecipe(s.stack) == null)
            return ProcessingResult.PASS;

        blockEntity.start();
        return ProcessingResult.HOLD;
    }

    public static ProcessingResult whenItemHeld(TransportedItemStack s, TransportedItemStackHandlerBehaviour i, DeployerBlockEntity blockEntity) {

        if (blockEntity.getSpeed() == 0)
            return ProcessingResult.PASS;
        BlockState blockState = blockEntity.getCachedState();
        if (!blockState.contains(FACING) || blockState.get(FACING) != Direction.DOWN)
            return ProcessingResult.PASS;

        DeployerPlayer player = blockEntity.getPlayer();
        ItemStack held = player == null ? ItemStack.EMPTY : player.cast().getMainHandStack();
        if (held.isEmpty())
            return ProcessingResult.HOLD;

        Recipe<?> recipe = blockEntity.getRecipe(s.stack);
        if (recipe == null)
            return ProcessingResult.PASS;

        if (blockEntity.state == State.RETRACTING && blockEntity.timer == 1000) {
            activate(s, i, blockEntity, recipe);
            return ProcessingResult.HOLD;
        }

        if (blockEntity.state == State.WAITING) {
            if (blockEntity.redstoneLocked)
                return ProcessingResult.PASS;
            blockEntity.start();
        }

        return ProcessingResult.HOLD;
    }

    public static void activate(
        TransportedItemStack transported,
        TransportedItemStackHandlerBehaviour handler,
        DeployerBlockEntity blockEntity,
        Recipe<?> recipe
    ) {
        World world = blockEntity.getWorld();
        TransportedItemStack result = null;
        ItemStack resultItem = null;
        boolean keepHeld = false;
        ItemStack heldItem = blockEntity.player.cast().getMainHandStack();
        if (recipe instanceof SandPaperPolishingRecipe polishingRecipe) {
            resultItem = polishingRecipe.craft(new SingleStackRecipeInput(transported.stack), world.getRegistryManager());
        } else if (recipe instanceof ItemApplicationRecipe itemApplicationRecipe) {
            resultItem = itemApplicationRecipe.craft(new ItemApplicationInput(transported.stack, heldItem), world.getRegistryManager());
            keepHeld = itemApplicationRecipe.keepHeldItem();
        }
        if (resultItem != null && !resultItem.isEmpty()) {
            result = transported.copy();
            boolean centered = BeltHelper.isItemUpright(resultItem);
            result.stack = resultItem;
            result.locked = true;
            result.angle = centered ? 180 : world.random.nextInt(360);
            result.locked = false;
        }

        blockEntity.award(AllAdvancements.DEPLOYER);

        transported.clearFanProcessingData();

        TransportedItemStack left = transported.copy();
        blockEntity.player.setSpawnedItemEffects(transported.stack.copy());
        left.stack.decrement(1);

        if (result == null) {
            resultItem = left.stack.copy();
            handler.handleProcessingOnItem(transported, TransportedResult.convertTo(left));
        } else {
            handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(List.of(result), left));
        }

        if (!keepHeld) {
            if (heldItem.getMaxDamage() > 0) {
                heldItem.damage(1, blockEntity.player.cast(), EquipmentSlot.MAINHAND);
            } else {
                heldItem.decrement(1);
            }
        }

        if (!resultItem.isEmpty())
            awardAdvancements(blockEntity, resultItem);

        BlockPos pos = blockEntity.getPos();
        if (heldItem.isEmpty())
            world.playSound(null, pos, SoundEvents.ENTITY_ITEM_BREAK.value(), SoundCategory.BLOCKS, .25f, 1);
        world.playSound(null, pos, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.BLOCKS, .25f, .75f);
        if (recipe instanceof SandPaperPolishingRecipe)
            AllSoundEvents.SANDING_SHORT.playOnServer(world, pos, .35f, 1f);

        blockEntity.notifyUpdate();
    }

    private static void awardAdvancements(DeployerBlockEntity blockEntity, ItemStack created) {
        CreateTrigger advancement;

        if (created.isOf(AllItems.ANDESITE_CASING))
            advancement = AllAdvancements.ANDESITE_CASING;
        else if (created.isOf(AllItems.BRASS_CASING))
            advancement = AllAdvancements.BRASS_CASING;
        else if (created.isOf(AllItems.COPPER_CASING))
            advancement = AllAdvancements.COPPER_CASING;
        else if (created.isOf(AllItems.RAILWAY_CASING))
            advancement = AllAdvancements.TRAIN_CASING;
        else
            return;

        blockEntity.award(advancement);
    }

}
