package com.zurrtum.create.content.kinetics.mechanicalArm;

import com.zurrtum.create.api.registry.CreateRegistries;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnmodifiableView;

import java.util.Collections;
import java.util.List;

public abstract class ArmInteractionPointType {
    private static final List<ArmInteractionPointType> SORTED_TYPES = new ReferenceArrayList<>();
    @UnmodifiableView
    public static final List<ArmInteractionPointType> SORTED_TYPES_VIEW = Collections.unmodifiableList(SORTED_TYPES);

    public static void register() {
        SORTED_TYPES.clear();
        CreateRegistries.ARM_INTERACTION_POINT_TYPE.forEach(SORTED_TYPES::add);
        SORTED_TYPES.sort((t1, t2) -> t2.getPriority() - t1.getPriority());
    }

    @Nullable
    public static ArmInteractionPointType getPrimaryType(World level, BlockPos pos, BlockState state) {
        for (ArmInteractionPointType type : SORTED_TYPES_VIEW)
            if (type.canCreatePoint(level, pos, state))
                return type;
        return null;
    }

    public abstract boolean canCreatePoint(World level, BlockPos pos, BlockState state);

    @Nullable
    public abstract ArmInteractionPoint createPoint(World level, BlockPos pos, BlockState state);

    public int getPriority() {
        return 0;
    }
}
