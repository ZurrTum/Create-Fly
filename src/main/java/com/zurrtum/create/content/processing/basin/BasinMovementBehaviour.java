package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.core.Direction;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasinMovementBehaviour extends MovementBehaviour {
    private static final Vec3 UP = Vec3.atLowerCornerOf(Direction.UP.getUnitVec3i());

    @Override
    public void tick(MovementContext context) {
        if (context.temporaryData == null) {
            Vec3 facingVec = context.rotation.apply(UP);
            if (Direction.getApproximateNearest(facingVec) == Direction.DOWN)
                dump(context, facingVec);
        }
    }

    @Nullable
    public static List<ItemStack> readInventory(MovementContext context) {
        return context.blockEntityData.getCompound("Inventory").map(nbt -> {
            RegistryOps<Tag> ops = context.world.registryAccess().createSerializationContext(NbtOps.INSTANCE);
            List<ItemStack> result = new ArrayList<>();
            nbt.getList("Input").ifPresent(list -> list.forEach(item -> ItemStack.CODEC.parse(ops, item).ifSuccess(result::add)));
            nbt.getList("Output").ifPresent(list -> list.forEach(item -> ItemStack.CODEC.parse(ops, item).ifSuccess(result::add)));
            if (result.isEmpty()) {
                return null;
            }
            return result;
        }).orElse(null);
    }

    private void dump(MovementContext context, Vec3 facingVec) {
        List<ItemStack> inventory = readInventory(context);
        if (inventory == null) {
            return;
        }
        Vec3 velocity = facingVec.scale(0.5);
        Level world = context.world;
        for (ItemStack stack : inventory) {
            ItemEntity item = new ItemEntity(world, context.position.x, context.position.y, context.position.z, stack);
            item.setDeltaMovement(velocity);
            world.addFreshEntity(item);
        }
        context.blockEntityData.remove("Inventory");
        // FIXME: Why are we setting client-side data here?
        if (context.contraption.entity.level().isClientSide()) {
            BlockEntity blockEntity = AllClientHandle.INSTANCE.getBlockEntityClientSide(context.contraption, context.localPos);
            if (blockEntity instanceof BasinBlockEntity basin) {
                basin.itemCapability.clearContent();
            }
        }
        context.temporaryData = Boolean.TRUE;
    }
}
