package com.zurrtum.create.client.ponder.api.element;

import com.zurrtum.create.catnip.data.Pair;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public interface WorldSectionElement extends AnimatedSceneElement {

    void mergeOnto(WorldSectionElement other);

    void set(Selection selection);

    void add(Selection toAdd);

    void erase(Selection toErase);

    void setCenterOfRotation(Vec3d center);

    void stabilizeRotation(Vec3d anchor);

    void selectBlock(BlockPos pos);

    void resetSelectedBlock();

    void queueRedraw();

    boolean isEmpty();

    void setEmpty();

    void setAnimatedRotation(Vec3d eulerAngles, boolean force);

    Vec3d getAnimatedRotation();

    void setAnimatedOffset(Vec3d offset, boolean force);

    Vec3d getAnimatedOffset();

    Pair<Vec3d, BlockHitResult> rayTrace(PonderLevel world, Vec3d source, Vec3d target);
}