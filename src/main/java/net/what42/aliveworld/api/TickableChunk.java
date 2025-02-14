package net.what42.aliveworld.api;

public interface TickableChunk {
    long getLastUpdateTime();
    void setLastUpdateTime(long time);
}