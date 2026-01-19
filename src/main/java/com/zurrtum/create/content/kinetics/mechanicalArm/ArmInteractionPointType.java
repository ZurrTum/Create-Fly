package com.zurrtum.create.content.kinetics.mechanicalArm;

import com.zurrtum.create.api.registry.CreateRegistries;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnmodifiableView;
import org.jspecify.annotations.Nullable;

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
    public static ArmInteractionPointType getPrimaryType(Level level, BlockPos pos, BlockState state) {
        for (ArmInteractionPointType type : SORTED_TYPES_VIEW)
            if (type.canCreatePoint(level, pos, state))
                return type;
        return null;
    }

    public abstract boolean canCreatePoint(Level level, BlockPos pos, BlockState state);

    @Nullable
    public abstract ArmInteractionPoint createPoint(Level level, BlockPos pos, BlockState state);

    public int getPriority() {
        return 0;
    }
}
