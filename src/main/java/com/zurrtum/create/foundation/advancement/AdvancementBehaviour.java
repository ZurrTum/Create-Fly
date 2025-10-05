package com.zurrtum.create.foundation.advancement;

import com.zurrtum.create.api.entity.FakePlayerHandler;
import com.zurrtum.create.foundation.advancement.CreateTrigger.Conditions;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import com.zurrtum.create.foundation.blockEntity.behaviour.BehaviourType;
import com.zurrtum.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.advancement.criterion.Criterion.ConditionsContainer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.ServerAdvancementLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.util.Uuids;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class AdvancementBehaviour extends BlockEntityBehaviour<SmartBlockEntity> {
    public static final BehaviourType<AdvancementBehaviour> TYPE = new BehaviourType<>();

    private UUID playerId;
    private final Set<CreateTrigger> advancements;

    public AdvancementBehaviour(SmartBlockEntity be, ServerPlayerEntity player, CreateTrigger... advancements) {
        super(be);
        this.advancements = new HashSet<>(List.of(advancements));
        playerId = player.getUuid();
        removeAwarded(player);
        blockEntity.markDirty();
    }

    public boolean isOwnerPresent() {
        return playerId != null;
    }

    @Override
    public void initialize() {
        ServerPlayerEntity player = getPlayer();
        if (player != null) {
            removeAwarded(player);
        }
    }

    private void removeAwarded(ServerPlayerEntity player) {
        if (advancements.isEmpty())
            return;
        ServerAdvancementLoader loader = player.getEntityWorld().getServer().getAdvancementLoader();
        PlayerAdvancementTracker advancementTracker = player.getAdvancementTracker();
        advancements.removeIf(trigger -> {
            Set<ConditionsContainer<Conditions>> containers = trigger.listeners.get(advancementTracker);
            if (containers != null) {
                return containers.stream().allMatch(container -> advancementTracker.getProgress(container.advancement()).isDone());
            }
            AdvancementEntry advancement = loader.get(trigger.id);
            if (advancement == null) {
                return true;
            }
            return advancementTracker.getProgress(advancement).isDone();
        });
        if (advancements.isEmpty()) {
            playerId = null;
            blockEntity.markDirty();
        }
    }

    public void awardPlayerIfNear(CreateTrigger advancement, int maxDistance) {
        ServerPlayerEntity player = getPlayer();
        if (player == null)
            return;
        if (player.squaredDistanceTo(Vec3d.ofCenter(getPos())) > maxDistance * maxDistance)
            return;
        award(advancement, player);
    }

    public void awardPlayer(CreateTrigger advancement) {
        ServerPlayerEntity player = getPlayer();
        if (player == null)
            return;
        award(advancement, player);
    }

    private void award(CreateTrigger advancement, ServerPlayerEntity player) {
        if (advancements.contains(advancement)) {
            advancement.trigger(player);
            removeAwarded(player);
        }
    }

    private ServerPlayerEntity getPlayer() {
        if (playerId == null)
            return null;
        return (ServerPlayerEntity) getWorld().getPlayerByUuid(playerId);
    }

    @Override
    public void write(WriteView view, boolean clientPacket) {
        super.write(view, clientPacket);
        if (playerId != null)
            view.put("Owner", Uuids.INT_STREAM_CODEC, playerId);
    }

    @Override
    public void read(ReadView view, boolean clientPacket) {
        super.read(view, clientPacket);
        playerId = view.read("Owner", Uuids.INT_STREAM_CODEC).orElse(null);
    }

    @Override
    public BehaviourType<?> getType() {
        return TYPE;
    }

    public static void tryAward(BlockView reader, BlockPos pos, CreateTrigger advancement) {
        AdvancementBehaviour behaviour = BlockEntityBehaviour.get(reader, pos, AdvancementBehaviour.TYPE);
        if (behaviour != null)
            behaviour.awardPlayer(advancement);
    }

    public static void setPlacedBy(World worldIn, BlockPos pos, LivingEntity placer) {
        if (worldIn.isClient())
            return;
        if (!(worldIn.getBlockEntity(pos) instanceof SmartBlockEntity blockEntity)) {
            return;
        }
        if (placer instanceof ServerPlayerEntity player) {
            if (FakePlayerHandler.has(player))
                return;
            blockEntity.addAdvancementBehaviour(player);
        }
    }

}
