package com.zurrtum.create.infrastructure.player;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.network.packet.c2s.common.SyncedClientOptions;
import net.minecraft.scoreboard.Team;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stat;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;

public class FakePlayerEntity extends ServerPlayerEntity {
    public FakePlayerEntity(ServerWorld world, GameProfile profile) {
        super(world.getServer(), world, profile, SyncedClientOptions.createDefault());
        this.networkHandler = new FakePlayerNetworkHandler(world.getServer(), this);
    }

    @Override
    public void tick() {
    }

    @Override
    public void setClientOptions(SyncedClientOptions settings) {
    }

    @Override
    public void increaseStat(Stat<?> stat, int amount) {
    }

    @Override
    public void resetStat(Stat<?> stat) {
    }

    @Override
    public boolean isInvulnerableTo(ServerWorld world, DamageSource damageSource) {
        return true;
    }

    @Nullable
    @Override
    public Team getScoreboardTeam() {
        // Scoreboard team is checked using the gameprofile name by default, which we don't want.
        return null;
    }

    @Override
    public void sleep(BlockPos pos) {
        // Don't lock bed forever.
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        return false;
    }

    @Override
    public void openEditSignScreen(SignBlockEntity sign, boolean front) {
    }

    @Override
    public OptionalInt openHandledScreen(@Nullable NamedScreenHandlerFactory factory) {
        return OptionalInt.empty();
    }

    @Override
    public void openHorseInventory(AbstractHorseEntity horse, Inventory inventory) {
    }
}
