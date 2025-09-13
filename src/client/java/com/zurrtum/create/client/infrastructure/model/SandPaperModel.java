package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.LayerRenderState;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;

import static com.zurrtum.create.Create.MOD_ID;

public class SandPaperModel implements ItemModel, SpecialModelRenderer<SandPaperModel.RenderData> {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/sand_paper");

    private final RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final List<BakedQuad> quads;
    private final ModelSettings settings;
    private final Supplier<Vector3f[]> vector;

    public SandPaperModel(List<BakedQuad> quads, ModelSettings settings) {
        this.quads = quads;
        this.settings = settings;
        this.vector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(this.quads));
    }

    @Override
    public void update(
        ItemRenderState state,
        ItemStack stack,
        ItemModelManager resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientWorld world,
        @Nullable LivingEntity user,
        int seed
    ) {
        state.addModelKey(this);
        state.markAnimated();
        ItemRenderState.LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderLayer(layer);
        layerRenderState.setVertices(vector);
        settings.addSettings(layerRenderState, displayContext);
        layerRenderState.getQuads().addAll(quads);

        RenderData data = new RenderData();
        data.state = layerRenderState;
        if (user == null) {
            user = MinecraftClient.getInstance().player;
        }
        if (user != null) {
            data.itemInUseCount = user.getItemUseTimeLeft();
        }

        SandPaperItemComponent component = stack.get(AllDataComponents.SAND_PAPER_POLISHING);
        if (component != null) {
            int maxUseTime = stack.getMaxUseTime(user);
            boolean jeiMode = stack.contains(AllDataComponents.SAND_PAPER_JEI);
            float partialTicks = AnimationTickHolder.getPartialTicks();
            float time = (float) (!jeiMode ? data.itemInUseCount : (-AnimationTickHolder.getTicks()) % maxUseTime) - partialTicks + 1.0F;
            data.reverseBobbing = time / (float) maxUseTime < 0.8F;
            if (data.reverseBobbing) {
                data.bobbing = -MathHelper.abs(MathHelper.cos(time / 4.0F * (float) Math.PI) * 0.1F);
            }

            ItemStack toPolish = component.item();
            data.item = new ItemRenderState();
            data.item.displayContext = displayContext;
            resolver.update(data.item, toPolish, ItemDisplayContext.GUI, world, user, seed);
        }

        layerRenderState.setSpecialModel(this, data);
    }

    @Override
    public void render(
        RenderData data,
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        VertexConsumerProvider vertexConsumers,
        int light,
        int overlay,
        boolean glint
    ) {
        assert data != null;
        LayerRenderState state = data.state;
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean firstPerson = leftHand || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;

        matrices.push();
        if (firstPerson && data.itemInUseCount > 0) {
            int modifier = leftHand ? -1 : 1;
            matrices.translate(0.5F, 0.5F, 0.5F);
            matrices.translate(modifier * .5f, 0, -.25f);
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(modifier * 40));
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(modifier * 10));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(modifier * 90));
            matrices.translate(-0.5F, -0.5F, -0.5F);
        }
        ItemRenderer.renderItem(
            ItemDisplayContext.NONE,
            matrices,
            vertexConsumers,
            light,
            overlay,
            state.tints,
            state.quads,
            state.renderLayer,
            state.glint
        );
        matrices.pop();

        if (data.item == null) {
            return;
        }

        matrices.push();
        matrices.translate(0.5F, 0.5F, 0.5F);
        if (displayContext == ItemDisplayContext.GUI) {
            matrices.translate(0.0F, .2f, 1.0F);
            matrices.scale(.75f, .75f, .75f);
        } else {
            int modifier = leftHand ? -1 : 1;
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(modifier * 40));
        }
        if (data.reverseBobbing) {
            if (displayContext == ItemDisplayContext.GUI) {
                matrices.translate(data.bobbing, data.bobbing, 0.0F);
            } else {
                matrices.translate(0.0F, data.bobbing, 0.0F);
            }
        }
        data.item.render(matrices, vertexConsumers, light, overlay);
        matrices.pop();
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RenderData getData(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class RenderData {
        LayerRenderState state;
        ItemRenderState item;
        int itemInUseCount;
        boolean reverseBobbing;
        float bobbing;
    }

    public record Unbaked(Identifier model) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Identifier.CODEC.fieldOf("model")
            .forGetter(Unbaked::model)).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public void resolve(Resolver resolver) {
            resolver.markDependency(model);
        }

        @Override
        public ItemModel bake(BakeContext context) {
            Baker baker = context.blockModelBaker();
            BakedSimpleModel model = baker.getModel(this.model);
            ModelTextures textures = model.getTextures();
            List<BakedQuad> quads = model.bakeGeometry(textures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, model, textures);
            return new SandPaperModel(quads, settings);
        }
    }
}
