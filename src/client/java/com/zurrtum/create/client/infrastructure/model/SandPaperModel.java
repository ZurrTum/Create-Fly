package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

import static com.zurrtum.create.Create.MOD_ID;

public class SandPaperModel implements ItemModel, SpecialModelRenderer<SandPaperModel.RenderData> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/sand_paper");

    private final RenderType layer = Sheets.translucentItemSheet();
    private final List<BakedQuad> quads;
    private final ModelRenderProperties settings;
    private final Supplier<Vector3fc[]> vector;

    public SandPaperModel(List<BakedQuad> quads, ModelRenderProperties settings) {
        this.quads = quads;
        this.settings = settings;
        this.vector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(this.quads));
    }

    @Override
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner ctx,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        ItemStackRenderState.LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderType(layer);
        layerRenderState.setExtents(vector);
        settings.applyToLayer(layerRenderState, displayContext);
        layerRenderState.prepareQuadList().addAll(quads);

        RenderData data = new RenderData();
        data.state = layerRenderState;
        Player entity;
        if (ctx instanceof Player player) {
            data.itemInUseCount = player.getUseItemRemainingTicks();
            entity = player;
        } else {
            LocalPlayer player = Minecraft.getInstance().player;
            data.itemInUseCount = player.getUseItemRemainingTicks();
            entity = player;
        }

        SandPaperItemComponent component = stack.get(AllDataComponents.SAND_PAPER_POLISHING);
        if (component != null) {
            int maxUseTime = stack.getUseDuration(entity);
            boolean jeiMode = stack.has(AllDataComponents.SAND_PAPER_JEI);
            float partialTicks = AnimationTickHolder.getPartialTicks();
            float time = (float) (!jeiMode ? data.itemInUseCount : (-AnimationTickHolder.getTicks()) % maxUseTime) - partialTicks + 1.0F;
            data.reverseBobbing = time / (float) maxUseTime < 0.8F;
            if (data.reverseBobbing) {
                data.bobbing = -Mth.abs(Mth.cos(time / 4.0F * (float) Math.PI) * 0.1F);
            }

            ItemStack toPolish = component.item();
            data.item = new ItemStackRenderState();
            data.item.displayContext = displayContext;
            resolver.appendItemLayers(data.item, toPolish, ItemDisplayContext.GUI, world, ctx, seed);
        }

        layerRenderState.setupSpecialModel(this, data);
    }

    @Override
    public void submit(
        @Nullable RenderData data,
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert data != null;
        LayerRenderState state = data.state;
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean firstPerson = leftHand || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        matrices.pushPose();
        if (firstPerson && data.itemInUseCount > 0) {
            int modifier = leftHand ? -1 : 1;
            matrices.translate(0.5F, 0.5F, 0.5F);
            matrices.translate(modifier * .5f, 0, -.25f);
            matrices.mulPose(Axis.ZP.rotationDegrees(modifier * 40));
            matrices.mulPose(Axis.XP.rotationDegrees(modifier * 10));
            matrices.mulPose(Axis.YP.rotationDegrees(modifier * 90));
            matrices.translate(-0.5F, -0.5F, -0.5F);
        }
        queue.submitItem(matrices, displayContext, light, overlay, 0, state.tintLayers, state.prepareQuadList(), state.renderType, state.foilType);
        matrices.popPose();

        if (data.item == null) {
            return;
        }

        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.5F);
        if (displayContext == ItemDisplayContext.GUI) {
            matrices.translate(0.0F, .2f, 1.0F);
            matrices.scale(.75f, .75f, .75f);
        } else {
            int modifier = leftHand ? -1 : 1;
            matrices.mulPose(Axis.YP.rotationDegrees(modifier * 40));
        }
        if (data.reverseBobbing) {
            if (displayContext == ItemDisplayContext.GUI) {
                matrices.translate(data.bobbing, data.bobbing, 0.0F);
            } else {
                matrices.translate(0.0F, data.bobbing, 0.0F);
            }
        }
        data.item.submit(matrices, queue, light, overlay, 0);
        matrices.popPose();
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderData extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class RenderData {
        LayerRenderState state;
        @Nullable ItemStackRenderState item;
        int itemInUseCount;
        boolean reverseBobbing;
        float bobbing;
    }

    public record Unbaked(Identifier model) implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.SandPaperModel.Unbaked> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(Identifier.CODEC.fieldOf("model")
                    .forGetter(com.zurrtum.create.client.infrastructure.model.SandPaperModel.Unbaked::model))
                .apply(instance, com.zurrtum.create.client.infrastructure.model.SandPaperModel.Unbaked::new));

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.SandPaperModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(this.model);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new SandPaperModel(quads, settings);
        }
    }
}
