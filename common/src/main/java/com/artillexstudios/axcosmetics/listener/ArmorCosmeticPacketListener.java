package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axapi.packet.PacketEvent;
import com.artillexstudios.axapi.packet.PacketListener;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundAddEntityWrapper;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundSetEquipmentWrapper;
import com.artillexstudios.axapi.utils.EquipmentSlot;
import com.artillexstudios.axapi.utils.Pair;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.cosmetics.type.ArmorCosmetic;
import org.bukkit.entity.Player;

import java.util.List;

public class ArmorCosmeticPacketListener extends PacketListener {

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.type() == ClientboundPacketTypes.ADD_ENTITY) {
            ClientboundAddEntityWrapper wrapper = new ClientboundAddEntityWrapper(event);
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(wrapper.uuid());
            if (user == null) {
                return;
            }

            Player player = user.onlinePlayer();
            if (player == null) {
                return;
            }

            ArmorCosmetic.sendEquipmentPacket(player.getEntityId(), event.player());
        } else if (event.type() == ClientboundPacketTypes.SET_EQUIPMENT) {
            ClientboundSetEquipmentWrapper wrapper = new ClientboundSetEquipmentWrapper(event);
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(wrapper.entityId());
            if (user == null) {
                return;
            }

            List<Pair<EquipmentSlot, WrappedItemStack>> items = wrapper.items();
            for (Cosmetic<?> cosmetic : user.getEquippedCosmetics()) {
                if (!(cosmetic instanceof ArmorCosmetic armorCosmetic)) {
                    continue;
                }

                EquipmentSlot equipmentSlot = armorCosmetic.config().equipmentSlot();
                boolean found = false;
                for (int i = 0; i < items.size(); i++) {
                    Pair<EquipmentSlot, WrappedItemStack> pair = items.get(i);
                    if (pair.first() == equipmentSlot) {
                        items.set(i, Pair.of(equipmentSlot, armorCosmetic.config().itemStack()));
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    items.add(Pair.of(equipmentSlot, armorCosmetic.config().itemStack()));
                }
            }

            wrapper.markDirty();
        } else if (event.type() == ClientboundPacketTypes.CONTAINER_SET_SLOT) {
            // TODO: Optimize maybe
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.player().getUniqueId());
            if (user == null) {
                return;
            }

            if (user.getEquippedCosmetics().isEmpty()) {
                return;
            }

            Player player = user.onlinePlayer();
            if (player == null) {
                return;
            }

            ArmorCosmetic.sendEquipmentPacket(player.getEntityId(), event.player());
        }
    }
}
