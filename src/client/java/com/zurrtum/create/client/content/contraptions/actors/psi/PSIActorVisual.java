package com.zurrtum.create.client.content.contraptions.actors.psi;

import com.zurrtum.create.catnip.animation.LerpedFloat;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.contraptions.render.ActorVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.psi.PortableStorageInterfaceMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;

public class PSIActorVisual extends ActorVisual {

    private final PIInstance instance;

    public PSIActorVisual(VisualizationContext context, VirtualRenderWorld world, MovementContext movementContext) {
        super(context, world, movementContext);

        instance = new PIInstance(context.instancerProvider(), movementContext.state, movementContext.localPos, false);

        instance.middle.light(localBlockLight(), 0);
        instance.top.light(localBlockLight(), 0);
    }

    @Override
    public void beginFrame() {
        LerpedFloat lf = PortableStorageInterfaceMovement.getAnimation(context);
        instance.tick(lf.settled());
        instance.beginFrame(lf.getValue(AnimationTickHolder.getPartialTicks()));
    }

    @Override
    protected void _delete() {
        instance.remove();
    }
}
