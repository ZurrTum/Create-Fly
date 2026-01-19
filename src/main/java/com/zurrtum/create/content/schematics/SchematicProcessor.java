package com.zurrtum.create.content.schematics;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllStructureProcessorTypes;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.foundation.blockEntity.EntityControlStructureProcessor;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jspecify.annotations.Nullable;

public class SchematicProcessor extends StructureProcessor implements EntityControlStructureProcessor {
    public static final SchematicProcessor INSTANCE = new SchematicProcessor();
    public static final MapCodec<SchematicProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private SchematicProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo processBlock(
        LevelReader world,
        BlockPos pos,
        BlockPos anotherPos,
        StructureTemplate.StructureBlockInfo rawInfo,
        StructureTemplate.StructureBlockInfo info,
        StructurePlaceSettings settings
    ) {
        if (info.nbt() != null && info.state().hasBlockEntity()) {
            BlockEntity be = ((EntityBlock) info.state().getBlock()).newBlockEntity(info.pos(), info.state());
            if (be != null) {
                CompoundTag nbt = NBTProcessors.process(info.state(), be, info.nbt(), false);
                if (nbt != info.nbt())
                    return new StructureTemplate.StructureBlockInfo(info.pos(), info.state(), nbt);
            }
        }
        return info;
    }

    @Override
    public boolean skip(Level world, StructureTemplate.StructureEntityInfo info) {
        return info.nbt.read("id", EntityType.CODEC).map(type -> !type.onlyOpCanSetNbt() && type.create(world, EntitySpawnReason.LOAD) != null)
            .orElse(false);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return AllStructureProcessorTypes.SCHEMATIC;
    }
}
