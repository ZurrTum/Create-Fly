package com.zurrtum.create.client.ponder.foundation.registration;

import com.zurrtum.create.client.ponder.api.registration.TagBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

public class PonderTagBuilder implements TagBuilder {

    final Identifier id;
    private final Consumer<PonderTagBuilder> onFinish;

    String title = "NO_TITLE";
    String description = "NO_DESCRIPTION";
    boolean addToIndex = false;
    @Nullable Identifier textureIconLocation;
    ItemStack itemIcon = ItemStack.EMPTY;
    ItemStack mainItem = ItemStack.EMPTY;

    public PonderTagBuilder(Identifier id, Consumer<PonderTagBuilder> onFinish) {
        this.id = id;
        this.onFinish = onFinish;
    }

    @Override
    public TagBuilder title(String title) {
        this.title = title;
        return this;
    }

    @Override
    public TagBuilder description(String description) {
        this.description = description;
        return this;
    }

    @Override
    public TagBuilder addToIndex() {
        this.addToIndex = true;
        return this;
    }

    @Override
    public TagBuilder icon(Identifier location) {
        this.textureIconLocation = Identifier.fromNamespaceAndPath(location.getNamespace(), "textures/ponder/tag/" + location.getPath() + ".png");
        return this;
    }

    @Override
    public TagBuilder icon(String path) {
        this.textureIconLocation = Identifier.fromNamespaceAndPath(id.getNamespace(), "textures/ponder/tag/" + path + ".png");
        return this;
    }

    @Override
    public TagBuilder idAsIcon() {
        return icon(id);
    }

    @Override
    public TagBuilder item(ItemLike item, boolean useAsIcon, boolean useAsMainItem) {
        if (useAsIcon)
            this.itemIcon = new ItemStack(item);
        if (useAsMainItem)
            this.mainItem = new ItemStack(item);
        return this;
    }

    @Override
    public void register() {
        onFinish.accept(this);
    }
}