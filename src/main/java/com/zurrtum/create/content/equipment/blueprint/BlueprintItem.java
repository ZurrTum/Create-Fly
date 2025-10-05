package com.zurrtum.create.content.equipment.blueprint;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class BlueprintItem extends Item {

    public BlueprintItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        Direction face = ctx.getSide();
        PlayerEntity player = ctx.getPlayer();
        ItemStack stack = ctx.getStack();
        BlockPos pos = ctx.getBlockPos().offset(face);

        if (player != null && !player.canPlaceOn(pos, face, stack))
            return ActionResult.FAIL;

        World world = ctx.getWorld();
        AbstractDecorationEntity hangingentity = new BlueprintEntity(
            world,
            pos,
            face,
            face.getAxis().isHorizontal() ? Direction.DOWN : ctx.getHorizontalPlayerFacing()
        );
        NbtComponent customData = stack.get(DataComponentTypes.CUSTOM_DATA);

        if (customData != null)
            EntityType.loadFromEntityNbt(world, player, hangingentity, customData);
        if (!hangingentity.canStayAttached())
            return ActionResult.CONSUME;
        if (!world.isClient()) {
            hangingentity.onPlace();
            world.spawnEntity(hangingentity);
        }

        stack.decrement(1);
        return ActionResult.SUCCESS;
    }

    //TODO
    //    public static void assignCompleteRecipe(World level, ItemStackHandler inv, Recipe<?> recipe) {
    //        NonNullList<Ingredient> ingredients = recipe.getIngredients();
    //
    //        for (int i = 0; i < 9; i++)
    //            inv.setStackInSlot(i, ItemStack.EMPTY);
    //        inv.setStackInSlot(9, recipe.getResultItem(level.registryAccess()));
    //
    //        if (recipe instanceof ShapedRecipe shapedRecipe) {
    //            for (int row = 0; row < shapedRecipe.getHeight(); row++)
    //                for (int col = 0; col < shapedRecipe.getWidth(); col++)
    //                    inv.setStackInSlot(row * 3 + col, convertIngredientToFilter(ingredients.get(row * shapedRecipe.getWidth() + col)));
    //        } else {
    //            for (int i = 0; i < ingredients.size(); i++)
    //                inv.setStackInSlot(i, convertIngredientToFilter(ingredients.get(i)));
    //        }
    //    }
    //
    //    private static ItemStack convertIngredientToFilter(Ingredient ingredient) {
    //        boolean isCompoundIngredient = ingredient.getCustomIngredient() instanceof CompoundIngredient;
    //        Ingredient.Value[] acceptedItems = ingredient.values;
    //        if (acceptedItems == null || acceptedItems.length > 18)
    //            return ItemStack.EMPTY;
    //        if (acceptedItems.length == 0)
    //            return ItemStack.EMPTY;
    //        if (acceptedItems.length == 1)
    //            return convertIItemListToFilter(acceptedItems[0], isCompoundIngredient);
    //
    //        ItemStack result = AllItems.FILTER.asStack();
    //        ItemStackHandler filterItems = FilterItem.getFilterItems(result);
    //        for (int i = 0; i < acceptedItems.length; i++)
    //            filterItems.setStackInSlot(i, convertIItemListToFilter(acceptedItems[i], isCompoundIngredient));
    //        result.set(AllDataComponents.FILTER_ITEMS, ItemHelper.containerContentsFromHandler(filterItems));
    //        return result;
    //    }
    //
    //    private static ItemStack convertIItemListToFilter(Value itemList, boolean isCompoundIngredient) {
    //        Collection<ItemStack> stacks = itemList.getItems();
    //        if (itemList instanceof ItemValue) {
    //            for (ItemStack itemStack : stacks)
    //                return itemStack;
    //        }
    //
    //        if (itemList instanceof TagValue tagValue) {
    //            ItemStack filterItem = AllItems.ATTRIBUTE_FILTER.asStack();
    //            filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_WHITELIST_MODE, AttributeFilterWhitelistMode.WHITELIST_DISJ);
    //            List<ItemAttributeEntry> attributes = new ArrayList<>();
    //            ItemAttribute at = new InTagAttribute(ItemTags.create(tagValue.tag().location()));
    //            attributes.add(new ItemAttribute.ItemAttributeEntry(at, false));
    //            filterItem.set(AllDataComponents.ATTRIBUTE_FILTER_MATCHED_ATTRIBUTES, attributes);
    //            return filterItem;
    //        }
    //
    //        if (isCompoundIngredient) {
    //            ItemStack result = AllItems.FILTER.asStack();
    //            ItemStackHandler filterItems = FilterItem.getFilterItems(result);
    //            int i = 0;
    //            for (ItemStack itemStack : stacks) {
    //                if (i >= 18)
    //                    break;
    //                filterItems.setStackInSlot(i++, itemStack);
    //            }
    //            result.set(AllDataComponents.FILTER_ITEMS, ItemHelper.containerContentsFromHandler(filterItems));
    //            result.set(AllDataComponents.FILTER_ITEMS_RESPECT_NBT, true);
    //            return result;
    //        }
    //
    //        return ItemStack.EMPTY;
    //    }

}