package com.zurrtum.create.content.kinetics.saw;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.base.BlockBreakingKineticBlockEntity;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.logistics.box.PackageItem;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.filtering.ServerFilteringBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.RecipeFinder;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SawBlockEntity extends BlockBreakingKineticBlockEntity {
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
    public void write(ValueOutput view, boolean clientPacket) {
        inventory.write(view);
        view.putInt("RecipeIndex", recipeIndex);
        super.write(view, clientPacket);

        if (!clientPacket || playEvent.isEmpty())
            return;
        view.store("PlayEvent", ItemStack.CODEC, playEvent);
        playEvent = ItemStack.EMPTY;
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        inventory.read(view);
        recipeIndex = view.getIntOr("RecipeIndex", 0);
        playEvent = view.read("PlayEvent", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    @Override
    protected AABB createRenderBoundingBox() {
        return new AABB(getBlockPos()).inflate(.125f);
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
                start(inventory.getItem(0));
            return;
        }

        float processingSpeed = Mth.clamp(Math.abs(getSpeed()) / 24, 1, 128);
        inventory.remainingTime -= processingSpeed;

        if (inventory.remainingTime > 0)
            spawnParticles(inventory.getItem(0));

        if (inventory.remainingTime < 5 && !inventory.appliedRecipe) {
            if (level.isClientSide())
                return;
            playEvent = inventory.getItem(0);
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
        Vec3 itemMovement = getItemMovementVec();
        Direction itemMovementFacing = Direction.getApproximateNearest(itemMovement.x, itemMovement.y, itemMovement.z);

        for (int slot = 0, size = inventory.getContainerSize(); slot < size; slot++) {
            ItemStack stack = inventory.getItem(slot);
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
                        inventory.setItem(slot, ItemStack.EMPTY);
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

        BlockPos nextPos = worldPosition.offset(BlockPos.containing(itemMovement));
        DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(level, nextPos, DirectBeltInputBehaviour.TYPE);
        if (behaviour != null) {
            boolean changed = false;
            if (!behaviour.canInsertFromSide(itemMovementFacing))
                return;
            if (level.isClientSide())
                return;
            for (int slot = 0, size = inventory.getContainerSize(); slot < size; slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty())
                    continue;
                ItemStack remainder = behaviour.handleInsertion(inventory.onExtract(stack.copy()), itemMovementFacing, false);
                int count = remainder.getCount();
                if (count == stack.getCount())
                    continue;
                if (count == 0) {
                    inventory.setItem(slot, ItemStack.EMPTY);
                } else {
                    stack.setCount(count);
                }
                changed = true;
            }
            if (changed) {
                setChanged();
                sendData();
            }
            return;
        }

        // Eject Items
        Vec3 outPos = VecHelper.getCenterOf(worldPosition).add(itemMovement.scale(.5f).add(0, .5, 0));
        Vec3 outMotion = itemMovement.scale(.0625).add(0, .125, 0);
        for (int slot = 0, size = inventory.getContainerSize(); slot < size; slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty())
                continue;
            ItemEntity entityIn = new ItemEntity(level, outPos.x, outPos.y, outPos.z, inventory.onExtract(stack));
            entityIn.setDeltaMovement(outMotion);
            level.addFreshEntity(entityIn);
        }
        inventory.clearContent();
        level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
        inventory.remainingTime = -1;
        sendData();
    }

    @Override
    public void destroy() {
        super.destroy();
        Containers.dropContents(level, worldPosition, inventory);
    }

    public void spawnEventParticles(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return;

        ParticleOptions particleData = null;
        if (stack.getItem() instanceof BlockItem)
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock().defaultBlockState());
        else
            particleData = new ItemParticleOption(ParticleTypes.ITEM, stack);

        RandomSource r = level.getRandom();
        Vec3 v = VecHelper.getCenterOf(worldPosition).add(0, 5 / 16f, 0);
        for (int i = 0; i < 10; i++) {
            Vec3 m = VecHelper.offsetRandomly(new Vec3(0, 0.25f, 0), r, .125f);
            level.addParticle(particleData, v.x, v.y, v.z, m.x, m.y, m.y);
        }
    }

    protected void spawnParticles(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return;

        ParticleOptions particleData = null;
        float speed = 1;
        if (stack.getItem() instanceof BlockItem)
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock().defaultBlockState());
        else {
            particleData = new ItemParticleOption(ParticleTypes.ITEM, stack);
            speed = .125f;
        }

        RandomSource r = level.getRandom();
        Vec3 vec = getItemMovementVec();
        Vec3 pos = VecHelper.getCenterOf(this.worldPosition);
        float offset = inventory.recipeDuration != 0 ? inventory.remainingTime / inventory.recipeDuration : 0;
        offset /= 2;
        if (inventory.appliedRecipe)
            offset -= .5f;
        level.addParticle(
            particleData,
            pos.x() + -vec.x * offset,
            pos.y() + .45f,
            pos.z() + -vec.z * offset,
            -vec.x * speed,
            r.nextFloat() * speed,
            -vec.z * speed
        );
    }

    public Vec3 getItemMovementVec() {
        boolean alongX = !getBlockState().getValue(SawBlock.AXIS_ALONG_FIRST_COORDINATE);
        int offset = getSpeed() < 0 ? -1 : 1;
        return new Vec3(offset * (alongX ? 1 : 0), 0, offset * (alongX ? 0 : -1));
    }

    private void applyRecipe() {
        ItemStack stack = inventory.getItem(0);
        if (PackageItem.isPackage(stack)) {
            inventory.clearContent();
            inventory.outputAllowInsertion();
            ItemContainerContents contents = stack.getOrDefault(AllDataComponents.PACKAGE_CONTENTS, ItemContainerContents.EMPTY);
            inventory.insert(contents.stream().toList());
            inventory.outputForbidInsertion();
            return;
        }

        SingleRecipeInput input = new SingleRecipeInput(stack);
        Pair<Recipe<SingleRecipeInput>, ItemStack> pair = updateRecipe(input, false);
        if (pair == null)
            return;

        inventory.remainingTime = 0;
        inventory.recipeDuration = 0;
        inventory.appliedRecipe = false;
        inventory.setItem(0, ItemStack.EMPTY);
        ItemStack output = pair.getSecond();
        if (output == null) {
            output = pair.getFirst().assemble(input, level.registryAccess());
        }
        List<ItemStack> list;
        ItemStack recipeRemainder = stack.getItem().getCraftingRemainder();
        if (recipeRemainder.isEmpty()) {
            list = ItemHelper.multipliedOutput(output, stack.getCount());
        } else {
            list = ItemHelper.multipliedOutput(List.of(output, recipeRemainder), stack.getCount());
        }
        for (int slot = 1, listSize = list.size(), invSize = inventory.getContainerSize(); slot < invSize; slot++) {
            inventory.setItem(slot, slot <= listSize ? list.get(slot - 1) : ItemStack.EMPTY);
        }
        award(AllAdvancements.SAW_PROCESSING);
    }

    private static boolean matchCuttingRecipe(RecipeHolder<? extends Recipe<?>> entry) {
        return entry.value().getType() == AllRecipeTypes.CUTTING && !AllRecipeTypes.shouldIgnoreInAutomation(entry);
    }

    private static boolean matchAllRecipe(RecipeHolder<? extends Recipe<?>> entry) {
        RecipeType<? extends Recipe<?>> type = entry.value().getType();
        return (type == AllRecipeTypes.CUTTING || type == RecipeType.STONECUTTING) && !AllRecipeTypes.shouldIgnoreInAutomation(entry);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private Pair<Recipe<SingleRecipeInput>, ItemStack> updateRecipe(SingleRecipeInput input, boolean plus) {
        List<RecipeHolder<?>> startedSearch = RecipeFinder.get(
            cuttingRecipesKey,
            (ServerLevel) level,
            AllConfigs.server().recipes.allowStonecuttingOnSaw.get() ? SawBlockEntity::matchAllRecipe : SawBlockEntity::matchCuttingRecipe
        );
        int index = 0;

        Recipe<SingleRecipeInput> first = null;
        if (filtering.getFilter().isEmpty()) {
            for (RecipeHolder<?> entry : startedSearch) {
                Recipe<SingleRecipeInput> recipe = (Recipe<SingleRecipeInput>) entry.value();
                if (recipe.matches(input, level)) {
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
            RegistryAccess registryManager = level.registryAccess();
            for (RecipeHolder<? extends Recipe<?>> entry : startedSearch) {
                Recipe<SingleRecipeInput> recipe = (Recipe<SingleRecipeInput>) entry.value();
                if (recipe.matches(input, level)) {
                    ItemStack output = recipe.assemble(input, registryManager);
                    if (filtering.test(output)) {
                        return Pair.of(recipe, output);
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
        if (level.isClientSide())
            return;

        inventory.clearContent();

        ItemStack stack = entity.getItem();
        int count = stack.getCount();
        int insert = inventory.insert(stack);
        if (insert == count)
            entity.discard();
        else if (insert != 0) {
            stack.shrink(insert);
            entity.setItem(stack);
        }
    }

    public void start(ItemStack inserted) {
        if (!canProcess())
            return;
        if (inventory.isEmpty())
            return;
        if (level.isClientSide())
            return;

        SingleRecipeInput input = new SingleRecipeInput(inventory.getItem(0));
        Pair<Recipe<SingleRecipeInput>, ItemStack> pair = updateRecipe(input, true);
        int time = 50;

        if (pair == null) {
            inventory.remainingTime = inventory.recipeDuration = 10;
            inventory.appliedRecipe = false;
            sendData();
            return;
        }

        if (pair.getFirst() instanceof CuttingRecipe cuttingRecipe) {
            time = cuttingRecipe.time();
        }

        inventory.remainingTime = time * Math.max(1, (inserted.getCount() / 5));
        inventory.recipeDuration = inventory.remainingTime;
        inventory.appliedRecipe = false;
        sendData();
    }

    protected boolean canProcess() {
        return getBlockState().getValue(SawBlock.FACING) == Direction.UP;
    }

    // Block Breaker

    @Override
    protected boolean shouldRun() {
        return getBlockState().getValue(SawBlock.FACING).getAxis().isHorizontal();
    }

    @Override
    protected BlockPos getBreakingPos() {
        return getBlockPos().relative(getBlockState().getValue(SawBlock.FACING));
    }

    @Override
    public void onBlockBroken(BlockState stateToBreak) {
        //        Optional<AbstractBlockBreakQueue> dynamicTree = TreeCutter.findDynamicTree(stateToBreak.getBlock(), breakingPos);
        //        if (dynamicTree.isPresent()) {
        //            dynamicTree.get().destroyBlocks(world, null, this::dropItemFromCutTree);
        //            return;
        //        }

        super.onBlockBroken(stateToBreak);
        TreeCutter.findTree(level, breakingPos, stateToBreak).destroyBlocks(level, null, this::dropItemFromCutTree);
    }

    public void dropItemFromCutTree(BlockPos pos, ItemStack stack) {
        float distance = (float) Math.sqrt(pos.distSqr(breakingPos));
        Vec3 dropPos = VecHelper.getCenterOf(pos);
        ItemEntity entity = new ItemEntity(level, dropPos.x, dropPos.y, dropPos.z, stack);
        entity.setDeltaMovement(Vec3.atLowerCornerOf(breakingPos.subtract(worldPosition)).scale(distance / 20f));
        level.addFreshEntity(entity);
    }

    @Override
    public boolean canBreak(BlockState stateToBreak, float blockHardness) {
        boolean sawable = isSawable(stateToBreak);
        return super.canBreak(stateToBreak, blockHardness) && sawable;
    }

    public static boolean isSawable(BlockState stateToBreak) {
        if (stateToBreak.is(BlockTags.SAPLINGS))
            return false;
        if (TreeCutter.isLog(stateToBreak) || (stateToBreak.is(BlockTags.LEAVES)))
            return true;
        if (TreeCutter.isRoot(stateToBreak))
            return true;
        Block block = stateToBreak.getBlock();
        if (block instanceof BambooStalkBlock)
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
