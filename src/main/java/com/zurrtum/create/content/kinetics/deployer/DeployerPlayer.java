package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.authlib.GameProfile;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.infrastructure.player.FakeGameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface DeployerPlayer {
    UUID FALLBACK_ID = UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d");

    static DeployerPlayer create(ServerWorld world, @Nullable UUID owner, @Nullable String name) {
        GameProfile profile = new FakeGameProfile(FALLBACK_ID, "Deployer", owner, name);
        if (FakePlayerHandler.FABRIC) {
            return new DeployerFabricFakePlayer(world, profile);
        } else {
            return new DeployerFakePlayer(world, profile);
        }
    }

    Pair<BlockPos, Float> getBlockBreakingProgress();

    void setBlockBreakingProgress(Pair<BlockPos, Float> blockBreakingProgress);

    ItemStack getSpawnedItemEffects();

    void setSpawnedItemEffects(ItemStack spawnedItemEffects);

    ServerPlayerInteractionManager getInteractionManager();

    boolean getPlacedTracks();

    void setPlacedTracks(boolean placedTracks);

    boolean isOnMinecartContraption();

    void setOnMinecartContraption(boolean onMinecartContraption);

    default ServerPlayerEntity cast() {
        return (ServerPlayerEntity) this;
    }

    boolean canModifyBlocks();

    PlayerInventory getInventory();

    ItemStack getMainHandStack();

    void setStackInHand(Hand hand, ItemStack stack);

    void setPosition(double x, double y, double z);

    void setYaw(float yaw);

    void setPitch(float pitch);

    int getId();

    void discard();

    ItemStack getActiveItem();

    void clearActiveItem();

    ItemEntity dropItem(ItemStack stack, boolean dropAtSelf, boolean retainOwnership);

    AttributeContainer getAttributes();

    ServerWorld getWorld();

    void resetLastAttackedTicks();

    void attack(Entity target);

    boolean isSneaking();

    boolean isBlockBreakingRestricted(World world, BlockPos pos, GameMode gameMode);

    boolean canHarvest(BlockState state);

    ItemStack getEquippedStack(EquipmentSlot slot);
}
