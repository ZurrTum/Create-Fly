package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.authlib.GameProfile;
import com.zurrtum.create.infrastructure.player.FakePlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;
import org.apache.commons.lang3.tuple.Pair;

public class DeployerFakePlayer extends FakePlayerEntity implements DeployerPlayer {
    public Pair<BlockPos, Float> blockBreakingProgress;
    private ItemStack spawnedItemEffects;
    public boolean placedTracks;
    public boolean onMinecartContraption;

    public DeployerFakePlayer(ServerWorld world, GameProfile profile) {
        super(world, profile);
        interactionManager.setGameMode(GameMode.SURVIVAL, null);
    }

    @Override
    public ServerPlayerInteractionManager getInteractionManager() {
        return interactionManager;
    }

    @Override
    public Pair<BlockPos, Float> getBlockBreakingProgress() {
        return blockBreakingProgress;
    }

    @Override
    public void setBlockBreakingProgress(Pair<BlockPos, Float> blockBreakingProgress) {
        this.blockBreakingProgress = blockBreakingProgress;
    }

    @Override
    public ItemStack getSpawnedItemEffects() {
        return spawnedItemEffects;
    }

    @Override
    public void setSpawnedItemEffects(ItemStack spawnedItemEffects) {
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
    public Text getDisplayName() {
        return Text.translatable("create.block.deployer.damage_source_name");
    }

    @Override
    public EntityDimensions getBaseDimensions(EntityPose pose) {
        return super.getBaseDimensions(pose).withEyeHeight(0);
    }

    @Override
    public Vec3d getEntityPos() {
        Vec3d pos = super.getEntityPos();
        return new Vec3d(pos.x, pos.y, pos.z);
    }

    @Override
    public float getAttackCooldownProgressPerTick() {
        return 1 / 64f;
    }

    @Override
    public boolean canConsume(boolean ignoreHunger) {
        return false;
    }

    @Override
    public boolean canHaveStatusEffect(StatusEffectInstance effect) {
        return false;
    }

    @Override
    public boolean isArmorSlot(EquipmentSlot slot) {
        return false;
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        ServerWorld world = getEntityWorld();
        if (blockBreakingProgress != null && !world.isClient())
            world.setBlockBreakingInfo(getId(), blockBreakingProgress.getKey(), -1);
        super.remove(reason);
    }
}
