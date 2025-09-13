package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.flywheel.api.instance.Instance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface BogeyVisual {
    void update(NbtCompound bogeyData, float wheelAngle, MatrixStack poseStack);

    void hide();

    void updateLight(int packedLight);

    void collectCrumblingInstances(Consumer<@Nullable Instance> consumer);

    void delete();
}
