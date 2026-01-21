package com.zurrtum.create.content.kinetics.press;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour.Mode;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour.PressingBehaviourSpecifics;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.content.processing.basin.BasinOperatingBlockEntity;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MechanicalPressBlockEntity extends BasinOperatingBlockEntity implements PressingBehaviourSpecifics {

    private static final Object compressingRecipesKey = new Object();

    public PressingBehaviour pressingBehaviour;
    private int tracksCreated;

    public MechanicalPressBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_PRESS, pos, state);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(worldPosition).expandTowards(0, -1.5, 0).expandTowards(0, 1, 0);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        pressingBehaviour = new PressingBehaviour(this);
        behaviours.add(pressingBehaviour);
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.PRESS, AllAdvancements.COMPACTING, AllAdvancements.TRACK_CRAFTING);
    }

    public void onItemPressed(ItemStack result) {
        award(AllAdvancements.PRESS);
        if (result.is(AllItemTags.TRACKS))
            tracksCreated += result.getCount();
        if (tracksCreated >= 1000) {
            award(AllAdvancements.TRACK_CRAFTING);
            tracksCreated = 0;
        }
    }

    public PressingBehaviour getPressingBehaviour() {
        return pressingBehaviour;
    }

    @Override
    public boolean tryProcessInBasin(boolean simulate) {
        applyBasinRecipe();

        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.isPresent()) {
            BasinInventory inputs = basin.get().itemCapability;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack stackInSlot = inputs.getItem(slot);
                if (stackInSlot.isEmpty())
                    continue;
                pressingBehaviour.particleItems.add(stackInSlot);
            }
        }

        return true;
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        AdvancementBehaviour behaviour = getBehaviour(AdvancementBehaviour.TYPE);
        if (behaviour != null && behaviour.isOwnerPresent())
            view.putInt("TracksCreated", tracksCreated);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        tracksCreated = view.getIntOr("TracksCreated", 0);
    }

    @Override
    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        ItemStack item = itemEntity.getItem();
        SingleRecipeInput input = new SingleRecipeInput(item);
        Optional<RecipeHolder<PressingRecipe>> recipe = getRecipe(input);
        if (recipe.isEmpty())
            return false;
        if (simulate)
            return true;

        ItemStack itemCreated = ItemStack.EMPTY;
        pressingBehaviour.particleItems.add(item);
        if (canProcessInBulk() || item.getCount() == 1) {
            RecipeApplier.applyCreateRecipeOn(itemEntity, input, recipe.get().value(), true);
            itemCreated = itemEntity.getItem().copy();
        } else {
            for (ItemStack result : RecipeApplier.applyCreateRecipeOn(level, 1, input, recipe.get().value(), true)) {
                if (itemCreated.isEmpty())
                    itemCreated = result.copy();
                ItemEntity created = new ItemEntity(level, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
                created.setDefaultPickUpDelay();
                created.setDeltaMovement(VecHelper.offsetRandomly(Vec3.ZERO, level.getRandom(), .05f));
                level.addFreshEntity(created);
            }
            item.shrink(1);
        }

        if (!itemCreated.isEmpty())
            onItemPressed(itemCreated);
        return true;
    }

    @Override
    public boolean tryProcessOnBelt(TransportedItemStack input, @Nullable List<ItemStack> outputList, boolean simulate) {
        SingleRecipeInput recipeInput = new SingleRecipeInput(input.stack);
        Optional<RecipeHolder<PressingRecipe>> recipe = getRecipe(recipeInput);
        if (recipe.isEmpty())
            return false;
        if (simulate)
            return true;
        pressingBehaviour.particleItems.add(input.stack);
        List<ItemStack> outputs = RecipeApplier.applyCreateRecipeOn(
            level,
            canProcessInBulk() ? input.stack.getCount() : 1,
            recipeInput,
            recipe.get().value(),
            true
        );

        for (ItemStack created : outputs) {
            if (!created.isEmpty()) {
                onItemPressed(created);
                break;
            }
        }

        outputList.addAll(outputs);
        return true;
    }

    @Override
    public void onPressingCompleted() {
        if (pressingBehaviour.onBasin() && matchBasinRecipe(currentRecipe) && getBasin().filter(BasinBlockEntity::canContinueProcessing).isPresent())
            startProcessingBasin();
        else
            basinChecker.scheduleUpdate();
    }

    public Optional<RecipeHolder<PressingRecipe>> getRecipe(SingleRecipeInput input) {
        return ((ServerLevel) level).recipeAccess().getRecipeFor(AllRecipeTypes.PRESSING, input, level);
    }

    public static boolean canCompress(Recipe<?> recipe) {
        if (recipe instanceof ShapedRecipe shapedRecipe) {
            return canCompress(shapedRecipe);
        } else if (recipe instanceof ShapelessRecipe shapelessRecipe) {
            return canCompress(shapelessRecipe);
        } else {
            return false;
        }
    }

    public static boolean canCompress(ShapedRecipe recipe) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (Optional<Ingredient> ingredient : recipe.getIngredients()) {
            if (ingredient.isPresent()) {
                ingredients.add(ingredient.get());
            } else {
                return false;
            }
        }
        int size = ingredients.size();
        if (size != 4 && size != 9) {
            return false;
        }
        return ItemHelper.matchAllIngredients(ingredients);
    }

    public static boolean canCompress(ShapelessRecipe recipe) {
        int size = recipe.ingredients.size();
        if (size != 4 && size != 9) {
            return false;
        }
        return ItemHelper.matchAllIngredients(recipe.ingredients);
    }

    @Override
    protected boolean matchStaticFilters(RecipeHolder<? extends Recipe<?>> recipe) {
        Recipe<?> value = recipe.value();
        return value instanceof CraftingRecipe && canCompress(value) && !AllRecipeTypes.shouldIgnoreInAutomation(recipe) || value.getType() == AllRecipeTypes.COMPACTING;
    }

    @Override
    public float getKineticSpeed() {
        return getSpeed();
    }

    @Override
    public boolean canProcessInBulk() {
        return AllConfigs.server().recipes.bulkPressing.get();
    }

    @Override
    protected Object getRecipeCacheKey() {
        return compressingRecipesKey;
    }

    @Override
    public int getParticleAmount() {
        return 15;
    }

    @Override
    public void startProcessingBasin() {
        if (pressingBehaviour.running && pressingBehaviour.runningTicks <= PressingBehaviour.CYCLE / 2)
            return;
        super.startProcessingBasin();
        pressingBehaviour.start(Mode.BASIN);
    }

    @Override
    protected void onBasinRemoved() {
        pressingBehaviour.particleItems.clear();
        pressingBehaviour.running = false;
        pressingBehaviour.runningTicks = 0;
        sendData();
    }

    @Override
    protected boolean isRunning() {
        return pressingBehaviour.running;
    }

    @Override
    protected Optional<CreateTrigger> getProcessedRecipeTrigger() {
        return Optional.of(AllAdvancements.COMPACTING);
    }

}
