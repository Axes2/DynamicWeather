package com.dynamicweather.client;

import net.minecraft.world.phys.Vec3;

public class RainDrop {
    public Vec3 position;
    public Vec3 velocity;
    public float age;
    public final float maxAge;

    public RainDrop(Vec3 position, Vec3 velocity, float maxAge) {
        this.position = position;
        this.velocity = velocity;
        this.age = 0;
        this.maxAge = maxAge;
    }

    public void tick() {
        position = position.add(velocity);
        age++;
    }

    public boolean isDead() {
        return age >= maxAge;
    }

    public float getAlpha() {
        float fadeStart = maxAge * 0.7f;
        if (age < fadeStart) return 1f;
        return Math.max(0f, (maxAge - age) / (maxAge - fadeStart));
    }
}
