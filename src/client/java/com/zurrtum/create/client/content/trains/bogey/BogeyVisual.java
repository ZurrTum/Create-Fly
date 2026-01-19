package com.zurrtum.create.client.content.trains.bogey;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zurrtum.create.client.flywheel.api.instance.Instance;
import net.minecraft.nbt.CompoundTag;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public interface BogeyVisual {
    void update(CompoundTag bogeyData, float wheelAngle, PoseStack poseStack);

    void hide();

    void updateLight(int packedLight);

    void collectCrumblingInstances(Consumer<@Nullable Instance> consumer);

    void delete();
}
