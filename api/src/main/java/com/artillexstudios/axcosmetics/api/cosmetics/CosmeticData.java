package com.artillexstudios.axcosmetics.api.cosmetics;

public record CosmeticData(int id, int counter, int color) {

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof CosmeticData that)) {
            return false;
        }

        return this.id() == that.id();
    }

    @Override
    public int hashCode() {
        return this.id();
    }
}
