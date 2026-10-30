package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.Create;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem;
import com.zurrtum.create.content.equipment.potatoCannon.PotatoCannonItem.Ammo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.FoilType;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

public class PotatoCannonModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/potato_cannon");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/potato_cannon/item");
    public static final Identifier COG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/potato_cannon/cog");

    private final ItemQuads itemQuads;
    private final ModelRenderProperties itemSettings;
    private final Supplier<Vector3fc[]> itemExtents;
    private final ItemQuads cogQuads;
    private final Supplier<Vector3fc[]> cogExtents;

    public PotatoCannonModel(List<BakedQuad> itemQuads, ModelRenderProperties itemSettings, List<BakedQuad> cogQuads) {
        this.itemQuads = ItemQuads.split(itemQuads);
        this.itemSettings = itemSettings;
        itemExtents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(itemQuads));
        this.cogQuads = ItemQuads.split(cogQuads);
        cogExtents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(cogQuads));
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
        LayerRenderState itemLayer = submitQuads(state, itemSettings, displayContext, itemQuads);
        itemLayer.setExtents(itemExtents);
        LayerRenderState cogLayer = submitQuads(state, itemSettings, displayContext, cogQuads);
        cogLayer.setExtents(cogExtents);
        float angle = AnimationTickHolder.getRenderTime() * -2.5f;
        boolean inMainHand = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
        if (inMainHand || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND || displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND) {
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null) {
                boolean leftHanded = player.getMainArm() == HumanoidArm.LEFT;
                float speed = Create.POTATO_CANNON_RENDER_HANDLER.getAnimation(
                    inMainHand ^ leftHanded,
                    AnimationTickHolder.getPartialTicks()
                );
                angle += 360 * Mth.clamp(speed * 5, 0, 1);
            }
        }
        Matrix4f pose = cogLayer.localTransform;
        pose.translate(0.5f, 0.53125f, 0.5f);
        pose.rotateZ(Mth.DEG_TO_RAD * (angle % 360));
        pose.translate(-0.5f, -0.53125f, -0.5f);
        if (stack.hasFoil()) {
            state.appendModelIdentityElement(FoilType.STANDARD);
            itemLayer.setFoilType(FoilType.STANDARD);
            cogLayer.setFoilType(FoilType.STANDARD);
        }
    }

    public static void renderDecorator(
        Minecraft client,
        GuiGraphicsExtractor drawContext,
        ItemStack stack,
        int x,
        int y
    ) {
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
        drawContext.item(ammo.stack(), 0, 0);
    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = MapCodec.unit(Unbaked::new);

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(ITEM_ID);
            resolver.markDependency(COG_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel item = baker.getModel(ITEM_ID);
            TextureSlots itemTextures = item.getTopTextureSlots();
            List<BakedQuad> itemQuads = item.bakeTopGeometry(itemTextures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties itemSettings = ModelRenderProperties.fromResolvedModel(baker, item, itemTextures);
            return new PotatoCannonModel(itemQuads, itemSettings, BakedModelHelper.bakeQuads(baker, COG_ID));
        }
    }
}
