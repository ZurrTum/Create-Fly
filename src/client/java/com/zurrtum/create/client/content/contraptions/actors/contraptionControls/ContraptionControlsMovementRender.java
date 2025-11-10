package com.zurrtum.create.client.content.contraptions.actors.contraptionControls;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Couple;
import com.zurrtum.create.catnip.math.AngleHelper;
import com.zurrtum.create.catnip.theme.Color;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderBehaviour;
import com.zurrtum.create.client.api.behaviour.movement.MovementRenderState;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.catnip.render.CachedBuffers;
import com.zurrtum.create.client.catnip.render.SuperByteBuffer;
import com.zurrtum.create.client.content.contraptions.render.ClientContraption;
import com.zurrtum.create.client.foundation.utility.DyeHelper;
import com.zurrtum.create.client.foundation.virtualWorld.VirtualRenderWorld;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlock;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsBlockEntity;
import com.zurrtum.create.content.contraptions.actors.contraptionControls.ContraptionControlsMovement;
import com.zurrtum.create.content.contraptions.behaviour.MovementContext;
import net.minecraft.block.BlockState;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.Random;

public class ContraptionControlsMovementRender implements MovementRenderBehaviour {
    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::new);

    @Override
    public MovementRenderState getRenderState(
        Vec3d camera,
        TextRenderer textRenderer,
        MovementContext context,
        VirtualRenderWorld renderWorld,
        Matrix4f worldMatrix4f
    ) {
        if (!(context.temporaryData instanceof ContraptionControlsMovement.ElevatorFloorSelection efs)) {
            return null;
        }
        BlockState blockState = context.state;
        if (!blockState.isOf(AllBlocks.CONTRAPTION_CONTROLS)) {
            return null;
        }
        BlockPos pos = context.localPos;
        ContraptionControlsMovementRenderState state = new ContraptionControlsMovementRenderState(pos);
        float flicker = RANDOM.get().nextFloat();
        if (ClientContraption.getBlockEntityClientSide(context.contraption, pos) instanceof ContraptionControlsBlockEntity cbe) {
            state.buttondepth = -1 / 24f * cbe.button.getValue(AnimationTickHolder.getPartialTicks(renderWorld));
        }
        state.layer = RenderLayer.getSolid();
        Direction facing = blockState.get(ContraptionControlsBlock.FACING);
        state.button = CachedBuffers.partialFacing(AllPartialModels.CONTRAPTION_CONTROLS_BUTTON, blockState, facing.getOpposite());
        state.light = WorldRenderer.getLightmapCoordinates(renderWorld, pos);
        state.world = context.world;
        state.worldMatrix4f = worldMatrix4f;
        String text = efs.currentShortName;
        String description = efs.currentLongName;
        Vec3d position = context.position;
        float playerDistance = (float) (position == null ? 0 : camera.squaredDistanceTo(position));
        boolean hideText = text.isBlank() || playerDistance > 100;
        boolean hideDescription = description.isBlank() || playerDistance > 20;
        if (hideText && hideDescription) {
            return state;
        }
        state.upAngle = AngleHelper.rad(AngleHelper.horizontalAngle(facing));
        state.westAngle = AngleHelper.rad(67.5f);
        Couple<Integer> couple = DyeHelper.getDyeColors(efs.targetYEqualsSelection ? DyeColor.WHITE : DyeColor.ORANGE);
        int brightColor = couple.getFirst();
        int darkColor = couple.getSecond();
        state.color = Color.mixColors(brightColor, darkColor, flicker / 4) | 0xFF000000;
        state.offsetZ = state.buttondepth - .25f;
        if (!hideText) {
            state.shadowColor = Color.mixColors(darkColor, 0, .35f) | 0xFF000000;
            int actualWidth = textRenderer.getWidth(text);
            int width = Math.max(actualWidth, 12);
            state.textScale = 1 / (5f * (width - .5f));
            state.textX = (float) Math.max(0, width - actualWidth) / 2;
            state.textY = (width - 8f) / 2;
            state.text = Text.literal(text).asOrderedText();
        }
        if (!hideDescription) {
            int actualWidth = textRenderer.getWidth(description);
            int width = Math.max(actualWidth, 55);
            state.descriptionScale = 1 / (3f * (width - .5f));
            state.descriptionX = (float) Math.max(0, width - actualWidth) / 2;
            state.descriptionY = (width - 8f) / 2;
            state.description = Text.literal(description).asOrderedText();
        }
        return state;
    }

    public static class ContraptionControlsMovementRenderState extends MovementRenderState implements OrderedRenderCommandQueue.Custom {
        public RenderLayer layer;
        public SuperByteBuffer button;
        public int light;
        public World world;
        public Matrix4f worldMatrix4f;
        public float buttondepth;
        public float upAngle;
        public float westAngle;
        public float offsetZ;
        public OrderedText text;
        public OrderedText description;
        public float textScale;
        public float textX;
        public float textY;
        public float descriptionScale;
        public float descriptionX;
        public float descriptionY;
        public int color;
        public int shadowColor;

        public ContraptionControlsMovementRenderState(BlockPos pos) {
            super(pos);
        }

        @Override
        public void render(MatrixStack matrices, OrderedRenderCommandQueue queue) {
            matrices.push();
            matrices.translate(0, buttondepth, 0);
            queue.submitCustom(matrices, layer, this);
            matrices.pop();
            if (text != null || description != null) {
                matrices.translate(0.5f, 0.5f, 0.5f);
                matrices.multiply(new Quaternionf().setAngleAxis(upAngle, 0, 1, 0));
                matrices.translate(-0.1f, 0.625f, 0f);
                matrices.multiply(new Quaternionf().setAngleAxis(westAngle, -1, 0, 0));
                if (text != null) {
                    matrices.push();
                    matrices.translate(0, 0.15f, offsetZ);
                    matrices.scale(textScale, -textScale, textScale);
                    queue.submitText(
                        matrices,
                        textX,
                        textY,
                        text,
                        false,
                        TextRenderer.TextLayerType.NORMAL,
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        color,
                        0,
                        0
                    );
                    matrices.translate(0.5f, 0.5f, -0.0625f);
                    queue.submitText(
                        matrices,
                        textX,
                        textY,
                        text,
                        false,
                        TextRenderer.TextLayerType.NORMAL,
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        shadowColor,
                        0,
                        0
                    );
                    matrices.pop();
                }
                if (description != null) {
                    matrices.push();
                    matrices.translate(-.0635f, 0.06f, offsetZ);
                    matrices.scale(descriptionScale, -descriptionScale, descriptionScale);
                    queue.submitText(
                        matrices,
                        descriptionX,
                        descriptionY,
                        description,
                        false,
                        TextRenderer.TextLayerType.NORMAL,
                        LightmapTextureManager.MAX_LIGHT_COORDINATE,
                        color,
                        0,
                        0
                    );
                    matrices.pop();
                }
            }
        }

        @Override
        public void render(MatrixStack.Entry matricesEntry, VertexConsumer vertexConsumer) {
            button.light(light).useLevelLight(world, worldMatrix4f).renderInto(matricesEntry, vertexConsumer);
        }
    }
}
