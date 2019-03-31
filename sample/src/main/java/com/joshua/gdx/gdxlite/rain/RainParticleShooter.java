package com.joshua.gdx.gdxlite.rain;

import com.joshua.gdx.gdxlite.math.Vector3;

import java.util.Random;

public class RainParticleShooter {
    private final static float SPEED_VARIANCE = 0.2f;

    private final Random random = new Random();

    private final Vector3 mCenter;
    private final float mShiftX, mShiftZ;
    private final float mSpeed;

    public RainParticleShooter(Vector3 center, float shiftX, float shiftZ, float speed) {
        this.mCenter = center;
        mShiftX = shiftX;
        mShiftZ = shiftZ;
        mSpeed = speed;
    }

    public void addParticles(RainParticlesSystem system, float currentTime, int count) {
        for (int i = 0; i < count; i++) {
            float speed = mSpeed * (1f + SPEED_VARIANCE * random.nextFloat());
            float x = mCenter.x + mShiftX * (random.nextFloat() * 2 - 1);
            float z = mCenter.z + mShiftZ * (random.nextFloat() * 2 - 1);
            system.addParticle(new Vector3(x, mCenter.y, z), speed, currentTime, 0);
        }
    }
}
