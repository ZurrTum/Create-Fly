package com.zurrtum.create.foundation.advancement;

import com.zurrtum.create.api.behaviour.BlockEntityBehaviour;
import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.foundation.advancement.CreateTrigger.Conditions;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.CriterionTrigger.Listener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdvancementBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<AdvancementBehaviour> TYPE = new BehaviourType<>();

    private @Nullable UUID playerId;
    private final Set<CreateTrigger> advancements;

    public AdvancementBehaviour(SmartBlockEntity be, ServerPlayer player, CreateTrigger... advancements) {
        super(be);
        this.advancements = new HashSet<>(List.of(advancements));
        playerId = player.getUUID();
        removeAwarded(player);
        blockEntity.setChanged();
    }

    public boolean isOwnerPresent() {
        return playerId != null;
    }

    @Override
    public void initialize() {
        ServerPlayer player = getPlayer();
        if (player != null) {
            removeAwarded(player);
        }
    }

    private void removeAwarded(ServerPlayer player) {
        if (advancements.isEmpty())
            return;
        ServerAdvancementManager loader = player.level().getServer().getAdvancements();
        PlayerAdvancements advancementTracker = player.getAdvancements();
        advancements.removeIf(trigger -> {
            Set<Listener<Conditions>> containers = trigger.listeners.get(advancementTracker);
            if (containers != null) {
                return containers.stream().allMatch(container -> advancementTracker.getOrStartProgress(container.advancement()).isDone());
            }
            AdvancementHolder advancement = loader.get(trigger.id);
            if (advancement == null) {
                return true;
            }
            return advancementTracker.getOrStartProgress(advancement).isDone();
        });
        if (advancements.isEmpty()) {
            playerId = null;
            blockEntity.setChanged();
        }
    }

    public void awardPlayerIfNear(CreateTrigger advancement, int maxDistance) {
        ServerPlayer player = getPlayer();
        if (player == null)
            return;
        if (player.distanceToSqr(Vec3.atCenterOf(getPos())) > maxDistance * maxDistance)
            return;
        award(advancement, player);
    }

    public void awardPlayer(CreateTrigger advancement) {
        ServerPlayer player = getPlayer();
        if (player == null)
            return;
        award(advancement, player);
    }

    private void award(CreateTrigger advancement, ServerPlayer player) {
        if (advancements.contains(advancement)) {
            advancement.trigger(player);
            removeAwarded(player);
        }
    }

    @Nullable
    private ServerPlayer getPlayer() {
        if (playerId == null)
            return null;
        return (ServerPlayer) getLevel().getPlayerByUUID(playerId);
    }

    @Override
    public void write(ValueOutput view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (playerId != null)
            view.store("Owner", UUIDUtil.CODEC, playerId);
    }

    @Override
    public void read(ValueInput view, boolean clientPacket) {
        super.read(view, clientPacket);
        playerId = view.read("Owner", UUIDUtil.CODEC).orElse(null);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public static void tryAward(BlockGetter reader, BlockPos pos, CreateTrigger advancement) {
        AdvancementBehaviour behaviour = BlockEntityBehaviour.get(reader, pos, AdvancementBehaviour.TYPE);
        if (behaviour != null)
            behaviour.awardPlayer(advancement);
    }

    public static void setPlacedBy(Level worldIn, BlockPos pos, @Nullable LivingEntity placer) {
        if (worldIn.isClientSide())
            return;
        if (!(worldIn.getBlockEntity(pos) instanceof SmartBlockEntity blockEntity)) {
            return;
        }
        if (placer instanceof ServerPlayer player) {
            if (FakePlayerHandler.has(player))
                return;
            blockEntity.addAdvancementBehaviour(player);
        }
    }

}
