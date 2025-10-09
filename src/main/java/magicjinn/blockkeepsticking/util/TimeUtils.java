package magicjinn.blockkeepsticking.util;

public class TimeUtils {
	// Constantes de tiempo en ticks
	public static final int TICKS_PER_SECOND = 20;
	public static final int TICKS_PER_MINUTE = TICKS_PER_SECOND * 60;
	public static final int MIN_PROCESS_INTERVAL = TICKS_PER_SECOND * 2;
	public static final int FORCED_MIN_PROGRESS = TICKS_PER_SECOND * 10;
	
	public static long calculateTimePassed(long currentTime, long lastTime) {
		if (lastTime == -1L) {
			return FORCED_MIN_PROGRESS;
		}
		long timePassed = currentTime - lastTime;
		return Math.max(timePassed, FORCED_MIN_PROGRESS);
	}
}
