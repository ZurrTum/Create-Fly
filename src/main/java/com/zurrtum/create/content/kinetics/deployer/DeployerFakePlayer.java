package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.authlib.GameProfile;
import com.zurrtum.create.infrastructure.player.FakePlayerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

public class DeployerFakePlayer extends FakePlayerEntity implements DeployerPlayer {
    public @Nullable Pair<BlockPos, Float> blockBreakingProgress;
    private @Nullable ItemStack spawnedItemEffects;
    public boolean placedTracks;
    public boolean onMinecartContraption;

    public DeployerFakePlayer(ServerLevel world, GameProfile profile) {
        super(world, profile);
        gameMode.setGameModeForPlayer(GameType.SURVIVAL, null);
    }

    @Override
    public ServerPlayerGameMode getInteractionManager() {
        return gameMode;
    }

    @Override
    @Nullable
    public Pair<BlockPos, Float> getBlockBreakingProgress() {
        return blockBreakingProgress;
    }

    @Override
    public void setBlockBreakingProgress(@Nullable Pair<BlockPos, Float> blockBreakingProgress) {
        this.blockBreakingProgress = blockBreakingProgress;
    }

    @Override
    @Nullable
    public ItemStack getSpawnedItemEffects() {
        return spawnedItemEffects;
    }

    @Override
    public void setSpawnedItemEffects(@Nullable ItemStack spawnedItemEffects) {
        this.spawnedItemEffects = spawnedItemEffects;
    }

    @Override
    public boolean getPlacedTracks() {
        return placedTracks;
    }

    @Override
    public void setPlacedTracks(boolean placedTracks) {
        this.placedTracks = placedTracks;
    }

    @Override
    public boolean isOnMinecartContraption() {
        return onMinecartContraption;
    }

    @Override
    public void setOnMinecartContraption(boolean onMinecartContraption) {
        this.onMinecartContraption = onMinecartContraption;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("create.block.deployer.damage_source_name");
    }

    @Override
    public EntityDimensions getDefaultDimensions(Pose pose) {
        return super.getDefaultDimensions(pose).withEyeHeight(0);
    }

    @Override
    public Vec3 position() {
        Vec3 pos = super.position();
        return new Vec3(pos.x, pos.y, pos.z);
    }

    @Override
    public float getCurrentItemAttackStrengthDelay() {
        return 1 / 64f;
    }

    @Override
    public boolean canEat(boolean ignoreHunger) {
        return false;
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        return false;
    }

    @Override
    public boolean doesEmitEquipEvent(EquipmentSlot slot) {
        return false;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        ServerLevel world = level();
        if (blockBreakingProgress != null && !world.isClientSide())
            world.destroyBlockProgress(getId(), blockBreakingProgress.getKey(), -1);
        super.remove(reason);
    }
}
