package magicjinn.theblockkeepsticking.simulator;

import java.util.ArrayList;
import java.util.List;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.blocks.TickingAbstractFurnace;
import magicjinn.theblockkeepsticking.framework.TickingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;

public class WorldSimulator {
    // Simulate the world for the given chunk

    private static final List<Class<? extends TickingBlock>> tickingBlockClasses =
            new ArrayList<>();

    public static void Initialize() {
        RegisterTickingBlock(TickingAbstractFurnace.class);
        // RegisterTickingBlock(TickingCampfire.class);
        // RegisterTickingBlock(TickingBrewingStand.class);
        // RegisterTickingBlock(TickingCropBlock.class);
        //
    }

    public static void RegisterTickingBlock(Class<? extends TickingBlock> blockClass) {
        tickingBlockClasses.add(blockClass);
    }

    public static void SimulateWorld(WorldChunk chunk) {
        World world = chunk.getWorld();
        long currentWorldTime = world.getTime();
        long lastTickTime = chunk.getAttached(TheBlockKeepsTicking.LAST_UPDATE_TIME);
        long ticksToSimulate = currentWorldTime - lastTickTime;

        for (int sectionY = 0; sectionY < chunk.getHeight() >> 4; ++sectionY) {
            ChunkSection section = chunk.getSectionArray()[sectionY];
            if (section == null || section.isEmpty())
                continue;

            for (int x = 0; x < 16; x++) {
                for (int y = 0; y < 16; y++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        Block block = state.getBlock();
                        for (Class<? extends TickingBlock> tickingClass : tickingBlockClasses) {
                            if (tickingClass.isInstance(block)) {
                                TickingBlock tickingBlock;
                                try {
                                    tickingBlock = tickingClass.cast(block);
                                } catch (ClassCastException e) {
                                    TheBlockKeepsTicking.LOGGER.error(
                                            "Error casting block to TickingBlock: {}",
                                            e.getMessage());
                                    continue;
                                }
                                TheBlockKeepsTicking.LOGGER.info("Simulating block {} for {} ticks",
                                        block.getTranslationKey(), ticksToSimulate);
                                tickingBlock.Simulate(ticksToSimulate);
                                break; // Break to avoid multiple matches (which is impossible, so
                                       // this saves time)
                            }
                        }
                    }
                }
            }
        }
    }

    public static void forEachBlockInChunk(WorldChunk chunk, BiConsumer<BlockPos, Block> action) {}
    
}
