package com.zurrtum.create.content.kinetics.saw;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.CreateSingleStackRollableRecipe;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.foundation.recipe.TimedRecipe;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.*;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Clearable;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.math.*;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SawBlockEntity extends BlockBreakingKineticBlockEntity implements Clearable {
    private static final Object cuttingRecipesKey = new Object();

    public ProcessingInventory inventory;
    private int recipeIndex;
    private ServerFilteringBehaviour filtering;

    public ItemStack playEvent;

    public SawBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.SAW, pos, state);
        inventory = new ProcessingInventory(
            this::start,
            direction -> direction != Direction.DOWN
        ).withSlotLimit(!AllConfigs.server().recipes.bulkCutting.get());
        inventory.remainingTime = -1;
        recipeIndex = 0;
        playEvent = ItemStack.EMPTY;
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        super.addBehaviours(behaviours);
        filtering = new ServerFilteringBehaviour(this).forRecipes();
        behaviours.add(filtering);
        behaviours.add(new DirectBeltInputBehaviour(this).allowingBeltFunnelsWhen(this::canProcess));
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.SAW_PROCESSING);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        inventory.write(view);
        view.putInt("RecipeIndex", recipeIndex);
        super.write(view, clientPacket);

        if (!clientPacket || playEvent.isEmpty())
            return;
        view.put("PlayEvent", ItemStack.CODEC, playEvent);
        playEvent = ItemStack.EMPTY;
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        inventory.read(view);
        recipeIndex = view.getInt("RecipeIndex", 0);
        playEvent = view.read("PlayEvent", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(getPos()).expand(.125f);
    }

    @Override
    public void tick() {
        if (shouldRun() && ticksUntilNextProgress < 0)
            destroyNextTick();
        super.tick();

        if (!canProcess())
            return;
        if (getSpeed() == 0)
            return;
        if (inventory.remainingTime == -1) {
            if (!inventory.isEmpty() && !inventory.appliedRecipe)
                start(inventory.getStack(0));
            return;
        }

        float processingSpeed = MathHelper.clamp(Math.abs(getSpeed()) / 24, 1, 128);
        inventory.remainingTime -= processingSpeed;

        if (inventory.remainingTime > 0)
            spawnParticles(inventory.getStack(0));

        if (inventory.remainingTime < 5 && !inventory.appliedRecipe) {
            if (world.isClient())
                return;
            playEvent = inventory.getStack(0);
            applyRecipe();
            inventory.appliedRecipe = true;
            inventory.recipeDuration = 20;
            inventory.remainingTime = 20;
            sendData();
            return;
        }

        if (inventory.remainingTime > 0)
            return;
        inventory.remainingTime = 0;
        Vec3d itemMovement = getItemMovementVec();
        Direction itemMovementFacing = Direction.getFacing(itemMovement.x, itemMovement.y, itemMovement.z);

        for (int slot = 0, size = inventory.size(); slot < size; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isEmpty())
                continue;
            ItemStack tryExportingToBeltFunnel = getBehaviour(DirectBeltInputBehaviour.TYPE).tryExportingToBeltFunnel(
                inventory.onExtract(stack.copy()),
                itemMovementFacing.getOpposite(),
                false
            );
            if (tryExportingToBeltFunnel != null) {
                int count = tryExportingToBeltFunnel.getCount();
                if (count != stack.getCount()) {
                    if (count == 0) {
                        inventory.setStack(slot, ItemStack.EMPTY);
                    } else {
                        stack.setCount(count);
                    }
                    notifyUpdate();
                    return;
                }
                if (!tryExportingToBeltFunnel.isEmpty())
                    return;
            }
        }

        BlockPos nextPos = pos.add(BlockPos.ofFloored(itemMovement));
        DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(world, nextPos, DirectBeltInputBehaviour.TYPE);
        if (behaviour != null) {
            boolean changed = false;
            if (!behaviour.canInsertFromSide(itemMovementFacing))
                return;
            if (world.isClient())
                return;
            for (int slot = 0, size = inventory.size(); slot < size; slot++) {
                ItemStack stack = inventory.getStack(slot);
                if (stack.isEmpty())
                    continue;
                ItemStack remainder = behaviour.handleInsertion(inventory.onExtract(stack.copy()), itemMovementFacing, false);
                int count = remainder.getCount();
                if (count == stack.getCount())
                    continue;
                if (count == 0) {
                    inventory.setStack(slot, ItemStack.EMPTY);
                } else {
                    stack.setCount(count);
                }
                changed = true;
            }
            if (changed) {
                markDirty();
                sendData();
            }
            return;
        }

        // Eject Items
        Vec3d outPos = VecHelper.getCenterOf(pos).add(itemMovement.multiply(.5f).add(0, .5, 0));
        Vec3d outMotion = itemMovement.multiply(.0625).add(0, .125, 0);
        for (int slot = 0, size = inventory.size(); slot < size; slot++) {
            ItemStack stack = inventory.getStack(slot);
            if (stack.isEmpty())
                continue;
            ItemEntity entityIn = new ItemEntity(world, outPos.x, outPos.y, outPos.z, inventory.onExtract(stack));
            entityIn.setVelocity(outMotion);
            world.spawnEntity(entityIn);
        }
        inventory.clear();
        world.updateComparators(pos, getCachedState().getBlock());
        inventory.remainingTime = -1;
        sendData();
    }

    @Override
    public void clear() {
        inventory.clear();
        filtering.setFilter(ItemStack.EMPTY);
    }

    @Override
    public void destroy() {
        super.destroy();
        ItemScatterer.spawn(world, pos, inventory);
    }

    public void spawnEventParticles(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return;

        ParticleEffect particleData = null;
        if (stack.getItem() instanceof BlockItem)
            particleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock().getDefaultState());
        else
            particleData = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);

        Random r = world.random;
        Vec3d v = VecHelper.getCenterOf(pos).add(0, 5 / 16f, 0);
        for (int i = 0; i < 10; i++) {
            Vec3d m = VecHelper.offsetRandomly(new Vec3d(0, 0.25f, 0), r, .125f);
            world.addParticleClient(particleData, v.x, v.y, v.z, m.x, m.y, m.y);
        }
    }

    protected void spawnParticles(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return;

        ParticleEffect particleData = null;
        float speed = 1;
        if (stack.getItem() instanceof BlockItem)
            particleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock().getDefaultState());
        else {
            particleData = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);
            speed = .125f;
        }

        Random r = world.random;
        Vec3d vec = getItemMovementVec();
        Vec3d pos = VecHelper.getCenterOf(this.pos);
        float offset = inventory.recipeDuration != 0 ? inventory.remainingTime / inventory.recipeDuration : 0;
        offset /= 2;
        if (inventory.appliedRecipe)
            offset -= .5f;
        world.addParticleClient(
            particleData,
            pos.getX() + -vec.x * offset,
            pos.getY() + .45f,
            pos.getZ() + -vec.z * offset,
            -vec.x * speed,
            r.nextFloat() * speed,
            -vec.z * speed
        );
    }

    public Vec3d getItemMovementVec() {
        boolean alongX = !getCachedState().get(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
        int offset = getSpeed() < 0 ? -1 : 1;
        return new Vec3d(offset * (alongX ? 1 : 0), 0, offset * (alongX ? 0 : -1));
    }

    private void applyRecipe() {
        ItemStack stack = inventory.getStack(0);
        if (PackageItem.isPackage(stack)) {
            inventory.clear();
            inventory.outputAllowInsertion();
            ContainerComponent contents = stack.getOrDefault(AllDataComponents.PACKAGE_CONTENTS, ContainerComponent.DEFAULT);
            inventory.insert(contents.stream().toList());
            inventory.outputForbidInsertion();
            return;
        }

        SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
        Pair<Recipe<SingleStackRecipeInput>, ItemStack> pair = updateRecipe(input, false);
        if (pair == null)
            return;

        inventory.clear();
        List<ItemStack> list = getResults(world, input, stack, pair);
        int i = 1;
        for (ItemStack result : list) {
            inventory.setStack(i++, result);
        }
        award(AllAdvancements.SAW_PROCESSING);
    }

    private static List<ItemStack> getResults(
        World level,
        SingleStackRecipeInput input,
        ItemStack stack,
        Pair<Recipe<SingleStackRecipeInput>, @Nullable ItemStack> pair
    ) {
        int rolls = stack.getCount();
        ItemStack recipeRemainder = stack.getItem().getRecipeRemainder();
        ItemStack output = pair.getSecond();
        if (output == null) {
            Recipe<SingleStackRecipeInput> recipe = pair.getFirst();
            if (recipe instanceof CreateSingleStackRollableRecipe rollableRecipe) {
                return RecipeApplier.applyRecipeOn(level.getRandom(), rolls, input, rollableRecipe);
            } else {
                output = recipe.craft(input, level.getRegistryManager());
            }
        }
        if (recipeRemainder.isEmpty()) {
            return ItemHelper.multipliedOutput(output, rolls);
        }
        return ItemHelper.multipliedOutput(List.of(output, recipeRemainder), rolls);
    }

    private static boolean matchCuttingRecipe(RecipeEntry<? extends Recipe<?>> entry) {
        return entry.value().getType() == AllRecipeTypes.CUTTING && !AllRecipeTypes.shouldIgnoreInAutomation(entry);
    }

    private static boolean matchAllRecipe(RecipeEntry<? extends Recipe<?>> entry) {
        RecipeType<? extends Recipe<?>> type = entry.value().getType();
        return (type == AllRecipeTypes.CUTTING || type == RecipeType.STONECUTTING) && !AllRecipeTypes.shouldIgnoreInAutomation(entry);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Pair<Recipe<SingleStackRecipeInput>, ItemStack> updateRecipe(SingleStackRecipeInput input, boolean plus) {
        List<RecipeEntry<?>> startedSearch = RecipeFinder.get(
            cuttingRecipesKey,
            (ServerWorld) world,
            AllConfigs.server().recipes.allowStonecuttingOnSaw.get() ? SawBlockEntity::matchAllRecipe : SawBlockEntity::matchCuttingRecipe
        );
        int index = 0;

        Recipe<SingleStackRecipeInput> first = null;
        if (filtering.getFilter().isEmpty()) {
            for (RecipeEntry<?> entry : startedSearch) {
                Recipe<SingleStackRecipeInput> recipe = (Recipe<SingleStackRecipeInput>) entry.value();
                if (recipe.matches(input, world)) {
                    if (first == null) {
                        first = recipe;
                    }
                    if (index == recipeIndex) {
                        if (plus) {
                            recipeIndex++;
                        }
                        return Pair.of(recipe, null);
                    }
                    index++;
                }
            }
        } else {
            DynamicRegistryManager registryManager = world.getRegistryManager();
            for (RecipeEntry<? extends Recipe<?>> entry : startedSearch) {
                Recipe<SingleStackRecipeInput> recipe = (Recipe<SingleStackRecipeInput>) entry.value();
                if (recipe.matches(input, world)) {
                    if (recipe instanceof CreateSingleStackRollableRecipe rollableRecipe) {
                        if (filtering.test(rollableRecipe.results().getFirst().create())) {
                            return Pair.of(recipe, null);
                        }
                    } else {
                        ItemStack output = recipe.craft(input, registryManager);
                        if (filtering.test(output)) {
                            return Pair.of(recipe, output);
                        }
                    }
                }
            }
        }
        recipeIndex = 0;
        return first == null ? null : Pair.of(first, null);
    }

    public void insertItem(ItemEntity entity) {
        if (!canProcess())
            return;
        if (!inventory.isEmpty())
            return;
        if (!entity.isAlive())
            return;
        if (world.isClient())
            return;

        inventory.clear();

        ItemStack stack = entity.getStack();
        int count = stack.getCount();
        int insert = inventory.insert(stack);
        if (insert == count)
            entity.discard();
        else if (insert != 0) {
            stack.decrement(insert);
            entity.setStack(stack);
        }
    }

    public void start(ItemStack inserted) {
        if (!canProcess())
            return;
        if (inventory.isEmpty())
            return;
        if (world.isClient())
            return;

        SingleStackRecipeInput input = new SingleStackRecipeInput(inventory.getStack(0));
        Pair<Recipe<SingleStackRecipeInput>, ItemStack> pair = updateRecipe(input, true);
        int time = 50;

        if (pair == null) {
            inventory.remainingTime = inventory.recipeDuration = 10;
            inventory.appliedRecipe = false;
            sendData();
            return;
        }

        if (pair.getFirst() instanceof TimedRecipe recipe) {
            time = recipe.time();
        }

        inventory.remainingTime = time * Math.max(1, (inserted.getCount() / 5));
        inventory.recipeDuration = inventory.remainingTime;
        inventory.appliedRecipe = false;
        sendData();
    }

    protected boolean canProcess() {
        return getCachedState().get(SawBlock.FACING) == Direction.UP;
    }

    // Block Breaker

    @Override
    protected boolean shouldRun() {
        return getCachedState().get(SawBlock.FACING).getAxis().isHorizontal();
    }

    @Override
    protected BlockPos getBreakingPos() {
        return getPos().offset(getCachedState().get(SawBlock.FACING));
    }

    @Override
    public void onBlockBroken(BlockState stateToBreak) {
        //        Optional<AbstractBlockBreakQueue> dynamicTree = TreeCutter.findDynamicTree(stateToBreak.getBlock(), breakingPos);
        //        if (dynamicTree.isPresent()) {
        //            dynamicTree.get().destroyBlocks(world, null, this::dropItemFromCutTree);
        //            return;
        //        }

        super.onBlockBroken(stateToBreak);
        TreeCutter.findTree(world, breakingPos, stateToBreak).destroyBlocks(world, null, this::dropItemFromCutTree);
    }

    public void dropItemFromCutTree(BlockPos pos, ItemStack stack) {
        float distance = (float) Math.sqrt(pos.getSquaredDistance(breakingPos));
        Vec3d dropPos = VecHelper.getCenterOf(pos);
        ItemEntity entity = new ItemEntity(world, dropPos.x, dropPos.y, dropPos.z, stack);
        entity.setVelocity(Vec3d.of(breakingPos.subtract(this.pos)).multiply(distance / 20f));
        world.spawnEntity(entity);
    }

    @Override
    public boolean canBreak(BlockState stateToBreak, float blockHardness) {
        boolean sawable = isSawable(stateToBreak);
        return super.canBreak(stateToBreak, blockHardness) && sawable;
    }

    public static boolean isSawable(BlockState stateToBreak) {
        if (stateToBreak.isIn(BlockTags.SAPLINGS))
            return false;
        if (TreeCutter.isLog(stateToBreak) || (stateToBreak.isIn(BlockTags.LEAVES)))
            return true;
        if (TreeCutter.isRoot(stateToBreak))
            return true;
        Block block = stateToBreak.getBlock();
        if (block instanceof BambooBlock)
            return true;
        if (block.equals(Blocks.PUMPKIN) || block.equals(Blocks.MELON))
            return true;
        if (block instanceof CactusBlock)
            return true;
        if (block instanceof SugarCaneBlock)
            return true;
        if (block instanceof KelpPlantBlock)
            return true;
        if (block instanceof KelpBlock)
            return true;
        if (block instanceof ChorusPlantBlock)
            return true;
        //TODO
        //        if (TreeCutter.canDynamicTreeCutFrom(block))
        //            return true;
        return false;
    }

}
