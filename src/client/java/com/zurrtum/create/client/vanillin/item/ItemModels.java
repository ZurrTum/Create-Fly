package com.zurrtum.create.client.vanillin.item;

import com.zurrtum.create.client.flywheel.api.material.Transparency;
import com.zurrtum.create.client.flywheel.api.model.Mesh;
import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.lib.material.Materials;
import com.zurrtum.create.client.flywheel.lib.material.SimpleMaterial;
import com.zurrtum.create.client.flywheel.lib.model.ModelUtil;
import com.zurrtum.create.client.flywheel.lib.model.SimpleModel;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedItemModelBufferer;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemChunkLayerSortedListBuilder;
import com.zurrtum.create.client.flywheel.lib.model.baked.MeshHelper;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import com.zurrtum.create.client.vanillin.Vanillin;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2BooleanLinkedOpenCustomHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.world.World;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ItemModels {
    public static final TagKey<Item> NO_INSTANCING = TagKey.of(RegistryKeys.ITEM, Vanillin.rl("no_instancing"));
    private static final Model EMPTY_MODEL = new SimpleModel(List.of());
    private static final RendererReloadCache<BakedModelKey, Model> MODEL_CACHE = new RendererReloadCache<>(key -> bakeModel(
        key.world(),
        key.stack(),
        key.displayContext()
    ));
    private static final Map<ItemStack, Boolean> SUPPORT_CACHE = new Object2BooleanLinkedOpenCustomHashMap<>(new Hash.Strategy<>() {
        public int hashCode(ItemStack itemStack) {
            return ItemStack.hashCode(itemStack);
        }

        public boolean equals(ItemStack itemStack, ItemStack itemStack2) {
            return itemStack == itemStack2 || itemStack != null && itemStack2 != null && ItemStack.areItemsAndComponentsEqual(itemStack, itemStack2);
        }
    });
    private static final ThreadLocal<ItemRenderState> STATE = ThreadLocal.withInitial(ItemRenderState::new);

    public static boolean isSupported(ItemStack stack, ItemDisplayContext context) {
        if (stack.isIn(NO_INSTANCING)) {
            return false;
        }
        Boolean cache = SUPPORT_CACHE.get(stack);
        if (cache != null) {
            return cache;
        }
        MinecraftClient mc = MinecraftClient.getInstance();
        ItemRenderState state = STATE.get();
        mc.getItemModelManager().clearAndUpdate(state, stack, context, mc.world, null, 0);
        boolean support = !state.isAnimated();
        SUPPORT_CACHE.put(stack.copy(), support);
        return support;
    }

    public static ItemModel getModel(ItemStack stack) {
        return MinecraftClient.getInstance().getBakedModelManager().getItemModel(stack.get(DataComponentTypes.ITEM_MODEL));
    }

    public static Model get(World world, ItemStack itemStack, ItemDisplayContext displayContext) {
        if (itemStack.isEmpty()) {
            return EMPTY_MODEL;
        }
        ClientWorld clientWorld = world instanceof ClientWorld ? (ClientWorld) world : null;
        return MODEL_CACHE.get(new BakedModelKey(clientWorld, itemStack, displayContext));
    }

    public static Model bakeModel(ClientWorld world, ItemStack itemStack, ItemDisplayContext displayContext) {
        var builder = ItemChunkLayerSortedListBuilder.<Model.ConfiguredMesh>getThreadLocal();
        BakedItemModelBufferer.bufferItemStack(
            itemStack, world, displayContext, (renderType, shaded, data) -> {
                var material = ModelUtil.getItemMaterial(renderType);
                if (material == null) {
                    material = Materials.TRANSLUCENT_ENTITY;
                }
                if (itemStack.getItem() instanceof BlockItem && material.transparency() == Transparency.TRANSLUCENT) {
                    material = SimpleMaterial.builderOf(material).transparency(Transparency.ORDER_INDEPENDENT).build();
                }
                Mesh mesh = MeshHelper.blockVerticesToMesh(data, "source=ItemModels,ItemStack=" + itemStack + ",renderType=" + renderType);
                builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
            }, (renderType, material, mesh) -> {
                if (itemStack.getItem() instanceof BlockItem && material.transparency() == Transparency.TRANSLUCENT) {
                    material = SimpleMaterial.builderOf(material).transparency(Transparency.ORDER_INDEPENDENT).build();
                }
                builder.add(renderType, new Model.ConfiguredMesh(material, mesh));
            }
        );
        return new SimpleModel(builder.build());
    }

    public record BakedModelKey(ClientWorld world, ItemStack stack, ItemDisplayContext displayContext) {
        @Override
        public int hashCode() {
            return Objects.hash(world, ItemStack.hashCode(stack), displayContext);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof BakedModelKey(ClientWorld otherWorld, ItemStack otherStack, ItemDisplayContext otherDisplayContext))) {
                return false;
            }
            boolean stackEqual = stack == otherStack || ItemStack.areItemsAndComponentsEqual(stack, otherStack);
            return world == otherWorld && stackEqual && displayContext == otherDisplayContext;
        }
    }
}
