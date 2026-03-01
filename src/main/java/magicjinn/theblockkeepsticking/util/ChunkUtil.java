package magicjinn.theblockkeepsticking.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.chunk.WorldChunk;

public final class ChunkUtil {

	private ChunkUtil() {
	}

	/**
	 * Returns the list of chunks that are currently loaded in the given world.
	 * 
	 * @param world the server world (e.g. overworld)
	 * @return list of block-ticking {@link WorldChunk}s, or empty list if the chunk
	 *         manager is not a {@link ServerChunkManager}
	 */
	public static List<WorldChunk> getLoadedChunks(ServerWorld world) {
		Benchmarker.StartBenchmark("getLoadedChunks");
		try {
			if (!(world.getChunkManager() instanceof ServerChunkManager manager)) {
				return Collections.emptyList();
			}
			List<WorldChunk> list = new ArrayList<>();
			manager.chunkLoadingManager.forEachBlockTickingChunk(list::add);
			return list;
		} finally {
			Benchmarker.EndBenchmark("getLoadedChunks");
		}
	}
}
