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
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.content.kinetics.base.DirectionalKineticBlock.FACING;

public class BeltDeployerCallbacks {

    public static ProcessingResult onItemReceived(TransportedItemStack s, TransportedItemStackHandlerBehaviour i, DeployerBlockEntity blockEntity) {

        if (blockEntity.getSpeed() == 0)
            return ProcessingResult.PASS;
        if (blockEntity.mode == Mode.PUNCH)
            return ProcessingResult.PASS;
        BlockState blockState = blockEntity.getBlockState();
        if (!blockState.hasProperty(FACING) || blockState.getValue(FACING) != Direction.DOWN)
            return ProcessingResult.PASS;
        if (blockEntity.state != State.WAITING)
            return ProcessingResult.HOLD;
        if (blockEntity.redstoneLocked)
            return ProcessingResult.PASS;

        DeployerPlayer player = blockEntity.getPlayer();
        ItemStack held = player == null ? ItemStack.EMPTY : player.cast().getMainHandItem();

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
        BlockState blockState = blockEntity.getBlockState();
        if (!blockState.hasProperty(FACING) || blockState.getValue(FACING) != Direction.DOWN)
            return ProcessingResult.PASS;

        DeployerPlayer player = blockEntity.getPlayer();
        ItemStack held = player == null ? ItemStack.EMPTY : player.cast().getMainHandItem();
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
        Level world = blockEntity.getLevel();
        List<TransportedItemStack> collect;
        ServerPlayer player = blockEntity.player.cast();
        ItemStack heldItem = player.getMainHandItem();
        boolean keepHeld;
        if (recipe instanceof SandPaperPolishingRecipe polishingRecipe) {
            ItemStack result = polishingRecipe.assemble(new SingleRecipeInput(transported.stack), world.registryAccess());
            TransportedItemStack copy = transported.copy();
            copy.stack = result;
            copy.angle = BeltHelper.isItemUpright(result) ? 180 : world.getRandom().nextInt(360);
            copy.locked = false;
            collect = List.of(copy);
            keepHeld = false;
        } else if (recipe instanceof ItemApplicationRecipe itemApplicationRecipe) {
            RandomSource random = world.getRandom();
            List<ItemStack> results = RecipeApplier.applyRecipeOn(
                random,
                1,
                new ItemApplicationInput(transported.stack, heldItem),
                itemApplicationRecipe
            );
            collect = new ArrayList<>(results.size());
            for (ItemStack result : results) {
                TransportedItemStack copy = transported.copy();
                copy.stack = result;
                copy.angle = BeltHelper.isItemUpright(result) ? 180 : random.nextInt(360);
                copy.locked = false;
                collect.add(copy);
            }
            keepHeld = itemApplicationRecipe.keepHeldItem();
        } else {
            collect = List.of();
            keepHeld = false;
        }

        blockEntity.award(AllAdvancements.DEPLOYER);

        transported.clearFanProcessingData();

        TransportedItemStack left = transported.copy();
        blockEntity.player.setSpawnedItemEffects(transported.stack.copy());
        left.stack.shrink(1);

        ItemStack resultItem;
        if (collect.isEmpty()) {
            resultItem = left.stack.copy();
            handler.handleProcessingOnItem(transported, TransportedResult.convertTo(left));
        } else {
            resultItem = collect.getFirst().stack;
            handler.handleProcessingOnItem(transported, TransportedResult.convertToAndLeaveHeld(collect, left));
        }

        if (!keepHeld) {
            if (heldItem.getMaxDamage() > 0) {
                heldItem.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            } else {
                ItemStack leftover = heldItem.getItem().getCraftingRemainder();
                heldItem.shrink(1);
                if (!leftover.isEmpty()) {
                    if (heldItem.isEmpty()) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, leftover);
                    } else if (!player.getInventory().add(leftover)) {
                        player.drop(leftover, false);
                    }
                }
            }
        }

        if (!resultItem.isEmpty())
            awardAdvancements(blockEntity, resultItem);

        BlockPos pos = blockEntity.getBlockPos();
        if (heldItem.isEmpty())
            world.playSound(null, pos, SoundEvents.ITEM_BREAK.value(), SoundSource.BLOCKS, .25f, 1);
        world.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, .25f, .75f);
        if (recipe instanceof SandPaperPolishingRecipe)
            AllSoundEvents.SANDING_SHORT.playOnServer(world, pos, .35f, 1f);

        blockEntity.notifyUpdate();
    }

    private static void awardAdvancements(DeployerBlockEntity blockEntity, ItemStack created) {
        CreateTrigger advancement;

        if (created.is(AllItems.ANDESITE_CASING))
            advancement = AllAdvancements.ANDESITE_CASING;
        else if (created.is(AllItems.BRASS_CASING))
            advancement = AllAdvancements.BRASS_CASING;
        else if (created.is(AllItems.COPPER_CASING))
            advancement = AllAdvancements.COPPER_CASING;
        else if (created.is(AllItems.RAILWAY_CASING))
            advancement = AllAdvancements.TRAIN_CASING;
        else
            return;

        blockEntity.award(advancement);
    }

}
