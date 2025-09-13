package com.zurrtum.create;

import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.content.kinetics.fan.processing.AllFanProcessingTypes;
import com.zurrtum.create.content.logistics.item.filter.attribute.ItemAttributeType;
import com.zurrtum.create.content.logistics.item.filter.attribute.SingletonItemAttribute;
import com.zurrtum.create.content.logistics.item.filter.attribute.attributes.*;
import com.zurrtum.create.foundation.fluid.FluidHelper;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.block.ComposterBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.EquippableComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import static com.zurrtum.create.Create.MOD_ID;

public class AllItemAttributeTypes {
    public static final ItemAttributeType PLACEABLE = singleton("placeable", s -> s.getItem() instanceof BlockItem);
    public static final ItemAttributeType CONSUMABLE = singleton("consumable", s -> s.contains(DataComponentTypes.FOOD));
    public static final ItemAttributeType FLUID_CONTAINER = singleton("fluid_container", FluidHelper::hasFluidInventory);
    public static final ItemAttributeType ENCHANTED = singleton("enchanted", ItemStack::hasEnchantments);
    public static final ItemAttributeType MAX_ENCHANTED = singleton("max_enchanted", AllItemAttributeTypes::maxEnchanted);
    public static final ItemAttributeType RENAMED = singleton("renamed", s -> s.contains(DataComponentTypes.CUSTOM_NAME));
    public static final ItemAttributeType DAMAGED = singleton("damaged", ItemStack::isDamaged);
    public static final ItemAttributeType BADLY_DAMAGED = singleton(
        "badly_damaged",
        s -> s.isDamaged() && (float) s.getDamage() / s.getMaxDamage() > 3 / 4f
    );
    public static final ItemAttributeType NOT_STACKABLE = singleton("not_stackable", ((Predicate<ItemStack>) ItemStack::isStackable).negate());
    public static final ItemAttributeType EQUIPABLE = singleton(
        "equipable", s -> {
            EquippableComponent equipable = s.get(DataComponentTypes.EQUIPPABLE);
            EquipmentSlot.Type type = equipable != null ? equipable.slot().getType() : EquipmentSlot.MAINHAND.getType();
            return type != EquipmentSlot.Type.HAND;
        }
    );
    public static final ItemAttributeType FURNACE_FUEL = singleton("furnace_fuel", (s, w) -> w.getFuelRegistry().isFuel(s));
    public static final ItemAttributeType WASHABLE = singleton("washable", AllFanProcessingTypes.SPLASHING::canProcess);
    public static final ItemAttributeType HAUNTABLE = singleton("hauntable", AllFanProcessingTypes.HAUNTING::canProcess);
    public static final ItemAttributeType CRUSHABLE = singleton(
        "crushable",
        (s, w) -> testRecipe(s, w, AllRecipeSets.CRUSHING) || testRecipe(s, w, AllRecipeSets.MILLING)
    );
    public static final ItemAttributeType SMELTABLE = singleton("smeltable", (s, w) -> testRecipe(s, w, RecipePropertySet.FURNACE_INPUT));
    public static final ItemAttributeType SMOKABLE = singleton("smokable", (s, w) -> testRecipe(s, w, RecipePropertySet.SMOKER_INPUT));
    public static final ItemAttributeType BLASTABLE = singleton("blastable", (s, w) -> testRecipe(s, w, RecipePropertySet.BLAST_FURNACE_INPUT));
    public static final ItemAttributeType COMPOSTABLE = singleton(
        "compostable",
        s -> ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.containsKey(s.getItem())
    );

    public static final ItemAttributeType IN_TAG = register("in_tag", new InTagAttribute.Type());
    public static final ItemAttributeType IN_ITEM_GROUP = register("in_item_group", new InItemGroupAttribute.Type());
    public static final ItemAttributeType ADDED_BY = register("added_by", new AddedByAttribute.Type());
    public static final ItemAttributeType HAS_ENCHANT = register("has_enchant", new EnchantAttribute.Type());
    public static final ItemAttributeType SHULKER_FILL_LEVEL = register("shulker_fill_level", new ShulkerFillLevelAttribute.Type());
    public static final ItemAttributeType HAS_COLOR = register("has_color", new ColorAttribute.Type());
    public static final ItemAttributeType HAS_FLUID = register("has_fluid", new FluidContentsAttribute.Type());
    public static final ItemAttributeType HAS_NAME = register("has_name", new ItemNameAttribute.Type());
    public static final ItemAttributeType BOOK_AUTHOR = register("book_author", new BookAuthorAttribute.Type());
    public static final ItemAttributeType BOOK_COPY = register("book_copy", new BookCopyAttribute.Type());

    private static <T extends Recipe<SingleStackRecipeInput>> boolean testRecipe(ItemStack s, World w, RegistryKey<RecipePropertySet> key) {
        return w.getRecipeManager().getPropertySet(key).canUse(s);
    }

    private static boolean maxEnchanted(ItemStack s) {
        for (Object2IntMap.Entry<RegistryEntry<Enchantment>> entry : s.getEnchantments().getEnchantmentEntries()) {
            if (entry.getKey().value().getMaxLevel() <= entry.getIntValue())
                return true;
        }

        return false;
    }

    private static ItemAttributeType singleton(String id, Predicate<ItemStack> predicate) {
        return register(id, new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, (stack, level) -> predicate.test(stack), id)));
    }

    private static ItemAttributeType singleton(String id, BiPredicate<ItemStack, World> predicate) {
        return register(id, new SingletonItemAttribute.Type(type -> new SingletonItemAttribute(type, predicate, id)));
    }

    private static ItemAttributeType register(String id, ItemAttributeType type) {
        return Registry.register(CreateRegistries.ITEM_ATTRIBUTE_TYPE, Identifier.of(MOD_ID, id), type);
    }

    public static void register() {
    }

}
