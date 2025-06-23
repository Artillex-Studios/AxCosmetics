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
import com.artillexstudios.axcosmetics.cosmetics.config.FirstPersonBackpackConfig;
import com.artillexstudios.axcosmetics.entitymeta.InteractionMeta;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class FirstPersonBackpackCosmetic extends Cosmetic<FirstPersonBackpackConfig> {
    private final Location location = new Location(null, 0, 0, 0);
    private Player player;
    private PacketEntity entity;
    private PacketEntity firstPersonEntity;
    private PacketEntity firstPersonInteractionEntity;
    private boolean equipped = false;

    public FirstPersonBackpackCosmetic(User user, CosmeticData data, FirstPersonBackpackConfig config) {
        super(user, data, config);
        if (Config.debug) {
            LogUtils.debug("Created first person backpack cosmetic for user {} with data: {}", user, data);
        }
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
        this.entity.setItem(EquipmentSlot.HELMET, this.config().itemStack(this.data()));
        ArmorStandMeta meta = (ArmorStandMeta) this.entity.meta();
        meta.invisible(true);
        meta.marker(true);
        this.entity.hide(this.player);
        this.entity.spawn();
        this.entity.ride(this.player.getEntityId());
        this.entity.rotateHead(this.location.getYaw());

        this.firstPersonInteractionEntity = NMSHandlers.getNmsHandler().createEntity(EntityType.INTERACTION, this.location);
        InteractionMeta interactionMeta = (InteractionMeta) this.firstPersonInteractionEntity.meta();
        interactionMeta.height((float) this.config().height());
        this.firstPersonInteractionEntity.setVisibleByDefault(false);
        this.firstPersonInteractionEntity.show(this.player);
        this.firstPersonInteractionEntity.spawn();
        this.firstPersonInteractionEntity.ride(this.player.getEntityId());

        this.firstPersonEntity = NMSHandlers.getNmsHandler().createEntity(EntityType.ARMOR_STAND, this.location);
        ArmorStandMeta otherMeta = (ArmorStandMeta) this.firstPersonEntity.meta();
        otherMeta.invisible(true);
        otherMeta.marker(true);
        this.firstPersonEntity.setVisibleByDefault(false);
        this.firstPersonEntity.show(this.player);
        this.firstPersonEntity.setItem(EquipmentSlot.HELMET, this.config().firstPersonItemStack());
        this.firstPersonEntity.spawn();
        this.firstPersonEntity.ride(this.firstPersonInteractionEntity.id());
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

        this.player.getLocation(this.location);
        float yaw = this.location.getYaw();
        this.firstPersonEntity.rotate(yaw, 0);
        this.firstPersonEntity.rotateHead(yaw);

        // TODO: Do we need to rotate the head?
        this.entity.rotate(yaw, 0);
        if (Config.forceRidePackets) {
            this.firstPersonEntity.ride(this.firstPersonInteractionEntity.id());
            this.firstPersonInteractionEntity.ride(this.player.getEntityId());
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
        this.firstPersonEntity.remove();
        this.firstPersonInteractionEntity.remove();
        this.player = null;
    }

    public Integer entityId() {
        if (!this.equipped) {
            return null;
        }

        return this.entity.id();
    }

    public Integer interactionEntityId() {
        if (!this.equipped) {
            return null;
        }

        return this.firstPersonInteractionEntity.id();
    }

    @Override
    public Collection<CosmeticSlot> validSlots() {
        return List.of(CosmeticSlots.BACKPACK);
    }
}
