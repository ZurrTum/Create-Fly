package com.zurrtum.create.client;

import com.zurrtum.create.AllEntityTypes;
import com.zurrtum.create.client.content.contraptions.actors.seat.SeatRenderer;
import com.zurrtum.create.client.content.contraptions.glue.SuperGlueRenderer;
import com.zurrtum.create.client.content.contraptions.render.*;
import com.zurrtum.create.client.content.equipment.blueprint.BlueprintRenderer;
import com.zurrtum.create.client.content.equipment.potatoCannon.PotatoProjectileRenderer;
import com.zurrtum.create.client.content.logistics.box.PackageRenderer;
import com.zurrtum.create.client.content.logistics.box.PackageVisual;
import com.zurrtum.create.client.content.logistics.depot.EjectorItemEntityRenderer;
import com.zurrtum.create.client.content.trains.entity.CarriageContraptionEntityRenderer;
import com.zurrtum.create.client.content.trains.entity.CarriageContraptionVisual;
import com.zurrtum.create.client.flywheel.lib.visualization.SimpleEntityVisualizer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public class AllEntityRenders {
    private static <T extends Entity, P extends T> void visual(
        EntityType<P> type,
        EntityRendererProvider<T> rendererFactory,
        SimpleEntityVisualizer.Factory<P> visualizerFactory
    ) {
        EntityRenderers.register(type, rendererFactory);
        SimpleEntityVisualizer.builder(type).factory(visualizerFactory).skipVanillaRender(blockEntity -> false).apply();
    }

    public static <T extends Entity> void render(EntityType<? extends T> type, EntityRendererProvider<T> factory) {
        EntityRenderers.register(type, factory);
    }

    public static void register() {
        render(AllEntityTypes.EJECTOR_ITEM, EjectorItemEntityRenderer::new);
        visual(AllEntityTypes.ORIENTED_CONTRAPTION, OrientedContraptionEntityRenderer::new, OrientedContraptionVisual::new);
        visual(AllEntityTypes.CONTROLLED_CONTRAPTION, ControlledContraptionEntityRenderer::new, ControlledContraptionVisual::new);
        visual(AllEntityTypes.CARRIAGE_CONTRAPTION, CarriageContraptionEntityRenderer::new, CarriageContraptionVisual::new);
        render(AllEntityTypes.SUPER_GLUE, SuperGlueRenderer::new);
        visual(AllEntityTypes.GANTRY_CONTRAPTION, ContraptionEntityRenderer::new, ContraptionVisual::new);
        render(AllEntityTypes.SEAT, SeatRenderer::new);
        render(AllEntityTypes.POTATO_PROJECTILE, PotatoProjectileRenderer::new);
        visual(AllEntityTypes.PACKAGE, PackageRenderer::new, PackageVisual::new);
        render(AllEntityTypes.CRAFTING_BLUEPRINT, BlueprintRenderer::new);
    }
}
