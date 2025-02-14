package net.what42.aliveworld.event;

import net.minecraft.world.chunk.WorldChunk;

public interface ChunkUnloadCallback {
    void onChunkUnload(WorldChunk chunk);
}
