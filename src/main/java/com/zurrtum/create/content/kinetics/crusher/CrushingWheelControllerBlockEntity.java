package com.zurrtum.create.content.kinetics.crusher;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllDamageSources;
import com.zurrtum.create.AllRecipeTypes;
import com.zurrtum.create.AllSynchedDatas;
import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.catnip.math.VecHelper;
import com.zurrtum.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.zurrtum.create.content.processing.recipe.ProcessingInventory;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.item.ItemHelper;
import com.zurrtum.create.infrastructure.config.AllConfigs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CrushingWheelControllerBlockEntity extends SmartBlockEntity implements Clearable {

    public @Nullable Entity processingEntity;
    public @Nullable UUID entityUUID;
    protected boolean searchForEntity;

    public ProcessingInventory inventory;
    public float crushingspeed;

    public CrushingWheelControllerBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.CRUSHING_WHEEL_CONTROLLER, pos, state);
        inventory = new ProcessingInventory(this::itemInserted, _ -> processingEntity == null);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState oldState) {
        super.preRemoveSideEffects(pos, oldState);
        Containers.dropContents(level, pos, inventory);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour<?>> behaviours) {
        behaviours.add(new DirectBeltInputBehaviour(this).onlyInsertWhen(this::supportsDirectBeltInput));
    }

    private boolean supportsDirectBeltInput(Direction side) {
        BlockState blockState = getBlockState();
        if (blockState == null)
            return false;
        Direction direction = blockState.getValue(CrushingWheelControllerBlock.FACING);
        return direction == Direction.DOWN || direction == side;
    }

    @Override
    public void tick() {
        super.tick();
        if (searchForEntity) {
            searchForEntity = false;
            List<Entity> search = level.getEntities((Entity) null, new AABB(getBlockPos()), e -> entityUUID.equals(e.getUUID()));
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

        Vec3 centerPos = VecHelper.getCenterOf(worldPosition);
        Direction facing = getBlockState().getValue(CrushingWheelControllerBlock.FACING);
        int offset = facing.getAxisDirection().getStep();
        Vec3 outSpeed = new Vec3(
            (facing.getAxis() == Axis.X ? 0.25D : 0.0D) * offset, offset == 1 ? (facing.getAxis() == Axis.Y ? 0.5D : 0.0D) : 0.0D
            // Increased upwards speed so upwards
            // crushing wheels shoot out the item
            // properly.
            , (facing.getAxis() == Axis.Z ? 0.25D : 0.0D) * offset
        ); // No downwards speed, so downwards crushing wheels
        // drop the items as before.
        Vec3 outPos = centerPos.add(
            (facing.getAxis() == Axis.X ? .55f * offset : 0f),
            (facing.getAxis() == Axis.Y ? .55f * offset : 0f),
            (facing.getAxis() == Axis.Z ? .55f * offset : 0f)
        );

        if (!hasEntity()) {

            float processingSpeed = Mth.clamp(
                (speed) / (!inventory.appliedRecipe ? (float) Math.log(inventory.getItem(0)
                    .getCount()) / (float) Math.log(2) : 1), .25f, 20
            );
            inventory.remainingTime -= processingSpeed;
            spawnParticles(inventory.getItem(0));

            if (level.isClientSide())
                return;

            if (inventory.remainingTime < 20 && !inventory.appliedRecipe) {
                applyRecipe();
                inventory.appliedRecipe = true;
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
                return;
            }

            if (inventory.remainingTime > 0) {
                return;
            }
            inventory.remainingTime = 0;

            // Output Items
            if (facing != Direction.UP) {
                BlockPos nextPos = worldPosition.below().relative(facing, facing.getAxis() == Axis.Y ? 0 : 1);

                DirectBeltInputBehaviour behaviour = BlockEntityBehaviour.get(level, nextPos, DirectBeltInputBehaviour.TYPE);
                if (behaviour != null) {
                    boolean changed = false;
                    if (!behaviour.canInsertFromSide(facing))
                        return;
                    for (int slot = 0, size = inventory.getContainerSize(); slot < size; slot++) {
                        ItemStack stack = inventory.getItem(slot);
                        if (stack.isEmpty())
                            continue;
                        ItemStack remainder = behaviour.handleInsertion(stack, facing, false);
                        if (ItemStack.matches(remainder, stack))
                            continue;
                        inventory.setItem(slot, remainder);
                        changed = true;
                    }
                    if (changed) {
                        setChanged();
                        sendData();
                    }
                    return;
                }
            }

            // Eject Items
            for (int slot = 0, size = inventory.getContainerSize(); slot < size; slot++) {
                ItemStack stack = inventory.getItem(slot);
                if (stack.isEmpty())
                    continue;
                ItemEntity entityIn = new ItemEntity(level, outPos.x, outPos.y, outPos.z, stack);
                entityIn.setDeltaMovement(outSpeed);
                AllSynchedDatas.BYPASS_CRUSHING_WHEEL.set(entityIn, Optional.of(worldPosition));
                level.addFreshEntity(entityIn);
            }
            inventory.clearContent();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);

            return;
        }

        if (!processingEntity.isAlive() || !processingEntity.getBoundingBox().intersects(new AABB(worldPosition).inflate(.5f))) {
            clear();
            return;
        }

        double xMotion = ((worldPosition.getX() + .5f) - processingEntity.getX()) / 2f;
        double zMotion = ((worldPosition.getZ() + .5f) - processingEntity.getZ()) / 2f;
        if (processingEntity.isShiftKeyDown())
            xMotion = zMotion = 0;
        double movement = Math.max(-speed / 4f, -.5f) * -offset;
        processingEntity.setDeltaMovement(new Vec3(
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

        if (level.isClientSide())
            return;

        if (!(processingEntity instanceof ItemEntity itemEntity)) {
            Vec3 entityOutPos = outPos.add(
                facing.getAxis() == Axis.X ? .5f * offset : 0f,
                facing.getAxis() == Axis.Y ? .5f * offset : 0f,
                facing.getAxis() == Axis.Z ? .5f * offset : 0f
            );
            int crusherDamage = AllConfigs.server().kinetics.crushingDamage.get();

            if (processingEntity instanceof LivingEntity livingEntity) {
                if ((livingEntity.getHealth() - crusherDamage <= 0) // Takes LivingEntity instances
                    // as exception, so it can
                    // move them before it would
                    // kill them.
                    && (livingEntity.hurtTime <= 0)) { // This way it can actually output the items
                    // to the right spot.
                    processingEntity.setPosRaw(entityOutPos.x, entityOutPos.y, entityOutPos.z);
                }
            }
            processingEntity.hurtServer((ServerLevel) level, AllDamageSources.get(level).crush, crusherDamage);
            if (!processingEntity.isAlive()) {
                processingEntity.setPosRaw(entityOutPos.x, entityOutPos.y, entityOutPos.z);
            }
            return;
        }

        itemEntity.setPickUpDelay(20);
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
        inventory.clearContent();
        inventory.setItem(0, itemEntity.getItem().copy());
        itemInserted(inventory.getItem(0));
        itemEntity.discard();
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 2 | 16);
    }

    protected void spawnParticles(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty())
            return;

        ParticleOptions particleData;
        if (stack.getItem() instanceof BlockItem)
            particleData = new BlockParticleOption(ParticleTypes.BLOCK, ((BlockItem) stack.getItem()).getBlock().defaultBlockState());
        else
            particleData = new ItemParticleOption(ParticleTypes.ITEM, stack);

        RandomSource r = level.getRandom();
        int x = worldPosition.getX();
        int y = worldPosition.getY();
        int z = worldPosition.getZ();
        for (int i = 0; i < 4; i++) {
            level.addParticle(particleData, x + r.nextFloat(), y + r.nextFloat(), z + r.nextFloat(), 0, 0, 0);
        }
    }

    private void applyRecipe() {
        AbstractCrushingRecipe recipe = findRecipe();

        List<ItemStack> list = new ArrayList<>();
        if (recipe != null) {
            ItemStack item = inventory.getItem(0);
            SingleRecipeInput input = new SingleRecipeInput(item);
            int rolls = item.getCount();
            inventory.clearContent();
            ItemStack recipeRemainder = item.getItem().getCraftingRemainder();
            if (recipeRemainder.isEmpty()) {
                recipeRemainder = null;
            }
            for (int roll = 0; roll < rolls; roll++) {
                List<ItemStack> rolledResults = recipe.assemble(input, level.getRandom());
                for (ItemStack stack : rolledResults) {
                    ItemHelper.addToList(stack, list);
                }
                if (recipeRemainder != null) {
                    ItemHelper.addToList(recipeRemainder, list);
                }
            }
            for (int slot = 0, max = Math.min(list.size(), inventory.getContainerSize() - 1); slot < max; slot++)
                inventory.setItem(slot + 1, list.get(slot));
        } else {
            inventory.clearContent();
        }

    }

    @Nullable
    public AbstractCrushingRecipe findRecipe() {
        RecipeManager recipeManager = ((ServerLevel) level).recipeAccess();
        SingleRecipeInput input = new SingleRecipeInput(inventory.getItem(0));
        AbstractCrushingRecipe crushingRecipe = recipeManager.getRecipeFor(AllRecipeTypes.CRUSHING, input, level).map(RecipeHolder::value)
            .orElse(null);
        if (crushingRecipe == null)
            crushingRecipe = recipeManager.getRecipeFor(AllRecipeTypes.MILLING, input, level).map(RecipeHolder::value).orElse(null);
        return crushingRecipe;
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        if (hasEntity())
            view.store("Entity", UUIDUtil.CODEC, entityUUID);
        inventory.write(view);
        view.putFloat("Speed", crushingspeed);
        super.write(view, clientPacket);
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        view.read("Entity", UUIDUtil.CODEC).ifPresent(uuid -> {
            if (!isOccupied()) {
                entityUUID = uuid;
                searchForEntity = true;
            }
        });
        crushingspeed = view.getFloatOr("Speed", 0);
        inventory.read(view);
    }

    @Override
    public void clearContent() {
        inventory.clearContent();
    }

    public void startCrushing(Entity entity) {
        processingEntity = entity;
        entityUUID = entity.getUUID();
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
