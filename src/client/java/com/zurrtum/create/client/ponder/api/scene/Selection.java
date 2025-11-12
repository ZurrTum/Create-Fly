package com.zurrtum.create.client.ponder.api.scene;

import com.zurrtum.create.client.catnip.outliner.Outline;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public interface Selection extends Iterable<BlockPos>, Predicate<BlockPos> {
    Selection add(Selection other);

    Selection substract(Selection other);

    Selection copy();

    Vec3 getCenter();

    Outline.OutlineParams makeOutline(Outliner outliner, Object slot);

    default Outline.OutlineParams makeOutline(Outliner outliner) {
        return makeOutline(outliner, this);
    }
}