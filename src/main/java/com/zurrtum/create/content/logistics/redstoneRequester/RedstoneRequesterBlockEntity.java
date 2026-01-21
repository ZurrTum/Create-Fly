package com.zurrtum.create.content.logistics.redstoneRequester;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllParticleTypes;
import com.zurrtum.create.AllSoundEvents;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.content.logistics.BigItemStack;
import com.zurrtum.create.content.logistics.packager.InventorySummary;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour.RequestType;
import com.zurrtum.create.content.logistics.stockTicker.StockCheckingBlockEntity;
import com.zurrtum.create.foundation.gui.menu.MenuProvider;
import com.zurrtum.create.infrastructure.component.PackageOrderWithCrafts;
import com.zurrtum.create.infrastructure.packet.s2c.RedstoneRequesterEffectPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class RedstoneRequesterBlockEntity extends StockCheckingBlockEntity implements MenuProvider {

    public boolean allowPartialRequests;
    public PackageOrderWithCrafts encodedRequest = PackageOrderWithCrafts.empty();
    public String encodedTargetAdress = "";

    public boolean lastRequestSucceeded;

    protected boolean redstonePowered;

    public RedstoneRequesterBlockEntity(BlockPos pos, BlockState state) {
        super(AllBlockEntityTypes.REDSTONE_REQUESTER, pos, state);
        allowPartialRequests = false;
    }

    protected void onRedstonePowerChanged() {
        boolean hasNeighborSignal = level.hasNeighborSignal(worldPosition);
        if (redstonePowered == hasNeighborSignal)
            return;

        lastRequestSucceeded = false;
        if (hasNeighborSignal)
            triggerRequest();

        redstonePowered = hasNeighborSignal;
        notifyUpdate();
    }

    public void triggerRequest() {
        if (encodedRequest.isEmpty())
            return;

        boolean anySucceeded = false;

        InventorySummary summaryOfOrder = new InventorySummary();
        encodedRequest.stacks().forEach(summaryOfOrder::add);

        InventorySummary summary = getAccurateSummary();
        for (BigItemStack entry : summaryOfOrder.getStacks()) {
            if (summary.getCountOf(entry.stack) >= entry.count) {
                anySucceeded = true;
                continue;
            }
            if (!allowPartialRequests && level instanceof ServerLevel serverLevel) {
                serverLevel.getServer().getPlayerList().broadcast(
                    null,
                    worldPosition.getX(),
                    worldPosition.getY(),
                    worldPosition.getZ(),
                    32,
                    serverLevel.dimension(),
                    new RedstoneRequesterEffectPacket(worldPosition, false)
                );
                return;
            }
        }

        broadcastPackageRequest(RequestType.REDSTONE, encodedRequest, null, encodedTargetAdress);
        if (level instanceof ServerLevel serverLevel)
            serverLevel.getServer().getPlayerList().broadcast(
                null,
                worldPosition.getX(),
                worldPosition.getY(),
                worldPosition.getZ(),
                32,
                serverLevel.dimension(),
                new RedstoneRequesterEffectPacket(worldPosition, anySucceeded)
            );
        lastRequestSucceeded = true;
    }

    @Override
    protected void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        redstonePowered = view.getBooleanOr("Powered", false);
        lastRequestSucceeded = view.getBooleanOr("Success", false);
        allowPartialRequests = view.getBooleanOr("AllowPartial", false);
        encodedRequest = view.read("EncodedRequest", PackageOrderWithCrafts.CODEC).orElse(PackageOrderWithCrafts.empty());
        encodedTargetAdress = view.getStringOr("EncodedAddress", "");
    }

    @Override
    public void writeSafe(ValueOutput view) {
        super.writeSafe(view);
        view.putBoolean("AllowPartial", allowPartialRequests);
        view.putString("EncodedAddress", encodedTargetAdress);
        view.store("EncodedRequest", PackageOrderWithCrafts.CODEC, encodedRequest);
    }

    @Override
    protected void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        view.putBoolean("Powered", redstonePowered);
        view.putBoolean("Success", lastRequestSucceeded);
        view.putBoolean("AllowPartial", allowPartialRequests);
        view.putString("EncodedAddress", encodedTargetAdress);
        view.store("EncodedRequest", PackageOrderWithCrafts.CODEC, encodedRequest);
    }

    public InteractionResult use(@Nullable Player player) {
        if (player == null || player.isCrouching())
            return InteractionResult.PASS;
        if (FakePlayerHandler.has(player))
            return InteractionResult.PASS;
        if (level.isClientSide())
            return InteractionResult.SUCCESS;
        if (!behaviour.mayInteractMessage(player))
            return InteractionResult.SUCCESS;

        openHandledScreen((ServerPlayer) player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public RedstoneRequesterMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer, RegistryFriendlyByteBuf extraData) {
        extraData.writeBlockPos(worldPosition);
        return new RedstoneRequesterMenu(pContainerId, pPlayerInventory, this);
    }

    public void playEffect(boolean success) {
        Vec3 vec3 = Vec3.atCenterOf(worldPosition);
        if (success) {
            AllSoundEvents.CONFIRM.playAt(level, worldPosition, 0.5f, 1.5f, false);
            AllSoundEvents.STOCK_LINK.playAt(level, worldPosition, 1.0f, 1.0f, false);
            level.addParticle(AllParticleTypes.WIFI, vec3.x, vec3.y, vec3.z, 1, 1, 1);
        } else {
            AllSoundEvents.DENY.playAt(level, worldPosition, 0.5f, 1, false);
            level.addParticle(ParticleTypes.ENCHANTED_HIT, vec3.x, vec3.y + 1, vec3.z, 0, 0, 0);
        }
    }

}