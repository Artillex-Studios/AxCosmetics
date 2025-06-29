package com.artillexstudios.axcosmetics.cosmetics.type;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axapi.packetentity.PacketEntity;
import com.artillexstudios.axapi.packetentity.meta.entity.AreaEffectCloudMeta;
import com.artillexstudios.axapi.packetentity.meta.entity.ArmorStandMeta;
import com.artillexstudios.axapi.particle.ParticleData;
import com.artillexstudios.axapi.particle.ParticleTypes;
import com.artillexstudios.axapi.particle.option.IntegerParticleOption;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticData;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlot;
import com.artillexstudios.axcosmetics.api.cosmetics.CosmeticSlots;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.cosmetics.config.FirstPersonBackpackConfig;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class LegacyFirstPersonBackpackCosmetic extends FirstPersonBackpackCosmetic {
    private final Location location = new Location(null, 0, 0, 0);
    private Player player;
    private PacketEntity entity;
    private PacketEntity firstPersonEntity;
    private PacketEntity firstPersonRiderEntity;
    private List<PacketEntity> heightEntities;
    private boolean equipped = false;
    private int tick = 0;

    public LegacyFirstPersonBackpackCosmetic(User user, CosmeticData data, FirstPersonBackpackConfig config) {
        super(user, data, config);
        if (Config.debug) {
            LogUtils.debug("Created legacy first person backpack cosmetic for user {} with data: {}", user, data);
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

        // Summon potions
        int rounded = (int) Math.round(this.config().height());
        this.heightEntities = new ArrayList<>(rounded);
        for (int i = 0; i < rounded; i++) {
            PacketEntity packetEntity = NMSHandlers.getNmsHandler().createEntity(EntityType.AREA_EFFECT_CLOUD, this.location);
            AreaEffectCloudMeta areaEffectCloudMeta = (AreaEffectCloudMeta) packetEntity.meta();
            areaEffectCloudMeta.point(true);
            areaEffectCloudMeta.invisible(true);
            areaEffectCloudMeta.radius(0.0f);
            areaEffectCloudMeta.particle(new ParticleData<>(ParticleTypes.BLOCK, new IntegerParticleOption(0)));
            packetEntity.setVisibleByDefault(false);
            packetEntity.show(this.player);
            packetEntity.spawn();

            if (i == 0) {
                this.firstPersonRiderEntity = packetEntity;
                packetEntity.ride(this.player.getEntityId());
                continue;
            }

            this.heightEntities.add(packetEntity);
            if (i == 1) {
                packetEntity.ride(this.firstPersonRiderEntity.id());
            } else {
                packetEntity.ride(this.heightEntities.get(i - 2).id());
            }
        }

        this.firstPersonEntity = NMSHandlers.getNmsHandler().createEntity(EntityType.ARMOR_STAND, this.location);
        ArmorStandMeta otherMeta = (ArmorStandMeta) this.firstPersonEntity.meta();
        otherMeta.invisible(true);
        otherMeta.marker(true);
        this.firstPersonEntity.setVisibleByDefault(false);
        this.firstPersonEntity.show(this.player);
        this.firstPersonEntity.setItem(EquipmentSlot.HELMET, this.config().firstPersonItemStack());
        this.firstPersonEntity.spawn();
        this.firstPersonEntity.ride(this.heightEntities.getLast().id());
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
        this.firstPersonEntity.rotate(yaw, 0);
        this.firstPersonEntity.rotateHead(yaw);

        this.entity.rotate(yaw, 0);
        if (this.tick % 10 == 0) {
            this.entity.rotateHead(yaw);
        }
        if (Config.forceRidePackets) {
            this.firstPersonRiderEntity.ride(this.player.getEntityId());
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
        for (PacketEntity heightEntity : this.heightEntities) {
            heightEntity.remove();
        }
        this.heightEntities.clear();
        this.heightEntities = null;
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
    public Integer firstPersonRiderId() {
        if (!this.equipped) {
            return null;
        }

        return this.firstPersonRiderEntity.id();
    }

    @Override
    public Collection<CosmeticSlot> validSlots() {
        return List.of(CosmeticSlots.BACKPACK);
    }
}
