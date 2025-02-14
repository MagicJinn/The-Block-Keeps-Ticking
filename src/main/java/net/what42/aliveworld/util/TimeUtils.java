package net.what42.aliveworld.util;

public class TimeUtils {
    // Constantes de tiempo en ticks
    public static final int TICK_PER_SECOND = 20;
    public static final int TICKS_PER_MINUTE = TICK_PER_SECOND * 60;
    public static final int MIN_PROCESS_INTERVAL = TICK_PER_SECOND * 2; // 2 segundos
    public static final int FORCED_MIN_PROGRESS = TICK_PER_SECOND * 10; // 10 segundos

    public static long calculateTimePassed(long currentTime, long lastTime) {
        if (lastTime == -1L) {
            return FORCED_MIN_PROGRESS;
        }
        long timePassed = currentTime - lastTime;
        return Math.max(timePassed, FORCED_MIN_PROGRESS);
    }

    public static int calculateRequiredFuel(int ticksNeeded, int burnTimePerFuel) {
        return (ticksNeeded + burnTimePerFuel - 1) / burnTimePerFuel;
    }

    public static String formatTicksToTime(long ticks) {
        long seconds = ticks / TICK_PER_SECOND;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%dm %ds", minutes, seconds);
    }
}