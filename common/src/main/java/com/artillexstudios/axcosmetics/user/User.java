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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class User implements com.artillexstudios.axcosmetics.api.user.User {
    private final ConcurrentHashMap<CosmeticSlot, ConcurrentLinkedDeque<Cosmetic<?>>> equipped = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CosmeticSlot, Cosmetic<?>> priorityEquipped = new ConcurrentHashMap<>();
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
            Cosmetic<CosmeticConfig> cosmetic = AxCosmeticsAPI.instance().createCosmetic(this, userDTO.cosmeticTypeId(), new CosmeticData(userDTO.cosmeticId(), userDTO.counter(), userDTO.color(), userDTO.timeStamp()));
            this.cosmetics.add(cosmetic);
            if (userDTO.equipped()) {
                this.priorityEquipped.put(cosmetic.slot(), cosmetic);
                this.equipped.put(cosmetic.slot(), new ConcurrentLinkedDeque<>(List.of(cosmetic)));
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
    public @Nullable <T extends CosmeticConfig> Cosmetic<T> getCosmetic(CosmeticSlot slot, boolean owned) {
        if (owned) {
            return (Cosmetic<T>) this.equipped.get(slot).stream()
                    .filter(cosmetic -> this.cosmetics.contains(cosmetic))
                    .findFirst()
                    .orElse(null);
        } else {
            return (Cosmetic<T>) this.equipped.get(slot).peekFirst();
        }
    }

    @Override
    public <T extends CosmeticConfig> CompletableFuture<?> addCosmetic(Cosmetic<T> cosmetic) {
        return this.accessor.insertCosmetic(this, cosmetic).thenRun(() -> {
            this.cosmetics.add(cosmetic);
        });
    }

    @Override
    public <T extends CosmeticConfig> CompletableFuture<?> deleteCosmetic(Cosmetic<T> cosmetic) {
        this.unequipCosmetic(cosmetic);
        return this.accessor.deleteCosmetic(cosmetic);
    }

    @Override
    public void updateCosmetic(CosmeticSlot slot) {
        ConcurrentLinkedDeque<Cosmetic<?>> cosmeticList = this.equipped.get(slot);
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
    public Collection<Cosmetic<?>> getEquippedCosmetics() {
        return this.priorityEquipped.values();
    }

    @Override
    public void equipCosmetic(Cosmetic<?> cosmetic) {
        boolean contains = this.cosmetics.contains(cosmetic);
        ConcurrentLinkedDeque<Cosmetic<?>> cosmeticList = this.equipped.computeIfAbsent(cosmetic.slot(), o -> new ConcurrentLinkedDeque<>());
        if (!cosmeticList.isEmpty()) {
            Cosmetic<?> equipped = cosmeticList.getFirst();

            if (contains && this.cosmetics.contains(equipped)) {
                cosmeticList.remove(equipped);
                this.priorityEquipped.remove(cosmetic.slot());
                this.accessor.updateCosmetic(equipped, false);
            }

            // Despawn after remove to prevent race conditions
            equipped.despawn();
        }

        cosmeticList.addFirst(cosmetic);
        cosmetic.spawn();
        this.priorityEquipped.put(cosmetic.slot(), cosmetic);
        if (contains) {
            this.accessor.updateCosmetic(cosmetic, true);
        }
    }

    @Override
    public boolean unequipCosmetic(Cosmetic<?> cosmetic) {
        return this.unequipCosmetic(cosmetic.slot());
    }

    @Override
    public boolean unequipCosmetic(CosmeticSlot slot) {
        ConcurrentLinkedDeque<Cosmetic<?>> cosmeticList = this.equipped.get(slot);
        if (cosmeticList == null || cosmeticList.isEmpty()) {
            return false;
        }

        Cosmetic<?> equipped = cosmeticList.removeFirst();
        this.priorityEquipped.remove(slot);
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

    @Override
    public <T extends CosmeticConfig> boolean isEquipped(Cosmetic<T> cosmetic) {
        return this.priorityEquipped.containsValue(cosmetic);
    }
}
