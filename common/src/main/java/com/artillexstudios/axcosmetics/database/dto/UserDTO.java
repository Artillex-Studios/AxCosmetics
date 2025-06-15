package com.artillexstudios.axcosmetics.database.dto;

import java.util.UUID;

public record UserDTO(int userId, String name, UUID uuid, int cosmeticId, int cosmeticTypeId, int counter, int color, boolean equipped) {
}
