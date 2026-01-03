package com.zurrtum.create.client.compat.accessories;

import com.zurrtum.create.AllItems;
import io.wispforest.accessories.api.AccessoriesStorage;
import io.wispforest.accessories.api.AccessoriesStorageLookup;
import io.wispforest.accessories.api.client.AccessoriesRenderStateKeys;
import io.wispforest.accessories.api.client.AccessoriesRendererRegistry;
import io.wispforest.accessories.api.client.AccessoryRenderState;
import io.wispforest.accessories.api.client.renderers.AccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotPath;
import io.wispforest.accessories.pond.AccessoriesRenderStateAPI;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.entity.state.PlayerEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.RotationAxis;

import java.util.Map;

import static com.zurrtum.create.Create.MOD_ID;

public class GoggleAccessoryRenderer implements AccessoryRenderer {
    public static final Identifier ID = Identifier.of(MOD_ID, "goggles");

    public static void register() {
        AccessoriesRendererRegistry.bindItemToRenderer(AllItems.GOGGLES, ID, GoggleAccessoryRenderer::new);
    }

    @Override
    public <S extends LivingEntityRenderState> void render(
        AccessoryRenderState accessoryState,
        S entityState,
        EntityModel<S> model,
        MatrixStack matrices,
        OrderedRenderCommandQueue queue
    ) {
        if (model instanceof PlayerEntityModel entityModel) {
            ItemRenderState item = accessoryState.getStateData(AccessoriesRenderStateKeys.ITEM_STACK_STATE);
            if (item == null) {
                return;
            }
            matrices.push();
            entityModel.getRootPart().applyTransform(matrices);
            entityModel.getHead().applyTransform(matrices);
            matrices.translate(0.0F, -0.25F, 0.0F);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180.0F));
            matrices.scale(0.625F, -0.625F, -0.625F);
            AccessoriesRenderStateAPI storge = (AccessoriesRenderStateAPI) entityState;
            if (headOccupied((PlayerEntityRenderState) entityState, storge.getStorageLookup())) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(180.0F));
                matrices.translate(0.0F, -0.25F, 0.0F);
            }
            int light = storge.getStateData(AccessoriesRenderStateKeys.LIGHT);
            item.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);
            matrices.pop();
        }
    }

    @Override
    public void extractRenderState(
        ItemStack stack,
        SlotPath path,
        AccessoriesStorageLookup storageLookup,
        LivingEntity entity,
        LivingEntityRenderState entityState,
        AccessoryRenderState state
    ) {
        ItemRenderState stackRenderState = new ItemRenderState();
        stackRenderState.displayContext = ItemDisplayContext.HEAD;
        MinecraftClient.getInstance().getItemModelManager()
            .update(stackRenderState, stack, stackRenderState.displayContext, entity.getEntityWorld(), entity, 0);
        state.setStateData(AccessoriesRenderStateKeys.ITEM_STACK_STATE, stackRenderState);
    }

    public static boolean headOccupied(PlayerEntityRenderState state, AccessoriesStorageLookup storageLookup) {
        if (!state.equippedHeadStack.isEmpty()) {
            return true;
        }
        Map<String, ? extends AccessoriesStorage> containers = storageLookup.getContainers();
        AccessoriesStorage head = containers.get("accessories_cosmetics:head");
        if (shouldRender(head, head.getCosmeticAccessories())) {
            return true;
        }
        AccessoriesStorage hat = containers.get("hat");
        return shouldRender(hat, hat.getCosmeticAccessories()) || shouldRender(hat, hat.getAccessories());
    }

    private static boolean shouldRender(AccessoriesStorage storage, Inventory inventory) {
        for (int i = 0, size = inventory.size(); i < size; i++) {
            ItemStack stack = inventory.getStack(i);
            if (stack.isEmpty()) {
                continue;
            }
            if (storage.shouldRender(i)) {
                return true;
            }
        }
        return false;
    }
}
