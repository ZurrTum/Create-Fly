package com.zurrtum.create.content.equipment.sandPaper;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.foundation.item.CustomTransformSoundItem;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SandPaperItem extends Item implements CustomTransformSoundItem {

    public SandPaperItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        if (itemstack.has(AllDataComponents.SAND_PAPER_POLISHING)) {
            playerIn.startUsingItem(handIn);
            return InteractionResult.PASS;
        }

        InteractionHand otherHand =
            handIn == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        ItemStack itemInOtherHand = playerIn.getItemInHand(otherHand);
        RecipePropertySet recipe = worldIn.recipeAccess().propertySet(AllRecipeSets.SAND_PAPER_POLISHING);
        if (recipe.test(itemInOtherHand)) {
            ItemStack item = itemInOtherHand.copy();
            ItemStack toPolish = item.split(1);
            playerIn.startUsingItem(handIn);
            itemstack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(toPolish));
            playerIn.setItemInHand(otherHand, item);
            return InteractionResult.SUCCESS.heldItemTransformedTo(itemstack);
        }

        BlockHitResult raytraceresult = getPlayerPOVHitResult(worldIn, playerIn, Fluid.NONE);
        Vec3 hitVec = raytraceresult.getLocation();

        AABB bb = new AABB(hitVec, hitVec).inflate(1.0f);
        ItemEntity pickUp = null;
        for (ItemEntity itemEntity : worldIn.getEntitiesOfClass(ItemEntity.class, bb)) {
            if (!itemEntity.isAlive()) {
                continue;
            }
            if (itemEntity.position().distanceTo(playerIn.position()) > 3) {
                continue;
            }
            ItemStack stack = itemEntity.getItem();
            if (!recipe.test(stack)) {
                continue;
            }
            pickUp = itemEntity;
            break;
        }

        if (pickUp == null) {
            return InteractionResult.FAIL;
        }

        ItemStack item = pickUp.getItem().copy();
        ItemStack toPolish = item.split(1);

        playerIn.startUsingItem(handIn);

        if (!worldIn.isClientSide()) {
            itemstack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(toPolish));
            if (item.isEmpty()) {
                pickUp.discard();
            } else {
                pickUp.setItem(item);
            }
        }

        return InteractionResult.SUCCESS.heldItemTransformedTo(itemstack);
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        RandomSource random = user.getRandom();
        if (stack.has(AllDataComponents.SAND_PAPER_POLISHING)) {
            ItemStack polishing = stack.get(AllDataComponents.SAND_PAPER_POLISHING).item();
            if (!polishing.isEmpty()) {
                user.spawnItemParticles(polishing, 1);
            }
        }

        // After 6 ticks play the sound every 7th
        if ((user.getTicksUsingItem() - 6) % 7 == 0) {
            user.playSound(
                AllSoundEvents.SANDING_SHORT.getMainEvent(),
                0.9F + 0.2F * random.nextFloat(),
                random.nextFloat() * 0.2F + 0.9F
            );
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (!(entityLiving instanceof Player player)) {
            return stack;
        }
        SandPaperItemComponent component = stack.get(AllDataComponents.SAND_PAPER_POLISHING);
        if (component == null) {
            return stack;
        }
        ItemStack toPolish = component.item();

        if (world.isClientSide()) {
            spawnParticles(
                entityLiving.getEyePosition(1).add(entityLiving.getLookAngle().scale(0.5f)),
                toPolish,
                world
            );
            return stack;
        }

        SingleRecipeInput input = new SingleRecipeInput(toPolish);
        ((ServerLevel) world).recipeAccess().getRecipeFor(AllRecipeTypes.SANDPAPER_POLISHING, input, world)
            .ifPresent(recipe -> {
                ItemStack polished = recipe.value().assemble(input);
                Inventory playerInv = player.getInventory();
                if (!polished.isEmpty()) {
                    playerInv.placeItemBackInInventory(polished, Prediction.SERVER_ONLY);
                }
                ItemStackTemplate recipeRemainder = toPolish.getItem().getCraftingRemainder();
                if (recipeRemainder != null) {
                    playerInv.placeItemBackInInventory(recipeRemainder.create(), Prediction.SERVER_ONLY);
                }
            });

        stack.remove(AllDataComponents.SAND_PAPER_POLISHING);
        stack.hurtAndBreak(1, entityLiving, entityLiving.getUsedItemHand().asEquipmentSlot());

        return stack;
    }

    public static void spawnParticles(Vec3 location, ItemStack polishedStack, Level world) {
        if (polishedStack.isEmpty()) {
            return;
        }
        ItemParticleOption option = new ItemParticleOption(
            ParticleTypes.ITEM,
            ItemStackTemplate.fromNonEmptyStack(polishedStack)
        );
        for (int i = 0; i < 20; i++) {
            Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, world.getRandom(), 1 / 8.0f);
            world.addParticle(option, location.x, location.y, location.z, motion.x, motion.y, motion.z);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) {
            return false;
        }
        if (stack.has(AllDataComponents.SAND_PAPER_POLISHING)) {
            ItemStack toPolish = stack.get(AllDataComponents.SAND_PAPER_POLISHING).item();
            //noinspection DataFlowIssue - toPolish won't be null as we do call .has before calling .get
            player.getInventory().placeItemBackInInventory(toPolish, Prediction.SERVER_ONLY);
            stack.remove(AllDataComponents.SAND_PAPER_POLISHING);
        }
        return false;
    }

    @Override
    public void playSound(
        Level level,
        @Nullable Player player,
        BlockPos pos,
        SoundEvent sound,
        SoundSource source,
        float volume,
        float pitch
    ) {
        AllSoundEvents.SANDING_LONG.play(
            level,
            player,
            pos,
            volume,
            pitch + (level.getRandom().nextFloat() * 0.5f - 1.0f) / 5.0f
        );
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.EAT;
    }
}
