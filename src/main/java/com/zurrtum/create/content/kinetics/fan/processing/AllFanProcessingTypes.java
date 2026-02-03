package com.zurrtum.create.content.kinetics.fan.processing;

import com.zurrtum.create.*;
import com.zurrtum.create.api.registry.CreateRegistries;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.content.processing.burner.BlazeBurnerBlock;
import com.zurrtum.create.content.processing.burner.LitBlazeBurnerBlock;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.foundation.recipe.RecipeApplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.equine.Horse;
import net.minecraft.world.entity.animal.equine.SkeletonHorse;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
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
        return Registry.register(CreateRegistries.FAN_PROCESSING_TYPE, Identifier.fromNamespaceAndPath(MOD_ID, name), type);
    }

    public static void register() {
    }

    public static class BlastingType implements FanProcessingType {
        @Override
        public boolean isValidAt(Level level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.is(AllFluidTags.FAN_PROCESSING_CATALYSTS_BLASTING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            if (blockState.is(AllBlockTags.FAN_PROCESSING_CATALYSTS_BLASTING)) {
                return !blockState.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) || blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL)
                    .isAtLeast(BlazeBurnerBlock.HeatLevel.FADING);
            }
            return false;
        }

        @Override
        public int getPriority() {
            return 100;
        }

        @Override
        public boolean canProcess(ItemStack stack, Level level) {
            SingleRecipeInput input = new SingleRecipeInput(stack);
            RecipeManager recipeManager = ((ServerLevel) level).recipeAccess();
            Optional<RecipeHolder<SmeltingRecipe>> smeltingRecipe = recipeManager.getRecipeFor(RecipeType.SMELTING, input, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

            if (smeltingRecipe.isPresent())
                return true;

            Optional<RecipeHolder<BlastingRecipe>> blastingRecipe = recipeManager.getRecipeFor(RecipeType.BLASTING, input, level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED);

            if (blastingRecipe.isPresent())
                return true;

            return !stack.has(DataComponents.DAMAGE_RESISTANT);
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, Level level) {
            SingleRecipeInput input = new SingleRecipeInput(stack);
            RecipeManager recipeManager = ((ServerLevel) level).recipeAccess();

            Optional<? extends RecipeHolder<? extends Recipe<SingleRecipeInput>>> smeltingRecipe = recipeManager.getRecipeFor(
                RecipeType.SMELTING,
                input,
                level
            ).filter(AllRecipeTypes.CAN_BE_AUTOMATED);

            if (smeltingRecipe.isEmpty()) {
                smeltingRecipe = recipeManager.getRecipeFor(RecipeType.BLASTING, input, level).filter(AllRecipeTypes.CAN_BE_AUTOMATED);
            }

            if (smeltingRecipe.isPresent()) {
                Optional<RecipeHolder<SmokingRecipe>> smokingRecipe = recipeManager.getRecipeFor(RecipeType.SMOKING, input, level)
                    .filter(AllRecipeTypes.CAN_BE_AUTOMATED);
                RegistryAccess registryAccess = level.registryAccess();
                ItemStack result = smeltingRecipe.get().value().assemble(input, registryAccess);
                if (smokingRecipe.isEmpty() || !ItemStack.isSameItem(smokingRecipe.get().value().assemble(input, registryAccess), result)) {
                    return ItemHelper.multipliedOutput(result, stack.getCount());
                }
            }

            return Collections.emptyList();
        }

        @Override
        public void spawnProcessingParticles(Level level, Vec3 pos) {
            if (level.random.nextInt(8) != 0)
                return;
            level.addParticle(ParticleTypes.LARGE_SMOKE, pos.x, pos.y + .25f, pos.z, 0, 1 / 16f, 0);
        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
            particleAccess.setColor(Color.mixColors(0xFF4400, 0xFF8855, random.nextFloat()));
            particleAccess.setAlpha(.5f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.FLAME, .25f);
            if (random.nextFloat() < 1 / 16f)
                particleAccess.spawnExtraParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.LAVA.defaultBlockState()), .25f);
        }

        @Override
        public void affectEntity(Entity entity, Level level) {
            if (level.isClientSide())
                return;

            if (!entity.fireImmune()) {
                entity.igniteForSeconds(10);
                entity.hurtServer((ServerLevel) level, AllDamageSources.get(level).fan_lava, 4);
            }
        }
    }

    public static class HauntingType implements FanProcessingType {
        @Override
        public boolean isValidAt(Level level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.is(AllFluidTags.FAN_PROCESSING_CATALYSTS_HAUNTING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            if (blockState.is(AllBlockTags.FAN_PROCESSING_CATALYSTS_HAUNTING)) {
                if (blockState.is(BlockTags.CAMPFIRES) && blockState.hasProperty(CampfireBlock.LIT) && !blockState.getValue(CampfireBlock.LIT)) {
                    return false;
                }
                return !blockState.hasProperty(LitBlazeBurnerBlock.FLAME_TYPE) || blockState.getValue(LitBlazeBurnerBlock.FLAME_TYPE) == LitBlazeBurnerBlock.FlameType.SOUL;
            }
            return false;
        }

        @Override
        public int getPriority() {
            return 300;
        }

        @Override
        public boolean canProcess(ItemStack stack, Level level) {
            return level.recipeAccess().propertySet(AllRecipeSets.HAUNTING).test(stack);
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, Level level) {
            SingleRecipeInput input = new SingleRecipeInput(stack);
            Optional<RecipeHolder<HauntingRecipe>> recipe = ((ServerLevel) level).recipeAccess().getRecipeFor(AllRecipeTypes.HAUNTING, input, level);
            return recipe.map(entry -> RecipeApplier.applyRecipeOn(level.getRandom(), stack.getCount(), input, entry.value())).orElse(null);
        }

        @Override
        public void spawnProcessingParticles(Level level, Vec3 pos) {
            if (level.random.nextInt(8) != 0)
                return;
            pos = pos.add(VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1).multiply(1, 0.05f, 1).normalize().scale(0.15f));
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, pos.x, pos.y + .45f, pos.z, 0, 0, 0);
            if (level.random.nextInt(2) == 0)
                level.addParticle(ParticleTypes.SMOKE, pos.x, pos.y + .25f, pos.z, 0, 0, 0);
        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
            particleAccess.setColor(Color.mixColors(0x0, 0x126568, random.nextFloat()));
            particleAccess.setAlpha(1f);
            if (random.nextFloat() < 1 / 128f)
                particleAccess.spawnExtraParticle(ParticleTypes.SOUL_FIRE_FLAME, .125f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.SMOKE, .125f);
        }

        @Override
        public void affectEntity(Entity entity, Level level) {
            if (level.isClientSide()) {
                if (entity instanceof Horse) {
                    Vec3 p = entity.getPosition(0);
                    Vec3 v = p.add(0, 0.5f, 0).add(VecHelper.offsetRandomly(Vec3.ZERO, level.random, 1).multiply(1, 0.2f, 1).normalize().scale(1f));
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, v.x, v.y, v.z, 0, 0.1f, 0);
                    if (level.random.nextInt(3) == 0)
                        level.addParticle(
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
                livingEntity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 30, 0, false, false));
                livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 1, false, false));
            }
            if (entity instanceof Horse horse) {
                int progress = AllSynchedDatas.HAUNTING.get(horse);
                if (progress < 100) {
                    if (progress % 10 == 0) {
                        level.playSound(
                            null,
                            entity.blockPosition(),
                            SoundEvents.SOUL_ESCAPE.value(),
                            SoundSource.NEUTRAL,
                            1f,
                            1.5f * progress / 100f
                        );
                    }
                    AllSynchedDatas.HAUNTING.set(horse, progress + 1);
                    return;
                }

                level.playSound(null, entity.blockPosition(), SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.NEUTRAL, 1.25f, 0.65f);

                SkeletonHorse skeletonHorse = EntityType.SKELETON_HORSE.create(level, EntitySpawnReason.NATURAL);
                RegistryAccess registryManager = level.registryAccess();
                try (ProblemReporter.ScopedCollector logging = new ProblemReporter.ScopedCollector(skeletonHorse.problemPath(), LOGGER)) {
                    TagValueOutput view = TagValueOutput.createWithContext(logging, registryManager);
                    horse.saveWithoutId(view);
                    CompoundTag serializeNBT = view.buildResult();
                    serializeNBT.remove("UUID");
                    skeletonHorse.load(TagValueInput.create(logging, registryManager, serializeNBT));
                }
                if (!horse.getBodyArmorItem().isEmpty())
                    horse.spawnAtLocation((ServerLevel) level, horse.getBodyArmorItem());
                skeletonHorse.setPos(horse.getPosition(0));
                level.addFreshEntity(skeletonHorse);
                horse.discard();
            }
        }
    }

    public static class SmokingType implements FanProcessingType {
        @Override
        public boolean isValidAt(Level level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.is(AllFluidTags.FAN_PROCESSING_CATALYSTS_SMOKING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            if (blockState.is(AllBlockTags.FAN_PROCESSING_CATALYSTS_SMOKING)) {
                if (blockState.is(BlockTags.CAMPFIRES) && blockState.hasProperty(CampfireBlock.LIT) && !blockState.getValue(CampfireBlock.LIT)) {
                    return false;
                }
                if (blockState.hasProperty(LitBlazeBurnerBlock.FLAME_TYPE) && blockState.getValue(LitBlazeBurnerBlock.FLAME_TYPE) != LitBlazeBurnerBlock.FlameType.REGULAR) {
                    return false;
                }
                return !blockState.hasProperty(BlazeBurnerBlock.HEAT_LEVEL) || blockState.getValue(BlazeBurnerBlock.HEAT_LEVEL) == BlazeBurnerBlock.HeatLevel.SMOULDERING;
            }
            return false;
        }

        @Override
        public int getPriority() {
            return 200;
        }

        @Override
        public boolean canProcess(ItemStack stack, Level level) {
            return ((ServerLevel) level).recipeAccess().getRecipeFor(RecipeType.SMOKING, new SingleRecipeInput(stack), level)
                .filter(AllRecipeTypes.CAN_BE_AUTOMATED).isPresent();
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, Level level) {
            SingleRecipeInput input = new SingleRecipeInput(stack);
            return ((ServerLevel) level).recipeAccess().getRecipeFor(RecipeType.SMOKING, input, level).filter(AllRecipeTypes.CAN_BE_AUTOMATED)
                .map(entry -> ItemHelper.multipliedOutput(entry.value().assemble(input, level.registryAccess()), stack.getCount())).orElse(null);
        }

        @Override
        public void spawnProcessingParticles(Level level, Vec3 pos) {
            if (level.random.nextInt(8) != 0)
                return;
            level.addParticle(ParticleTypes.POOF, pos.x, pos.y + .25f, pos.z, 0, 1 / 16f, 0);
        }

        @Override
        public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
            particleAccess.setColor(Color.mixColors(0x0, 0x555555, random.nextFloat()));
            particleAccess.setAlpha(1f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.SMOKE, .125f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.LARGE_SMOKE, .125f);
        }

        @Override
        public void affectEntity(Entity entity, Level level) {
            if (level.isClientSide())
                return;

            if (!entity.fireImmune()) {
                entity.igniteForSeconds(2);
                entity.hurtServer((ServerLevel) level, AllDamageSources.get(level).fan_fire, 2);
            }
        }
    }

    public static class SplashingType implements FanProcessingType {
        @Override
        public boolean isValidAt(Level level, BlockPos pos) {
            FluidState fluidState = level.getFluidState(pos);
            if (fluidState.is(AllFluidTags.FAN_PROCESSING_CATALYSTS_SPLASHING)) {
                return true;
            }
            BlockState blockState = level.getBlockState(pos);
            return blockState.is(AllBlockTags.FAN_PROCESSING_CATALYSTS_SPLASHING);
        }

        @Override
        public int getPriority() {
            return 400;
        }

        @Override
        public boolean canProcess(ItemStack stack, Level level) {
            return level.recipeAccess().propertySet(AllRecipeSets.SPLASHING).test(stack);
        }

        @Override
        @Nullable
        public List<ItemStack> process(ItemStack stack, Level level) {
            SingleRecipeInput input = new SingleRecipeInput(stack);
            return ((ServerLevel) level).recipeAccess().getRecipeFor(AllRecipeTypes.SPLASHING, input, level)
                .map(entry -> RecipeApplier.applyRecipeOn(level.getRandom(), stack.getCount(), input, entry.value())).orElse(null);
        }

        @Override
        public void spawnProcessingParticles(Level level, Vec3 pos) {
            if (level.random.nextInt(8) != 0)
                return;
            level.addParticle(
                new DustParticleOptions(0x0055FF, 1),
                pos.x + (level.random.nextFloat() - .5f) * .5f,
                pos.y + .5f,
                pos.z + (level.random.nextFloat() - .5f) * .5f,
                0,
                1 / 8f,
                0
            );
            level.addParticle(
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
        public void morphAirFlow(AirFlowParticleAccess particleAccess, RandomSource random) {
            particleAccess.setColor(Color.mixColors(0x4499FF, 0x2277FF, random.nextFloat()));
            particleAccess.setAlpha(1f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.BUBBLE, .125f);
            if (random.nextFloat() < 1 / 32f)
                particleAccess.spawnExtraParticle(ParticleTypes.BUBBLE_POP, .125f);
        }

        @Override
        public void affectEntity(Entity entity, Level level) {
            if (level.isClientSide())
                return;

            if (entity instanceof EnderMan || entity.getType() == EntityType.SNOW_GOLEM || entity.getType() == EntityType.BLAZE) {
                entity.hurtServer((ServerLevel) level, entity.damageSources().drown(), 2);
            }
            if (entity.isOnFire()) {
                entity.clearFire();
                level.playSound(
                    null,
                    entity.blockPosition(),
                    SoundEvents.GENERIC_EXTINGUISH_FIRE,
                    SoundSource.NEUTRAL,
                    0.7F,
                    1.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.4F
                );
            }
        }
    }
}
