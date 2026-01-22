package com.zurrtum.create.content.equipment.sandPaper;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipePropertySet;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class SandPaperItem extends Item {

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

        InteractionHand otherHand = handIn == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
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

        AABB bb = new AABB(hitVec, hitVec).inflate(1f);
        ItemEntity pickUp = null;
        for (ItemEntity itemEntity : worldIn.getEntitiesOfClass(ItemEntity.class, bb)) {
            if (!itemEntity.isAlive())
                continue;
            if (itemEntity.position().distanceTo(playerIn.position()) > 3)
                continue;
            ItemStack stack = itemEntity.getItem();
            if (!recipe.test(stack))
                continue;
            pickUp = itemEntity;
            break;
        }

        if (pickUp == null)
            return InteractionResult.FAIL;

        ItemStack item = pickUp.getItem().copy();
        ItemStack toPolish = item.split(1);

        playerIn.startUsingItem(handIn);

        if (!worldIn.isClientSide()) {
            itemstack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(toPolish));
            if (item.isEmpty())
                pickUp.discard();
            else
                pickUp.setItem(item);
        }

        return InteractionResult.SUCCESS.heldItemTransformedTo(itemstack);
    }

    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        RandomSource random = user.getRandom();
        if (stack.has(AllDataComponents.SAND_PAPER_POLISHING)) {
            ItemStack polishing = stack.get(AllDataComponents.SAND_PAPER_POLISHING).item();
            if (!polishing.isEmpty())
                user.spawnItemParticles(polishing, 1);
        }

        // After 6 ticks play the sound every 7th
        if ((user.getTicksUsingItem() - 6) % 7 == 0) {
            user.playSound(AllSoundEvents.SANDING_SHORT.getMainEvent(), 0.9F + 0.2F * random.nextFloat(), random.nextFloat() * 0.2F + 0.9F);
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level world, LivingEntity entityLiving) {
        if (!(entityLiving instanceof Player player))
            return stack;
        SandPaperItemComponent component = stack.get(AllDataComponents.SAND_PAPER_POLISHING);
        if (component == null)
            return stack;
        ItemStack toPolish = component.item();

        if (world.isClientSide()) {
            spawnParticles(entityLiving.getEyePosition(1).add(entityLiving.getLookAngle().scale(.5f)), toPolish, world);
            return stack;
        }

        SingleRecipeInput input = new SingleRecipeInput(toPolish);
        ((ServerLevel) world).recipeAccess().getRecipeFor(AllRecipeTypes.SANDPAPER_POLISHING, input, world).ifPresent(recipe -> {
            ItemStack polished = recipe.value().assemble(input);
            Inventory playerInv = player.getInventory();
            if (!polished.isEmpty()) {
                playerInv.placeItemBackInInventory(polished);
            }
            ItemStackTemplate recipeRemainder = toPolish.getItem().getCraftingRemainder();
            if (recipeRemainder != null) {
                playerInv.placeItemBackInInventory(recipeRemainder.create());
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
        ItemParticleOption option = new ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(polishedStack));
        for (int i = 0; i < 20; i++) {
            Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, world.getRandom(), 1 / 8f);
            world.addParticle(option, location.x, location.y, location.z, motion.x, motion.y, motion.z);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player))
            return false;
        if (stack.has(AllDataComponents.SAND_PAPER_POLISHING)) {
            ItemStack toPolish = stack.get(AllDataComponents.SAND_PAPER_POLISHING).item();
            //noinspection DataFlowIssue - toPolish won't be null as we do call .has before calling .get
            player.getInventory().placeItemBackInInventory(toPolish);
            stack.remove(AllDataComponents.SAND_PAPER_POLISHING);
        }
        return false;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        Optional<BlockState> newState = ((AxeItem) Items.DIAMOND_AXE).getStripped(state);
        if (newState.isPresent()) {
            AllSoundEvents.SANDING_LONG.play(level, player, pos, 1, 1 + (level.getRandom().nextFloat() * 0.5f - 1f) / 5f);
            level.levelEvent(player, LevelEvent.PARTICLES_SCRAPE, pos, 0); // Spawn particles
        } else {
            newState = Optional.ofNullable(HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));

            if (newState.isPresent()) {
                AllSoundEvents.SANDING_LONG.play(level, player, pos, 1, 1 + (level.getRandom().nextFloat() * 0.5f - 1f) / 5f);
                level.levelEvent(player, LevelEvent.PARTICLES_WAX_OFF, pos, 0); // Spawn particles
            }
        }

        if (newState.isPresent()) {
            level.setBlockAndUpdate(pos, newState.get());
            if (player != null)
                stack.hurtAndBreak(1, player, player.getUsedItemHand().asEquipmentSlot());
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
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
