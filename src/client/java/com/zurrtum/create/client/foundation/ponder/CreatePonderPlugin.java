package com.zurrtum.create.client.foundation.ponder;

import com.zurrtum.create.AllBlocks;
import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderScenes;
import com.zurrtum.create.client.infrastructure.ponder.AllCreatePonderTags;
import com.zurrtum.create.client.ponder.api.level.PonderLevel;
import com.zurrtum.create.client.ponder.api.registration.*;
import com.zurrtum.create.content.kinetics.crank.ValveHandleBlock;
import com.zurrtum.create.content.logistics.packagePort.postbox.PostboxBlock;
import com.zurrtum.create.content.logistics.tableCloth.TableClothBlock;
import net.minecraft.resources.ResourceLocation;

import static com.zurrtum.create.Create.MOD_ID;

public class CreatePonderPlugin implements PonderPlugin {

    @Override
    public String getModId() {
        return MOD_ID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        AllCreatePonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        AllCreatePonderTags.register(helper);
    }

    @Override
    public void registerSharedText(SharedTextRegistrationHelper helper) {
        helper.registerSharedText("rpm8", "8 RPM");
        helper.registerSharedText("rpm16", "16 RPM");
        helper.registerSharedText("rpm16_source", "Source: 16 RPM");
        helper.registerSharedText("rpm32", "32 RPM");

        helper.registerSharedText("movement_anchors", "With the help of Super Glue, larger structures can be moved.");
        helper.registerSharedText("behaviour_modify_value_panel", "This behaviour can be modified using the value panel");
        helper.registerSharedText("storage_on_contraption", "Inventories attached to the Contraption will pick up their drops automatically");
    }

    @Override
    public void onPonderLevelRestore(PonderLevel ponderLevel) {
        PonderWorldBlockEntityFix.fixControllerBlockEntities(ponderLevel);
    }

    @Override
    public void indexExclusions(IndexExclusionHelper helper) {
        helper.excludeBlockVariants(ValveHandleBlock.class, AllBlocks.COPPER_VALVE_HANDLE);
        helper.excludeBlockVariants(PostboxBlock.class, AllBlocks.WHITE_POSTBOX);
        helper.excludeBlockVariants(TableClothBlock.class, AllBlocks.WHITE_TABLE_CLOTH);
    }
}