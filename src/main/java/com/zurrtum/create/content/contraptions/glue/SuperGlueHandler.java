package com.zurrtum.create.content.contraptions.glue;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.levelWrappers.RayTraceLevel;
import com.zurrtum.create.infrastructure.packet.s2c.GlueEffectPacket;
import net.minecraft.block.Blocks;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.TypedEntityData;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.HashSet;
import java.util.Set;

public class SuperGlueHandler {

    public static void glueListensForBlockPlacement(ServerWorld world, PlayerEntity player, BlockPos pos) {
        Set<SuperGlueEntity> cached = new HashSet<>();
        for (Direction direction : Iterate.directions) {
            BlockPos relative = pos.offset(direction);
            if (SuperGlueEntity.isGlued(world, pos, direction, cached) && BlockMovementChecks.isMovementNecessary(
                world.getBlockState(relative),
                world,
                relative
            )) {
                world.getChunkManager().sendToNearbyPlayers(player, new GlueEffectPacket(pos, direction, true));
            }
        }

        glueInOffHandAppliesOnBlockPlace(world, player, pos);
    }

    public static void glueInOffHandAppliesOnBlockPlace(ServerWorld world, PlayerEntity placer, BlockPos pos) {
        EntityAttributeInstance reachAttribute = placer.getAttributeInstance(EntityAttributes.BLOCK_INTERACTION_RANGE);
        if (reachAttribute == null)
            return;
        ItemStack itemstack = placer.getOffHandStack();
        if (!itemstack.isOf(AllItems.SUPER_GLUE))
            return;
        if (placer.getMainHandStack().isOf(AllItems.WRENCH))
            return;
        //TODO
        //        if (event.getPlacedAgainst() == IPlacementHelper.ID)
        //            return;

        double distance = reachAttribute.getValue();
        Vec3d start = placer.getCameraPosVec(1);
        Vec3d look = placer.getRotationVec(1);
        Vec3d end = start.add(look.x * distance, look.y * distance, look.z * distance);

        RayTraceLevel rayTraceLevel = new RayTraceLevel(world, (p, state) -> p.equals(pos) ? Blocks.AIR.getDefaultState() : state);
        BlockHitResult ray = rayTraceLevel.raycast(new RaycastContext(
            start,
            end,
            RaycastContext.ShapeType.OUTLINE,
            RaycastContext.FluidHandling.NONE,
            placer
        ));

        Direction face = ray.getSide();
        if (face == null || ray.getType() == Type.MISS)
            return;

        BlockPos gluePos = ray.getBlockPos();
        if (!gluePos.offset(face).equals(pos)) {
            return;
        }

        if (SuperGlueEntity.isGlued(world, gluePos, face, null))
            return;

        SuperGlueEntity entity = new SuperGlueEntity(world, SuperGlueEntity.span(gluePos, gluePos.offset(face)));
        NbtComponent customData = itemstack.get(DataComponentTypes.CUSTOM_DATA);
        if (customData != null)
            EntityType.loadFromEntityNbt(world, placer, entity, TypedEntityData.create(entity.getType(), customData.copyNbt()));

        if (SuperGlueEntity.isValidFace(world, gluePos, face)) {
            world.spawnEntity(entity);
            world.getChunkManager().sendToOtherNearbyPlayers(entity, new GlueEffectPacket(gluePos, face, true));
            itemstack.damage(1, placer, EquipmentSlot.MAINHAND);
        }
    }

}
