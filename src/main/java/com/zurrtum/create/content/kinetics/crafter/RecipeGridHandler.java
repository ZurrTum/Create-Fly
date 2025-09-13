package com.zurrtum.create.content.kinetics.crafter;

import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.Pointing;
import com.zurrtum.create.foundation.codec.CreateCodecs;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.*;
import java.util.function.Predicate;

import static com.zurrtum.create.content.kinetics.base.HorizontalKineticBlock.HORIZONTAL_FACING;

public class RecipeGridHandler {

    public static List<MechanicalCrafterBlockEntity> getAllCraftersOfChain(MechanicalCrafterBlockEntity root) {
        return getAllCraftersOfChainIf(root, Predicates.alwaysTrue());
    }

    public static List<MechanicalCrafterBlockEntity> getAllCraftersOfChainIf(
        MechanicalCrafterBlockEntity root,
        Predicate<MechanicalCrafterBlockEntity> test
    ) {
        return getAllCraftersOfChainIf(root, test, false);
    }

    public static List<MechanicalCrafterBlockEntity> getAllCraftersOfChainIf(
        MechanicalCrafterBlockEntity root,
        Predicate<MechanicalCrafterBlockEntity> test,
        boolean poweredStart
    ) {
        List<MechanicalCrafterBlockEntity> crafters = new ArrayList<>();
        List<Pair<MechanicalCrafterBlockEntity, MechanicalCrafterBlockEntity>> frontier = new ArrayList<>();
        Set<MechanicalCrafterBlockEntity> visited = new HashSet<>();
        frontier.add(Pair.of(root, null));

        boolean empty = false;
        boolean allEmpty = true;

        while (!frontier.isEmpty()) {
            Pair<MechanicalCrafterBlockEntity, MechanicalCrafterBlockEntity> pair = frontier.removeFirst();
            MechanicalCrafterBlockEntity current = pair.getFirst();
            MechanicalCrafterBlockEntity last = pair.getSecond();

            if (visited.contains(current))
                return null;
            if (!(test.test(current)))
                empty = true;
            else
                allEmpty = false;

            crafters.add(current);
            visited.add(current);

            MechanicalCrafterBlockEntity target = getTargetingCrafter(current);
            if (target != last && target != null)
                frontier.add(Pair.of(target, current));
            for (MechanicalCrafterBlockEntity preceding : getPrecedingCrafters(current))
                if (preceding != last)
                    frontier.add(Pair.of(preceding, current));
        }

        return empty && !poweredStart || allEmpty ? null : crafters;
    }

    public static MechanicalCrafterBlockEntity getTargetingCrafter(MechanicalCrafterBlockEntity crafter) {
        BlockState state = crafter.getCachedState();
        if (!isCrafter(state))
            return null;

        BlockPos targetPos = crafter.getPos().offset(MechanicalCrafterBlock.getTargetDirection(state));
        MechanicalCrafterBlockEntity targetBE = CrafterHelper.getCrafter(crafter.getWorld(), targetPos);
        if (targetBE == null)
            return null;

        BlockState targetState = targetBE.getCachedState();
        if (!isCrafter(targetState))
            return null;
        if (state.get(HORIZONTAL_FACING) != targetState.get(HORIZONTAL_FACING))
            return null;
        return targetBE;
    }

    public static List<MechanicalCrafterBlockEntity> getPrecedingCrafters(MechanicalCrafterBlockEntity crafter) {
        BlockPos pos = crafter.getPos();
        World world = crafter.getWorld();
        List<MechanicalCrafterBlockEntity> crafters = new ArrayList<>();
        BlockState blockState = crafter.getCachedState();
        if (!isCrafter(blockState))
            return crafters;

        Direction blockFacing = blockState.get(HORIZONTAL_FACING);
        Direction blockPointing = MechanicalCrafterBlock.getTargetDirection(blockState);
        for (Direction facing : Iterate.directions) {
            if (blockFacing.getAxis() == facing.getAxis())
                continue;
            if (blockPointing == facing)
                continue;

            BlockPos neighbourPos = pos.offset(facing);
            BlockState neighbourState = world.getBlockState(neighbourPos);
            if (!isCrafter(neighbourState))
                continue;
            if (MechanicalCrafterBlock.getTargetDirection(neighbourState) != facing.getOpposite())
                continue;
            if (blockFacing != neighbourState.get(HORIZONTAL_FACING))
                continue;
            MechanicalCrafterBlockEntity be = CrafterHelper.getCrafter(world, neighbourPos);
            if (be == null)
                continue;

            crafters.add(be);
        }

        return crafters;
    }

    private static boolean isCrafter(BlockState state) {
        return state.isOf(AllBlocks.MECHANICAL_CRAFTER);
    }

    public static ItemStack tryToApplyRecipe(ServerWorld world, GroupedItems items) {
        items.calcStats();
        CraftingRecipeInput craftingInput = items.toCraftingInput();
        ItemStack result = null;
        DynamicRegistryManager registryAccess = world.getRegistryManager();
        ServerRecipeManager recipeManager = world.getRecipeManager();
        if (AllConfigs.server().recipes.allowRegularCraftingInCrafter.get()) {
            result = recipeManager.preparedRecipes.find(RecipeType.CRAFTING, craftingInput, world).filter(r -> isRecipeAllowed(r, craftingInput))
                .findFirst().map(r -> r.value().craft(craftingInput, registryAccess)).orElse(null);
        }
        if (result == null)
            result = recipeManager.getFirstMatch(AllRecipeTypes.MECHANICAL_CRAFTING, craftingInput, world)
                .map(r -> r.value().craft(craftingInput, registryAccess)).orElse(null);
        return result;
    }

    public static boolean isRecipeAllowed(RecipeEntry<CraftingRecipe> recipe, CraftingRecipeInput craftingInput) {
        if (recipe.value() instanceof FireworkRocketRecipe) {
            int numItems = 0;
            for (int i = 0; i < craftingInput.size(); i++) {
                if (!craftingInput.getStackInSlot(i).isEmpty()) {
                    numItems++;
                }
            }
            if (numItems > AllConfigs.server().recipes.maxFireworkIngredientsInCrafter.get()) {
                return false;
            }
        }
        if (AllRecipeTypes.AUTOMATION_IGNORE.test(recipe))
            return false;
        return AllRecipeTypes.CAN_BE_AUTOMATED.test(recipe);
    }

    public static class GroupedItems {
        public static final Codec<Map<Pair<Integer, Integer>, ItemStack>> GRID_CODEC = CreateCodecs.getCodecMap(
            Pair.codec(Codec.INT, Codec.INT),
            ItemStack.OPTIONAL_CODEC
        );
        public static final Codec<GroupedItems> CODEC = RecordCodecBuilder.create(instance -> instance.group(GRID_CODEC.fieldOf("Grid")
            .forGetter(i -> i.grid)).apply(instance, GroupedItems::new));
        public Map<Pair<Integer, Integer>, ItemStack> grid = new HashMap<>();
        public int minX;
        public int minY;
        int maxX;
        int maxY;
        public int width;
        public int height;
        boolean statsReady;

        public GroupedItems() {
        }

        public GroupedItems(ItemStack stack) {
            grid.put(Pair.of(0, 0), stack);
        }

        public GroupedItems(Map<Pair<Integer, Integer>, ItemStack> grid) {
            this.grid.putAll(grid);
        }

        public void mergeOnto(GroupedItems other, Pointing pointing) {
            int xOffset = pointing == Pointing.LEFT ? 1 : pointing == Pointing.RIGHT ? -1 : 0;
            int yOffset = pointing == Pointing.DOWN ? 1 : pointing == Pointing.UP ? -1 : 0;
            grid.forEach((pair, stack) -> other.grid.put(Pair.of(pair.getFirst() + xOffset, pair.getSecond() + yOffset), stack));
            other.statsReady = false;
        }

        public void write(WriteView view) {
            WriteView.ListView list = view.getList("Grid");
            grid.forEach((pair, stack) -> {
                WriteView entry = list.add();
                entry.putInt("x", pair.getFirst());
                entry.putInt("y", pair.getSecond());
                entry.put("item", ItemStack.OPTIONAL_CODEC, stack);
            });
        }

        public static GroupedItems read(ReadView view) {
            GroupedItems items = new GroupedItems();
            ReadView.ListReadView list = view.getListReadView("Grid");
            list.forEach(entry -> {
                int x = entry.getInt("x", 0);
                int y = entry.getInt("y", 0);
                ItemStack stack = entry.read("item", ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
                items.grid.put(Pair.of(x, y), stack);
            });
            return items;
        }

        public void calcStats() {
            if (statsReady)
                return;
            statsReady = true;

            minX = 0;
            minY = 0;
            maxX = 0;
            maxY = 0;

            for (Pair<Integer, Integer> pair : grid.keySet()) {
                int x = pair.getFirst();
                int y = pair.getSecond();
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }

            width = maxX - minX + 1;
            height = maxY - minY + 1;
        }

        public boolean onlyEmptyItems() {
            for (ItemStack stack : grid.values())
                if (!stack.isEmpty())
                    return false;
            return true;
        }

        public CraftingRecipeInput toCraftingInput() {
            List<ItemStack> list = new ArrayList<>(width * height);
            int minX = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE;
            int maxY = Integer.MIN_VALUE;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int xp = x + this.minX;
                    int yp = y + this.minY;
                    ItemStack stack = grid.get(Pair.of(xp, yp));
                    if (stack == null || stack.isEmpty())
                        continue;
                    minX = Math.min(minX, xp);
                    maxX = Math.max(maxX, xp);
                    minY = Math.min(minY, yp);
                    maxY = Math.max(maxY, yp);
                }
            }

            int w = maxX - minX + 1;
            int h = maxY - minY + 1;

            for (int y = 0; y < h; y++) {
                for (int x = 0; x < w; x++) {
                    ItemStack stack = grid.get(Pair.of(x + minX, maxY - y));
                    list.add(stack == null ? ItemStack.EMPTY : stack.copy());
                }
            }

            return new CraftingRecipeInput(w, h, list);
        }
    }

}
