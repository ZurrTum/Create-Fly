package com.zurrtum.create.api.contraption.storage.item;

import com.mojang.serialization.Codec;
import com.zurrtum.create.api.contraption.storage.item.menu.MountedStorageMenus;
import com.zurrtum.create.content.contraptions.Contraption;
import com.zurrtum.create.infrastructure.items.ItemInventory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryOps;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.structure.StructureTemplate.StructureBlockInfo;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.OptionalInt;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class MountedItemStorage implements ItemInventory {
    public static final Codec<MountedItemStorage> CODEC = MountedItemStorageType.CODEC.dispatch(storage -> storage.type, type -> type.codec);

    @SuppressWarnings("deprecation")
    public static final PacketCodec<RegistryByteBuf, MountedItemStorage> STREAM_CODEC = PacketCodec.of(
        (b, t) -> t.encode(
            RegistryOps.of(
                NbtOps.INSTANCE,
                t.getRegistryManager()
            ), CODEC, b
        ),
        b -> b.decode(RegistryOps.of(NbtOps.INSTANCE, b.getRegistryManager()), CODEC)
    );

    public final MountedItemStorageType<? extends MountedItemStorage> type;

    protected MountedItemStorage(MountedItemStorageType<?> type) {
        this.type = Objects.requireNonNull(type);
    }

    /**
     * Un-mount this storage back into the world. The expected storage type of the target
     * block has already been checked to make sure it matches this storage's type.
     */
    public abstract void unmount(World level, BlockState state, BlockPos pos, @Nullable BlockEntity be);

    /**
     * Handle a player clicking on this mounted storage. This is always called on the server.
     * The default implementation will try to open a generic GUI for standard inventories.
     * For this to work, this storage must have 1-6 complete rows of 9 slots.
     *
     * @return true if the interaction was successful
     */
    public boolean handleInteraction(ServerPlayerEntity player, Contraption contraption, StructureBlockInfo info) {
        ServerWorld level = player.getEntityWorld();
        BlockPos localPos = info.pos();
        Vec3d localPosVec = Vec3d.ofCenter(localPos);
        Predicate<PlayerEntity> stillValid = p -> {
            Vec3d currentPos = contraption.entity.toGlobalVector(localPosVec, 0);
            return this.isMenuValid(player, contraption, currentPos);
        };
        Text menuName = this.getMenuName(info, contraption);
        Inventory handler = this.getHandlerForMenu(info, contraption);
        Consumer<PlayerEntity> onClose = p -> {
            Vec3d newPos = contraption.entity.toGlobalVector(localPosVec, 0);
            this.playClosingSound(level, newPos);
        };

        OptionalInt id = player.openHandledScreen(this.createMenuProvider(menuName, handler, stillValid, onClose));
        if (id.isPresent()) {
            Vec3d globalPos = contraption.entity.toGlobalVector(localPosVec, 0);
            this.playOpeningSound(level, globalPos);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Get the item handler that will be used by this storage's menu. This is useful for
     * handling multi-blocks, such as double chests.
     */
    protected Inventory getHandlerForMenu(StructureBlockInfo info, Contraption contraption) {
        return this;
    }

    /**
     * @param player the player who opened the menu
     * @param pos    the center of this storage in-world
     * @return true if a GUI opened for this storage is still valid
     */
    protected boolean isMenuValid(ServerPlayerEntity player, Contraption contraption, Vec3d pos) {
        return contraption.entity.isAlive() && player.squaredDistanceTo(pos) < (8 * 8);
    }

    /**
     * @return the title to be shown in the GUI when this storage is opened
     */
    protected Text getMenuName(StructureBlockInfo info, Contraption contraption) {
        MutableText blockName = info.state().getBlock().getName();
        return Text.translatable("create.contraptions.moving_container", blockName);
    }

    /**
     * @return a MenuProvider that provides the menu players will see when opening this storage
     */
    @Nullable
    protected NamedScreenHandlerFactory createMenuProvider(
        Text name,
        Inventory handler,
        Predicate<PlayerEntity> stillValid,
        Consumer<PlayerEntity> onClose
    ) {
        return MountedStorageMenus.createGeneric(name, handler, stillValid, onClose);
    }

    /**
     * Play the sound made by opening this storage's GUI.
     */
    protected void playOpeningSound(ServerWorld level, Vec3d pos) {
        level.playSound(null, BlockPos.ofFloored(pos), SoundEvents.BLOCK_BARREL_OPEN, SoundCategory.BLOCKS, 0.75f, 1f);
    }

    /**
     * Play the sound made by closing this storage's GUI.
     */
    protected void playClosingSound(ServerWorld level, Vec3d pos) {
    }
}