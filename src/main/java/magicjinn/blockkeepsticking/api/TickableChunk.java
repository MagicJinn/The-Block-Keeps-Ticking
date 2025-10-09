package magicjinn.blockkeepsticking.api;

public interface TickableChunk {
	long getLastUpdateTime();
	void setLastUpdateTime(long time);
}
