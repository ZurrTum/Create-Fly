package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem.Ammo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
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
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.joml.Vector3fc;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class PotatoCannonModel implements ItemModel, SpecialModelRenderer<PotatoCannonModel.CogRenderData> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/potato_cannon");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/potato_cannon/item");
    public static final Identifier COG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/potato_cannon/cog");

    private final RenderType layer = Sheets.translucentItemSheet();
    private final List<BakedQuad> itemQuads;
    private final ModelRenderProperties itemSettings;
    private final Supplier<Vector3fc[]> itemVector;
    private final List<BakedQuad> cogQuads;
    private final ModelRenderProperties cogSettings;
    private final Supplier<Vector3fc[]> cogVector;

    public PotatoCannonModel(Tuple<List<BakedQuad>, ModelRenderProperties> item, Tuple<List<BakedQuad>, ModelRenderProperties> cog) {
        itemQuads = item.getA();
        itemSettings = item.getB();
        itemVector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(itemQuads));
        cogQuads = cog.getA();
        cogSettings = cog.getB();
        cogVector = Suppliers.memoize(() -> BlockModelWrapper.computeExtents(cogQuads));
    }

    @Override
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner user,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        ItemStackRenderState.FoilType glint;
        if (stack.hasFoil()) {
            state.appendModelIdentityElement(ItemStackRenderState.FoilType.STANDARD);
            glint = ItemStackRenderState.FoilType.STANDARD;
        } else {
            glint = ItemStackRenderState.FoilType.NONE;
        }
        update(state, displayContext, itemQuads, itemSettings, itemVector, glint);

        CogRenderData cog = new CogRenderData();
        cog.state = update(state, displayContext, cogQuads, cogSettings, cogVector, glint);
        cog.state.setTransform(itemSettings.transforms().getTransform(displayContext));
        cog.rotation = AnimationTickHolder.getRenderTime() * -2.5f;
        boolean inMainHand = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (inMainHand || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;
                float speed = Create.POTATO_CANNON_RENDER_HANDLER.getAnimation(inMainHand ^ leftHanded, AnimationTickHolder.getPartialTicks());
                cog.rotation += 360 * Mth.clamp(speed * 5, 0, 1);
            }
        }
        cog.rotation %= 360;
        cog.state.setupSpecialModel(this, cog);
    }

    private LayerRenderState update(
        ItemStackRenderState state,
        ItemDisplayContext displayContext,
        List<BakedQuad> quads,
        ModelRenderProperties settings,
        Supplier<Vector3fc[]> vector,
        ItemStackRenderState.FoilType glint
    ) {
        LayerRenderState layerRenderState = state.newLayer();
        layerRenderState.setRenderType(layer);
        layerRenderState.setExtents(vector);
        settings.applyToLayer(layerRenderState, displayContext);
        layerRenderState.prepareQuadList().addAll(quads);
        layerRenderState.setFoilType(glint);
        return layerRenderState;
    }

    @Override
    public void submit(
        CogRenderData data,
        ItemDisplayContext displayContext,
        PoseStack matrices,
        SubmitNodeCollector queue,
        int light,
        int overlay,
        boolean glint,
        int i
    ) {
        assert data != null;
        matrices.translate(0.5f, 0.53125f, 0.5f);
        matrices.mulPose(Axis.ZP.rotationDegrees(data.rotation));
        matrices.translate(-0.5f, -0.53125f, -0.5f);
        LayerRenderState state = data.state;
        queue.submitItem(matrices, displayContext, light, overlay, 0, state.tintLayers, state.quads, state.renderType, state.foilType);
    }

    public static void renderDecorator(Minecraft client, GuiGraphics drawContext, ItemStack stack, int x, int y) {
        if (client.player == null) {
            return;
        }
        Ammo ammo = PotatoCannonItem.getAmmo(client.player, stack);
        if (ammo == null) {
            return;
        }
        Matrix3x2fStack matrices = drawContext.pose();
        matrices.translate(x, y + 8);
        matrices.scale(0.5f);
        drawContext.renderItem(ammo.stack(), 0, 0);
    }

    private void fill(
        VertexConsumer vertexConsumer,
        PoseStack.Pose entry,
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
        vertexConsumer.addVertex(entry, x1, y1, depth).setColor(red, green, blue, alpha);
        vertexConsumer.addVertex(entry, x1, y2, depth).setColor(red, green, blue, alpha);
        vertexConsumer.addVertex(entry, x2, y2, depth).setColor(red, green, blue, alpha);
        vertexConsumer.addVertex(entry, x2, y1, depth).setColor(red, green, blue, alpha);
    }

    public static class CogRenderData {
        LayerRenderState state;
        float rotation;
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        throw new UnsupportedOperationException();
    }

    @Override
    public CogRenderData extractArgument(ItemStack stack) {
        throw new UnsupportedOperationException();
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.PotatoCannonModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.PotatoCannonModel.Unbaked::new);

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.PotatoCannonModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(COG_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            return new PotatoCannonModel(bake(baker, ITEM_ID), bake(baker, COG_ID));
        }

        private static Tuple<List<BakedQuad>, ModelRenderProperties> bake(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new Tuple<>(quads, settings);
        }
    }
}
