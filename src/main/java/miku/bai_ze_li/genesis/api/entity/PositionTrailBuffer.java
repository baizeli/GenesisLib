package miku.bai_ze_li.genesis.api.entity;

import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class PositionTrailBuffer {
    private final List<Vec3> positions = new ArrayList<>();
    private final int maxPoints;

    public PositionTrailBuffer(int maxPoints) {
        this.maxPoints = Math.max(1, maxPoints);
    }

    public void record(Vec3 position) {
        record(position, 0.0D);
    }

    public void record(Vec3 position, double minDistanceSqr) {
        if (!positions.isEmpty() && position.distanceToSqr(positions.get(positions.size() - 1)) < minDistanceSqr) {
            return;
        }

        positions.add(position);
        trimTo(maxPoints);
    }

    public void trimTo(int targetSize) {
        int clampedSize = Math.max(0, Math.min(targetSize, maxPoints));
        while (positions.size() > clampedSize) {
            positions.remove(0);
        }
    }

    public void clear() {
        positions.clear();
    }

    public boolean isEmpty() {
        return positions.isEmpty();
    }

    public int size() {
        return positions.size();
    }

    public Vec3 last() {
        return positions.isEmpty() ? null : positions.get(positions.size() - 1);
    }

    public List<Vec3> snapshot() {
        return new ArrayList<>(positions);
    }

    public List<Vec3> renderSnapshot(Vec3 renderPosition, Vec3 motion) {
        List<Vec3> result = snapshot();
        if (result.isEmpty()) {
            result.add(renderPosition.subtract(motion.scale(2.0D)));
            result.add(renderPosition.subtract(motion));
            result.add(renderPosition);
            return result;
        }

        while (result.size() < 2) {
            result.add(0, result.get(0).subtract(motion));
        }

        Vec3 last = result.get(result.size() - 1);
        if (last.distanceToSqr(renderPosition) > 0.000001D) {
            result.add(renderPosition);
        }
        return result;
    }

    public List<Vec3> view() {
        return Collections.unmodifiableList(positions);
    }
}
