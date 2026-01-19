package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.infrastructure.component.ClipboardContent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.TextureSlots;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Supplier;

import static com.zurrtum.create.Create.MOD_ID;

public class ClipboardModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/clipboard");
    public static final Identifier EMPTY_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/clipboard_0");
    public static final Identifier WRITTEN_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/clipboard_1");
    public static final Identifier EDITING_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/clipboard_2");
    private final RenderType layer = Sheets.translucentItemSheet();
    private final ModelData[] models;

    public ClipboardModel(ModelData[] models) {
        this.models = models;
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
        int index = stack.getOrDefault(AllDataComponents.CLIPBOARD_CONTENT, ClipboardContent.EMPTY).type().ordinal();
        state.appendModelIdentityElement(this);
        state.appendModelIdentityElement(index);
        models[index].update(state, layer, displayContext);

    }

    public static class Unbaked implements ItemModel.Unbaked {
        public static final MapCodec<com.zurrtum.create.client.infrastructure.model.ClipboardModel.Unbaked> CODEC = MapCodec.unit(com.zurrtum.create.client.infrastructure.model.ClipboardModel.Unbaked::new);

        @Override
        public MapCodec<com.zurrtum.create.client.infrastructure.model.ClipboardModel.Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(EMPTY_ID);
            resolver.markDependency(WRITTEN_ID);
            resolver.markDependency(EDITING_ID);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            ModelBaker baker = context.blockModelBaker();
            ModelData[] models = new ModelData[3];
            models[0] = ModelData.bake(baker, EMPTY_ID);
            models[1] = ModelData.bake(baker, WRITTEN_ID);
            models[2] = ModelData.bake(baker, EDITING_ID);
            return new ClipboardModel(models);
        }
    }

    public record ModelData(List<BakedQuad> quads, ModelRenderProperties settings, Supplier<Vector3fc[]> vector) {
        public static ModelData bake(ModelBaker baker, Identifier id) {
            ResolvedModel model = baker.getModel(id);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new ModelData(quads, settings, Suppliers.memoize(() -> BlockModelWrapper.computeExtents(quads)));
        }

        public void update(ItemStackRenderState state, RenderType layer, ItemDisplayContext displayContext) {
            ItemStackRenderState.LayerRenderState layerRenderState = state.newLayer();
            layerRenderState.setRenderType(layer);
            layerRenderState.setExtents(vector);
            settings.applyToLayer(layerRenderState, displayContext);
            layerRenderState.prepareQuadList().addAll(quads);
        }
    }
}
