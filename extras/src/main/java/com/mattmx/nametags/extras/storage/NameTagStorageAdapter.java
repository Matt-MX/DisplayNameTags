package com.mattmx.nametags.extras.storage;

import com.mattmx.nametags.extras.schema.CustomNameTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public interface NameTagStorageAdapter {

    static Throwable storageUnavailable() {
        return new IllegalStateException("Storage is not available!");
    }

    void start();

    default void setPlayerNameTag(@NotNull UUID uniqueId, @Nullable CustomNameTag nameTag) {
        this.setNameTag(uniqueId.toString(), nameTag);
    }

    void setNameTag(@NotNull String id, @Nullable CustomNameTag nameTag);

    default @NotNull Optional<CustomNameTag> getPlayerNameTag(@NotNull UUID uniqueId) {
        return this.getNameTag(uniqueId.toString());
    }

    @NotNull Optional<CustomNameTag> getNameTag(@NotNull String id);

    void stop();

    boolean isConnected();

}
