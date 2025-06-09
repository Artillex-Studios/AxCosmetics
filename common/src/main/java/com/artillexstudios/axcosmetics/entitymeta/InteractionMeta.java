package com.artillexstudios.axcosmetics.entitymeta;

import com.artillexstudios.axapi.packetentity.meta.EntityMeta;
import com.artillexstudios.axapi.packetentity.meta.Metadata;
import com.artillexstudios.axapi.packetentity.meta.serializer.EntityDataAccessor;
import com.artillexstudios.axapi.packetentity.meta.serializer.EntityDataSerializers;

public class InteractionMeta extends EntityMeta {
    public static final EntityDataAccessor<Float> WIDTH = EntityDataSerializers.FLOAT.createAccessor(8);
    public static final EntityDataAccessor<Float> HEIGHT = EntityDataSerializers.FLOAT.createAccessor(9);
    public static final EntityDataAccessor<Boolean> RESPONSIVE = EntityDataSerializers.BOOLEAN.createAccessor(10);

    public InteractionMeta(Metadata metadata) {
        super(metadata);
    }

    public void width(float width) {
        this.metadata.set(InteractionMeta.WIDTH, width);
    }

    public float width() {
        return this.metadata.get(InteractionMeta.WIDTH);
    }

    public void height(float height) {
        this.metadata.set(InteractionMeta.HEIGHT, height);
    }

    public float height() {
        return this.metadata.get(InteractionMeta.HEIGHT);
    }

    public void responsive(boolean responsive) {
        this.metadata.set(InteractionMeta.RESPONSIVE, responsive);
    }

    public boolean responsive() {
        return this.metadata.get(InteractionMeta.RESPONSIVE);
    }

    @Override
    protected void defineDefaults() {
        this.metadata.define(InteractionMeta.WIDTH, 1.0f);
        this.metadata.define(InteractionMeta.HEIGHT, 1.0f);
        this.metadata.define(InteractionMeta.RESPONSIVE, false);
    }
}
