package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Optional;

public class ManualApplicationHelper {
    public static ActionResult manualApplicationRecipesApplyInWorld(
        World level,
        PlayerEntity player,
        ItemStack heldItem,
        Hand hand,
        BlockHitResult hit,
        BlockPos pos
    ) {
        BlockState blockState = level.getBlockState(pos);

        if (heldItem.isEmpty())
            return null;
        if (blockState.isAir())
            return null;

        ItemStack block = blockState.getBlock().asItem().getDefaultStack();
        if (level.isClient()) {
            RecipeManager recipeManager = level.getRecipeManager();
            if (recipeManager.getPropertySet(AllRecipeSets.ITEM_APPLICATION_TARGET)
                .canUse(block) && recipeManager.getPropertySet(AllRecipeSets.ITEM_APPLICATION_INGREDIENT).canUse(heldItem)) {
                return ActionResult.SUCCESS;
            }
            return null;
        }

        ItemApplicationInput input = new ItemApplicationInput(block, heldItem);
        Optional<RecipeEntry<ManualApplicationRecipe>> foundRecipe = ((ServerWorld) level).getRecipeManager()
            .getFirstMatch(AllRecipeTypes.ITEM_APPLICATION, input, level);
        if (foundRecipe.isEmpty()) {
            return null;
        }

        level.playSound(null, pos, SoundEvents.BLOCK_COPPER_BREAK, SoundCategory.PLAYERS, 1, 1.45f);
        ManualApplicationRecipe recipe = foundRecipe.get().value();
        level.breakBlock(pos, false);

        ItemStack stack = recipe.craft(input, level.getRegistryManager());
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            BlockState transformedBlock = BlockHelper.copyProperties(blockState, blockItem.getBlock().getDefaultState());
            level.setBlockState(pos, transformedBlock, Block.NOTIFY_ALL);
            awardAdvancements((ServerPlayerEntity) player, transformedBlock);
        } else {
            Block.dropStack(level, pos, stack);
        }

        if (!heldItem.contains(DataComponentTypes.UNBREAKABLE) && !player.isCreative() && !recipe.keepHeldItem()) {
            if (heldItem.getMaxDamage() > 0) {
                heldItem.damage(1, player, EquipmentSlot.MAINHAND);
            } else {
                heldItem.decrement(1);
            }
        }
        return ActionResult.SUCCESS;
    }

    private static void awardAdvancements(ServerPlayerEntity player, BlockState placed) {
        CreateTrigger advancement;

        if (placed.isOf(AllBlocks.ANDESITE_CASING))
            advancement = AllAdvancements.ANDESITE_CASING;
        else if (placed.isOf(AllBlocks.BRASS_CASING))
            advancement = AllAdvancements.BRASS_CASING;
        else if (placed.isOf(AllBlocks.COPPER_CASING))
            advancement = AllAdvancements.COPPER_CASING;
        else if (placed.isOf(AllBlocks.RAILWAY_CASING))
            advancement = AllAdvancements.TRAIN_CASING;
        else
            return;

        advancement.trigger(player);
    }
}