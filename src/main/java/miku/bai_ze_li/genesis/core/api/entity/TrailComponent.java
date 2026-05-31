package miku.bai_ze_li.genesis.core.api.entity;

import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class TrailComponent {
    private final Vec3[][] trailPositions;
    private final int maxLength;
    private int trailPointer = 0;
    private int currentSize = 0;
    private boolean hasTrail = false;

    public TrailComponent(int maxLength) {
        this.maxLength = maxLength;
        this.trailPositions = new Vec3[maxLength][2];
    }

    public void updateTrail(Vec3 startPos, Vec3 endPos) {
        if (!hasTrail) {
            this.currentSize = 0;
            this.trailPointer = 0;
            this.hasTrail = true;
        }

        this.trailPositions[this.trailPointer] = new Vec3[]{startPos, endPos};
        this.trailPointer = (this.trailPointer + 1) % this.maxLength;

        if (this.currentSize < this.maxLength) {
            this.currentSize++;
        }
    }

    @Nullable
    public Vec3[] getTrailPosition(int pointer, float partialTick) {
        if (!hasTrail || currentSize < 2 || pointer >= currentSize - 1) {
            return null;
        }

        int latestIdx = (this.trailPointer - 1 + this.maxLength) % this.maxLength;
        int i = (latestIdx - pointer + this.maxLength) % this.maxLength;
        int j = (latestIdx - pointer - 1 + this.maxLength) % this.maxLength;

        Vec3[] currFrame = trailPositions[i];
        Vec3[] prevFrame = trailPositions[j];
        if (currFrame == null || prevFrame == null) {
            return null;
        }

        Vec3 interpolatedStart = prevFrame[0].lerp(currFrame[0], partialTick);
        Vec3 interpolatedEnd = prevFrame[1].lerp(currFrame[1], partialTick);
        return new Vec3[]{interpolatedStart, interpolatedEnd};
    }

    public boolean hasTrail() {
        return hasTrail;
    }

    public void setHasTrail(boolean hasTrail) {
        if (this.hasTrail != hasTrail) {
            this.hasTrail = hasTrail;
            if (!hasTrail) {
                this.currentSize = 0;
                this.trailPointer = 0;
            }
        }
    }

    public int getMaxLength() {
        return maxLength;
    }

    public int getAvailableSegments() {
        return Math.max(0, currentSize - 1);
    }
}
