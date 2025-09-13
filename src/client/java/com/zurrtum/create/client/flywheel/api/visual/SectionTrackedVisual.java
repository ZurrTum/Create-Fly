package com.zurrtum.create.client.flywheel.api.visual;

import it.unimi.dsi.fastutil.longs.LongSet;
import org.jetbrains.annotations.ApiStatus;

public sealed interface SectionTrackedVisual extends Visual permits LightUpdatedVisual, ShaderLightVisual {
    /**
     * Set the section collector object.
     *
     * <p>This method is only called once, upon visual creation.
     * <br>If the collector is invoked in this method, the
     * visual will immediately be tracked in the given sections.
     *
     * @param collector The collector.
     */
    void setSectionCollector(SectionCollector collector);

    @ApiStatus.NonExtendable
    interface SectionCollector {
        /**
         * Assign the set of sections this visual wants to track itself in.
         */
        void sections(LongSet sections);
    }
}
