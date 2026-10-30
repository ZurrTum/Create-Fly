package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.zurrtum.create.AllDataComponents;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;
import com.zurrtum.create.infrastructure.component.SandPaperItemComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.item.ItemStackRenderState.LayerRenderState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.ItemQuads;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

public class SandPaperModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/sand_paper");

    private final ItemQuads quads;
    private final ModelRenderProperties settings;
    private final Supplier<Vector3fc[]> extents;

    public SandPaperModel(List<BakedQuad> quads, ModelRenderProperties settings) {
        this.quads = ItemQuads.split(quads);
        this.settings = settings;
        extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(quads));
    }

    @Override
    public void update(
        ItemStackRenderState state,
        ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner ctx,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        LayerRenderState itemLayer = submitQuads(state, settings, displayContext, quads);
        itemLayer.setExtents(extents);
        Player entity;
        int itemInUseCount;
        if (ctx instanceof Player player) {
            itemInUseCount = player.getUseItemRemainingTicks();
            entity = player;
        } else {
            LocalPlayer player = Minecraft.getInstance().player;
            itemInUseCount = player.getUseItemRemainingTicks();
            entity = player;
        }
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        if ((leftHand || displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) && itemInUseCount > 0) {
            Matrix4f pose = itemLayer.localTransform;
            pose.translate(0.5F, 0.5F, 0.5F);
            if (leftHand) {
                pose.translate(-0.5f, 0, -0.25f);
                pose.rotateZ(Radians.DEG_320);
                pose.rotateX(Radians.DEG_350);
                pose.rotateY(Radians.DEG_270);
            } else {
                pose.translate(0.5f, 0, -0.25f);
                pose.rotateZ(Radians.DEG_40);
                pose.rotateX(Radians.DEG_10);
                pose.rotateY(Radians.DEG_90);
            }
            pose.translate(-0.5F, -0.5F, -0.5F);
        }
        SandPaperItemComponent component = stack.get(AllDataComponents.SAND_PAPER_POLISHING);
        if (component != null) {
            int i = state.activeLayerCount;
            resolver.appendItemLayers(state, component.item(), ItemDisplayContext.GUI, world, ctx, seed);
            int size = state.activeLayerCount;
            if (i != size) {
                int maxUseTime = stack.getUseDuration(entity);
                boolean jeiMode = stack.has(AllDataComponents.SAND_PAPER_JEI);
                float partialTicks = AnimationTickHolder.getPartialTicks();
                float time = (jeiMode ? -AnimationTickHolder.getTicks() % maxUseTime :
                    itemInUseCount) - partialTicks + 1.0F;
                LayerRenderState[] layers = state.layers;
                ItemTransform transform = settings.transforms().getTransform(displayContext);
                boolean applyLeftHandFix = displayContext.leftHand();
                boolean reverseBobbing = time / maxUseTime < 0.8F;
                boolean isGui = displayContext == ItemDisplayContext.GUI;
                float yRadians = leftHand ? Radians.DEG_320 : Radians.DEG_40;
                float bobbing = reverseBobbing ? -Mth.abs(Mth.cos(time / 4.0F * (float) Math.PI) * 0.1F) : 0;
                for (; i < size; i++) {
                    LayerRenderState layer = layers[i];
                    ItemTransform itemTransform = layer.itemTransform;
                    layer.itemTransform = transform;
                    Matrix4f pose = layer.localTransform;
                    pose.mulLocal(ItemModelRenderHelper.getPose(applyLeftHandFix, itemTransform));
                    if (reverseBobbing) {
                        if (isGui) {
                            pose.translateLocal(bobbing, bobbing, 0.0F);
                        } else {
                            pose.translateLocal(0.0F, bobbing, 0.0F);
                        }
                    }
                    if (isGui) {
                        pose.scaleLocal(0.75f);
                        pose.translateLocal(0.5f, 0.7f, 1.5f);
                    } else {
                        pose.rotateLocalY(yRadians);
                        pose.translateLocal(0.5f, 0.5f, 0.5f);
                    }
                }
            }
        }
    }

    public record Unbaked(Identifier model) implements ItemModel.Unbaked {
        public static final MapCodec<Unbaked> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Identifier.CODEC.fieldOf(
            "model").forGetter(Unbaked::model)).apply(instance, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return CODEC;
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(model);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(this.model);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new SandPaperModel(quads, settings);
        }
    }
}
