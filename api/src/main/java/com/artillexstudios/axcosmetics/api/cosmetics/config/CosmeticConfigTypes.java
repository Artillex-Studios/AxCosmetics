package com.artillexstudios.axcosmetics.api.cosmetics.config;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

public interface CosmeticConfigTypes {

    <T extends CosmeticConfig> void register(String identifier, BiFunction<String, Map<String, Object>, T> function);

    void deregister(String identifier);

    @Nullable
    <T extends CosmeticConfig> BiFunction<String, Map<String, Object>, T> fetch(String identifier);

    Collection<BiFunction<String, Map<String, Object>, ?>> registered();

    Set<String> names();
}
