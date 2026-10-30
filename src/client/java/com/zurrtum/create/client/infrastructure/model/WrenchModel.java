package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.foundation.blockEntity.behaviour.scrollValue.ScrollValueHandler;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

public class WrenchModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/wrench");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wrench/item");
    public static final Identifier GEAR_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/wrench/gear");

    private final ItemQuads itemQuads;
    private final ModelRenderProperties itemSettings;
    private final Supplier<Vector3fc[]> itemExtents;
    private final ItemQuads gearQuads;
    private final ModelRenderProperties gearSettings;
    private final Supplier<Vector3fc[]> gearExtents;

    public WrenchModel(
        List<BakedQuad> itemQuads,
        ModelRenderProperties itemSettings,
        List<BakedQuad> gearQuads,
        ModelRenderProperties gearSettings
    ) {
        this.itemQuads = ItemQuads.split(itemQuads);
        this.itemSettings = itemSettings;
        itemExtents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(itemQuads));
        this.gearQuads = ItemQuads.split(gearQuads);
        this.gearSettings = gearSettings;
        gearExtents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(gearQuads));
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
        submitQuads(state, itemSettings, displayContext, itemQuads).setExtents(itemExtents);
        LayerRenderState gearLayer = submitQuads(state, gearSettings, displayContext, gearQuads);
        gearLayer.setExtents(gearExtents);
        float scroll = ScrollValueHandler.getScroll(AnimationTickHolder.getPartialTicks());
        Matrix4f pose = gearLayer.localTransform;
        pose.translate(0.5625f, 0.5f, 0.5f);
        pose.rotateY(Mth.DEG_TO_RAD * scroll);
        pose.translate(-0.5625f, -0.5f, -0.5f);
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
            resolver.markDependency(GEAR_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel item = baker.getModel(ITEM_ID);
            TextureSlots itemTextures = item.getTopTextureSlots();
            List<BakedQuad> itemQuads = item.bakeTopGeometry(itemTextures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties itemSettings = ModelRenderProperties.fromResolvedModel(baker, item, itemTextures);
            ResolvedModel gear = baker.getModel(GEAR_ID);
            TextureSlots gearTextures = gear.getTopTextureSlots();
            List<BakedQuad> gearQuads = gear.bakeTopGeometry(gearTextures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties gearSettings = ModelRenderProperties.fromResolvedModel(baker, gear, gearTextures);
            return new WrenchModel(itemQuads, itemSettings, gearQuads, gearSettings);
        }
    }
}
