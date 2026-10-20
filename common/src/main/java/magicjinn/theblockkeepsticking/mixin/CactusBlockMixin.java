package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.Blocks;

@Mixin(CactusBlock.class)
public class CactusBlockMixin implements TickingAccessor {
    @Shadow @Final private static int MAX_CACTUS_GROWING_HEIGHT;
    @Shadow @Final private static int ATTEMPT_GROW_CACTUS_FLOWER_AGE;
    @Shadow @Final private static double ATTEMPT_GROW_CACTUS_FLOWER_SMALL_CACTUS_CHANCE;
    @Shadow @Final private static double ATTEMPT_GROW_CACTUS_FLOWER_TALL_CACTUS_CHANCE;

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        // Only simulate if block above is air
        BlockPos abovePos = pos.above();
        if (!level.isEmptyBlock(abovePos))
            return false;

        // Determine the height of the current cactus tower
        int height = 1;
        BlockPos currentPos = pos;
        while (level.getBlockState(currentPos.below(height)).is(Blocks.CACTUS))
            height++;

        if (height > MAX_CACTUS_GROWING_HEIGHT)
            return false;

        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level);
        if (randomTicks <= 0)
            return false;

        boolean changed = false;
        int age = state.getValue(CactusBlock.AGE);

        for (int i = 0; i < randomTicks; i++) {
            // If age 8, try to place a flower
            if (age == ATTEMPT_GROW_CACTUS_FLOWER_AGE && level.isEmptyBlock(currentPos.above())) {
                double flowerChance = height >= MAX_CACTUS_GROWING_HEIGHT
                        ? ATTEMPT_GROW_CACTUS_FLOWER_TALL_CACTUS_CHANCE
                        : ATTEMPT_GROW_CACTUS_FLOWER_SMALL_CACTUS_CHANCE;
                if (level.getRandom().nextDouble() <= flowerChance) {
                    level.setBlock(currentPos.above(), Blocks.CACTUS_FLOWER.defaultBlockState(), 3);
                    changed = true;
                    break; // Stop further ticks for this block
                }
            }
            // Try cactus growth at age 15 if tower < max height
            else if (age == CactusBlock.MAX_AGE && height < MAX_CACTUS_GROWING_HEIGHT
                    && level.isEmptyBlock(currentPos.above())) {
                age = 0;
                height++;

                // Set old cactus to age 0
                state = state.setValue(CactusBlock.AGE, 0);
                level.setBlock(currentPos, state, 260);

                // New cactus block
                currentPos = currentPos.above();
                level.setBlock(currentPos, Blocks.CACTUS.defaultBlockState(), 3);
                changed = true;
                continue;
            }

            // Increment age if below max
            if (age < CactusBlock.MAX_AGE) {
                age++;
                changed = true;
            }
        }

        // Update the age of the current block if it changed
        if (changed) {
            level.setBlock(currentPos, state.setValue(CactusBlock.AGE, age), 260);
        }

        return changed;
    }
}
