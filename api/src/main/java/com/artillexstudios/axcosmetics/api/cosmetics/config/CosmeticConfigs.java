package com.artillexstudios.axcosmetics.api.cosmetics.config;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface CosmeticConfigs {

    CompletableFuture<?> register(CosmeticConfig config);

    void deregister(CosmeticConfig config);

    @Nullable
    CosmeticConfig fetch(String identifier);

    @Nullable
    CosmeticConfig fetch(int identifier);

    Collection<CosmeticConfig> registered();

    Set<String> names();
}
