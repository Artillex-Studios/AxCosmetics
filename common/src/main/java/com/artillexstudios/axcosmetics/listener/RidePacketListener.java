package com.artillexstudios.axcosmetics.listener;

import com.artillexstudios.axapi.packet.ClientboundPacketTypes;
import com.artillexstudios.axapi.packet.PacketEvent;
import com.artillexstudios.axapi.packet.PacketListener;
import com.artillexstudios.axapi.packet.wrapper.clientbound.ClientboundSetPassengersWrapper;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcosmetics.api.AxCosmeticsAPI;
import com.artillexstudios.axcosmetics.api.cosmetics.Cosmetic;
import com.artillexstudios.axcosmetics.api.user.User;
import com.artillexstudios.axcosmetics.config.Config;
import com.artillexstudios.axcosmetics.cosmetics.type.FirstPersonBackpackCosmetic;

public final class RidePacketListener extends PacketListener {

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.type() == ClientboundPacketTypes.SET_PASSENGERS) {
            ClientboundSetPassengersWrapper wrapper = new ClientboundSetPassengersWrapper(event);
            User receiver = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.player());
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(wrapper.vehicleId());
            if (user == null || receiver == null) {
                if (Config.debug) {
                    LogUtils.debug("Passenger packet, receiver: {}, user: {}", receiver, user);
                }
                return;
            }

            if (Config.debug) {
                LogUtils.debug("Passenger packet fixing, receiver: {}, user: {}", receiver, user);
            }
            for (Cosmetic<?> equippedCosmetic : user.getEquippedCosmetics()) {
                if (!(equippedCosmetic instanceof FirstPersonBackpackCosmetic cosmetic)) {
                    continue;
                }

                if (receiver == user) {
                    // First person cosmetic
                    if (!wrapper.passengers().contains(cosmetic.interactionEntityId())) {
                        if (Config.debug) {
                            LogUtils.debug("Fixing passengers for user: {}, vehicle: {}, passengers: {}", receiver, user, wrapper.passengers());
                        }

                        wrapper.passengers().add(cosmetic.interactionEntityId());
                    } else if (Config.debug) {
                        LogUtils.debug("No need to fix, the entity is already there!");
                    }
                } else {
                    // Sending to someone else, so not first person
                    if (!wrapper.passengers().contains(cosmetic.entityId())) {
                        if (Config.debug) {
                            LogUtils.debug("Fixing passengers for user: {}, vehicle: {}, passengers: {}", receiver, user, wrapper.passengers());
                        }

                        wrapper.passengers().add(cosmetic.entityId());
                    } else if (Config.debug) {
                        LogUtils.debug("No need to fix, the entity is already there!");
                    }
                }
            }

            wrapper.markDirty();
        }
    }
}
