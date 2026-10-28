package com.zurrtum.create.client.infrastructure.model;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.catnip.animation.AnimationTickHolder;
import com.zurrtum.create.client.content.equipment.extendoGrip.ExtendoGripRenderHandler;
import com.zurrtum.create.client.content.kinetics.base.Radians;
import com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper;
import com.zurrtum.create.client.foundation.model.BakedModelHelper;
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
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;
import static com.zurrtum.create.client.flywheel.lib.model.baked.ItemModelRenderHelper.submitQuads;

public class ExtendoGripModel implements ItemModel {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(MOD_ID, "model/extendo_grip");
    public static final Identifier ITEM_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/item");
    public static final Identifier COG_ID = Identifier.fromNamespaceAndPath(MOD_ID, "item/extendo_grip/cog");
    public static final Identifier THIN_SHORT_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/extendo_grip/thin_short"
    );
    public static final Identifier WIDE_SHORT_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/extendo_grip/wide_short"
    );
    public static final Identifier THIN_LONG_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/extendo_grip/thin_long"
    );
    public static final Identifier WIDE_LONG_ID = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "item/extendo_grip/wide_long"
    );
    public static final Identifier DEPLOYER_HAND_POINTING = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "block/deployer/hand_pointing"
    );
    public static final Identifier DEPLOYER_HAND_PUNCHING = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "block/deployer/hand_punching"
    );
    public static final Identifier DEPLOYER_HAND_HOLDING = Identifier.fromNamespaceAndPath(
        MOD_ID,
        "block/deployer/hand_holding"
    );

    private final ModelRenderProperties settings;
    private final Supplier<Vector3fc[]> extents;
    private final List<BakedQuad> item;
    private final List<BakedQuad> cog;
    private final List<BakedQuad> thinShort;
    private final List<BakedQuad> wideShort;
    private final List<BakedQuad> thinLong;
    private final List<BakedQuad> wideLong;
    private final List<BakedQuad> pointing;
    private final List<BakedQuad> punching;
    private final List<BakedQuad> holding;

    public ExtendoGripModel(
        ModelRenderProperties settings,
        List<BakedQuad> item,
        List<BakedQuad> cog,
        List<BakedQuad> thinShort,
        List<BakedQuad> wideShort,
        List<BakedQuad> thinLong,
        List<BakedQuad> wideLong,
        List<BakedQuad> pointing,
        List<BakedQuad> punching,
        List<BakedQuad> holding
    ) {
        this.settings = settings;
        this.item = item;
        extents = Suppliers.memoize(() -> CuboidItemModelWrapper.computeExtents(item));
        this.cog = cog;
        this.thinShort = thinShort;
        this.wideShort = wideShort;
        this.thinLong = thinLong;
        this.wideLong = wideLong;
        this.pointing = pointing;
        this.punching = punching;
        this.holding = holding;
    }

    @Override
    public void update(
        ItemStackRenderState state,
        @Nullable ItemStack stack,
        ItemModelResolver resolver,
        ItemDisplayContext displayContext,
        @Nullable ClientLevel world,
        @Nullable ItemOwner ctx,
        int seed
    ) {
        state.appendModelIdentityElement(this);
        state.setAnimated();
        boolean leftHand = displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean rightHand = displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        boolean applyLeftHandFix = displayContext.leftHand();
        List<BakedQuad> hand = leftHand || rightHand ? ExtendoGripRenderHandler.holding ? holding : punching : pointing;
        float animation;
        if (leftHand || rightHand) {
            animation = Mth.lerp(
                AnimationTickHolder.getPartialTicks(),
                ExtendoGripRenderHandler.lastMainHandAnimation,
                ExtendoGripRenderHandler.mainHandAnimation
            );
        } else {
            animation = 0.25f;
        }
        animation = animation * animation * animation;
        float extensionAngle = Mth.lerp(animation, 24.0f, 156.0f);
        float oppositeRadians = Mth.DEG_TO_RAD * (180 - extensionAngle);
        float halfAngle = extensionAngle / 2;
        float angle = AnimationTickHolder.getRenderTime() * -2;
        if (leftHand || rightHand) {
            angle += 360 * animation;
        }
        float offsetZ = -0.7f - animation * 2.25f;
        ItemTransform transform = settings.transforms().getTransform(displayContext);
        submitQuads(state, settings, displayContext, item).setExtents(extents);
        LayerRenderState cogLayer = submitQuads(state, settings, displayContext, cog);
        LayerRenderState thinShortLayer1 = submitQuads(state, settings, displayContext, thinShort);
        LayerRenderState thinShortLayer2 = submitQuads(state, settings, displayContext, thinShort);
        LayerRenderState wideLongLayer1 = submitQuads(state, settings, displayContext, wideLong);
        LayerRenderState wideShortLayer1 = submitQuads(state, settings, displayContext, wideShort);
        LayerRenderState wideShortLayer2 = submitQuads(state, settings, displayContext, wideShort);
        LayerRenderState thinLongLayer1 = submitQuads(state, settings, displayContext, thinLong);
        LayerRenderState handLayer1 = submitQuads(state, settings, displayContext, hand);
        Matrix4f pose = cogLayer.localTransform;
        pose.translate(0.5f, 0.5625f, 0.5f);
        pose.rotateZ(Mth.DEG_TO_RAD * (angle % 360));
        pose.translate(-0.5f, -0.5625f, -0.5f);
        pose = thinShortLayer1.localTransform;
        pose.translate(0, 0.5625f, 0.0625f);
        pose.scale(1, 1, 1 + animation);
        wideShortLayer1.localTransform.set(pose);
        pose.rotateX(Mth.DEG_TO_RAD * -halfAngle);
        pose = wideLongLayer1.localTransform.set(pose);
        pose.translate(0, 0.34375f, 0);
        pose.rotateX(-oppositeRadians);
        pose = thinShortLayer2.localTransform.set(pose);
        pose.translate(0, 0.6875f, 0);
        pose.rotateX(oppositeRadians);
        pose.translate(0, 0.03125f, 0);
        pose = wideShortLayer1.localTransform;
        pose.rotateX(Mth.DEG_TO_RAD * (halfAngle - 180));
        pose = thinLongLayer1.localTransform.set(pose);
        pose.translate(0, 0.34375f, 0);
        pose.rotateX(oppositeRadians);
        pose = wideShortLayer2.localTransform.set(pose);
        pose.translate(0, 0.6875f, 0);
        pose.rotateX(-oppositeRadians);
        pose.translate(0, 0.03125f, 0);
        pose = handLayer1.localTransform.set(pose);
        pose.translate(0, 0.34375f, 0);
        pose.rotateX(Mth.DEG_TO_RAD * (180 - halfAngle));
        pose.rotateY(Radians.DEG_180);
        pose.translate(0, 0, -0.25f);
        pose.scale(1, 1, 1 / (1 + animation));
        pose.translate(-1.0f, -0.5f, -0.5f);
        if (stack == null) {
            LayerRenderState itemLayer = submitQuads(state, settings, displayContext, item);
            itemLayer.setExtents(extents);
            pose = itemLayer.localTransform;
            pose.translate(0.45f, 0.65f, offsetZ);
            pose.mul(ItemModelRenderHelper.getPose(applyLeftHandFix, transform));
            LayerRenderState cogLayer2 = submitQuads(state, settings, displayContext, cog);
            LayerRenderState thinShortLayer3 = submitQuads(state, settings, displayContext, thinShort);
            LayerRenderState thinShortLayer4 = submitQuads(state, settings, displayContext, thinShort);
            LayerRenderState wideLongLayer2 = submitQuads(state, settings, displayContext, wideLong);
            LayerRenderState wideShortLayer3 = submitQuads(state, settings, displayContext, wideShort);
            LayerRenderState wideShortLayer4 = submitQuads(state, settings, displayContext, wideShort);
            LayerRenderState thinLongLayer2 = submitQuads(state, settings, displayContext, thinLong);
            LayerRenderState handLayer2 = submitQuads(state, settings, displayContext, hand);
            cogLayer2.localTransform.set(pose).mul(cogLayer.localTransform);
            thinShortLayer3.localTransform.set(pose).mul(thinShortLayer1.localTransform);
            thinShortLayer4.localTransform.set(pose).mul(thinShortLayer2.localTransform);
            wideLongLayer2.localTransform.set(pose).mul(wideLongLayer1.localTransform);
            wideShortLayer3.localTransform.set(pose).mul(wideShortLayer1.localTransform);
            wideShortLayer4.localTransform.set(pose).mul(wideShortLayer2.localTransform);
            thinLongLayer2.localTransform.set(pose).mul(thinLongLayer1.localTransform);
            handLayer2.localTransform.set(handLayer1.localTransform).mulLocal(pose);
        } else if (!stack.is(AllItems.EXTENDO_GRIP)) {
            int i = state.activeLayerCount;
            resolver.appendItemLayers(state, stack, displayContext, world, ctx, seed);
            int size = state.activeLayerCount;
            if (i != size) {
                LayerRenderState[] layers = state.layers;
                LayerRenderState layer = layers[i];
                boolean blockLight = layer.usesBlockLight;
                HumanoidArm mainArm = HumanoidArm.RIGHT;
                if (ctx instanceof Avatar entity) {
                    mainArm = entity.getMainArm();
                } else {
                    LocalPlayer player = Minecraft.getInstance().player;
                    if (player != null) {
                        mainArm = player.getMainArm();
                    }
                }
                float blockOffsetX;
                float yRadians;
                if (blockLight) {
                    if (rightHand ^ mainArm == HumanoidArm.LEFT) {
                        blockOffsetX = 0.15f;
                        yRadians = Radians.DEG_45;
                    } else {
                        blockOffsetX = -0.15f;
                        yRadians = Radians.DEG_315;
                    }
                } else {
                    blockOffsetX = 0;
                    yRadians = 0;
                }
                do {
                    ItemTransform itemTransform = layer.itemTransform;
                    layer.itemTransform = transform;
                    pose = layer.localTransform;
                    pose.mulLocal(ItemModelRenderHelper.getPose(applyLeftHandFix, itemTransform));
                    if (blockLight) {
                        pose.scaleLocal(1.25f);
                        pose.translateLocal(blockOffsetX, -0.15f, -0.05f);
                        pose.rotateLocalY(yRadians);
                    }
                    pose.translateLocal(0.45f, 0.65f, offsetZ);
                    if (++i == size) {
                        break;
                    }
                    layer = layers[i];
                } while (true);
            }
        }
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
            resolver.markDependency(THIN_SHORT_ID);
            resolver.markDependency(WIDE_SHORT_ID);
            resolver.markDependency(THIN_LONG_ID);
            resolver.markDependency(WIDE_LONG_ID);
            resolver.markDependency(DEPLOYER_HAND_POINTING);
            resolver.markDependency(DEPLOYER_HAND_PUNCHING);
            resolver.markDependency(DEPLOYER_HAND_HOLDING);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transformation) {
            ModelBaker baker = context.blockModelBaker();
            ResolvedModel model = baker.getModel(ITEM_ID);
            TextureSlots textures = model.getTopTextureSlots();
            List<BakedQuad> quads = model.bakeTopGeometry(textures, baker, BlockModelRotation.IDENTITY).getAll();
            ModelRenderProperties settings = ModelRenderProperties.fromResolvedModel(baker, model, textures);
            return new ExtendoGripModel(
                settings,
                quads,
                BakedModelHelper.bakeQuads(baker, COG_ID),
                BakedModelHelper.bakeQuads(baker, THIN_SHORT_ID),
                BakedModelHelper.bakeQuads(baker, WIDE_SHORT_ID),
                BakedModelHelper.bakeQuads(baker, THIN_LONG_ID),
                BakedModelHelper.bakeQuads(baker, WIDE_LONG_ID),
                BakedModelHelper.bakeQuads(baker, DEPLOYER_HAND_POINTING),
                BakedModelHelper.bakeQuads(baker, DEPLOYER_HAND_PUNCHING),
                BakedModelHelper.bakeQuads(baker, DEPLOYER_HAND_HOLDING)
            );
        }
    }
}
