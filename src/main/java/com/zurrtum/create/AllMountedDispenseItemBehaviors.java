package com.zurrtum.create;

import com.zurrtum.create.api.contraption.dispenser.DefaultMountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.MountedProjectileDispenseBehavior;
import com.zurrtum.create.api.contraption.dispenser.OptionalMountedDispenseBehavior;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BeehiveBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FluidDrainable;
import net.minecraft.block.entity.BeehiveBlockEntity;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.SmallFireballEntity;
import net.minecraft.entity.projectile.thrown.LingeringPotionEntity;
import net.minecraft.entity.projectile.thrown.SplashPotionEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.potion.Potions;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldEvents;
import org.jetbrains.annotations.Nullable;

public class AllMountedDispenseItemBehaviors {
    private static final MountedDispenseBehavior SPAWN_EGG = new DefaultMountedDispenseBehavior() {
        @Override
        protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
            if (!(stack.getItem() instanceof SpawnEggItem egg))
                return super.execute(stack, context, pos, facing);

            if (context.world instanceof ServerWorld serverLevel) {
                EntityType<?> type = egg.getEntityType(serverLevel.getRegistryManager(), stack);
                BlockPos offset = BlockPos.ofFloored(facing.x + .7, facing.y + .7, facing.z + .7);
                Entity entity = type.spawnFromItemStack(serverLevel, stack, null, pos.add(offset), SpawnReason.DISPENSER, facing.y < .5, false);
                if (entity != null) {
                    entity.setVelocity(context.motion.multiply(2));
                }
            }

            stack.decrement(1);
            return stack;
        }
    };
    private static final MountedDispenseBehavior TNT = new DefaultMountedDispenseBehavior() {
        @Override
        protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
            double x = pos.getX() + facing.x * .7 + .5;
            double y = pos.getY() + facing.y * .7 + .5;
            double z = pos.getZ() + facing.z * .7 + .5;
            TntEntity tnt = new TntEntity(context.world, x, y, z, null);
            tnt.addVelocity(context.motion.x, context.motion.y, context.motion.z);
            context.world.spawnEntity(tnt);
            context.world.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.ENTITY_TNT_PRIMED, SoundCategory.BLOCKS, 1, 1);
            stack.decrement(1);
            return stack;
        }
    };
    private static final MountedDispenseBehavior FIREWORK = new DefaultMountedDispenseBehavior() {
        @Override
        protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
            double x = pos.getX() + facing.x * .7 + .5;
            double y = pos.getY() + facing.y * .7 + .5;
            double z = pos.getZ() + facing.z * .7 + .5;
            FireworkRocketEntity firework = new FireworkRocketEntity(context.world, stack, x, y, z, true);
            firework.setVelocity(facing.x, facing.y, facing.z, 0.5F, 1.0F);
            context.world.spawnEntity(firework);
            stack.decrement(1);
            return stack;
        }

        @Override
        protected void playSound(WorldAccess level, BlockPos pos) {
            level.syncWorldEvent(WorldEvents.FIREWORK_ROCKET_SHOOTS, pos, 0);
        }
    };
    private static final MountedDispenseBehavior FIRE_CHARGE = new DefaultMountedDispenseBehavior() {
        @Override
        protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
            Random random = context.world.random;
            double x = pos.getX() + facing.x * .7 + .5;
            double y = pos.getY() + facing.y * .7 + .5;
            double z = pos.getZ() + facing.z * .7 + .5;
            SmallFireballEntity fireball = new SmallFireballEntity(
                context.world, x, y, z, new Vec3d(
                random.nextGaussian() * 0.05 + facing.x + context.motion.x,
                random.nextGaussian() * 0.05 + facing.y + context.motion.y,
                random.nextGaussian() * 0.05 + facing.z + context.motion.z
            ).normalize()
            );
            fireball.setItem(stack); // copies the stack
            context.world.spawnEntity(fireball);
            stack.decrement(1);
            return stack;
        }

        @Override
        protected void playSound(WorldAccess level, BlockPos pos) {
            level.syncWorldEvent(WorldEvents.BLAZE_SHOOTS, pos, 0);
        }
    };
    private static final MountedDispenseBehavior BUCKET = new DefaultMountedDispenseBehavior() {
        @Override
        protected ItemStack execute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
            BlockPos interactionPos = pos.offset(MountedDispenseBehavior.getClosestFacingDirection(facing));
            BlockState state = context.world.getBlockState(interactionPos);
            if (!(state.getBlock() instanceof FluidDrainable bucketPickup)) {
                return super.execute(stack, context, pos, facing);
            }

            ItemStack bucket = bucketPickup.tryDrainFluid(null, context.world, interactionPos, state);
            MountedDispenseBehavior.placeItemInInventory(bucket, context, pos);
            stack.decrement(1);
            return stack;
        }
    };
    private static final MountedDispenseBehavior SPLASH_POTIONS = new MountedProjectileDispenseBehavior() {
        @Override
        protected ProjectileEntity getProjectile(World level, double x, double y, double z, ItemStack stack, Direction facing) {
            return new SplashPotionEntity(level, x, y, z, stack);
        }

        @Override
        protected float getUncertainty() {
            return super.getUncertainty() * 0.5f;
        }

        @Override
        protected float getPower() {
            return super.getPower() * 1.25f;
        }
    };
    private static final MountedDispenseBehavior LINGERING_POTIONS = new MountedProjectileDispenseBehavior() {
        @Override
        protected ProjectileEntity getProjectile(World level, double x, double y, double z, ItemStack stack, Direction facing) {
            return new LingeringPotionEntity(level, x, y, z, stack);
        }

        @Override
        protected float getUncertainty() {
            return super.getUncertainty() * 0.5f;
        }

        @Override
        protected float getPower() {
            return super.getPower() * 1.25f;
        }
    };
    private static final MountedDispenseBehavior BOTTLE = new OptionalMountedDispenseBehavior() {
        @Override
        @Nullable
        protected ItemStack doExecute(ItemStack stack, MovementContext context, BlockPos pos, Vec3d facing) {
            BlockPos interactionPos = pos.offset(MountedDispenseBehavior.getClosestFacingDirection(facing));
            BlockState state = context.world.getBlockState(interactionPos);
            Block block = state.getBlock();

            if (block instanceof BeehiveBlock hive && state.isIn(BlockTags.BEEHIVES) && state.get(BeehiveBlock.HONEY_LEVEL) >= 5) {
                hive.takeHoney(context.world, state, interactionPos, null, BeehiveBlockEntity.BeeState.BEE_RELEASED);
                MountedDispenseBehavior.placeItemInInventory(new ItemStack(Items.HONEY_BOTTLE), context, pos);
                stack.decrement(1);
                return stack;
            } else if (context.world.getFluidState(interactionPos).isIn(FluidTags.WATER)) {
                ItemStack waterBottle = PotionContentsComponent.createStack(Items.POTION, Potions.WATER);
                MountedDispenseBehavior.placeItemInInventory(waterBottle, context, pos);
                stack.decrement(1);
                return stack;
            } else {
                return null;
            }
        }
    };

    public static void register() {
        MountedDispenseBehavior.REGISTRY.registerProvider(item -> item instanceof SpawnEggItem ? SPAWN_EGG : null);

        MountedDispenseBehavior.REGISTRY.register(Items.TNT, TNT);
        MountedDispenseBehavior.REGISTRY.register(Items.FIREWORK_ROCKET, FIREWORK);
        MountedDispenseBehavior.REGISTRY.register(Items.FIRE_CHARGE, FIRE_CHARGE);
        MountedDispenseBehavior.REGISTRY.register(Items.BUCKET, BUCKET);
        MountedDispenseBehavior.REGISTRY.register(Items.GLASS_BOTTLE, BOTTLE);

        // potions can't be automatically converted since they use a weird wrapper thing
        MountedDispenseBehavior.REGISTRY.register(Items.SPLASH_POTION, SPLASH_POTIONS);
        MountedDispenseBehavior.REGISTRY.register(Items.LINGERING_POTION, LINGERING_POTIONS);
    }
}
