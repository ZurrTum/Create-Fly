package com.zurrtum.create.client.content.trains.bogey;

import com.zurrtum.create.client.AllBogeyStyleRenders;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlock;
import com.zurrtum.create.content.trains.bogey.AbstractBogeyBlockEntity;
import com.zurrtum.create.content.trains.bogey.BogeySize;
import com.zurrtum.create.content.trains.bogey.BogeyStyle;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.block.entity.state.BlockEntityRenderState;
import net.minecraft.client.render.command.ModelCommandRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.state.CameraRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BogeyBlockEntityRenderer<T extends AbstractBogeyBlockEntity> implements BlockEntityRenderer<T, BogeyBlockEntityRenderer.BogeyBlockEntityRenderState> {
    public BogeyBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
    }

    @Override
    public BogeyBlockEntityRenderState createRenderState() {
        return new BogeyBlockEntityRenderState();
    }

    @Override
    public void updateRenderState(
        T be,
        BogeyBlockEntityRenderState state,
        float tickProgress,
        Vec3d cameraPos,
        @Nullable ModelCommandRenderer.CrumblingOverlayCommand crumblingOverlay
    ) {
        state.blockState = be.getCachedState();
        if (!(state.blockState.getBlock() instanceof AbstractBogeyBlock<?> bogey)) {
            return;
        }
        state.pos = be.getPos();
        state.type = be.getType();
        World world = be.getWorld();
        state.lightmapCoordinates = world != null ? WorldRenderer.getLightmapCoordinates(
            world,
            state.pos
        ) : LightmapTextureManager.MAX_LIGHT_COORDINATE;
        state.angle = be.getVirtualAngle(tickProgress);
        if (state.blockState.get(AbstractBogeyBlock.AXIS) == Direction.Axis.X) {
            state.yRot = MathHelper.RADIANS_PER_DEGREE * 90;
        }
        state.style = be.getStyle();
        state.size = bogey.getSize();
        state.bogeyData = be.getBogeyData();
        if (state.bogeyData == null) {
            state.bogeyData = new NbtCompound();
        }
        state.data = AllBogeyStyleRenders.getRenderData(be, state, tickProgress, cameraPos, false);
    }

    @Override
    public void render(BogeyBlockEntityRenderState state, MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState) {
        if (state.data == null) {
            return;
        }
        matrices.push();
        matrices.translate(.5f, .5f, .5f);
        if (state.yRot != 0) {
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(state.yRot));
        }
        state.data.render(matrices, queue, cameraState);
        matrices.pop();
    }

    public static class BogeyBlockEntityRenderState extends BlockEntityRenderState {
        public float angle;
        public float yRot;
        public BogeyStyle style;
        public BogeySize size;
        public NbtCompound bogeyData;
        public BogeyRenderState data;
    }

    public interface BogeyRenderState {
        void render(MatrixStack matrices, OrderedRenderCommandQueue queue, CameraRenderState cameraState);
    }
}
