package com.zurrtum.create.content.kinetics.fan.processing;

import com.zurrtum.create.*;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.LitBlazeBurnerBlock;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.SkeletonHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.*;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.zurrtum.create.Create.LOGGER;
import static com.zurrtum.create.Create.MOD_ID;

public class AllFanProcessingTypes {
    public static final BlastingType BLASTING = register("blasting", new BlastingType());
    public static final HauntingType HAUNTING = register("haunting", new HauntingType());
    public static final SmokingType SMOKING = register("smoking", new SmokingType());
    public static final SplashingType SPLASHING = register("splashing", new SplashingType());

    private static <T extends FanProcessingType> T register(String name, T type) {
        return Registry.register(CreateRegistries.FAN_PROCESSING_TYPE, Identifier.of(MOD_ID, name), type);
    }

    public static void register() {
    }

    public static class BlastingType implements FanProcessingType {
        @Override
        public boolean isValidAt(World level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.isIn(AllFluidTags.FAN_PROCESSING_CATALYSTS_BLASTING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            if (blockState.isIn(AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING)) {
                return !blockState.contains(BlazeBurnerBlock.HEAT_LEVEL) || blockState.get(BlazeBurnerBlock.HEAT_LEVEL)
                    .isAtLeast(BlazeBurnerBlock.HeatLevel.FADING);
            }
            return false;
        }

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public boolean canProcess(ItemStack stack, World level) {
            SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
            ServerRecipeManager recipeManager = ((ServerWorld) level).getRecipeManager();
            Optional<RecipeEntry<SmeltingRecipe>> smeltingRecipe = recipeManager.getFirstMatch(RecipeType.SMELTING, input, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

            if (smeltingRecipe.isPresent())
                return true;

            Optional<RecipeEntry<BlastingRecipe>> blastingRecipe = recipeManager.getFirstMatch(RecipeType.BLASTING, input, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

            if (blastingRecipe.isPresent())
                return true;

            return !stack.contains(DataComponentTypes.DAMAGE_RESISTANT);
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, World level) {
            SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
            ServerRecipeManager recipeManager = ((ServerWorld) level).getRecipeManager();

            Optional<? extends RecipeEntry<? extends Recipe<SingleStackRecipeInput>>> smeltingRecipe = recipeManager.getFirstMatch(
                RecipeType.SMELTING,
                input,
                level
            ).filter(AllRecipeTypes.CAN_BE_AUTOMATED);

            if (smeltingRecipe.isEmpty()) {
                smeltingRecipe = recipeManager.getFirstMatch(RecipeType.BLASTING, input, level).filter(AllRecipeTypes.CAN_BE_AUTOMATED);
            }

            if (smeltingRecipe.isPresent()) {
                Optional<RecipeEntry<SmokingRecipe>> smokingRecipe = recipeManager.getFirstMatch(RecipeType.SMOKING, input, level)
                    .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
                DynamicRegistryManager registryAccess = level.getRegistryManager();
                if (smokingRecipe.isEmpty() || !ItemStack.areItemsEqual(
                    smokingRecipe.get().value().craft(input, registryAccess),
                    smeltingRecipe.get().value().craft(input, registryAccess)
                )) {
                    return RecipeApplier.applyRecipeOn(level, stack.getCount(), input, smeltingRecipe.get(), false);
                }
            }

            return Collections.emptyList();
        }

        @Override
        public void spawnProcessingParticles(World level, Vec3d pos) {
            if (level.random.nextInt(8) != 0)
                return;
            level.addParticleClient(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + .25f, pos.z, 0, 1 / 16f, 0);
        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, Random random) {
            particleAccess.setColor(Color.mixColors(0xFF4400, 0xFF8855, random.nextFloat()));
            particleAccess.setAlpha(.5f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.FLAME, .25f);
            if (random.nextFloat() < 1 / 16f)
                particleAccess.spawnExtraParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK, Blocks.LAVA.getDefaultState()), .25f);
        }

        @Override
        public void affectEntity(Entity entity, World level) {
            if (level.isClient)
                return;

            if (!entity.isFireImmune()) {
                entity.setOnFireFor(10);
                entity.damage((ServerWorld) level, AllDamageSources.get(level).fan_lava, 4);
            }
        }
    }

    public static class HauntingType implements FanProcessingType {
        @Override
        public boolean isValidAt(World level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.isIn(AllFluidTags.FAN_PROCESSING_CATALYSTS_HAUNTING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            if (blockState.isIn(AllBlockTags.FAN_PROCESSING_CATALYSTS_HAUNTING)) {
                if (blockState.isIn(BlockTags.CAMPFIRES) && blockState.contains(CampfireBlock.LIT) && !blockState.get(CampfireBlock.LIT)) {
                    return false;
                }
                return !blockState.contains(LitBlazeBurnerBlock.FLAME_TYPE) || blockState.get(LitBlazeBurnerBlock.FLAME_TYPE) == LitBlazeBurnerBlock.FlameType.SOUL;
            }
            return false;
        }

        @Override
        public int getPriority() {
            return 300;
        }

        @Override
        public boolean canProcess(ItemStack stack, World level) {
            return level.getRecipeManager().getPropertySet(AllRecipeSets.HAUNTING).canUse(stack);
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, World level) {
            SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
            Optional<RecipeEntry<HauntingRecipe>> recipe = ((ServerWorld) level).getRecipeManager()
                .getFirstMatch(AllRecipeTypes.HAUNTING, input, level);
            return recipe.map(entry -> RecipeApplier.applyCreateRecipeOn(level, stack.getCount(), input, entry.value(), true)).orElse(null);
        }

        @Override
        public void spawnProcessingParticles(World level, Vec3d pos) {
            if (level.random.nextInt(8) != 0)
                return;
            pos = pos.add(VecHelper.offsetRandomly(Vec3d.ZERO, level.random, 1).multiply(1, 0.05f, 1).normalize().multiply(0.15f));
            level.addParticleClient(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y + .45f, pos.z, 0, 0, 0);
            if (level.random.nextInt(2) == 0)
                level.addParticleClient(ParticleTypes.SMOKE, pos.x, pos.y + .25f, pos.z, 0, 0, 0);
        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, Random random) {
            particleAccess.setColor(Color.mixColors(0x0, 0x126568, random.nextFloat()));
            particleAccess.setAlpha(1f);
            if (random.nextFloat() < 1 / 128f)
                particleAccess.spawnExtraParticle(ParticleTypes.SOUL_FIRE_FLAME, .125f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.SMOKE, .125f);
        }

        @Override
        public void affectEntity(Entity entity, World level) {
            if (level.isClient) {
                if (entity instanceof HorseEntity) {
                    Vec3d p = entity.getLerpedPos(0);
                    Vec3d v = p.add(0, 0.5f, 0)
                        .add(VecHelper.offsetRandomly(Vec3d.ZERO, level.random, 1).multiply(1, 0.2f, 1).normalize().multiply(1f));
                    level.addParticleClient(ParticleTypes.SOUL_FIRE_FLAME, v.x, v.y, v.z, 0, 0.1f, 0);
                    if (level.random.nextInt(3) == 0)
                        level.addParticleClient(
                            ParticleTypes.LARGE_SMOKE,
                            p.x,
                            p.y + .5f,
                            p.z,
                            (level.random.nextFloat() - .5f) * .5f,
                            0.1f,
                            (level.random.nextFloat() - .5f) * .5f
                        );
                }
                return;
            }

            if (entity instanceof LivingEntity livingEntity) {
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 30, 0, false, false));
                livingEntity.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 20, 1, false, false));
            }
            if (entity instanceof HorseEntity horse) {
                int progress = AllSynchedDatas.HAUNTING.get(horse);
                if (progress < 100) {
                    if (progress % 10 == 0) {
                        level.playSound(
                            null,
                            entity.getBlockPos(),
                            SoundEvents.PARTICLE_SOUL_ESCAPE.value(),
                            SoundCategory.NEUTRAL,
                            1f,
                            1.5f * progress / 100f
                        );
                    }
                    AllSynchedDatas.HAUNTING.set(horse, progress + 1);
                    return;
                }

                level.playSound(null, entity.getBlockPos(), SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE, SoundCategory.NEUTRAL, 1.25f, 0.65f);

                SkeletonHorseEntity skeletonHorse = EntityType.SKELETON_HORSE.create(level, SpawnReason.NATURAL);
                DynamicRegistryManager registryManager = level.getRegistryManager();
                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(skeletonHorse.getErrorReporterContext(), LOGGER)) {
                    NbtWriteView view = NbtWriteView.create(logging, registryManager);
                    horse.writeData(view);
                    NbtCompound serializeNBT = view.getNbt();
                    serializeNBT.remove("UUID");
                    skeletonHorse.readData(NbtReadView.create(logging, registryManager, serializeNBT));
                }
                if (!horse.getBodyArmor().isEmpty())
                    horse.dropStack((ServerWorld) level, horse.getBodyArmor());
                skeletonHorse.setPosition(horse.getLerpedPos(0));
                level.spawnEntity(skeletonHorse);
                horse.discard();
            }
        }
    }

    public static class SmokingType implements FanProcessingType {
        @Override
        public boolean isValidAt(World level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.isIn(AllFluidTags.FAN_PROCESSING_CATALYSTS_SMOKING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            if (blockState.isIn(AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING)) {
                if (blockState.isIn(BlockTags.CAMPFIRES) && blockState.contains(CampfireBlock.LIT) && !blockState.get(CampfireBlock.LIT)) {
                    return false;
                }
                if (blockState.contains(LitBlazeBurnerBlock.FLAME_TYPE) && blockState.get(LitBlazeBurnerBlock.FLAME_TYPE) != LitBlazeBurnerBlock.FlameType.REGULAR) {
                    return false;
                }
                return !blockState.contains(BlazeBurnerBlock.HEAT_LEVEL) || blockState.get(BlazeBurnerBlock.HEAT_LEVEL) == BlazeBurnerBlock.HeatLevel.SMOULDERING;
            }
            return false;
        }

        @Override
        public int getPriority() {
            return 200;
        }

        @Override
        public boolean canProcess(ItemStack stack, World level) {
            return ((ServerWorld) level).getRecipeManager().getFirstMatch(RecipeType.SMOKING, new SingleStackRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED).isPresent();
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, World level) {
            SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
            return ((ServerWorld) level).getRecipeManager().getFirstMatch(RecipeType.SMOKING, input, level).filter(AllRecipeTypes.CAN_BE_AUTOMATED)
                .map(entry -> RecipeApplier.applyRecipeOn(level, stack.getCount(), input, entry, false)).orElse(null);
        }

        @Override
        public void spawnProcessingParticles(World level, Vec3d pos) {
            if (level.random.nextInt(8) != 0)
                return;
            level.addParticleClient(ParticleTypes.POOF, pos.x, pos.y + .25f, pos.z, 0, 1 / 16f, 0);
        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, Random random) {
            particleAccess.setColor(Color.mixColors(0x0, 0x555555, random.nextFloat()));
            particleAccess.setAlpha(1f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.SMOKE, .125f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.LARGE_SMOKE, .125f);
        }

        @Override
        public void affectEntity(Entity entity, World level) {
            if (level.isClient)
                return;

            if (!entity.isFireImmune()) {
                entity.setOnFireFor(2);
                entity.damage((ServerWorld) level, AllDamageSources.get(level).fan_fire, 2);
            }
        }
    }

    public static class SplashingType implements FanProcessingType {
        @Override
        public boolean isValidAt(World level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.isIn(AllFluidTags.FAN_PROCESSING_CATALYSTS_SPLASHING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            return blockState.isIn(AllBlockTags.FAN_PROCESSING_CATALYSTS_SPLASHING);
        }

        @Override
        public int getPriority() {
            return 400;
        }

        @Override
        public boolean canProcess(ItemStack stack, World level) {
            return level.getRecipeManager().getPropertySet(AllRecipeSets.SPLASHING).canUse(stack);
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, World level) {
            SingleStackRecipeInput input = new SingleStackRecipeInput(stack);
            return ((ServerWorld) level).getRecipeManager().getFirstMatch(AllRecipeTypes.SPLASHING, input, level)
                .map(entry -> RecipeApplier.applyCreateRecipeOn(level, stack.getCount(), input, entry.value(), true)).orElse(null);
        }

        @Override
        public void spawnProcessingParticles(World level, Vec3d pos) {
            if (level.random.nextInt(8) != 0)
                return;
            level.addParticleClient(
                new DustParticleEffect(0x0055FF, 1),
                pos.x + (level.random.nextFloat() - .5f) * .5f,
                pos.y + .5f,
                pos.z + (level.random.nextFloat() - .5f) * .5f,
                0,
                1 / 8f,
                0
            );
            level.addParticleClient(
                ParticleTypes.SPIT,
                pos.x + (level.random.nextFloat() - .5f) * .5f,
                pos.y + .5f,
                pos.z + (level.random.nextFloat() - .5f) * .5f,
                0,
                1 / 8f,
                0
            );
        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, Random random) {
            particleAccess.setColor(Color.mixColors(0x4499FF, 0x2277FF, random.nextFloat()));
            particleAccess.setAlpha(1f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.BUBBLE, .125f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.BUBBLE_POP, .125f);
        }

        @Override
        public void affectEntity(Entity entity, World level) {
            if (level.isClient)
                return;

            if (entity instanceof EndermanEntity || entity.getType() == EntityType.SNOW_GOLEM || entity.getType() == EntityType.BLAZE) {
                entity.damage((ServerWorld) level, entity.getDamageSources().drown(), 2);
            }
            if (entity.isOnFire()) {
                entity.extinguish();
                level.playSound(
                    null,
                    entity.getBlockPos(),
                    SoundEvents.ENTITY_GENERIC_EXTINGUISH_FIRE,
                    SoundCategory.NEUTRAL,
                    0.7F,
                    1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F
                );
            }
        }
    }
}
