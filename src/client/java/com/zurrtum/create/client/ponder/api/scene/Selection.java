package com.zurrtum.create.client.ponder.api.scene;

import com.zurrtum.create.client.catnip.outliner.Outline;
import com.zurrtum.create.client.catnip.outliner.Outliner;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.function.Predicate;

public interface Selection extends Iterable<BlockPos>, Predicate<BlockPos> {
    Selection add(Selection other);

    Selection substract(Selection other);

    Selection copy();

    Vec3d getCenter();

    Outline.OutlineParams makeOutline(Outliner outliner, Object slot);

    default Outline.OutlineParams makeOutline(Outliner outliner) {
        return makeOutline(outliner, this);
    }
}