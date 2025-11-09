package magicjinn.theblockkeepsticking.util;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.block.AmethystClusterBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BuddingAmethystBlock;
import net.minecraft.util.math.Direction;

public class AmethystClusterTracker {
    public static final int MAX_AGE = 3;
    public int age;
    public Direction direction;

    private static final Map<Integer, Block> indexToBlock = new HashMap<>();
    private static final Map<Block, Integer> blockToIndex = new HashMap<>();

    public AmethystClusterTracker(Direction direction, int age) {
        this.age = age;
        this.direction = direction;
    }

    public static void Initialize() {
        Block[] blocks = {Blocks.SMALL_AMETHYST_BUD, Blocks.MEDIUM_AMETHYST_BUD,
                Blocks.LARGE_AMETHYST_BUD, Blocks.AMETHYST_CLUSTER};
        for (int i = 0; i < blocks.length; i++) {
            indexToBlock.put(i, blocks[i]);
            blockToIndex.put(blocks[i], i);
        }
    }

    // This sucks
    public static Block getBlockByIndex(int index) {
        int clampedIndex = Math.max(-1, Math.min(MAX_AGE, index));
        return indexToBlock.get(clampedIndex);
    }

    public static int getIndexByBlock(Block block) {
        int index = blockToIndex.getOrDefault(block, -1);
        return Math.max(-1, Math.min(MAX_AGE, index));
    }

    public boolean isFullGrown() {
        return isFullGrown(age);
    }

    public static boolean isFullGrown(int age) {
        return age >= MAX_AGE;
    }

    public static boolean isFullGrown(Block block) {
        return isFullGrown(getIndexByBlock(block));
    }

    public static boolean isOrCanBeAmethystCluster(Block block, BlockState blockState,
            Direction direction) {
        return (((block instanceof AmethystClusterBlock)
                && blockState.get(AmethystClusterBlock.FACING) == direction && !isFullGrown(block)))
                || BuddingAmethystBlock.canGrowIn(blockState);
    }
}

