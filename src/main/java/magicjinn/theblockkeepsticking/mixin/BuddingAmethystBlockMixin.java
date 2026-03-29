package magicjinn.theblockkeepsticking.mixin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.AmethystClusterTracker;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

@Mixin(BuddingAmethystBlock.class)
public class BuddingAmethystBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level,
                BuddingAmethystBlock.GROWTH_CHANCE);

        if (randomTicks <= 0)
            return false;

        AmethystClusterTracker.Initialize();

        List<Direction> directions = new ArrayList<>(Arrays.asList(Direction.values()));
        List<AmethystClusterTracker> amethystClusterTrackers = new ArrayList<>();

        for (int i = directions.size() - 1; i >= 0; i--) {
            Direction direction = directions.get(i);
            BlockState blockState = level.getBlockState(pos.relative(direction));
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

            setAmethistCluster(amethystClusterTracker.direction, amethystClusterTracker.age, level,
                    pos);
        }

        return true;
    }

    private static void setAmethistCluster(Direction direction, int state, Level level,
            BlockPos pos) {
        Block block = AmethystClusterTracker.getBlockByIndex(state);
        BlockState blockState = level.getBlockState(pos.relative(direction));
        boolean shouldBeWaterlogged = blockState.getFluidState().is(Fluids.WATER);

        BlockState newState = (BlockState) block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction)
                .setValue(AmethystClusterBlock.WATERLOGGED, shouldBeWaterlogged);
        level.setBlockAndUpdate(pos.relative(direction), newState);
    }
}
