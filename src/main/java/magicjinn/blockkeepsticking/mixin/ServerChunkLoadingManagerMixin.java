package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.manager.ChunkManager;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkLoadingManager.class)
public class ServerChunkLoadingManagerMixin {
	
	@Inject(method = "save(Z)V", at = @At("HEAD"))
	private void onSave(boolean flush, CallbackInfo ci) {
		if (flush) {
			@SuppressWarnings({ "resource", "unused" })
			ServerChunkLoadingManager storage = (ServerChunkLoadingManager) (Object) this;
			// Since we no longer have direct access to the chunk, the ChunkManager will
			// take care of processing the pending chunks
			ChunkManager.getInstance().processAllPendingChunks();
		}
	}
}
