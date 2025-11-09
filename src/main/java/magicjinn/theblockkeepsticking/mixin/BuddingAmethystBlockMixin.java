package magicjinn.theblockkeepsticking.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.AmethystClusterTracker;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.fluid.Fluids;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(BuddingAmethystBlock.class)
public class BuddingAmethystBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world, 5);

        if (randomTicks <= 0)
            return false;

        AmethystClusterTracker.Initialize();

        List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
        List<AmethystClusterTracker> amethystClusterTrackers = new ArrayList<>();

        for (int i = directions.size() - 1; i >= 0; i--) {
            Direction direction = directions.get(i);
            BlockState blockState = world.getBlockState(pos.offset(direction));
            Block block = blockState.getBlock();

            if (!AmethystClusterTracker.isOrCanBeAmethystCluster(block, blockState, direction)) {
                directions.remove(i);
            } else {
                int age = AmethystClusterTracker.getIndexByBlock(block);
                amethystClusterTrackers.add(new AmethystClusterTracker(direction, age));
            }
        }

        if (amethystClusterTrackers.isEmpty())
            return false;

        // Simulate wasted attempts by removing ticks for each direction that is not a bud
        randomTicks = randomTicks / directions.size() * amethystClusterTrackers.size();

        // Shuffle the amethyst cluster trackers to simulate random direction selection
        Collections.shuffle(amethystClusterTrackers);

        for (int i = 0; i < randomTicks; i++) {

            // Cycle through the directions
            int index = i % amethystClusterTrackers.size();
            AmethystClusterTracker amethystClusterTracker = amethystClusterTrackers.get(index);

            // Skip fully grown clusters
            if (amethystClusterTracker.isFullGrown())
                continue;

            if (amethystClusterTracker.age == -1) {
                // Small bud
                amethystClusterTracker.age =
                        AmethystClusterTracker.getIndexByBlock(Blocks.SMALL_AMETHYST_BUD);
            } else {
                // Grow existing bud
                amethystClusterTracker.age =
                        Math.min(amethystClusterTracker.age + 1, AmethystClusterTracker.MAX_AGE);
            }
        }

        for (int i = 0; i < amethystClusterTrackers.size(); i++) {
            AmethystClusterTracker amethystClusterTracker = amethystClusterTrackers.get(i);

            // Skip ungrown buds
            if (amethystClusterTracker.age == -1)
                continue;

            setAmethistCluster(amethystClusterTracker.direction, amethystClusterTracker.age, world,
                    pos);
        }

        return true;
    }

    private static void setAmethistCluster(Direction direction, int state, World world,
            BlockPos pos) {
        Block block = AmethystClusterTracker.getBlockByIndex(state);
        BlockState blockState = world.getBlockState(pos.offset(direction));

        BlockState newState = (BlockState) block.getDefaultState()
                .with(AmethystClusterBlock.FACING, direction).with(AmethystClusterBlock.WATERLOGGED,
                        blockState.getFluidState().getFluid() == Fluids.WATER);
        world.setBlockState(pos.offset(direction), newState);
    }
}
