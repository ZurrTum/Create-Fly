package com.zurrtum.create.client.vanillin.visuals;

import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.transform.TransformStack;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractEntityVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.flywheel.lib.visual.util.InstanceRecycler;
import com.zurrtum.create.client.vanillin.item.ItemModels;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.ItemModelManager;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.render.item.model.ItemModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.LightType;

public class ItemVisual extends AbstractEntityVisual<ItemEntity> implements SimpleDynamicVisual {

    private static final ThreadLocal<Random> RANDOM = ThreadLocal.withInitial(Random::createLocal);

    private final MatrixStack pPoseStack = new MatrixStack();
    private final ItemRenderState itemRenderState = new ItemRenderState();
    private ItemModel itemModel;
    private Model currentModel;
    private ItemStack currentStack;

    private final InstanceRecycler<TransformedInstance> instances;

    public ItemVisual(VisualizationContext ctx, ItemEntity entity, float partialTick) {
        super(ctx, entity, partialTick);

        updateModel(entity.getStack());

        instances = new InstanceRecycler<>(this::getInstance);

        animate(partialTick);
    }

    public static boolean isSupported(ItemEntity entity) {
        if (entity.getClass() != ItemEntity.class) {
            return false;
        }
        return ItemModels.isSupported(entity.getStack(), ItemDisplayContext.GROUND);
    }

    @Override
    public void beginFrame(Context ctx) {
        if (!isVisible(ctx.frustum())) {
            return;
        }

        ItemStack stack = entity.getStack();
        if (!ItemStack.areItemsAndComponentsEqual(currentStack, stack)) {
            updateModel(stack);
            instances.delete();
        }
        animate(ctx.partialTick());
    }

    private void updateModel(ItemStack stack) {
        currentStack = stack.copy();
        itemModel = ItemModels.getModel(currentStack);
        currentModel = ItemModels.get(level, currentStack, ItemDisplayContext.GROUND);
    }

    private TransformedInstance getInstance() {
        return visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, currentModel).createInstance();
    }

    private void animate(float partialTick) {
        pPoseStack.loadIdentity();
        TransformStack.of(pPoseStack).translate(getVisualPosition(partialTick));

        instances.resetCount();

        ItemStack itemstack = entity.getStack();
        if (itemstack.isEmpty()) {
            return;
        }

        itemRenderState.clear();
        ItemModelManager manager = MinecraftClient.getInstance().getItemModelManager();
        ClientWorld world = entity.getWorld() instanceof ClientWorld clientWorld ? clientWorld : null;
        itemRenderState.displayContext = ItemDisplayContext.GROUND;
        itemModel.update(itemRenderState, itemstack, manager, ItemDisplayContext.GROUND, world, null, entity.getId());

        if (itemRenderState.isEmpty()) {
            return;
        }

        float age = entity.getItemAge() + partialTick;
        Box box = itemRenderState.getModelBoundingBox();
        float f = -((float) box.minY) + 0.0625F;
        if (shouldBob()) {
            float g = MathHelper.sin(age / 10.0F + entity.uniqueOffset) * 0.1F + 0.1F;
            pPoseStack.translate(0.0F, g + f, 0.0F);
        } else {
            pPoseStack.translate(0.0F, f, 0.0F);
        }
        float h = ItemEntity.getRotation(age, entity.uniqueOffset);
        pPoseStack.multiply(RotationAxis.POSITIVE_Y.rotation(h));

        int i = this.getRenderAmount(itemstack);
        int seed = itemstack.isEmpty() ? 187 : Item.getRawId(itemstack.getItem()) + itemstack.getDamage();
        var random = RANDOM.get();
        random.setSeed(seed);

        int light = LightmapTextureManager.pack(
            level.getLightLevel(LightType.BLOCK, entity.getBlockPos()),
            level.getLightLevel(LightType.SKY, entity.getBlockPos())
        );

        float lengthZ = (float) box.getLengthZ();
        if (lengthZ > 0.0625F) {
            instances.get().setTransform(pPoseStack.peek()).light(light).setChanged();

            if (shouldSpreadItems()) {
                for (int j = 1; j < i; j++) {
                    pPoseStack.push();
                    float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float z = (random.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    pPoseStack.translate(x, y, z);
                    instances.get().setTransform(pPoseStack.peek()).light(light).setChanged();
                    pPoseStack.pop();
                }
            }
        } else {
            float l = lengthZ * 1.5F;
            pPoseStack.translate(0.0F, 0.0F, -(l * (i - 1) / 2.0F));
            instances.get().setTransform(pPoseStack.peek()).light(light).setChanged();
            pPoseStack.translate(0.0F, 0.0F, l);

            if (shouldSpreadItems()) {
                for (int m = 1; m < i; m++) {
                    pPoseStack.push();
                    float x = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    float y = (random.nextFloat() * 2.0F - 1.0F) * 0.15F * 0.5F;
                    pPoseStack.translate(x, y, 0.0F);
                    instances.get().setTransform(pPoseStack.peek()).light(light).setChanged();
                    pPoseStack.pop();
                    pPoseStack.translate(0.0F, 0.0F, l);
                }
            }
        }

        instances.discardExtra();
    }

    protected int getRenderAmount(ItemStack pStack) {
        int i = 1;
        if (pStack.getCount() > 48) {
            i = 5;
        } else if (pStack.getCount() > 32) {
            i = 4;
        } else if (pStack.getCount() > 16) {
            i = 3;
        } else if (pStack.getCount() > 1) {
            i = 2;
        }

        return i;
    }

    /**
     * @return If items should spread out when rendered in 3D
     */
    public boolean shouldSpreadItems() {
        return true;
    }

    /**
     * @return If items should have a bob effect
     */
    public boolean shouldBob() {
        return true;
    }

    @Override
    protected void _delete() {
        instances.delete();
    }

}
