package com.zurrtum.create.content.kinetics.deployer;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.entity.FakePlayer;
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
import org.apache.commons.lang3.tuple.Pair;

public class DeployerFabricFakePlayer extends FakePlayer implements DeployerPlayer {
    private Pair<BlockPos, Float> blockBreakingProgress;
    private ItemStack spawnedItemEffects;
    public boolean placedTracks;
    public boolean onMinecartContraption;

    protected DeployerFabricFakePlayer(ServerWorld world, GameProfile profile) {
        super(world, profile);
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
    public Vec3d getPos() {
        Vec3d pos = super.getPos();
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
    public void remove(RemovalReason reason) {
        ServerWorld world = getWorld();
        if (blockBreakingProgress != null && !world.isClient())
            world.setBlockBreakingInfo(getId(), blockBreakingProgress.getKey(), -1);
        super.remove(reason);
    }
}
