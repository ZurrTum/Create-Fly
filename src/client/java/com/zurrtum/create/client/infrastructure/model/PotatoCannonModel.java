package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem.Ammo;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.TexturedRenderLayers;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.ItemRenderState.LayerRenderState;
import net.minecraft.client.render.item.model.BasicItemModel;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.render.item.model.special.SpecialModelRenderer;
import net.minecraft.client.render.model.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.HeldItemContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3f;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class PotatoCannonModel implements ItemModel, SpecialModelRenderer<PotatoCannonModel.CogRenderData> {
    public static final Identifier ID = Identifier.of(MOD_ID, "model/potato_cannon");
    public static final Identifier ITEM_ID = Identifier.of(MOD_ID, "item/potato_cannon/item");
    public static final Identifier COG_ID = Identifier.of(MOD_ID, "item/potato_cannon/cog");

    private final RenderLayer layer = TexturedRenderLayers.getItemEntityTranslucentCull();
    private final List<BakedQuad> itemQuads;
    private final ModelSettings itemSettings;
    private final Supplier<Vector3f[]> itemVector;
    private final List<BakedQuad> cogQuads;
    private final ModelSettings cogSettings;
    private final Supplier<Vector3f[]> cogVector;

    public PotatoCannonModel(Pair<List<BakedQuad>, ModelSettings> item, Pair<List<BakedQuad>, ModelSettings> cog) {
        itemQuads = item.getLeft();
        itemSettings = item.getRight();
        itemVector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(itemQuads));
        cogQuads = cog.getLeft();
        cogSettings = cog.getRight();
        cogVector = Suppliers.memoize(() -> BasicItemModel.bakeQuads(cogQuads));
    }

    @Override
    public void update(
        ItemRenderState state,
        ItemStack stack,
        ItemModelManager resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientWorld world,
        @Nullable HeldItemContext user,
        int seed
    ) {
        state.addModelKey(this);
        state.markAnimated();
        ItemRenderState.Glint glint;
        if (stack.hasGlint()) {
            state.addModelKey(ItemRenderState.Glint.STANDARD);
            glint = ItemRenderState.Glint.STANDARD;
        } else {
            glint = ItemRenderState.Glint.NONE;
        }
        update(state, displayContext, itemQuads, itemSettings, itemVector, glint);

        CogRenderData cog = new CogRenderData();
        cog.state = update(state, displayContext, cogQuads, cogSettings, cogVector, glint);
        cog.state.setTransform(itemSettings.transforms().getTransformation(displayContext));
        cog.rotation = AnimationTickHolder.getRenderTime() * -2.5f;
        boolean inMainHand = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (inMainHand || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {
                boolean leftHanded = player.getMainArm() == Arm.LEFT;
                float speed = Create.POTATO_CANNON_RENDER_HANDLER.getAnimation(inMainHand ^ leftHanded, AnimationTickHolder.getPartialTicks());
                cog.rotation += 360 * MathHelper.clamp(speed * 5, 0, 1);
            }
        }
        cog.rotation %= 360;
        cog.state.setSpecialModel(this, cog);
    }

    private LayerRenderState update(
        ItemRenderState state,
        ItemDisplayContext displayContext,
        List<BakedQuad> quads,
        ModelSettings settings,
        Supplier<Vector3f[]> vector,
        ItemRenderState.Glint glint
    ) {
        LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderLayer(layer);
        layerRenderState.setVertices(vector);
        settings.addSettings(layerRenderState, displayContext);
        layerRenderState.getQuads().addAll(quads);
        layerRenderState.setGlint(glint);
        return layerRenderState;
    }

    @Override
    public void render(
        CogRenderData data,
        ItemDisplayContext displayContext,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert data != null;
        matrices.translate(0.5f, 0.53125f, 0.5f);
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(data.rotation));
        matrices.translate(-0.5f, -0.53125f, -0.5f);
        LayerRenderState state = data.state;
        queue.submitItem(matrices, displayContext, light, overlay, 0, state.tints, state.getQuads(), state.renderLayer, state.glint);
    }

    public static void renderDecorator(MinecraftClient client, DrawContext drawContext, ItemStack stack, int x, int y) {
        if (client.player == null) {
            return;
        }
        Ammo ammo = PotatoCannonItem.getAmmo(client.player, stack);
        if (ammo == null) {
            return;
        }
        Matrix3x2fStack matrices = drawContext.getMatrices();
        matrices.translate(x, y + 8);
        matrices.scale(0.5f);
        drawContext.drawItem(ammo.stack(), 0, 0);
    }

    private void fill(
        VertexConsumer vertexConsumer,
        MatrixStack.Entry entry,
        int x1,
        int x2,
        int y1,
        int y2,
        float depth,
        int red,
        int green,
        int blue,
        int alpha
    ) {
        vertexConsumer.vertex(entry, x1, y1, depth).color(red, green, blue, alpha);
        vertexConsumer.vertex(entry, x1, y2, depth).color(red, green, blue, alpha);
        vertexConsumer.vertex(entry, x2, y2, depth).color(red, green, blue, alpha);
        vertexConsumer.vertex(entry, x2, y1, depth).color(red, green, blue, alpha);
    }

    public static class CogRenderData {
        LayerRenderState state;
        float rotation;
    }

    @Override
    public void collectVertices(Set<Vector3f> vertices) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CogRenderData getData(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> getCodec() {
            return CODEC;
        }

        @Override
        public void resolve(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(COG_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakeContext context) {
            Baker baker = context.blockModelBaker();
            return new PotatoCannonModel(bake(baker, ITEM_ID), bake(baker, COG_ID));
        }

        private static Pair<List<BakedQuad>, ModelSettings> bake(Baker baker, Identifier id) {
            BakedSimpleModel model = baker.getModel(id);
            ModelTextures textures = model.getTextures();
            List<BakedQuad> quads = model.bakeGeometry(textures, baker, ModelRotation.X0_Y0).getAllQuads();
            ModelSettings settings = ModelSettings.resolveSettings(baker, model, textures);
            return new Pair<>(quads, settings);
        }
    }
}
