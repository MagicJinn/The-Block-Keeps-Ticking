package magicjinn.blockkeepsticking.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import magicjinn.blockkeepsticking.simulator.BrewingSimulator;

public interface BrewingStandAccess {
	BrewingSimulator createSimulator(World world); // We now pass World to the simulator
	void apply(World world, BlockPos pos, BlockState state, BrewingSimulator simulator);
}