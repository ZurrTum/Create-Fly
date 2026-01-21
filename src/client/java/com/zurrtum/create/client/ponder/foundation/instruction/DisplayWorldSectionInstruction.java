package com.zurrtum.create.client.ponder.foundation.instruction;

import com.zurrtum.create.client.ponder.api.element.WorldSectionElement;
import com.zurrtum.create.client.ponder.api.scene.Selection;
import com.zurrtum.create.client.ponder.foundation.PonderScene;
import com.zurrtum.create.client.ponder.foundation.element.WorldSectionElementImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.jspecify.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class DisplayWorldSectionInstruction extends FadeIntoSceneInstruction<WorldSectionElement> {

    private final Selection initialSelection;
    @Nullable
    private final Supplier<WorldSectionElement> mergeOnto;
    private BlockPos glue;

    public DisplayWorldSectionInstruction(
        int fadeInTicks,
        @Nullable Direction fadeInFrom,
        Selection selection,
        @Nullable Supplier<WorldSectionElement> mergeOnto
    ) {
        this(fadeInTicks, fadeInFrom, selection, mergeOnto, null);
    }

    public DisplayWorldSectionInstruction(
        int fadeInTicks,
        @Nullable Direction fadeInFrom,
        Selection selection,
        @Nullable Supplier<WorldSectionElement> mergeOnto,
        @Nullable BlockPos glue
    ) {
        super(fadeInTicks, fadeInFrom, new WorldSectionElementImpl(selection));
        initialSelection = selection;
        this.mergeOnto = mergeOnto;
        this.glue = glue;
    }

    @Override
    protected void firstTick(PonderScene scene) {
        super.firstTick(scene);
        Optional.ofNullable(mergeOnto).ifPresent(wse -> element.setAnimatedOffset(wse.get().getAnimatedOffset(), true));
        element.set(initialSelection);
        element.setVisible(true);
    }

    @Override
    public void tick(PonderScene scene) {
        super.tick(scene);
        if (remainingTicks > 0)
            return;
        Optional.ofNullable(mergeOnto).ifPresent(c -> element.mergeOnto(c.get()));
        //TODO
        //if (glue != null)
        //	SuperGlueItem.spawnParticles(scene.getWorld(), glue, fadeInFrom, true);
    }

    @Override
    protected Class<WorldSectionElement> getElementClass() {
        return WorldSectionElement.class;
    }

}