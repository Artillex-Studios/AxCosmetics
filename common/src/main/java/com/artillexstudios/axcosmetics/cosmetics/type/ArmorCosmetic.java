package com.artillexstudios.axcosmetics.cosmetics.type;

import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.cosmetics.config.ArmorCosmeticConfig;

import java.util.Collection;
import java.util.List;

public class ArmorCosmetic extends Cosmetic<ArmorCosmeticConfig> {

    public ArmorCosmetic(User user, CosmeticData data, ArmorCosmeticConfig config) {
        super(user, data, config);
    }

    @Override
    public void update() {
        // do nothing
    }

    @Override
    public Collection<Class<? extends CosmeticSlot>> validSlots() {
        return List.of();
    }
}
