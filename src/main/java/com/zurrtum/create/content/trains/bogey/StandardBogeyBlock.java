package com.zurrtum.create.content.trains.bogey;

import com.zurrtum.create.AllBlockEntityTypes;
import com.zurrtum.create.AllBogeyStyles;
import com.zurrtum.create.AllItems;
import com.zurrtum.create.AllTrackMaterials;
import com.zurrtum.create.api.schematic.requirement.SpecialBlockItemRequirement;
import com.zurrtum.create.foundation.block.IBE;
import com.zurrtum.create.foundation.block.ProperWaterloggedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class StandardBogeyBlock extends AbstractBogeyBlock<StandardBogeyBlockEntity> implements IBE<StandardBogeyBlockEntity>, ProperWaterloggedBlock, SpecialBlockItemRequirement {

    public StandardBogeyBlock(Properties props, BogeySize size) {
        super(props, size);
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false));
    }

    public static StandardBogeyBlock small(Properties props) {
        return new StandardBogeyBlock(props, AllBogeySizes.SMALL);
    }

    public static StandardBogeyBlock large(Properties props) {
        return new StandardBogeyBlock(props, AllBogeySizes.LARGE);
    }

    @Override
    public Identifier getTrackType(BogeyStyle style) {
        return AllTrackMaterials.ANDESITE.getId();
    }

    @Override
    public double getWheelPointSpacing() {
        return 2;
    }

    @Override
    public double getWheelRadius() {
        return (size == AllBogeySizes.LARGE ? 12.5 : 6.5) / 16d;
    }

    @Override
    public Vec3 getConnectorAnchorOffset() {
        return new Vec3(0, 7 / 32f, 1);
    }

    @Override
    public BogeyStyle getDefaultStyle() {
        return AllBogeyStyles.STANDARD;
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state, boolean includeData) {
        return AllItems.RAILWAY_CASING.getDefaultInstance();
    }

    @Override
    public Class<StandardBogeyBlockEntity> getBlockEntityClass() {
        return StandardBogeyBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StandardBogeyBlockEntity> getBlockEntityType() {
        return AllBlockEntityTypes.BOGEY;
    }

}
