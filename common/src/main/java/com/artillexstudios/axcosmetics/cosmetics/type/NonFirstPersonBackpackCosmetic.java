package com.artillexstudios.axcosmetics.cosmetics.type;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.packetentity.PacketEntity;
import com.artillexstudios.axapi.packetentity.meta.entity.ArmorStandMeta;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.cosmetics.config.BackpackConfig;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class NonFirstPersonBackpackCosmetic extends Cosmetic<BackpackConfig> implements BackpackCosmetic {
    private final Location location = new Location(null, 0, 0, 0);
    private Player player;
    private PacketEntity entity;
    private boolean equipped = false;
    private int tick = 0;

    public NonFirstPersonBackpackCosmetic(User user, CosmeticData data, BackpackConfig config) {
        super(user, data, config);
    }

    @Override
    public void spawn() {
        if (Config.debug) {
            LogUtils.debug("Backpack equip!");
        }
        this.player = this.user().onlinePlayer();
        if (this.player == null) {
            throw new IllegalStateException();
        }

        // Update the location
        this.player.getLocation(this.location);

        this.entity = NMSHandlers.getNmsHandler().createEntity(EntityType.ARMOR_STAND, this.location);
        this.entity.setItem(EquipmentSlot.HELMET, this.config().itemStack());
        ArmorStandMeta meta = (ArmorStandMeta) this.entity.meta();
        meta.invisible(true);
        meta.marker(true);
        this.entity.spawn();
        this.entity.ride(this.player.getEntityId());
        this.entity.rotateHead(this.location.getYaw());
        this.equipped = true;
    }

    @Override
    public void update() {
        if (!this.equipped) {
            return;
        }

        if (this.player == null) {
            LogUtils.debug("Attempted to tick null player {}!", this.user().player().getName());
            return;
        }
        this.tick++;

        this.player.getLocation(this.location);
        float yaw = this.location.getYaw();

        this.entity.rotateHead(yaw);
        if (Config.forceRidePackets) {
            this.entity.ride(this.player.getEntityId());
        }
    }

    @Override
    public void despawn() {
        if (Config.debug) {
            LogUtils.debug("Backpack unequip!");
        }

        if (!this.equipped) {
            return;
        }

        this.equipped = false;
        this.entity.remove();
        this.player = null;
        this.tick = 0;
    }

    @Override
    public Integer entityId() {
        if (!this.equipped) {
            return null;
        }

        return this.entity.id();
    }

    @Override
    public Collection<CosmeticSlot> validSlots() {
        return List.of(CosmeticSlots.BACKPACK);
    }
}
