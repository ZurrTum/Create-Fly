package com.zurrtum.create.content.kinetics.mixer;

import com.zurrtum.create.AllAdvancements;
import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.press.MechanicalPressBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinBlockEntity;
import com.zurrtum.create.content.processing.basin.BasinInventory;
import com.zurrtum.create.content.processing.basin.BasinOperatingBlockEntity;
import com.zurrtum.create.foundation.advancement.CreateTrigger;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;
import com.zurrtum.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour.TankSegment;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import com.zurrtum.create.infrastructure.particle.FluidParticleData;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MechanicalMixerBlockEntity extends BasinOperatingBlockEntity {

    private static List<Recipe<?>> shapelessOrMixingRecipes;

    public int runningTicks;
    public int processingTicks;
    public boolean running;

    public MechanicalMixerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.MECHANICAL_MIXER, pos, state);
    }

    public float getRenderedHeadOffset(float partialTicks) {
        int localTick;
        float offset = 0;
        if (running) {
            if (runningTicks < 20) {
                localTick = runningTicks;
                float num = (localTick + partialTicks) / 20f;
                num = ((2 - MathHelper.cos((float) (num * Math.PI))) / 2);
                offset = num - .5f;
            } else if (runningTicks <= 20) {
                offset = 1;
            } else {
                localTick = 40 - runningTicks;
                float num = (localTick - partialTicks) / 20f;
                num = ((2 - MathHelper.cos((float) (num * Math.PI))) / 2);
                offset = num - .5f;
            }
        }
        return offset + 7 / 16f;
    }

    public float getRenderedHeadRotationSpeed(float partialTicks) {
        float speed = getSpeed();
        if (running) {
            if (runningTicks < 15) {
                return speed;
            }
            if (runningTicks <= 20) {
                return speed * 2;
            }
            return speed;
        }
        return speed / 2;
    }

    @Override
    public List<CreateTrigger> getAwardables() {
        return List.of(AllAdvancements.MIXER);
    }

    @Override
    protected Box createRenderBoundingBox() {
        return new Box(pos).stretch(0, -1.5, 0);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        running = view.getBoolean("Running", false);
        runningTicks = view.getInt("Ticks", 0);
        super.read(view, clientPacket);

        if (clientPacket && hasWorld())
            getBasin().ifPresent(bte -> bte.setAreFluidsMoving(running && runningTicks <= 20));
    }

    @Override
    protected void write(WriteView view, boolean clientPacket) {
        view.putBoolean("Running", running);
        view.putInt("Ticks", runningTicks);
        super.write(view, clientPacket);
    }

    @Override
    public void tick() {
        super.tick();

        if (runningTicks >= 40) {
            running = false;
            runningTicks = 0;
            basinChecker.scheduleUpdate();
            return;
        }

        float speed = Math.abs(getSpeed());
        if (running && world != null) {
            if (world.isClient && runningTicks == 20)
                renderParticles();

            if ((!world.isClient || isVirtual()) && runningTicks == 20) {
                if (processingTicks < 0) {
                    float recipeSpeed = 1;
                    //TODO
                    //                    if (currentRecipe instanceof StandardProcessingRecipe) {
                    //                        int t = ((StandardProcessingRecipe<?>) currentRecipe).getProcessingDuration();
                    //                        if (t != 0)
                    //                            recipeSpeed = t / 100f;
                    //                    }

                    processingTicks = MathHelper.clamp((MathHelper.floorLog2((int) (512 / speed))) * MathHelper.ceil(recipeSpeed * 15) + 1, 1, 512);

                    Optional<BasinBlockEntity> basin = getBasin();
                    if (basin.isPresent()) {
                        Couple<SmartFluidTankBehaviour> tanks = basin.get().getTanks();
                        if (!tanks.getFirst().isEmpty() || !tanks.getSecond().isEmpty())
                            world.playSound(
                                null,
                                pos,
                                SoundEvents.BLOCK_BUBBLE_COLUMN_WHIRLPOOL_AMBIENT,
                                SoundCategory.BLOCKS,
                                .75f,
                                speed < 65 ? .75f : 1.5f
                            );
                    }

                } else {
                    processingTicks--;
                    if (processingTicks == 0) {
                        runningTicks++;
                        processingTicks = -1;
                        applyBasinRecipe();
                        sendData();
                    }
                }
            }

            if (runningTicks != 20)
                runningTicks++;
        }
    }

    public void renderParticles() {
        Optional<BasinBlockEntity> basin = getBasin();
        if (basin.isEmpty() || world == null)
            return;

        BasinInventory inv = basin.get().itemCapability;
        for (int slot = 0, size = inv.size(); slot < size; slot++) {
            ItemStack stackInSlot = inv.getStack(slot);
            if (stackInSlot.isEmpty())
                continue;
            ItemStackParticleEffect data = new ItemStackParticleEffect(ParticleTypes.ITEM, stackInSlot);
            spillParticle(data);
        }

        for (SmartFluidTankBehaviour behaviour : basin.get().getTanks()) {
            if (behaviour == null)
                continue;
            for (TankSegment tankSegment : behaviour.getTanks()) {
                if (tankSegment.isEmpty(0))
                    continue;
                spillParticle(new FluidParticleData(AllParticleTypes.FLUID_PARTICLE, tankSegment.getRenderedFluid().getFluid()));
            }
        }
    }

    protected void spillParticle(ParticleEffect data) {
        float angle = world.random.nextFloat() * 360;
        Vec3d offset = new Vec3d(0, 0, 0.25f);
        offset = VecHelper.rotate(offset, angle, Axis.Y);
        Vec3d target = VecHelper.rotate(offset, getSpeed() > 0 ? 25 : -25, Axis.Y).add(0, .25f, 0);
        Vec3d center = offset.add(VecHelper.getCenterOf(pos));
        target = VecHelper.offsetRandomly(target.subtract(offset), world.random, 1 / 128f);
        world.addParticleClient(data, center.x, center.y - 1.75f, center.z, target.x, target.y, target.z);
    }

    //TODO
    //    @Override
    //    protected Optional<Recipe<?>> getMatchingRecipes() {
    //        Optional<Recipe<?>> matchingRecipes = super.getMatchingRecipes();
    //
    //        if (!AllConfigs.server().recipes.allowBrewingInMixer.get())
    //            return matchingRecipes;
    //
    //        Optional<BasinBlockEntity> basin = getBasin();
    //        if (basin.isEmpty())
    //            return matchingRecipes;
    //
    //        BasinBlockEntity basinBlockEntity = basin.get();
    //        if (basinBlockEntity.isEmpty())
    //            return matchingRecipes;
    //
    //        Inventory availableItems = basinBlockEntity.itemCapability;
    //
    //        for (int i = 0, size = availableItems.size(); i < size; i++) {
    //            ItemStack stack = availableItems.getStack(i);
    //            if (stack.isEmpty())
    //                continue;
    //
    //            List<MixingRecipe> list = PotionMixingRecipes.sortRecipesByItem(level).get(stack.getItem());
    //            if (list == null)
    //                continue;
    //            for (MixingRecipe mixingRecipe : list)
    //                if (matchBasinRecipe(mixingRecipe))
    //                    matchingRecipes.add(mixingRecipe);
    //        }
    //
    //        return matchingRecipes;
    //    }

    @Override
    protected List<Recipe<?>> getBasinRecipes() {
        if (shapelessOrMixingRecipes != null) {
            return shapelessOrMixingRecipes;
        }
        shapelessOrMixingRecipes = new ArrayList<>();
        PreparedRecipes allRecipes = ((ServerWorld) world).getRecipeManager().preparedRecipes;
        allRecipes.getAll(AllRecipeTypes.MIXING).stream().map(RecipeEntry::value).forEach(shapelessOrMixingRecipes::add);
        if (AllConfigs.server().recipes.allowShapelessInMixer.get()) {
            allRecipes.byType.asMap().forEach((type, recipes) -> {
                if (recipes.isEmpty()) {
                    return;
                }
                if (type != RecipeType.CRAFTING) {
                    RecipeEntry<?> first = recipes.iterator().next();
                    if (!(first.value() instanceof ShapelessRecipe) || AllRecipeTypes.AUTOMATION_IGNORE.test(first)) {
                        return;
                    }
                }
                recipes.forEach(entry -> {
                    if (entry.value() instanceof ShapelessRecipe recipe && recipe.ingredients.size() > 1 && AllRecipeTypes.CAN_BE_AUTOMATED.test(entry) && !MechanicalPressBlockEntity.canCompress(
                        recipe)) {
                        shapelessOrMixingRecipes.add(recipe);
                    }
                });
            });
        }
        return shapelessOrMixingRecipes;
    }

    @Override
    public void startProcessingBasin() {
        if (running && runningTicks <= 20)
            return;
        super.startProcessingBasin();
        running = true;
        runningTicks = 0;
    }

    @Override
    public boolean continueWithPreviousRecipe() {
        runningTicks = 20;
        return true;
    }

    @Override
    protected void onBasinRemoved() {
        if (!running)
            return;
        runningTicks = 40;
        running = false;
    }

    @Override
    protected boolean isRunning() {
        return running;
    }

    @Override
    protected Optional<CreateTrigger> getProcessedRecipeTrigger() {
        return Optional.of(AllAdvancements.MIXER);
    }
}
