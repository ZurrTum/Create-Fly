package com.zurrtum.create.client.content.logistics.packagerLink;

import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelBehaviour;
import com.zurrtum.create.client.content.logistics.factoryBoard.FactoryPanelConnectionHandler;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBehaviour;
import com.zurrtum.create.content.logistics.packagerLink.LogisticallyLinkedBlockItem;
import com.zurrtum.create.foundation.blockEntity.SmartBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LogisticallyLinkedClientHandler {

    private static @Nullable UUID previouslyHeldFrequency;

    public static void tick(Minecraft mc) {
        previouslyHeldFrequency = null;

        LocalPlayer player = mc.player;
        if (player == null)
            return;
        ItemStack mainHandItem = player.getMainHandItem();
        if (!(mainHandItem.getItem() instanceof LogisticallyLinkedBlockItem) || !LogisticallyLinkedBlockItem.isTuned(mainHandItem))
            return;

        TypedEntityData<BlockEntityType<?>> data = mainHandItem.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null || !data.contains("Freq")) {
            return;
        }
        CompoundTag tag = data.copyTagWithoutId();
        Optional<UUID> uuid = tag.read("Freq", UUIDUtil.CODEC);
        if (uuid.isEmpty())
            return;

        previouslyHeldFrequency = uuid.get();

        for (LogisticallyLinkedBehaviour behaviour : LogisticallyLinkedBehaviour.getAllPresent(previouslyHeldFrequency, false, true)) {
            SmartBlockEntity be = behaviour.blockEntity;
            VoxelShape shape = be.getBlockState().getShape(player.level(), be.getBlockPos());
            if (shape.isEmpty())
                continue;
            if (!player.blockPosition().closerThan(be.getBlockPos(), 64))
                continue;
            List<AABB> list = shape.toAabbs();
            for (int i = 0, size = list.size(); i < size; i++) {
                AABB aabb = list.get(i);
                Outliner.getInstance().showAABB(Pair.of(behaviour, i), aabb.inflate(-1 / 128f).move(be.getBlockPos()), 2).lineWidth(1 / 32f)
                    .disableLineNormals().colored(AnimationTickHolder.getTicks() % 16 < 8 ? 0x708DAD : 0x90ADCD);
            }

        }
    }

    public static void tickPanel(FactoryPanelBehaviour fpb) {
        if (previouslyHeldFrequency == null)
            return;
        if (!previouslyHeldFrequency.equals(fpb.getNetwork()))
            return;
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;
        if (!player.blockPosition().closerThan(fpb.getPos(), 64))
            return;

        Outliner.getInstance()
            .showAABB(fpb, FactoryPanelConnectionHandler.getBB(fpb.blockEntity.getBlockState(), fpb.getPanelPosition()).inflate(-1.5 / 128f))
            .lineWidth(1 / 32f).disableLineNormals().colored(AnimationTickHolder.getTicks() % 16 < 8 ? 0x708DAD : 0x90ADCD);
    }

}
