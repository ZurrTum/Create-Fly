package com.zurrtum.create.content.equipment.sandPaper;

import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.AllRecipeSets;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.block.BlockState;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.item.consume.UseAction;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipePropertySet;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.RaycastContext.FluidHandling;
import net.minecraft.world.World;
import net.minecraft.world.WorldEvents;

import java.util.Optional;

public class SandPaperItem extends Item {

    public SandPaperItem(Settings properties) {
        super(properties);
    }

    @Override
    public ActionResult use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getStackInHand(handIn);

        if (itemstack.contains(AllDataComponents.SAND_PAPER_POLISHING)) {
            playerIn.setCurrentHand(handIn);
            return ActionResult.PASS;
        }

        Hand otherHand = handIn == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND;
        ItemStack itemInOtherHand = playerIn.getStackInHand(otherHand);
        RecipePropertySet recipe = worldIn.getRecipeManager().getPropertySet(AllRecipeSets.SAND_PAPER_POLISHING);
        if (recipe.canUse(itemInOtherHand)) {
            ItemStack item = itemInOtherHand.copy();
            ItemStack toPolish = item.split(1);
            playerIn.setCurrentHand(handIn);
            itemstack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(toPolish));
            playerIn.setStackInHand(otherHand, item);
            return ActionResult.SUCCESS.withNewHandStack(itemstack);
        }

        BlockHitResult raytraceresult = raycast(worldIn, playerIn, FluidHandling.NONE);
        Vec3d hitVec = raytraceresult.getPos();

        Box bb = new Box(hitVec, hitVec).expand(1f);
        ItemEntity pickUp = null;
        for (ItemEntity itemEntity : worldIn.getNonSpectatingEntities(ItemEntity.class, bb)) {
            if (!itemEntity.isAlive())
                continue;
            if (itemEntity.getEntityPos().distanceTo(playerIn.getEntityPos()) > 3)
                continue;
            ItemStack stack = itemEntity.getStack();
            if (!recipe.canUse(stack))
                continue;
            pickUp = itemEntity;
            break;
        }

        if (pickUp == null)
            return ActionResult.FAIL;

        ItemStack item = pickUp.getStack().copy();
        ItemStack toPolish = item.split(1);

        playerIn.setCurrentHand(handIn);

        if (!worldIn.isClient()) {
            itemstack.set(AllDataComponents.SAND_PAPER_POLISHING, new SandPaperItemComponent(toPolish));
            if (item.isEmpty())
                pickUp.discard();
            else
                pickUp.setStack(item);
        }

        return ActionResult.SUCCESS.withNewHandStack(itemstack);
    }

    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        Random random = user.getRandom();
        if (stack.contains(AllDataComponents.SAND_PAPER_POLISHING)) {
            ItemStack polishing = stack.get(AllDataComponents.SAND_PAPER_POLISHING).item();
            if (!polishing.isEmpty())
                user.spawnItemParticles(polishing, 1);
        }

        // After 6 ticks play the sound every 7th
        if ((user.getItemUseTime() - 6) % 7 == 0) {
            user.playSound(AllSoundEvents.SANDING_SHORT.getMainEvent(), 0.9F + 0.2F * random.nextFloat(), random.nextFloat() * 0.2F + 0.9F);
        }
    }

    @Override
    public ItemStack finishUsing(ItemStack stack, World world, LivingEntity entityLiving) {
        if (!(entityLiving instanceof PlayerEntity player))
            return stack;
        SandPaperItemComponent component = stack.get(AllDataComponents.SAND_PAPER_POLISHING);
        if (component == null)
            return stack;
        ItemStack toPolish = component.item();

        if (world.isClient()) {
            spawnParticles(entityLiving.getCameraPosVec(1).add(entityLiving.getRotationVector().multiply(.5f)), toPolish, world);
            return stack;
        }

        SingleStackRecipeInput input = new SingleStackRecipeInput(toPolish);
        ((ServerWorld) world).getRecipeManager().getFirstMatch(AllRecipeTypes.SANDPAPER_POLISHING, input, world).ifPresent(recipe -> {
            ItemStack polished = recipe.value().craft(input, world.getRegistryManager());
            PlayerInventory playerInv = player.getInventory();
            if (!polished.isEmpty()) {
                playerInv.offerOrDrop(polished);
            }
            ItemStack recipeRemainder = toPolish.getItem().getRecipeRemainder();
            if (!recipeRemainder.isEmpty()) {
                playerInv.offerOrDrop(recipeRemainder);
            }
        });

        stack.remove(AllDataComponents.SAND_PAPER_POLISHING);
        stack.damage(1, entityLiving, entityLiving.getActiveHand().getEquipmentSlot());

        return stack;
    }

    public static void spawnParticles(Vec3d location, ItemStack polishedStack, World world) {
        for (int i = 0; i < 20; i++) {
            Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, world.random, 1 / 8f);
            world.addParticleClient(
                new ItemStackParticleEffect(ParticleTypes.ITEM, polishedStack),
                location.x,
                location.y,
                location.z,
                motion.x,
                motion.y,
                motion.z
            );
        }
    }

    @Override
    public boolean onStoppedUsing(ItemStack stack, World worldIn, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof PlayerEntity player))
            return false;
        if (stack.contains(AllDataComponents.SAND_PAPER_POLISHING)) {
            ItemStack toPolish = stack.get(AllDataComponents.SAND_PAPER_POLISHING).item();
            //noinspection DataFlowIssue - toPolish won't be null as we do call .has before calling .get
            player.getInventory().offerOrDrop(toPolish);
            stack.remove(AllDataComponents.SAND_PAPER_POLISHING);
        }
        return false;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        PlayerEntity player = context.getPlayer();
        ItemStack stack = context.getStack();
        World level = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = level.getBlockState(pos);

        Optional<BlockState> newState = ((AxeItem) Items.DIAMOND_AXE).getStrippedState(state);
        if (newState.isPresent()) {
            AllSoundEvents.SANDING_LONG.play(level, player, pos, 1, 1 + (level.random.nextFloat() * 0.5f - 1f) / 5f);
            level.syncWorldEvent(player, WorldEvents.BLOCK_SCRAPED, pos, 0); // Spawn particles
        } else {
            newState = Optional.ofNullable(HoneycombItem.WAXED_TO_UNWAXED_BLOCKS.get().get(state.getBlock()))
                .map(block -> block.getStateWithProperties(state));

            if (newState.isPresent()) {
                AllSoundEvents.SANDING_LONG.play(level, player, pos, 1, 1 + (level.random.nextFloat() * 0.5f - 1f) / 5f);
                level.syncWorldEvent(player, WorldEvents.WAX_REMOVED, pos, 0); // Spawn particles
            }
        }

        if (newState.isPresent()) {
            level.setBlockState(pos, newState.get());
            if (player != null)
                stack.damage(1, player, player.getActiveHand().getEquipmentSlot());
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    @Override
    public int getMaxUseTime(ItemStack stack, LivingEntity user) {
        return 32;
    }

    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.EAT;
    }
}
