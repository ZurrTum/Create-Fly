package com.zurrtum.create.client.vanillin.visuals;

import com.zurrtum.create.client.flywheel.api.model.Model;
import com.zurrtum.create.client.flywheel.api.visual.EntityVisual;
import com.zurrtum.create.client.flywheel.api.visualization.VisualizationContext;
import com.zurrtum.create.client.flywheel.lib.instance.InstanceTypes;
import com.zurrtum.create.client.flywheel.lib.instance.TransformedInstance;
import com.zurrtum.create.client.flywheel.lib.model.baked.BakedModelBuilder;
import com.zurrtum.create.client.flywheel.lib.util.RendererReloadCache;
import com.zurrtum.create.client.flywheel.lib.visual.AbstractVisual;
import com.zurrtum.create.client.flywheel.lib.visual.SimpleDynamicVisual;
import com.zurrtum.create.client.vanillin.item.ItemModels;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.model.BlockStateManagers;
import net.minecraft.client.render.model.BlockStateModel;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;
import org.joml.Matrix4f;

public class ItemFrameVisual extends AbstractVisual implements EntityVisual<ItemFrameEntity>, SimpleDynamicVisual {
    public static final RendererReloadCache<BlockStateModel, Model> MODEL_RESOURCE_LOCATION = new RendererReloadCache<>(model -> new BakedModelBuilder(
        model).build());

    private final Matrix4f baseTransform = new Matrix4f();

    private final TransformedInstance frame;
    private final TransformedInstance item;
    private final ItemFrameEntity entity;
    private BlockStateModel lastFrameModel;
    private ItemStack lastItemStack;

    public ItemFrameVisual(VisualizationContext ctx, ItemFrameEntity entity, float partialTick) {
        super(ctx, entity.getWorld(), partialTick);

        this.entity = entity;

        lastItemStack = entity.getHeldItemStack().copy();

        lastFrameModel = getFrameModel();
        var frameModel = MODEL_RESOURCE_LOCATION.get(lastFrameModel);

        frame = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, frameModel).createInstance();

        frame.setTransform(baseTransform);

        item = ctx.instancerProvider().instancer(InstanceTypes.TRANSFORMED, ItemModels.get(level, lastItemStack, ItemDisplayContext.FIXED))
            .createInstance();

        animate(partialTick);
    }

    public static boolean shouldVisualize(ItemFrameEntity entity) {
        // We don't support map rendering, and we can't support exotic item models.
        return !entity.getHeldItemStack().isOf(Items.FILLED_MAP) && ItemModels.isSupported(entity.getHeldItemStack(), ItemDisplayContext.FIXED);
    }

    @Override
    public void beginFrame(Context ctx) {
        animate(ctx.partialTick());
    }

    public void animate(float partialTick) {
        var light = LightmapTextureManager.pack(getBlockLightLevel(entity.getBlockPos()), getSkyLightLevel(entity.getBlockPos()));

        boolean invisible = entity.isInvisible();

        Direction direction = entity.getHorizontalFacing();
        var origin = visualizationContext.renderOrigin();

        float d = 0.46875f;

        float x = (float) (entity.getX() - origin.getX() + direction.getOffsetX() * d);
        float y = (float) (entity.getY() - origin.getY() + direction.getOffsetY() * d);
        float z = (float) (entity.getZ() - origin.getZ() + direction.getOffsetZ() * d);

        baseTransform.translation(x, y, z);
        baseTransform.rotateXYZ(MathHelper.RADIANS_PER_DEGREE * entity.getPitch(), MathHelper.RADIANS_PER_DEGREE * (180.0f - entity.getYaw()), 0.0f);

        var stack = entity.getHeldItemStack();
        var frameLocation = getFrameModel();

        if (frameLocation != lastFrameModel) {
            visualizationContext.instancerProvider().instancer(InstanceTypes.TRANSFORMED, MODEL_RESOURCE_LOCATION.get(frameLocation))
                .stealInstance(frame);
            lastFrameModel = frameLocation;
        }

        frame.setVisible(!invisible);

        frame.setTransform(baseTransform).translate(-0.5f, -0.5f, -0.5f).light(light).setChanged();

        if (!ItemStack.areEqual(lastItemStack, stack)) {
            lastItemStack = stack.copy();
            visualizationContext.instancerProvider()
                .instancer(InstanceTypes.TRANSFORMED, ItemModels.get(level, lastItemStack, ItemDisplayContext.FIXED)).stealInstance(item);
        }

        item.setTransform(baseTransform);

        if (invisible) {
            item.translate(0.0F, 0.0F, 0.5F);
        } else {
            item.translate(0.0F, 0.0F, 0.4375F);
        }

        int i = entity.containsMap() ? entity.getRotation() % 4 * 2 : entity.getRotation();

        item.rotateZDegrees(i * 360.0F / 8.0F);

        item.scale(0.5F, 0.5F, 0.5F);

        item.light(getLightVal(light)).setChanged();
    }

    @Override
    public void update(float partialTick) {

    }

    @Override
    protected void _delete() {
        frame.delete();
        item.delete();
    }

    private int getLightVal(int regularLightVal) {
        return entity.getType() == EntityType.GLOW_ITEM_FRAME ? 15728880 : regularLightVal;
    }

    protected int getSkyLightLevel(BlockPos pos) {
        return level.getLightLevel(LightType.SKY, pos);
    }

    protected int getBlockLightLevelBase(BlockPos pos) {
        return entity.isOnFire() ? 15 : level.getLightLevel(LightType.BLOCK, pos);
    }

    protected int getBlockLightLevel(BlockPos pos) {
        return entity.getType() == EntityType.GLOW_ITEM_FRAME ? Math.max(5, getBlockLightLevelBase(pos)) : getBlockLightLevelBase(pos);
    }

    public BlockStateModel getFrameModel() {
        boolean bl = entity.getType() == EntityType.GLOW_ITEM_FRAME;
        BlockState state = BlockStateManagers.getStateForItemFrame(bl, false);
        return MinecraftClient.getInstance().getBlockRenderManager().getModel(state);
    }
}
