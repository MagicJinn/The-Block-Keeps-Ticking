package magicjinn.theblockkeepsticking.mixin;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
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
    private final static int MAX_AGE = 3;

    private static final Map<Integer, Block> indexToBlock = new HashMap<>();
    private static final Map<Block, Integer> blockToIndex = new HashMap<>();

    private static void initialize() {
        Block[] blocks = {Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD,
                Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER};
        for (int i = 0; i < blocks.length; i++) {
            indexToBlock.put(i, blocks[i]);
            blockToIndex.put(blocks[i], i);
        }
    }

    // This sucks
    private static Block getBlockByIndex(int index) {
        int clampedIndex = Math.max(0, Math.min(MAX_AGE, index));
        return indexToBlock.get(clampedIndex);
    }

    private static int getIndexByBlock(Block block) {
        int index = blockToIndex.getOrDefault(block, 0);
        return Math.max(0, Math.min(MAX_AGE, index));
    }

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        initialize();

        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world, 5) * 100;

        if (randomTicks <= 0)
            return false;

        // Shuffle the directions initially, instead of random pick each time
        List<Direction> directions = Arrays.asList(Direction.values());
        Collections.shuffle(directions);

        boolean[] isAmethystBlock = new boolean[directions.size()];

        // Check if ageing is required by checking if any of the blocks are not fully grown
        boolean ageingRequired = false;
        int[] states = new int[directions.size()];
        for (int i = 0; i < directions.size(); i++) {
            BlockPos blockPos = pos.offset(directions.get(i));
            BlockState blockState = world.getBlockState(blockPos);
            Block block = blockState.getBlock();

            for (int c = 0; c < MAX_AGE; c++) {
                if (block == getBlockByIndex(states[c])
                        || BuddingAmethystBlock.canGrowIn(blockState)) {
                    isAmethystBlock[i] = true;
                    break;
                }
            }

            int index = getIndexByBlock(block);
            if (index < MAX_AGE)
                ageingRequired = true;
            states[i] = index;
            isAmethystBlock[i] = true;
        }
        if (!ageingRequired)
            return false;

        for (int i = 0; i < randomTicks; i++) {
            // Check each direction repeat whether all blocks are fully grown
            // To prevent super long loops
            if (i % directions.size() == 0 && i != 0) {
                boolean allFullGrown = true;
                for (int j = 0; j < directions.size(); j++) {
                    if (states[j] < MAX_AGE && isAmethystBlock[j]) {
                        allFullGrown = false;
                        break;
                    }
                }
                // If earlier check didn't exit us, but now all blocks are fully grown,
                // we grew a bud
                if (allFullGrown)
                    break;
            }

            // Cycle through the directions
            int directionIndex = i % directions.size();
            Direction direction = directions.get(directionIndex);
            BlockPos blockPos = pos.offset(direction);
            BlockState blockState = world.getBlockState(blockPos);

            // Skip non amethyst cluster blocks
            // Skip fully grown clusters
            if (states[directionIndex] >= MAX_AGE || !isAmethystBlock[directionIndex])
                continue;

            if (BuddingAmethystBlock.canGrowIn(blockState) && states[directionIndex] == 0) {
                // Small bud
                states[directionIndex] = getIndexByBlock(Blocks.SMALL_AMETHYST_BUD);
                TheBlockKeepsTicking.LOGGER.info("Growing small bud at {}", blockPos);
            } else if (blockState.get(AmethystClusterBlock.FACING) == direction) {
                // Grow existing bud
                TheBlockKeepsTicking.LOGGER.info("Growing existing bud at {}", blockPos);
                states[directionIndex] = Math.min(states[directionIndex] + 1, MAX_AGE);
            }
        }

        for (int i = 0; i < directions.size(); i++) {
            Direction direction = directions.get(i);

            if (!isAmethystBlock[i])
                continue;

            Block block = getBlockByIndex(states[i]);
            BlockState blockState = world.getBlockState(pos.offset(direction));

            BlockState newState = (BlockState) block.getDefaultState()
                    .with(AmethystClusterBlock.FACING, direction)
                    .with(AmethystClusterBlock.WATERLOGGED,
                            blockState.getFluidState().getFluid() == Fluids.WATER);
            world.setBlockState(pos.offset(direction), newState);
        }

        return true;
    }
}
