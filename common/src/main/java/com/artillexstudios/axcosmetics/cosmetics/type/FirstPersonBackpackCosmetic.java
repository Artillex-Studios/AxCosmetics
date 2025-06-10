package com.artillexstudios.axcosmetics.cosmetics.type;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.packetentity.PacketEntity;
import com.artillexstudios.axapi.packetentity.meta.entity.ArmorStandMeta;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.cosmetics.config.FirstPersonBackpackConfig;
import com.artillexstudios.axcosmetics.entitymeta.InteractionMeta;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

public final class FirstPersonBackpackCosmetic extends Cosmetic<FirstPersonBackpackConfig> {
    private final Location location = new Location(null, 0, 0, 0);
    private final Player player = this.user().onlinePlayer();
    private PacketEntity entity;
    private PacketEntity firstPersonEntity;
    private PacketEntity firstPersonInteractionEntity;

    public FirstPersonBackpackCosmetic(User user, CosmeticData data, FirstPersonBackpackConfig config) {
        super(user, data, config);
    }

    @Override
    public void spawn() {
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
        this.entity.hide(this.player);
        this.entity.spawn();
        this.entity.ride(this.player.getEntityId());

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
        this.firstPersonEntity.spawn();
        this.firstPersonEntity.setItem(EquipmentSlot.HELMET, this.config().firstPersonItemStack());
        this.firstPersonEntity.ride(this.firstPersonInteractionEntity.id());
    }

    @Override
    public void update() {
        this.player.getLocation(this.location);
        float yaw = this.location.getYaw();
        this.firstPersonEntity.rotate(yaw, 0);
        this.firstPersonEntity.rotateHead(yaw);

        this.entity.rotate(yaw, 0);
    }

    @Override
    public void despawn() {
        this.entity.remove();
        this.firstPersonEntity.remove();
        this.firstPersonInteractionEntity.remove();
    }

    @Override
    public Collection<CosmeticSlot> validSlots() {
        return List.of();
    }
}
