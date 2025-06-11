package com.artillexstudios.axcosmetics.user;

import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class User implements com.artillexstudios.axcosmetics.api.user.User {
    private final ConcurrentHashMap<CosmeticSlot, Cosmetic<?>> equipped = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Cosmetic<?>> cosmetics = new ConcurrentLinkedQueue<>();
    private final int id;
    private final OfflinePlayer offlinePlayer;
    private Player onlinePlayer;
    private final DatabaseAccessor accessor;

    public User(int id, OfflinePlayer offlinePlayer, DatabaseAccessor accessor) {
        this.id = id;
        this.offlinePlayer = offlinePlayer;
        this.accessor = accessor;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public OfflinePlayer player() {
        return this.offlinePlayer;
    }

    public void onlinePlayer(Player player) {
        this.onlinePlayer = player;
    }

    @Override
    public @Nullable Player onlinePlayer() {
        return this.onlinePlayer;
    }

    @Override
    public @Nullable <T extends CosmeticConfig> Cosmetic<T> getCosmetic(CosmeticSlot slot) {
        return (Cosmetic<T>) this.equipped.get(slot);
    }

    @Override
    public <T extends CosmeticConfig> void addCosmetic(Cosmetic<T> cosmetic) {
        this.cosmetics.add(cosmetic);
    }

    @Override
    public void updateCosmetic(CosmeticSlot slot) {
        Cosmetic<?> cosmetic = this.equipped.get(slot);
        if (cosmetic == null) {
            return;
        }

        cosmetic.update();
    }

    @Override
    public Collection<Cosmetic<?>> getCosmetics() {
        return Collections.unmodifiableCollection(this.cosmetics);
    }

    @Override
    public Collection<Cosmetic<?>> getEquippedCosmetics() {
        return Collections.unmodifiableCollection(this.equipped.values());
    }

    @Override
    public void equipCosmetic(Cosmetic<?> cosmetic) {
        Cosmetic<?> equipped = this.equipped.get(cosmetic.slot());
        if (equipped != null) {
            equipped.despawn();
        }

        this.equipped.put(cosmetic.slot(), cosmetic);
        cosmetic.spawn();
    }

    @Override
    public boolean unequipCosmetic(Cosmetic<?> cosmetic) {
        return this.unequipCosmetic(cosmetic.slot());
    }

    @Override
    public boolean unequipCosmetic(CosmeticSlot slot) {
        Cosmetic<?> equipped = this.equipped.remove(slot);
        if (equipped != null) {
            equipped.despawn();
            return true;
        }

        return false;
    }
}
