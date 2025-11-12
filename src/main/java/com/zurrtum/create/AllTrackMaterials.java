package com.zurrtum.create;

import com.zurrtum.create.content.trains.track.TrackMaterial;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

public class AllTrackMaterials {
    public static final TrackMaterial ANDESITE = new TrackMaterial(ResourceLocation.fromNamespaceAndPath(MOD_ID, "andesite"), () -> AllBlocks.TRACK);

    public static void register() {
    }
}
