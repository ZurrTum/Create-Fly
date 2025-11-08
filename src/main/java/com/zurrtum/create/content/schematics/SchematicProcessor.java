package com.zurrtum.create.content.schematics;

import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllStructureProcessorTypes;
import com.zurrtum.create.catnip.nbt.NBTProcessors;
import com.zurrtum.create.foundation.blockEntity.EntityControlStructureProcessor;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.structure.processor.StructureProcessorType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

public class SchematicProcessor extends StructureProcessor implements EntityControlStructureProcessor {

    public static final SchematicProcessor INSTANCE = new SchematicProcessor();
    public static final MapCodec<SchematicProcessor> CODEC = MapCodec.unit(() -> INSTANCE);

    private SchematicProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(
        WorldView world,
        BlockPos pos,
        BlockPos anotherPos,
        StructureTemplate.StructureBlockInfo rawInfo,
        StructureTemplate.StructureBlockInfo info,
        StructurePlacementData settings
    ) {
        if (info.nbt() != null && info.state().hasBlockEntity()) {
            BlockEntity be = ((BlockEntityProvider) info.state().getBlock()).createBlockEntity(info.pos(), info.state());
            if (be != null) {
                NbtCompound nbt = NBTProcessors.process(info.state(), be, info.nbt(), false);
                if (nbt != info.nbt())
                    return new StructureTemplate.StructureBlockInfo(info.pos(), info.state(), nbt);
            }
        }
        return info;
    }

    @Override
    public boolean skip(World world, StructureTemplate.StructureEntityInfo info) {
        return info.nbt.get("id", EntityType.CODEC).map(type -> !type.canPotentiallyExecuteCommands() && type.create(world, SpawnReason.LOAD) != null)
            .orElse(false);
    }

    @Override
    protected StructureProcessorType<?> getType() {
        return AllStructureProcessorTypes.SCHEMATIC;
    }
}
