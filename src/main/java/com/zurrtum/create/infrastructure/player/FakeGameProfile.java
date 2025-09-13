package com.zurrtum.create.infrastructure.player;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

// Credit to Mekanism for this approach. Helps fake players get past claims and
// protection by other mods
public class FakeGameProfile extends GameProfile {
    private final UUID owner;
    private final String name;

    public FakeGameProfile(UUID fallbackID, String fallbackName, @Nullable UUID owner, @Nullable String name) {
        super(fallbackID, fallbackName);
        this.name = name;
        this.owner = owner;
    }

    @Override
    public UUID getId() {
        return owner == null ? super.getId() : owner;
    }

    @Override
    public String getName() {
        if (owner == null)
            return super.getName();
        return name == null ? super.getName() : name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GameProfile otherProfile))
            return false;
        return Objects.equals(getId(), otherProfile.getId()) && Objects.equals(getName(), otherProfile.getName());
    }

    @Override
    public int hashCode() {
        UUID id = getId();
        String name = getName();
        int result = id == null ? 0 : id.hashCode();
        result = 31 * result + (name == null ? 0 : name.hashCode());
        return result;
    }
}
