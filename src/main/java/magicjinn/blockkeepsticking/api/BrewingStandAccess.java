package magicjinn.blockkeepsticking.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import magicjinn.blockkeepsticking.simulator.BrewingSimulator;

public interface BrewingStandAccess {
	BrewingSimulator createSimulator();
	void apply(World world, BlockPos pos, BlockState state, BrewingSimulator simulator);
}
