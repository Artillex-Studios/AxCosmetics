package com.artillexstudios.axcosmetics.cosmetics.type;

import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.cosmetics.config.FirstPersonBackpackConfig;

public abstract class FirstPersonBackpackCosmetic extends Cosmetic<FirstPersonBackpackConfig> {

    public FirstPersonBackpackCosmetic(User user, CosmeticData data, FirstPersonBackpackConfig config) {
        super(user, data, config);
    }

    public abstract Integer entityId();

    public abstract Integer firstPersonRiderId();
}
