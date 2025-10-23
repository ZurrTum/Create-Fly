package com.zurrtum.create.content.processing.basin;

import com.zurrtum.create.AllClientHandle;
import com.zurrtum.create.api.behaviour.movement.MovementBehaviour;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BasinMovementBehaviour extends MovementBehaviour {
    private static final Vec3d UP = Vec3d.of(Direction.UP.getVector());

    @Override
    public void tick(MovementContext context) {
        if (context.temporaryData == null) {
            Vec3d facingVec = context.rotation.apply(UP);
            if (Direction.getFacing(facingVec) == Direction.DOWN)
                dump(context, facingVec);
        }
    }

    @Nullable
    public static List<ItemStack> readInventory(MovementContext context) {
        return context.blockEntityData.getCompound("Inventory").map(nbt -> {
            RegistryOps<NbtElement> ops = context.world.getRegistryManager().getOps(NbtOps.INSTANCE);
            List<ItemStack> result = new ArrayList<>();
            nbt.getList("Input").ifPresent(list -> list.forEach(item -> ItemStack.CODEC.parse(ops, item).ifSuccess(result::add)));
            nbt.getList("Output").ifPresent(list -> list.forEach(item -> ItemStack.CODEC.parse(ops, item).ifSuccess(result::add)));
            if (result.isEmpty()) {
                return null;
            }
            return result;
        }).orElse(null);
    }

    private void dump(MovementContext context, Vec3d facingVec) {
        List<ItemStack> inventory = readInventory(context);
        if (inventory == null) {
            return;
        }
        Vec3d velocity = facingVec.multiply(0.5);
        World world = context.world;
        for (ItemStack stack : inventory) {
            ItemEntity item = new ItemEntity(world, context.position.x, context.position.y, context.position.z, stack);
            item.setVelocity(velocity);
            world.spawnEntity(item);
        }
        context.blockEntityData.remove("Inventory");
        // FIXME: Why are we setting client-side data here?
        if (context.contraption.entity.getWorld().isClient) {
            BlockEntity blockEntity = AllClientHandle.INSTANCE.getBlockEntityClientSide(context.contraption, context.localPos);
            if (blockEntity instanceof BasinBlockEntity basin) {
                basin.itemCapability.clear();
            }
        }
        context.temporaryData = Boolean.TRUE;
    }
}
