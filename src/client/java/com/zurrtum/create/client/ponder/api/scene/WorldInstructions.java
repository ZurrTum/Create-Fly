package com.zurrtum.create.client.ponder.api.scene;

import com.zurrtum.create.client.ponder.api.element.ElementLink;
import com.zurrtum.create.client.ponder.api.element.EntityElement;
import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public interface WorldInstructions {
    RegistryWrapper.WrapperLookup getHolderLookupProvider();

    void incrementBlockBreakingProgress(BlockPos pos);

    void showSection(Selection selection, Direction fadeInDirection);

    void showSectionAndMerge(Selection selection, Direction fadeInDirection, ElementLink<WorldSectionElement> link);

    void glueBlockOnto(BlockPos position, Direction fadeInDirection, ElementLink<WorldSectionElement> link);

    ElementLink<WorldSectionElement> showIndependentSection(Selection selection, Direction fadeInDirection);

    ElementLink<WorldSectionElement> showIndependentSectionImmediately(Selection selection);

    void hideSection(Selection selection, Direction fadeOutDirection);

    void hideIndependentSection(ElementLink<WorldSectionElement> link, Direction fadeOutDirection);

    void restoreBlocks(Selection selection);

    ElementLink<WorldSectionElement> makeSectionIndependent(Selection selection);

    void rotateSection(ElementLink<WorldSectionElement> link, double xRotation, double yRotation, double zRotation, int duration);

    void configureCenterOfRotation(ElementLink<WorldSectionElement> link, Vec3d anchor);

    void configureStabilization(ElementLink<WorldSectionElement> link, Vec3d anchor);

    void moveSection(ElementLink<WorldSectionElement> link, Vec3d offset, int duration);

    void setBlocks(Selection selection, BlockState state, boolean spawnParticles);

    void destroyBlock(BlockPos pos);

    void setBlock(BlockPos pos, BlockState state, boolean spawnParticles);

    void replaceBlocks(Selection selection, BlockState state, boolean spawnParticles);

    void modifyBlock(BlockPos pos, UnaryOperator<BlockState> stateFunc, boolean spawnParticles);

    void cycleBlockProperty(BlockPos pos, Property<?> property);

    void modifyBlocks(Selection selection, UnaryOperator<BlockState> stateFunc, boolean spawnParticles);

    void toggleRedstonePower(Selection selection);

    <T extends Entity> void modifyEntities(Class<T> entityClass, Consumer<T> entityCallBack);

    <T extends Entity> void modifyEntitiesInside(Class<T> entityClass, Selection area, Consumer<T> entityCallBack);

    void modifyEntity(ElementLink<EntityElement> link, Consumer<Entity> entityCallBack);

    ElementLink<EntityElement> createEntity(Function<World, Entity> factory);

    ElementLink<EntityElement> createItemEntity(Vec3d location, Vec3d motion, ItemStack stack);

    void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> beType, Consumer<NbtCompound> consumer);

    <T extends BlockEntity> void modifyBlockEntity(BlockPos position, Class<T> beType, Consumer<T> consumer);

    void modifyBlockEntityNBT(Selection selection, Class<? extends BlockEntity> teType, Consumer<NbtCompound> consumer, boolean reDrawBlocks);
}