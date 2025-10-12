package magicjinn.blockkeepsticking.mixin;

import magicjinn.blockkeepsticking.api.TickableChunk;
import magicjinn.blockkeepsticking.simulator.ChunkLifeSimulator;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Chunk.class)
public class ChunkAccessMixin implements TickableChunk {
	private long lastUpdateTime = -1L;

	@Inject(method = "increaseInhabitedTime", at = @At("HEAD"))
	public void onIncreaseInhabitedTime(long time, CallbackInfo ci) {
		if ((Object) this instanceof WorldChunk chunk) {
			World world = chunk.getWorld();

			// Actualizar cuando el chunk se carga
			long currentTime = world.getTime();
			if (this.lastUpdateTime == -1L) {
				this.lastUpdateTime = currentTime;
				return;
			}

			// Calcular tiempo transcurrido desde la última actualización
			long timePassed = currentTime - this.lastUpdateTime;

			// Solo procesar si hay algo para actualizar
			if (timePassed > 0 && hasUpdatableContent(chunk)) {
				ChunkLifeSimulator.simulate(chunk, timePassed);
			}

			this.lastUpdateTime = currentTime;
		}
	}

	private boolean hasUpdatableContent(WorldChunk chunk) {
		// Check specific block entities that we are interested in
		for (var entry : chunk.getBlockEntities().entrySet()) {
			BlockEntity blockEntity = entry.getValue();
			if (blockEntity instanceof net.minecraft.block.entity.AbstractFurnaceBlockEntity
					|| blockEntity instanceof net.minecraft.block.entity.BrewingStandBlockEntity
					|| blockEntity instanceof net.minecraft.block.entity.CampfireBlockEntity) {
				return true;
			}
		}

		// Check entities only if necessary
		ChunkPos pos = chunk.getPos();
		Box chunkBox = new Box(
				pos.getStartX(), chunk.getWorld().getBottomY(), pos.getStartZ(),
				pos.getEndX() + 1, chunk.getWorld().getHeight(), pos.getEndZ() + 1);

		if (!chunk.getWorld().getEntitiesByClass(net.minecraft.entity.Entity.class, chunkBox, e -> true).isEmpty()) {
			return true;
		}

		// Quickly check if there are sections with blocks
		for (var section : chunk.getSectionArray()) {
			if (section != null && !section.isEmpty()) {
				return true;
			}
		}

		return false;
	}

	@Override
	public long getLastUpdateTime() {
		return this.lastUpdateTime;
	}

	@Override
	public void setLastUpdateTime(long time) {
		this.lastUpdateTime = time;
	}
}
