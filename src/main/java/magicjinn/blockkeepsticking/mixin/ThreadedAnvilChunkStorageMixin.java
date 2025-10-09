package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.manager.ChunkManager;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThreadedAnvilChunkStorage.class)
public class ThreadedAnvilChunkStorageMixin {
	
	@Inject(method = "save(Z)V", at = @At("HEAD"))
	private void onSave(boolean flush, CallbackInfo ci) {
		if (flush) {
			@SuppressWarnings({ "resource", "unused" })
			ThreadedAnvilChunkStorage storage = (ThreadedAnvilChunkStorage)(Object)this;
			// Como ya no tenemos acceso directo al chunk, el ChunkManager se encargará
			// de procesar los chunks pendientes
			ChunkManager.getInstance().processAllPendingChunks();
		}
	}
}
