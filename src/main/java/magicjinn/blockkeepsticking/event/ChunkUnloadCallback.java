package magicjinn.blockkeepsticking.event;

import net.minecraft.world.chunk.WorldChunk;

public interface ChunkUnloadCallback {
	void onChunkUnload(WorldChunk chunk);
}
