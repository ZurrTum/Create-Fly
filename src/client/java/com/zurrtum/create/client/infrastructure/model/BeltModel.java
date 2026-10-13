package com.zurrtum.create.client.infrastructure.model;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.catnip.data.Iterate;
import com.zurrtum.create.client.AllPartialModels;
import com.zurrtum.create.client.AllSpriteShifts;
import com.zurrtum.create.client.catnip.render.ShiftStitchedSprite;
import com.zurrtum.create.client.catnip.render.SpriteShiftMap;
import com.zurrtum.create.client.flywheel.lib.model.baked.PartialModel;
import com.zurrtum.create.client.foundation.model.SimpleModelPart;
import com.zurrtum.create.content.kinetics.belt.BeltBlock;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity;
import com.zurrtum.create.content.kinetics.belt.BeltBlockEntity.CasingType;
import com.zurrtum.create.content.kinetics.belt.BeltPart;
import com.zurrtum.create.content.kinetics.belt.BeltSlope;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.BakedQuad.MaterialInfo;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.sprite.Material.Baked;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.UnknownNullability;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.zurrtum.create.Create.MOD_ID;

public class BeltModel extends WrapperBlockStateModel {
    private static final Material ANDESITE_MATERIAL = new Material(Identifier.fromNamespaceAndPath(
        MOD_ID,
        "block/andesite_casing"
    ));
    private static final SpriteShiftMap SPRITE_SHIFT = AllSpriteShifts.ANDESIDE_BELT_CASING;
    private static final ShiftStitchedSprite LEFT_CULL = SPRITE_SHIFT.get(Identifier.fromNamespaceAndPath(
        MOD_ID,
        "block/belt/brass_casing_14"
    ));
    private static final ShiftStitchedSprite RIGHT_CULL = SPRITE_SHIFT.get(Identifier.fromNamespaceAndPath(
        MOD_ID,
        "block/belt/brass_casing_15"
    ));
    private @Nullable BlockStateModel andesiteModel;
    private final PartialModel brassCover;
    private final PartialModel andesiteCover;
    private final @Nullable DiagonalCullModel cullModel;
    private @UnknownNullability Baked andesiteParticle;

    public BeltModel(BlockState state, UnbakedRoot unbaked) {
        super(state, unbaked);
        Direction direction = state.getValue(BeltBlock.HORIZONTAL_FACING);
        boolean alongX = direction.getAxis() == Axis.X;
        brassCover = alongX ? AllPartialModels.BRASS_BELT_COVER_X : AllPartialModels.BRASS_BELT_COVER_Z;
        andesiteCover = alongX ? AllPartialModels.ANDESITE_BELT_COVER_X : AllPartialModels.ANDESITE_BELT_COVER_Z;
        BeltSlope slope = state.getValue(BeltBlock.SLOPE);
        int sign;
        if (slope == BeltSlope.UPWARD) {
            sign = 1;
        } else if (slope == BeltSlope.DOWNWARD) {
            sign = -1;
        } else {
            cullModel = null;
            return;
        }
        BeltPart part = state.getValue(BeltBlock.PART);
        Vec3i offset = direction.getUnitVec3i();
        int offsetX = offset.getX();
        int offsetZ = offset.getZ();
        if (part == BeltPart.START) {
            cullModel = new OneCheckModel(offsetX, sign, offsetZ);
        } else if (part == BeltPart.END) {
            cullModel = new OneCheckModel(-offsetX, -sign, -offsetZ);
        } else {
            cullModel = new MiddleModel(offsetX * sign, offsetZ * sign);
        }
    }

    private BlockStateModel createModel(RandomSource random, boolean andesite, int flag) {
        List<BlockStateModelPart> parts = new ArrayList<>(1);
        model.collectParts(random, parts);
        TextureAtlasSprite leftSprite = null;
        TextureAtlasSprite rightSprite = null;
        if ((flag & 1) != 0) {
            leftSprite = LEFT_CULL.getOriginal();
        }
        if ((flag & 2) != 0) {
            rightSprite = RIGHT_CULL.getOriginal();
        }
        QuadCollection.Builder builder = new QuadCollection.Builder();
        MaterialInfo info;
        TextureAtlasSprite sprite;
        for (BlockStateModelPart part : parts) {
            for (BakedQuad quad : part.getQuads(null)) {
                info = quad.materialInfo();
                sprite = info.sprite();
                if (sprite == leftSprite || sprite == rightSprite) {
                    continue;
                }
                if (andesite) {
                    builder.addUnculledFace(SPRITE_SHIFT.replaceQuad(quad, info, sprite));
                } else {
                    builder.addUnculledFace(quad);
                }
            }
            for (Direction direction : Iterate.directions) {
                for (BakedQuad quad : part.getQuads(direction)) {
                    info = quad.materialInfo();
                    sprite = info.sprite();
                    if (sprite == leftSprite || sprite == rightSprite) {
                        continue;
                    }
                    if (andesite) {
                        builder.addCulledFace(direction, SPRITE_SHIFT.replaceQuad(quad, info, sprite));
                    } else {
                        builder.addCulledFace(direction, quad);
                    }
                }
            }
        }
        QuadCollection quad = builder.build();
        if (quad == QuadCollection.EMPTY) {
            return model;
        }
        BlockStateModelPart firstPart = parts.getFirst();
        SimpleModelPart part = new SimpleModelPart(quad, firstPart.useAmbientOcclusion(), firstPart.particleMaterial());
        return new SingleVariant(part);
    }

    @Override
    public BlockStateModel bake(BlockState state, ModelBaker baker) {
        andesiteModel = null;
        if (cullModel != null) {
            cullModel.clear();
        }
        andesiteParticle = baker.materials().get(ANDESITE_MATERIAL, () -> ANDESITE_MATERIAL.sprite().toString());
        return super.bake(state, baker);
    }

    @Override
    public void addPartsWithInfo(
        BlockAndTintGetter world,
        BlockPos pos,
        BlockState state,
        RandomSource random,
        List<BlockStateModelPart> parts
    ) {
        BeltBlockEntity blockentity = (BeltBlockEntity) world.getBlockEntity(pos);
        if (blockentity == null || blockentity.casing == CasingType.NONE) {
            return;
        }
        if (blockentity.casing == CasingType.BRASS) {
            if (cullModel == null) {
                model.collectParts(random, parts);
            } else {
                cullModel.get(random, world, pos).collectParts(random, parts);
            }
            if (blockentity.covered) {
                brassCover.get().collectParts(random, parts);
            }
            return;
        }
        if (cullModel != null) {
            cullModel.getAndesite(random, world, pos).collectParts(random, parts);
        } else if (andesiteModel != null) {
            andesiteModel.collectParts(random, parts);
        } else {
            (andesiteModel = createModel(random, true, 0)).collectParts(random, parts);
        }
        if (blockentity.covered) {
            andesiteCover.get().collectParts(random, parts);
        }
    }

    @Override
    public boolean needUpdateTerrainParticle() {
        return true;
    }

    @Override
    public Baked particleMaterialWithInfo(BlockAndTintGetter world, BlockPos pos, BlockState state) {
        if (state.getValue(BeltBlock.CASING) && world.getBlockEntity(pos) instanceof BeltBlockEntity blockEntity && blockEntity.casing == CasingType.ANDESITE) {
            return andesiteParticle;
        }
        return model.particleMaterial();
    }

    private static abstract class DiagonalCullModel {
        public abstract BlockStateModel get(RandomSource random, BlockAndTintGetter world, BlockPos origin);

        public abstract BlockStateModel getAndesite(RandomSource random, BlockAndTintGetter world, BlockPos origin);

        protected boolean test(BlockAndTintGetter world, BlockPos pos) {
            BlockState state = world.getBlockState(pos);
            if (state.is(AllBlocks.BELT)) {
                return state.getValue(BeltBlock.CASING);
            }
            return false;
        }

        public abstract void clear();
    }

    class OneCheckModel extends DiagonalCullModel {
        private final int flag;
        private final Vec3i offset;
        private @Nullable BlockStateModel cullModel;
        private @Nullable BlockStateModel cullAndesiteModel;

        public OneCheckModel(int offsetX, int offsetY, int offsetZ) {
            flag = offsetY > 0 ? 2 : 1;
            offset = new Vec3i(offsetX, offsetY, offsetZ);
        }

        @Override
        public BlockStateModel get(RandomSource random, BlockAndTintGetter world, BlockPos origin) {
            if (test(world, origin.offset(offset))) {
                if (cullModel != null) {
                    return cullModel;
                }
                return cullModel = createModel(random, false, flag);
            }
            return model;
        }

        @Override
        public BlockStateModel getAndesite(RandomSource random, BlockAndTintGetter world, BlockPos origin) {
            if (test(world, origin.offset(offset))) {
                if (cullAndesiteModel != null) {
                    return cullAndesiteModel;
                }
                return cullAndesiteModel = createModel(random, true, flag);
            }
            if (andesiteModel != null) {
                return andesiteModel;
            }
            return andesiteModel = createModel(random, true, 0);
        }

        @Override
        public void clear() {
            cullModel = null;
            cullAndesiteModel = null;
        }
    }

    class MiddleModel extends DiagonalCullModel {
        private final Vec3i leftOffset;
        private final Vec3i rightOffset;
        private @Nullable BlockStateModel leftCullModel;
        private @Nullable BlockStateModel rightCullModel;
        private @Nullable BlockStateModel bothCullModel;
        private @Nullable BlockStateModel leftCullAndesiteModel;
        private @Nullable BlockStateModel rightCullAndesiteModel;
        private @Nullable BlockStateModel bothCullAndesiteModel;

        public MiddleModel(int offsetX, int offsetZ) {
            rightOffset = new Vec3i(offsetX, 1, offsetZ);
            leftOffset = new Vec3i(-offsetX, -1, -offsetZ);
        }

        @Override
        public BlockStateModel get(RandomSource random, BlockAndTintGetter world, BlockPos origin) {
            MutableBlockPos pos = new MutableBlockPos();
            if (test(world, pos.setWithOffset(origin, leftOffset))) {
                if (test(world, pos.setWithOffset(origin, rightOffset))) {
                    if (bothCullModel != null) {
                        return bothCullModel;
                    }
                    return bothCullModel = createModel(random, false, 3);
                }
                if (leftCullModel != null) {
                    return leftCullModel;
                }
                return leftCullModel = createModel(random, false, 1);
            }
            if (test(world, pos.setWithOffset(origin, rightOffset))) {
                if (rightCullModel != null) {
                    return rightCullModel;
                }
                return rightCullModel = createModel(random, false, 2);
            }
            return model;
        }

        @Override
        public BlockStateModel getAndesite(RandomSource random, BlockAndTintGetter world, BlockPos origin) {
            MutableBlockPos pos = new MutableBlockPos();
            if (test(world, pos.setWithOffset(origin, leftOffset))) {
                if (test(world, pos.setWithOffset(origin, rightOffset))) {
                    if (bothCullAndesiteModel != null) {
                        return bothCullAndesiteModel;
                    }
                    return bothCullAndesiteModel = createModel(random, true, 3);
                }
                if (leftCullAndesiteModel != null) {
                    return leftCullAndesiteModel;
                }
                return leftCullAndesiteModel = createModel(random, true, 1);
            }
            if (test(world, pos.setWithOffset(origin, rightOffset))) {
                if (rightCullAndesiteModel != null) {
                    return rightCullAndesiteModel;
                }
                return rightCullAndesiteModel = createModel(random, true, 2);
            }
            if (andesiteModel != null) {
                return andesiteModel;
            }
            return andesiteModel = createModel(random, true, 0);
        }

        @Override
        public void clear() {
            leftCullModel = null;
            rightCullModel = null;
            bothCullModel = null;
            leftCullAndesiteModel = null;
            rightCullAndesiteModel = null;
            bothCullAndesiteModel = null;
        }
    }
}
