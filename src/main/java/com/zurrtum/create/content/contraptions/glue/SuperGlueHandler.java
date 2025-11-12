package com.zurrtum.create.content.contraptions.glue;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.api.contraption.BlockMovementChecks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.catnip.levelWrappers.RayTraceLevel;
import com.zurrtum.create.infrastructure.packet.s2c.GlueEffectPacket;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

public class SuperGlueHandler {

    public static void glueListensForBlockPlacement(ServerLevel world, Player player, BlockPos pos) {
        Set<SuperGlueEntity> cached = new HashSet<>();
        for (Direction direction : Iterate.directions) {
            BlockPos relative = pos.relative(direction);
            if (SuperGlueEntity.isGlued(world, pos, direction, cached) && BlockMovementChecks.isMovementNecessary(
                world.getBlockState(relative),
                world,
                relative
            )) {
                world.getChunkSource().sendToTrackingPlayersAndSelf(player, new GlueEffectPacket(pos, direction, true));
            }
        }

        glueInOffHandAppliesOnBlockPlace(world, player, pos);
    }

    public static void glueInOffHandAppliesOnBlockPlace(ServerLevel world, Player placer, BlockPos pos) {
        AttributeInstance reachAttribute = placer.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (reachAttribute == null)
            return;
        ItemStack itemstack = placer.getOffhandItem();
        if (!itemstack.is(AllItems.SUPER_GLUE))
            return;
        if (placer.getMainHandItem().is(AllItems.WRENCH))
            return;
        //TODO
        //        if (event.getPlacedAgainst() == IPlacementHelper.ID)
        //            return;

        double distance = reachAttribute.getValue();
        Vec3 start = placer.getEyePosition(1);
        Vec3 look = placer.getViewVector(1);
        Vec3 end = start.add(look.x * distance, look.y * distance, look.z * distance);

        RayTraceLevel rayTraceLevel = new RayTraceLevel(world, (p, state) -> p.equals(pos) ? Blocks.AIR.defaultBlockState() : state);
        BlockHitResult ray = rayTraceLevel.clip(new ClipContext(start, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, placer));

        Direction face = ray.getDirection();
        if (face == null || ray.getType() == Type.MISS)
            return;

        BlockPos gluePos = ray.getBlockPos();
        if (!gluePos.relative(face).equals(pos)) {
            return;
        }

        if (SuperGlueEntity.isGlued(world, gluePos, face, null))
            return;

        SuperGlueEntity entity = new SuperGlueEntity(world, SuperGlueEntity.span(gluePos, gluePos.relative(face)));
        CustomData customData = itemstack.get(DataComponents.CUSTOM_DATA);
        if (customData != null)
            EntityType.updateCustomEntityTag(world, placer, entity, TypedEntityData.of(entity.getType(), customData.copyTag()));

        if (SuperGlueEntity.isValidFace(world, gluePos, face)) {
            world.addFreshEntity(entity);
            world.getChunkSource().sendToTrackingPlayers(entity, new GlueEffectPacket(gluePos, face, true));
            itemstack.hurtAndBreak(1, placer, EquipmentSlot.MAINHAND);
        }
    }

}
