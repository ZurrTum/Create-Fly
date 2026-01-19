package com.zurrtum.create.infrastructure.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.Container;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

import java.util.OptionalInt;

public class FakePlayerEntity extends ServerPlayer {
    public FakePlayerEntity(ServerLevel world, GameProfile profile) {
        super(world.getServer(), world, profile, ClientInformation.createDefault());
        this.connection = new FakePlayerNetworkHandler(world.getServer(), this);
    }

    @Override
    public void tick() {
    }

    @Override
    public void updateOptions(ClientInformation settings) {
    }

    @Override
    public void awardStat(Stat<?> stat, int amount) {
    }

    @Override
    public void resetStat(Stat<?> stat) {
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel world, DamageSource damageSource) {
        return true;
    }

    @Nullable
    @Override
    public PlayerTeam getTeam() {
        // Scoreboard team is checked using the gameprofile name by default, which we don't want.
        return null;
    }

    @Override
    public void startSleeping(BlockPos pos) {
        // Don't lock bed forever.
    }

    @Override
    public boolean startRiding(Entity entity, boolean force, boolean emitEvent) {
        return false;
    }

    @Override
    public void openTextEdit(SignBlockEntity sign, boolean front) {
    }

    @Override
    public OptionalInt openMenu(@Nullable MenuProvider factory) {
        return OptionalInt.empty();
    }

    @Override
    public void openHorseInventory(AbstractHorse horse, Container inventory) {
    }
}
