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
import org.apache.commons.lang3.ArrayUtils;

public final class RidePacketListener extends PacketListener {

    @Override
    public void onPacketSending(PacketEvent event) {
        if (event.type() == ClientboundPacketTypes.SET_PASSENGERS) {
            User receiver = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(event.player());
            if (receiver == null) {
                if (Config.debug) {
                    LogUtils.debug("Passenger packet, receiver null!");
                }
                return;
            }

            ClientboundSetPassengersWrapper wrapper = new ClientboundSetPassengersWrapper(event);
            User user = AxCosmeticsAPI.instance().getUserIfLoadedImmediately(wrapper.vehicleId());
            if (user == null) {
                if (Config.debug) {
                    LogUtils.debug("Passenger packet, user null!");
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
                    Integer interactionEntityId = cosmetic.firstPersonRiderId();
                    if (interactionEntityId == null) {
                        continue;
                    }

                    if (!ArrayUtils.contains(wrapper.passengers(), interactionEntityId)) {
                        if (Config.debug) {
                            LogUtils.debug("Fixing passengers for user: {}, vehicle: {}, passengers: {}", receiver, user, wrapper.passengers());
                        }

                        wrapper.passengers(ArrayUtils.add(wrapper.passengers(), interactionEntityId));
                    } else if (Config.debug) {
                        LogUtils.debug("No need to fix, the entity is already there!");
                    }
                } else {
                    Integer entityId = cosmetic.entityId();
                    if (entityId == null) {
                        continue;
                    }

                    // Sending to someone else, so not first person
                    if (!ArrayUtils.contains(wrapper.passengers(), entityId)) {
                        if (Config.debug) {
                            LogUtils.debug("Fixing passengers for user: {}, vehicle: {}, passengers: {}", receiver, user, wrapper.passengers());
                        }

                        wrapper.passengers(ArrayUtils.add(wrapper.passengers(), entityId));
                    } else if (Config.debug) {
                        LogUtils.debug("No need to fix, the entity is already there!");
                    }
                }
            }
        }
    }
}
