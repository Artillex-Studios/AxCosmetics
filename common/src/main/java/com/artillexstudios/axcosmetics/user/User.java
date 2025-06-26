package com.artillexstudios.axcosmetics.user;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.config.CosmeticConfig;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.database.DatabaseAccessor;
import com.artillexstudios.axcosmetics.database.dto.UserDTO;
import org.apache.commons.lang3.function.TriFunction;
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
import java.util.concurrent.atomic.AtomicInteger;

public final class User implements com.artillexstudios.axcosmetics.api.user.User {
    // The cosmetics, which are equipped by the player.
    // These can be added by the API, thus not necessarily
    private final ConcurrentHashMap<CosmeticSlot, ConcurrentLinkedDeque<Cosmetic<?>>> equipped = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<CosmeticSlot, Cosmetic<?>> priorityEquipped = new ConcurrentHashMap<>();
    private final ConcurrentLinkedQueue<Cosmetic<?>> cosmetics = new ConcurrentLinkedQueue<>();
    private final ConcurrentHashMap<CosmeticSlot, AtomicInteger> slotCounters = new ConcurrentHashMap<>();
    private final int id;
    private final DatabaseAccessor accessor;
    private OfflinePlayer offlinePlayer;
    private Player onlinePlayer;

    // TODO: UserLoadEvent
    public User(int id, OfflinePlayer offlinePlayer, List<UserDTO> userDTOS, DatabaseAccessor accessor) {
        this.id = id;
        this.offlinePlayer = offlinePlayer;
        this.accessor = accessor;
        for (UserDTO userDTO : userDTOS) {
            if (userDTO.cosmeticTypeId() == null || userDTO.cosmeticId() == null) {
                continue;
            }

            Cosmetic<CosmeticConfig> cosmetic = AxCosmeticsAPI.instance().createCosmetic(this, userDTO.cosmeticTypeId(), new CosmeticData(userDTO.cosmeticId(), userDTO.counter(), userDTO.color(), userDTO.timeStamp()));
            if (cosmetic == null) {
                LogUtils.warn("Encountered unknown cosmetic with data: {}!", userDTO);
                continue;
            }

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

    public void player(OfflinePlayer offlinePlayer) {
        this.offlinePlayer = offlinePlayer;
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
            return (Cosmetic<T>) this.priorityEquipped.get(slot);
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
        this.cosmetics.remove(cosmetic);
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
        ConcurrentLinkedDeque<Cosmetic<?>> cosmeticList = this.equipped.get(cosmetic.slot());
        if (cosmeticList == null || cosmeticList.isEmpty()) {
            return false;
        }

        Cosmetic<?> equipped = cosmeticList.getFirst();
        if (equipped != null && !equipped.equals(cosmetic)) {
            return false;
        }

        cosmeticList.removeFirst();
        this.priorityEquipped.remove(cosmetic.slot());
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

    @Override
    public void hideSlot(CosmeticSlot slot) {
        this.slotCounters.computeIfAbsent(slot, val -> new AtomicInteger(0))
                .incrementAndGet();
        Cosmetic<?> remove = this.priorityEquipped.remove(slot);
        if (remove != null) {
            remove.despawn();
        }
    }

    @Override
    public void showSlot(CosmeticSlot slot) {
        AtomicInteger atomicInteger = this.slotCounters.get(slot);
        if (atomicInteger == null || atomicInteger.get() == 0) {
            LogUtils.warn("This slot is already shown!");
            return;
        }

        if (atomicInteger.decrementAndGet() == 0) {
            ConcurrentLinkedDeque<Cosmetic<?>> cosmetics = this.equipped.get(slot);
            if (cosmetics != null && !cosmetics.isEmpty()) {
                Cosmetic<?> first = cosmetics.getFirst();
                this.priorityEquipped.put(slot, first);
                first.spawn();
            }
        }
    }

    @Override
    public boolean isSlotHidden(CosmeticSlot slot) {
        AtomicInteger atomicInteger = this.slotCounters.get(slot);
        return atomicInteger != null && atomicInteger.get() > 0;
    }

    @Override
    public boolean has(CosmeticConfig config) {
        return this.cosmetics.stream()
                .anyMatch(cosmetic -> cosmetic.config().equals(config));
    }

    @Override
    public List<Cosmetic<?>> matching(CosmeticConfig config) {
        List<Cosmetic<?>> matching = new ArrayList<>();
        for (Cosmetic<?> cosmetic : this.cosmetics) {
            if (cosmetic.config().equals(config)) {
                matching.add(cosmetic);
            }
        }

        return matching;
    }

    @Override
    public synchronized void updatePermissionCosmetics() {
        Player player = this.onlinePlayer;
        if (player == null) {
            LogUtils.warn("Can't update permissions for a player who is not yet online!");
            return;
        }

        for (CosmeticConfig cosmeticConfig : AxCosmeticsAPI.instance().cosmeticConfigs().registered()) {
            if (cosmeticConfig.permission() == null) {
                if (Config.debug) {
                    LogUtils.debug("Cosmetic's permission is null!");
                }
                continue;
            }

            if (!player.hasPermission(cosmeticConfig.permission())) {
                if (Config.debug) {
                    LogUtils.debug("Has no permission!");
                }

                List<Cosmetic<?>> matching = this.matching(cosmeticConfig);
                if (matching.isEmpty()) {
                    if (Config.debug) {
                        LogUtils.debug("Has no matching");
                    }
                    continue;
                }

                // Remove cosmetics that the user doesn't have permissions for
                for (Cosmetic<?> cosmetic : matching) {
                    if (Config.debug) {
                        LogUtils.debug("Deleting cosmetic {}", cosmetic);
                    }
                    this.deleteCosmetic(cosmetic);
                }
                continue;
            }

            if (Config.debug) {
                LogUtils.debug("Has permission!");
            }
            if (this.has(cosmeticConfig)) {
                if (Config.debug) {
                    LogUtils.debug("Alread has cosmetic!");
                }
                continue;
            }

            TriFunction<com.artillexstudios.axcosmetics.api.user.User, CosmeticData, CosmeticConfig, Cosmetic<CosmeticConfig>> fetch1 = AxCosmeticsAPI.instance().cosmeticTypes().fetch(cosmeticConfig.type());
            if (fetch1 == null) {
                if (Config.debug) {
                    LogUtils.debug("Couldn't find cosmetic provider for type: {}!", cosmeticConfig.type());
                }
                continue;
            }

            if (Config.debug) {
                LogUtils.debug("Adding cosmetic!");
            }
            Cosmetic<?> cosmetic = fetch1.apply(this, new CosmeticData(0, 0, 0, System.currentTimeMillis()), cosmeticConfig);
            this.addCosmetic(cosmetic);
        }
    }

    @Override
    public String toString() {
        return "User{" +
                "equipped=" + equipped +
                ", priorityEquipped=" + priorityEquipped +
                ", cosmetics=" + cosmetics +
                ", slotCounters=" + slotCounters +
                ", id=" + id +
                ", accessor=" + accessor +
                ", offlinePlayer=" + offlinePlayer +
                ", onlinePlayer=" + onlinePlayer +
                '}';
    }
}
