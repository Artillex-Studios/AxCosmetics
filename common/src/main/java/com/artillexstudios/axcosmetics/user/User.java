package com.artillexstudios.axcosmetics.user;

import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import com.artillexstudios.axcosmetics.database.dto.UserDTO;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class User implements com.artillexstudios.axcosmetics.api.user.User {
    private final ConcurrentHashMap<CosmeticSlot, List<Cosmetic<?>>> equipped = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Cosmetic<?>> cosmetics = new ConcurrentLinkedQueue<>();
    private final int id;
    private final OfflinePlayer offlinePlayer;
    private final DatabaseAccessor accessor;
    private Player onlinePlayer;

    // TODO: UserLoadEvent
    public User(int id, OfflinePlayer offlinePlayer, List<UserDTO> userDTOS, DatabaseAccessor accessor) {
        this.id = id;
        this.offlinePlayer = offlinePlayer;
        this.accessor = accessor;
        for (UserDTO userDTO : userDTOS) {
            Cosmetic<CosmeticConfig> cosmetic = AxCosmeticsAPI.instance().createCosmetic(this, userDTO.cosmeticTypeId(), new CosmeticData(userDTO.cosmeticId(), userDTO.counter(), userDTO.color()));
            this.cosmetics.add(cosmetic);
            if (userDTO.equipped()) {
                this.equipCosmetic(cosmetic);
            }
        }
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
        if (cosmetic.data().id() <= 0) {
            this.accessor.insertCosmetic(this, cosmetic).thenRun(() -> {
                this.cosmetics.add(cosmetic);
            });
        } else {
            // TODO: Update cosmetic to new owner maybe? I don't know yet
            this.cosmetics.add(cosmetic);
        }
    }

    @Override
    public void updateCosmetic(CosmeticSlot slot) {
        List<Cosmetic<?>> cosmeticList = this.equipped.get(slot);
        if (cosmeticList == null || cosmeticList.isEmpty()) {
            return;
        }

        Cosmetic<?> cosmetic = cosmeticList.getFirst();
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
    public List<? extends Cosmetic<?>> getEquippedCosmetics() {
        return this.equipped.values().stream()
                .map(List::getFirst)
                .toList();
    }

    @Override
    public void equipCosmetic(Cosmetic<?> cosmetic) {
        boolean contains = this.cosmetics.contains(cosmetic);
        List<Cosmetic<?>> cosmeticList = this.equipped.computeIfAbsent(cosmetic.slot(), o -> new ArrayList<>());
        if (!cosmeticList.isEmpty()) {
            Cosmetic<?> equipped = cosmeticList.getFirst();
            equipped.despawn();

            if (contains && this.cosmetics.contains(equipped)) {
                cosmeticList.remove(equipped);
                this.accessor.updateCosmetic(equipped, false);
            }
        }

        cosmeticList.addFirst(cosmetic);
        if (contains) {
            this.accessor.updateCosmetic(cosmetic, true);
        }
        cosmetic.spawn();
    }

    @Override
    public boolean unequipCosmetic(Cosmetic<?> cosmetic) {
        return this.unequipCosmetic(cosmetic.slot());
    }

    @Override
    public boolean unequipCosmetic(CosmeticSlot slot) {
        List<Cosmetic<?>> cosmeticList = this.equipped.get(slot);
        if (cosmeticList == null || cosmeticList.isEmpty()) {
            return false;
        }

        Cosmetic<?> equipped = cosmeticList.removeFirst();
        if (equipped != null) {
            equipped.despawn();
            if (this.cosmetics.contains(equipped)) {
                this.accessor.updateCosmetic(equipped, false);
            }
            if (cosmeticList.isEmpty()) {
                return true;
            }

            this.equipCosmetic(cosmeticList.getFirst());
            return true;
        }

        return false;
    }
}
