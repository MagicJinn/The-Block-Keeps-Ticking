package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.Blocks;

@Mixin(CactusBlock.class)
public class CactusBlockMixin implements TickingAccessor {
    @Shadow @Final private static int TALL_THRESHOLD;
    @Shadow @Final private static int FLOWER_GROWTH_AGE;
    @Shadow @Final static double FLOWER_CHANCE_WHEN_SHORT = 0.1;
    @Shadow @Final private static double FLOWER_CHANCE_WHEN_TALL = 0.25;

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        // Only simulate if block above is air
        BlockPos abovePos = pos.up();
        if (!world.isAir(abovePos))
            return false;

        // Determine the height of the current cactus tower
        int height = 1;
        BlockPos currentPos = pos;
        while (world.getBlockState(currentPos.down(height)).isOf(Blocks.CACTUS))
            height++;

        if (height > TALL_THRESHOLD)
            return false;

        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world);
        if (randomTicks <= 0)
            return false;

        boolean changed = false;
        int age = state.get(CactusBlock.AGE);

        for (int i = 0; i < randomTicks; i++) {
            // If age 8, try to place a flower
            if (age == FLOWER_GROWTH_AGE && world.isAir(currentPos.up())) {
                double flowerChance = height >= TALL_THRESHOLD ? FLOWER_CHANCE_WHEN_TALL
                        : FLOWER_CHANCE_WHEN_SHORT;
                if (world.random.nextDouble() <= flowerChance) {
                    world.setBlockState(currentPos.up(), Blocks.CACTUS_FLOWER.getDefaultState(), 3);
                    changed = true;
                    break; // Stop further ticks for this block
                }
            }
            // Try cactus growth at age 15 if tower < max height
            else if (age == CactusBlock.MAX_AGE && height < TALL_THRESHOLD
                    && world.isAir(currentPos.up())) {
                BlockPos newCactusPos = currentPos.up();
                world.setBlockState(newCactusPos, Blocks.CACTUS.getDefaultState(), 3);

                age = 0;
                height++;
                currentPos = newCactusPos;

                // Set old cactus to age 0
                state = state.with(CactusBlock.AGE, 0);
                world.setBlockState(currentPos, state, 260);
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
            world.setBlockState(currentPos, state.with(CactusBlock.AGE, age), 260);
        }

        return changed;
    }
}
