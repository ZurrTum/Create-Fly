package com.zurrtum.create.content.kinetics.press;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllItemTags;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.transport.TransportedItemStack;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour.Mode;
import com.zurrtum.create.content.kinetics.press.PressingBehaviour.PressingBehaviourSpecifics;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.content.processing.basin.BasinOperatingBlockEntity;
import com.zurrtum.create.foundation.advancement.AdvancementBehaviour;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

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
    protected Box createRenderBoundingBox() {
        return new Box(pos).stretch(0, -1.5, 0).stretch(0, 1, 0);
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
        if (result.isIn(AllItemTags.TRACKS))
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
                ItemStack stackInSlot = inputs.getStack(slot);
                if (stackInSlot.isEmpty())
                    continue;
                pressingBehaviour.particleItems.add(stackInSlot);
            }
        }

        return true;
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        AdvancementBehaviour behaviour = getBehaviour(AdvancementBehaviour.TYPE);
        if (behaviour != null && behaviour.isOwnerPresent())
            view.putInt("TracksCreated", tracksCreated);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        tracksCreated = view.getInt("TracksCreated", 0);
    }

    @Override
    public boolean tryProcessInWorld(ItemEntity itemEntity, boolean simulate) {
        ItemStack item = itemEntity.getStack();
        SingleStackRecipeInput input = new SingleStackRecipeInput(item);
        Optional<RecipeEntry<PressingRecipe>> recipe = getRecipe(input);
        if (recipe.isEmpty())
            return false;
        if (simulate)
            return true;

        ItemStack itemCreated = ItemStack.EMPTY;
        pressingBehaviour.particleItems.add(item);
        if (canProcessInBulk() || item.getCount() == 1) {
            RecipeApplier.applyCreateRecipeOn(itemEntity, input, recipe.get().value());
            itemCreated = itemEntity.getStack().copy();
        } else {
            for (ItemStack result : RecipeApplier.applyCreateRecipeOn(world, 1, input, recipe.get().value())) {
                if (itemCreated.isEmpty())
                    itemCreated = result.copy();
                ItemEntity created = new ItemEntity(world, itemEntity.getX(), itemEntity.getY(), itemEntity.getZ(), result);
                created.setToDefaultPickupDelay();
                created.setVelocity(VecHelper.offsetRandomly(Vec3d.ZERO, world.random, .05f));
                world.spawnEntity(created);
            }
            item.decrement(1);
        }

        if (!itemCreated.isEmpty())
            onItemPressed(itemCreated);
        return true;
    }

    @Override
    public boolean tryProcessOnBelt(TransportedItemStack input, List<ItemStack> outputList, boolean simulate) {
        SingleStackRecipeInput recipeInput = new SingleStackRecipeInput(input.stack);
        Optional<RecipeEntry<PressingRecipe>> recipe = getRecipe(recipeInput);
        if (recipe.isEmpty())
            return false;
        if (simulate)
            return true;
        pressingBehaviour.particleItems.add(input.stack);
        List<ItemStack> outputs = RecipeApplier.applyCreateRecipeOn(
            world,
            canProcessInBulk() ? input.stack.getCount() : 1,
            recipeInput,
            recipe.get().value()
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

    public Optional<RecipeEntry<PressingRecipe>> getRecipe(SingleStackRecipeInput input) {
        return ((ServerWorld) world).getRecipeManager().getFirstMatch(AllRecipeTypes.PRESSING, input, world);
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
        List<Ingredient> ingredients = recipe.getIngredients().stream().filter(Optional::isPresent).map(Optional::get).toList();
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
    protected boolean matchStaticFilters(RecipeEntry<? extends Recipe<?>> recipe) {
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
