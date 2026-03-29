package magicjinn.theblockkeepsticking.util;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public final class ChunkUtil {

	private ChunkUtil() {
	}

	/**
	 * Returns the list of chunks that are currently block-ticking in the given
	 * level.
	 *
	 * @param level the server world (e.g. overworld)
	 * @return list of block-ticking {@link LevelChunk}s
	 */
	public static List<LevelChunk> getLoadedChunks(ServerLevel level) {
		Benchmarker.StartBenchmark("getLoadedChunks");
		try {
			ServerChunkCache chunkSource = level.getChunkSource();
			List<LevelChunk> list = new ArrayList<>();
			chunkSource.chunkMap.forEachBlockTickingChunk(list::add);
			return list;
		} finally {
			Benchmarker.EndBenchmark("getLoadedChunks");
		}
	}
}
