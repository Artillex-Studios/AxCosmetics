package com.artillexstudios.axcosmetics.cosmetics;

import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;

import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class CosmeticTicker {
    private ScheduledFuture<?> future;

    public void start() {
        if (this.future != null && !this.future.isCancelled()) {
            throw new IllegalStateException();
        }

        this.future = AsyncUtils.scheduleAtFixedRate(() -> {
            for (User onlineUser : AxCosmeticsAPI.instance().userRepository().onlineUsers()) {
                Collection<? extends Cosmetic<?>> equippedCosmetics = onlineUser.getEquippedCosmetics();
                for (Cosmetic<?> cosmetic : equippedCosmetics) {
                    try {
                        cosmetic.update();
                    } catch (Exception exception) {
                        LogUtils.warn("Failed to tick cosmetic!", exception);
                        continue;
                    }
                }
            }
        }, 0, Config.tickFrequency, TimeUnit.MILLISECONDS);
    }

    public void cancel() {
        if (this.future == null || this.future.isCancelled()) {
            throw new IllegalStateException();
        }

        this.future.cancel(false);
        this.future = null;
    }
}
