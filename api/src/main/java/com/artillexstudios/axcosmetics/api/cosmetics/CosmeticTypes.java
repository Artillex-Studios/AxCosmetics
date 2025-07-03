package com.artillexstudios.axcosmetics.api.cosmetics;

import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.api.user.User;
import org.apache.commons.lang3.function.TriFunction;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface CosmeticTypes {

    <T extends CosmeticConfig> void register(String identifier, TriFunction<User, CosmeticData, T, Cosmetic<T>> function);

    void deregister(String identifier);

    @Nullable
    <T extends CosmeticConfig> TriFunction<User, CosmeticData, T, Cosmetic<T>> fetch(String identifier);

    Collection<TriFunction<User, CosmeticData, ?, Cosmetic<?>>> registered();

    /**
     * Get the names of the registered cosmetictypes.
     * @return An immutable set of the names of the registered cosmetictypes.
     */
    Set<String> names();
}
