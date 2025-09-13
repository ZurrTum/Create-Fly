package com.zurrtum.create.content.processing.burner;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.AllEntityTags;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.Create;
import com.zurrtum.create.catnip.math.VecHelper;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.block.spawner.MobSpawnerEntry;
import net.minecraft.block.spawner.MobSpawnerLogic;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.ReadView;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Hand;
import net.minecraft.util.collection.Weighted;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BlazeBurnerBlockItem extends BlockItem {

    private final boolean capturedBlaze;

    public static BlazeBurnerBlockItem empty(Settings properties) {
        return new BlazeBurnerBlockItem(AllBlocks.BLAZE_BURNER, properties, false);
    }

    public static BlazeBurnerBlockItem withBlaze(Block block, Settings properties) {
        return new BlazeBurnerBlockItem(block, properties, true);
    }

    @Override
    public void appendBlocks(Map<Block, Item> p_195946_1_, Item p_195946_2_) {
        if (!hasCapturedBlaze())
            return;
        super.appendBlocks(p_195946_1_, p_195946_2_);
    }

    private BlazeBurnerBlockItem(Block block, Settings properties, boolean capturedBlaze) {
        super(block, properties);
        this.capturedBlaze = capturedBlaze;
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        if (hasCapturedBlaze())
            return super.useOnBlock(context);

        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockEntity be = world.getBlockEntity(pos);
        PlayerEntity player = context.getPlayer();

        if (!(be instanceof MobSpawnerBlockEntity mbe))
            return super.useOnBlock(context);

        MobSpawnerLogic spawner = mbe.getLogic();

        List<MobSpawnerEntry> possibleSpawns = spawner.spawnPotentials.getEntries().stream().map(Weighted::value).toList();

        if (possibleSpawns.isEmpty()) {
            possibleSpawns = new ArrayList<>();
            possibleSpawns.add(spawner.spawnEntry);
        }

        try (ErrorReporter.Logging logging = new ErrorReporter.Logging(mbe.getReporterContext(), Create.LOGGER)) {
            for (MobSpawnerEntry e : possibleSpawns) {
                ReadView readView = NbtReadView.create(logging, world.getRegistryManager(), e.entity());
                Optional<EntityType<?>> optionalEntity = EntityType.fromData(readView);
                if (optionalEntity.isEmpty() || !optionalEntity.get().isIn(AllEntityTags.BLAZE_BURNER_CAPTURABLE))
                    continue;

                spawnCaptureEffects(world, VecHelper.getCenterOf(pos));
                if (world.isClient || player == null)
                    return ActionResult.SUCCESS;

                giveBurnerItemTo(player, context.getStack(), context.getHand());
                return ActionResult.SUCCESS;
            }
        }

        return super.useOnBlock(context);
    }

    @Override
    public ActionResult useOnEntity(ItemStack heldItem, PlayerEntity player, LivingEntity entity, Hand hand) {
        if (hasCapturedBlaze())
            return ActionResult.PASS;
        if (!entity.getType().isIn(AllEntityTags.BLAZE_BURNER_CAPTURABLE))
            return ActionResult.PASS;

        World world = player.getWorld();
        spawnCaptureEffects(world, entity.getPos());
        if (world.isClient)
            return ActionResult.FAIL;

        giveBurnerItemTo(player, heldItem, hand);
        entity.discard();
        return ActionResult.FAIL;
    }

    protected void giveBurnerItemTo(PlayerEntity player, ItemStack heldItem, Hand hand) {
        ItemStack filled = AllItems.BLAZE_BURNER.getDefaultStack();
        if (!player.isCreative())
            heldItem.decrement(1);
        if (heldItem.isEmpty()) {
            player.setStackInHand(hand, filled);
            return;
        }
        player.getInventory().offerOrDrop(filled);
    }

    private void spawnCaptureEffects(World world, Vec3d vec) {
        if (world.isClient) {
            for (int i = 0; i < 40; i++) {
                Vec3d motion = VecHelper.offsetRandomly(Vec3d.ZERO, world.random, .125f);
                world.addParticleClient(ParticleTypes.FLAME, vec.x, vec.y, vec.z, motion.x, motion.y, motion.z);
                Vec3d circle = motion.multiply(1, 0, 1).normalize().multiply(.5f);
                world.addParticleClient(ParticleTypes.SMOKE, circle.x, vec.y, circle.z, 0, -0.125, 0);
            }
            return;
        }

        BlockPos soundPos = BlockPos.ofFloored(vec);
        world.playSound(null, soundPos, SoundEvents.ENTITY_BLAZE_HURT, SoundCategory.HOSTILE, .25f, .75f);
        world.playSound(null, soundPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.HOSTILE, .5f, .75f);
    }

    public boolean hasCapturedBlaze() {
        return capturedBlaze;
    }

}
