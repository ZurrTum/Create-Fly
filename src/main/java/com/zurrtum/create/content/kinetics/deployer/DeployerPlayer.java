package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.authlib.GameProfile;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface DeployerPlayer {
    UUID FALLBACK_ID = UUID.fromString("9e2faded-cafe-4ec2-c314-dad129ae971d");

    static DeployerPlayer create(ServerWorld world, @Nullable UUID owner, @Nullable String name) {
        GameProfile profile = new GameProfile(owner == null ? FALLBACK_ID : owner, owner == null || name == null ? "Deployer" : name);
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
}
