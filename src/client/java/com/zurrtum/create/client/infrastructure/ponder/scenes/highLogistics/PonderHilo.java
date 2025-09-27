package com.zurrtum.create.client.infrastructure.ponder.scenes.highLogistics;

import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.client.foundation.ponder.CreateSceneBuilder;
import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.EntityElement;
import com.zurrtum.create.content.logistics.box.PackageEntity;
import com.zurrtum.create.content.logistics.packager.PackagerBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class PonderHilo {

    public static void packagerCreate(CreateSceneBuilder scene, BlockPos pos, ItemStack box) {
        scene.world().modifyBlockEntity(
            pos, PackagerBlockEntity.class, be -> {
                be.animationTicks = PackagerBlockEntity.CYCLE;
                be.animationInward = false;
                be.heldBox = box;
            }
        );
    }

    public static void packagerUnpack(CreateSceneBuilder scene, BlockPos pos, ItemStack box) {
        scene.world().modifyBlockEntity(
            pos, PackagerBlockEntity.class, be -> {
                be.animationTicks = PackagerBlockEntity.CYCLE;
                be.animationInward = true;
                be.previouslyUnwrapped = box;
            }
        );
    }

    public static void packagerClear(CreateSceneBuilder scene, BlockPos pos) {
        scene.world().modifyBlockEntity(pos, PackagerBlockEntity.class, be -> be.heldBox = ItemStack.EMPTY);
    }

    public static ElementLink<EntityElement> packageHopsOffBelt(CreateSceneBuilder scene, BlockPos beltPos, Direction side, ItemStack box) {
        scene.world().removeItemsFromBelt(beltPos);
        return scene.world().createEntity(l -> {
            int offsetX = side.getOffsetX();
            int offsetZ = side.getOffsetZ();
            PackageEntity packageEntity = new PackageEntity(
                l,
                beltPos.getX() + 0.5 + offsetX * 0.675,
                beltPos.getY() + 0.875,
                beltPos.getZ() + 0.5 + offsetZ * 0.675
            );
            packageEntity.setVelocity(new Vec3d(offsetX, 1f, offsetZ).multiply(0.125f));
            packageEntity.box = box;
            return packageEntity;
        });
    }

    public static void linkEffect(CreateSceneBuilder scene, BlockPos pos) {
        scene.world().flashDisplayLink(pos);
        scene.addInstruction(s -> {
            Vec3d vec3 = Vec3d.ofCenter(pos);
            s.getWorld().addParticleClient(AllParticleTypes.WIFI, vec3.x, vec3.y, vec3.z, 1, 1, 1);
        });
    }

    public static void requesterEffect(CreateSceneBuilder scene, BlockPos pos) {
        scene.addInstruction(s -> {
            Vec3d vec3 = Vec3d.ofCenter(pos);
            s.getWorld().addParticleClient(AllParticleTypes.WIFI, vec3.x, vec3.y, vec3.z, 1, 1, 1);
        });
    }

}