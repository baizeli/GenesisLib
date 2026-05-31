package miku.bai_ze_li.genesis.core.api.time;

public final class TickCooldown {
    private final int durationTicks;
    private int lastUseTick;

    public TickCooldown(int durationTicks) {
        this.durationTicks = Math.max(0, durationTicks);
        this.lastUseTick = -this.durationTicks;
    }

    public boolean isReady(int currentTick) {
        return currentTick - lastUseTick >= durationTicks;
    }

    public boolean isCoolingDown(int currentTick) {
        return !isReady(currentTick);
    }

    public int remainingTicks(int currentTick) {
        return Math.max(0, durationTicks - (currentTick - lastUseTick));
    }

    public void trigger(int currentTick) {
        this.lastUseTick = currentTick;
    }

    public void reset() {
        this.lastUseTick = -this.durationTicks;
    }
}
