package magicjinn.blockkeepsticking.event;

import net.minecraft.world.chunk.WorldChunk;
import java.util.ArrayList;
import java.util.List;

public class ChunkEvents {
	private static final List<ChunkUnloadCallback> UNLOAD_CALLBACKS = new ArrayList<>();
	
	public static void registerUnloadCallback(ChunkUnloadCallback callback) {
		UNLOAD_CALLBACKS.add(callback);
	}
	
	public static void onChunkUnload(WorldChunk chunk) {
		for (ChunkUnloadCallback callback : UNLOAD_CALLBACKS) {
			callback.onChunkUnload(chunk);
		}
	}
}
