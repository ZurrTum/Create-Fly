package com.zurrtum.create.content.kinetics.deployer;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.utility.BlockHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.RecipeAccess;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class ManualApplicationHelper {
    @Nullable
    public static InteractionResult manualApplicationRecipesApplyInWorld(
        Level level,
        Player player,
        ItemStack heldItem,
        InteractionHand hand,
        BlockHitResult hit,
        BlockPos pos
    ) {
        BlockState blockState = level.getBlockState(pos);

        if (heldItem.isEmpty())
            return null;
        if (blockState.isAir())
            return null;

        ItemStack block = blockState.getBlock().asItem().getDefaultInstance();
        if (level.isClientSide()) {
            RecipeAccess recipeManager = level.recipeAccess();
            if (recipeManager.propertySet(AllRecipeSets.ITEM_APPLICATION_TARGET)
                .test(block) && recipeManager.propertySet(AllRecipeSets.ITEM_APPLICATION_INGREDIENT).test(heldItem)) {
                return InteractionResult.SUCCESS;
            }
            return null;
        }

        ItemApplicationInput input = new ItemApplicationInput(block, heldItem);
        Optional<RecipeHolder<ManualApplicationRecipe>> foundRecipe = ((ServerLevel) level).recipeAccess()
            .getRecipeFor(AllRecipeTypes.ITEM_APPLICATION, input, level);
        if (foundRecipe.isEmpty()) {
            return null;
        }

        level.playSound(null, pos, SoundEvents.COPPER_BREAK, SoundSource.PLAYERS, 1, 1.45f);
        ManualApplicationRecipe recipe = foundRecipe.get().value();
        level.destroyBlock(pos, false);

        ItemStack stack = recipe.assemble(input);
        Item item = stack.getItem();
        if (item instanceof BlockItem blockItem) {
            BlockState transformedBlock = BlockHelper.copyProperties(blockState, blockItem.getBlock().defaultBlockState());
            level.setBlock(pos, transformedBlock, Block.UPDATE_ALL);
            awardAdvancements((ServerPlayer) player, transformedBlock);
        } else {
            Block.popResource(level, pos, stack);
        }

        if (!heldItem.has(DataComponents.UNBREAKABLE) && !player.isCreative() && !recipe.keepHeldItem()) {
            if (heldItem.getMaxDamage() > 0) {
                heldItem.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            } else {
                ItemStackTemplate leftover = heldItem.getItem().getCraftingRemainder();
                heldItem.shrink(1);
                if (leftover != null) {
                    ItemStack itemStack = leftover.create();
                    if (heldItem.isEmpty()) {
                        player.setItemInHand(hand, itemStack);
                    } else if (!player.getInventory().add(itemStack)) {
                        player.drop(itemStack, false);
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static void awardAdvancements(ServerPlayer player, BlockState placed) {
        CreateTrigger advancement;

        if (placed.is(AllBlocks.ANDESITE_CASING))
            advancement = AllAdvancements.ANDESITE_CASING;
        else if (placed.is(AllBlocks.BRASS_CASING))
            advancement = AllAdvancements.BRASS_CASING;
        else if (placed.is(AllBlocks.COPPER_CASING))
            advancement = AllAdvancements.COPPER_CASING;
        else if (placed.is(AllBlocks.RAILWAY_CASING))
            advancement = AllAdvancements.TRAIN_CASING;
        else
            return;

        advancement.trigger(player);
    }
}