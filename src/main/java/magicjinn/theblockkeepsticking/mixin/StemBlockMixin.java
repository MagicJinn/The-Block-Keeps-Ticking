package magicjinn.theblockkeepsticking.mixin;

import java.util.Optional;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.TheBlockKeepsTicking;
import magicjinn.theblockkeepsticking.accessors.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Type;
import net.minecraft.world.World;

@Mixin(StemBlock.class)
public class StemBlockMixin implements TickingBlockAccessor {

    @Shadow @Final private RegistryKey<Block> gourdBlock;
    @Shadow @Final private RegistryKey<Block> attachedStemBlock;

    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        StemBlock stem = (StemBlock) (Object) this;

        int randomTicks = TickingBlock.CropGrowthAmount(ticksToSimulate, stem, world, state, pos);
        TheBlockKeepsTicking.LOGGER.info("Simulating StemBlock {} ticksToSimulate, {} randomTicks",
                ticksToSimulate, randomTicks);
        if (randomTicks <= 0)
            return false;

        // Because mojang is stupid
        int age = (int) state.get(StemBlock.AGE);
        int maxAge = (int) StemBlock.MAX_AGE;
        int ageDiff = maxAge - age; // Difference between age and maxAge
        if (ageDiff > 0) {
            // Grow the stem
            int newAge = Math.min(StemBlock.MAX_AGE, age + ageDiff);
            BlockState newState = state.with(StemBlock.AGE, newAge);
            world.setBlockState(pos, newState, 2);
        } else
            return false;

        // Calculate how many ticks are left to grow gourds
        int gourdTicks = randomTicks - ageDiff;

        if (gourdTicks > 0) {
            var directions = Type.HORIZONTAL.getShuffled(world.random);
            Registry<Block> registry = world.getRegistryManager().getOrThrow(RegistryKeys.BLOCK);
            Optional<Block> optionalGourd = registry.getOptionalValue(gourdBlock);
            Optional<Block> optionalStemBlock = registry.getOptionalValue(attachedStemBlock);
            if (optionalGourd.isEmpty() || optionalStemBlock.isEmpty())
                return false;

            // As long as gourdTicks != 0, try all 4 directions
            for (Direction dir : directions) {
                if (gourdTicks <= 0)
                    return false;

                // Check whether the gourd fits on the block
                BlockPos blockPos = pos.offset(dir);
                BlockState blockGourdWillBeOn = world.getBlockState(blockPos.down());
                if (world.getBlockState(blockPos).isAir()
                        && (blockGourdWillBeOn.isOf(Blocks.FARMLAND)
                                || blockGourdWillBeOn.isIn(BlockTags.DIRT))) {
                    world.setBlockState(blockPos, // Set gourd
                            (BlockState) ((Block) optionalGourd.get()).getDefaultState());
                    world.setBlockState(pos, (BlockState) ((Block) optionalStemBlock.get())
                            .getDefaultState().with(HorizontalFacingBlock.FACING, dir));

                    // Placed gourd, exit
                    break;
                } else {
                    // Failed to gourd
                    gourdTicks--;
                }
            }
        } else
            // No gourdTicks
            return false;
        return ageDiff > 0; // Everything went swell
    }
}
