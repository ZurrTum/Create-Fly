package com.zurrtum.create.content.kinetics.crusher;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ItemStackParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.*;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CrushingWheelControllerBlockEntity extends SmartBlockEntity {

    public Entity processingEntity;
    public UUID entityUUID;
    protected boolean searchForEntity;

    public ProcessingInventory inventory;
    public float crushingspeed;

    public CrushingWheelControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER, pos, state);
        inventory = new ProcessingInventory(this::itemInserted, d -> processingEntity == null);
    }

    @Override
    public void onBlockReplaced(BlockPos pos, BlockState oldState) {
        super.onBlockReplaced(pos, oldState);
        ItemScatterer.spawn(world, pos, inventory);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput));
    }

    private boolean supportsDirectBeltInput(Direction side) {
        BlockState blockState = getCachedState();
        if (blockState == null)
            return false;
        Direction direction = blockState.get(CrushingWheelControllerBlock.FACING);
        return direction == Direction.DOWN || direction == side;
    }

    @Override
    public void tick() {
        super.tick();
        if (searchForEntity) {
            searchForEntity = false;
            List<Entity> search = world.getOtherEntities(null, new Box(getPos()), e -> entityUUID.equals(e.getUuid()));
            if (search.isEmpty())
                clear();
            else
                processingEntity = search.getFirst();
        }

        if (!isOccupied())
            return;
        if (crushingspeed == 0)
            return;

        float speed = crushingspeed * 4;

        Vec3d centerPos = VecHelper.getCenterOf(pos);
        Direction facing = getCachedState().get(CrushingWheelControllerBlock.FACING);
        int offset = facing.getDirection().offset();
        Vec3d outSpeed = new Vec3d(
            (facing.getAxis() == Axis.X ? 0.25D : 0.0D) * offset, offset == 1 ? (facing.getAxis() == Axis.Y ? 0.5D : 0.0D) : 0.0D
            // Increased upwards speed so upwards
            // crushing wheels shoot out the item
            // properly.
            , (facing.getAxis() == Axis.Z ? 0.25D : 0.0D) * offset
        ); // No downwards speed, so downwards crushing wheels
        // drop the items as before.
        Vec3d outPos = centerPos.add(
            (facing.getAxis() == Axis.X ? .55f * offset : 0f),
            (facing.getAxis() == Axis.Y ? .55f * offset : 0f),
            (facing.getAxis() == Axis.Z ? .55f * offset : 0f)
        );

        if (!hasEntity()) {

            float processingSpeed = MathHelper.clamp(
                (speed) / (!inventory.appliedRecipe ? (float) Math.log(inventory.getStack(0)
                    .getCount()) / (float) Math.log(2) : 1), .25f, 20
            );
            inventory.remainingTime -= processingSpeed;
            spawnParticles(inventory.getStack(0));

            if (world.isClient)
                return;

            if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
                applyRecipe();
                inventory.appliedRecipe = true;
                world.updateListeners(pos, getCachedState(), getCachedState(), 2 | 16);
                return;
            }

            if (inventory.remainingTime > 0) {
                return;
            }
            inventory.remainingTime = 0;

            // Output Items
            if (facing != Direction.UP) {
                BlockPos nextPos = pos.down().offset(facing, facing.getAxis() == Axis.Y ? 0 : 1);

                DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(world, nextPos, DirectBeltInputBehaviour.TYPE);
                if (behaviour != null) {
                    boolean changed = false;
                    if (!behaviour.canInsertFromSide(facing))
                        return;
                    for (int slot = 0, size = inventory.size(); slot < size; slot++) {
                        ItemStack stack = inventory.getStack(slot);
                        if (stack.isEmpty())
                            continue;
                        ItemStack remainder = behaviour.handleInsertion(stack, facing, false);
                        if (ItemStack.areEqual(remainder, stack))
                            continue;
                        inventory.setStack(slot, remainder);
                        changed = true;
                    }
                    if (changed) {
                        markDirty();
                        sendData();
                    }
                    return;
                }
            }

            // Eject Items
            for (int slot = 0, size = inventory.size(); slot < size; slot++) {
                ItemStack stack = inventory.getStack(slot);
                if (stack.isEmpty())
                    continue;
                ItemEntity entityIn = new ItemEntity(world, outPos.x, outPos.y, outPos.z, stack);
                entityIn.setVelocity(outSpeed);
                AllSynchedDatas.BYPASS_CRUSHING_WHEEL.set(entityIn, Optional.of(pos));
                world.spawnEntity(entityIn);
            }
            inventory.clear();
            world.updateListeners(pos, getCachedState(), getCachedState(), 2 | 16);

            return;
        }

        if (!processingEntity.isAlive() || !processingEntity.getBoundingBox().intersects(new Box(pos).expand(.5f))) {
            clear();
            return;
        }

        double xMotion = ((pos.getX() + .5f) - processingEntity.getX()) / 2f;
        double zMotion = ((pos.getZ() + .5f) - processingEntity.getZ()) / 2f;
        if (processingEntity.isSneaking())
            xMotion = zMotion = 0;
        double movement = Math.max(-speed / 4f, -.5f) * -offset;
        processingEntity.setVelocity(new Vec3d(
            facing.getAxis() == Axis.X ? movement : xMotion, facing.getAxis() == Axis.Y ? movement : 0f // Do
            // not
            // move
            // entities
            // upwards
            // or
            // downwards
            // for
            // horizontal
            // crushers,
            , facing.getAxis() == Axis.Z ? movement : zMotion
        )); // Or they'll only get their feet crushed.

        if (world.isClient)
            return;

        if (!(processingEntity instanceof ItemEntity itemEntity)) {
            Vec3d entityOutPos = outPos.add(
                facing.getAxis() == Axis.X ? .5f * offset : 0f,
                facing.getAxis() == Axis.Y ? .5f * offset : 0f,
                facing.getAxis() == Axis.Z ? .5f * offset : 0f
            );
            int crusherDamage = AllConfigs.server().kinetics.crushingDamage.get();

            if (processingEntity instanceof LivingEntity) {
                if ((((LivingEntity) processingEntity).getHealth() - crusherDamage <= 0) // Takes LivingEntity instances
                    // as exception, so it can
                    // move them before it would
                    // kill them.
                    && (((LivingEntity) processingEntity).hurtTime <= 0)) { // This way it can actually output the items
                    // to the right spot.
                    processingEntity.setPos(entityOutPos.x, entityOutPos.y, entityOutPos.z);
                }
            }
            processingEntity.damage((ServerWorld) world, AllDamageSources.get(world).crush, crusherDamage);
            if (!processingEntity.isAlive()) {
                processingEntity.setPos(entityOutPos.x, entityOutPos.y, entityOutPos.z);
            }
            return;
        }

        itemEntity.setPickupDelay(20);
        if (facing.getAxis() == Axis.Y) {
            if (processingEntity.getY() * -offset < (centerPos.y - .25f) * -offset) {
                intakeItem(itemEntity);
            }
        } else if (facing.getAxis() == Axis.Z) {
            if (processingEntity.getZ() * -offset < (centerPos.z - .25f) * -offset) {
                intakeItem(itemEntity);
            }
        } else {
            if (processingEntity.getX() * -offset < (centerPos.x - .25f) * -offset) {
                intakeItem(itemEntity);
            }
        }
    }

    private void intakeItem(ItemEntity itemEntity) {
        inventory.clear();
        inventory.setStack(0, itemEntity.getStack().copy());
        itemInserted(inventory.getStack(0));
        itemEntity.discard();
        world.updateListeners(pos, getCachedState(), getCachedState(), 2 | 16);
    }

    protected void spawnParticles(ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return;

        ParticleEffect particleData;
        if (stack.getItem() instanceof BlockItem)
            particleData = new BlockStateParticleEffect(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock().getDefaultState());
        else
            particleData = new ItemStackParticleEffect(ParticleTypes.ITEM, stack);

        Random r = world.random;
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        for (int i = 0; i < 4; i++) {
            world.addParticleClient(particleData, x + r.nextFloat(), y + r.nextFloat(), z + r.nextFloat(), 0, 0, 0);
        }
    }

    private void applyRecipe() {
        AbstractCrushingRecipe recipe = findRecipe();

        List<ItemStack> list = new ArrayList<>();
        if (recipe != null) {
            ItemStack item = inventory.getStack(0);
            SingleStackRecipeInput input = new SingleStackRecipeInput(item);
            int rolls = item.getCount();
            inventory.clear();
            for (int roll = 0; roll < rolls; roll++) {
                List<ItemStack> rolledResults = recipe.craft(input, world.random);
                for (ItemStack stack : rolledResults) {
                    ItemHelper.addToList(stack, list);
                }
            }
            for (int slot = 0, max = Math.min(list.size(), inventory.size() - 1); slot < max; slot++)
                inventory.setStack(slot + 1, list.get(slot));
        } else {
            inventory.clear();
        }

    }

    public AbstractCrushingRecipe findRecipe() {
        ServerRecipeManager recipeManager = ((ServerWorld) world).getRecipeManager();
        SingleStackRecipeInput input = new SingleStackRecipeInput(inventory.getStack(0));
        AbstractCrushingRecipe crushingRecipe = recipeManager.getFirstMatch(AllRecipeTypes.CRUSHING, input, world).map(RecipeEntry::value)
            .orElse(null);
        if (crushingRecipe == null)
            crushingRecipe = recipeManager.getFirstMatch(AllRecipeTypes.MILLING, input, world).map(RecipeEntry::value).orElse(null);
        return crushingRecipe;
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        if (hasEntity())
            view.put("Entity", Uuids.INT_STREAM_CODEC, entityUUID);
        inventory.write(view);
        view.putFloat("Speed", crushingspeed);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        view.read("Entity", Uuids.INT_STREAM_CODEC).ifPresent(uuid -> {
            if (!isOccupied()) {
                entityUUID = uuid;
                searchForEntity = true;
            }
        });
        crushingspeed = view.getFloat("Speed", 0);
        inventory.read(view);
    }

    public void startCrushing(Entity entity) {
        processingEntity = entity;
        entityUUID = entity.getUuid();
    }

    private void itemInserted(ItemStack stack) {
        AbstractCrushingRecipe recipe = findRecipe();
        inventory.remainingTime = recipe != null ? recipe.time() : 100;
        inventory.appliedRecipe = false;
    }

    public void clear() {
        processingEntity = null;
        entityUUID = null;
    }

    public boolean isOccupied() {
        return hasEntity() || !inventory.isEmpty();
    }

    public boolean hasEntity() {
        return processingEntity != null;
    }

}
