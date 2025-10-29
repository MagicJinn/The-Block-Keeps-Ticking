package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.Blocks;

@Mixin(CactusBlock.class)
public class CactusBlockMixin implements TickingBlockAccessor {
    @Shadow @Final static int TALL_THRESHOLD;

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world);
        if (randomTicks <= 0)
            return false;

        int height = 1; // Check the height of the current cactus
        final int maxHeight = TALL_THRESHOLD;
        while (world.getBlockState(pos.down(height)).isOf(Blocks.CACTUS) && height < maxHeight)
            height++;

        if (height >= maxHeight)
            return false;

        int age = state.get(CactusBlock.AGE);
        final int maxAge = CactusBlock.MAX_AGE;
        if (age >= maxAge)
            return false;

        boolean changed = false;
        for (int i = 0; i < randomTicks; i++) {
            BlockPos abovePos = pos.up();

            if (world.isAir(abovePos)) {
                if (age == 8 && Blocks.CACTUS.getDefaultState().canPlaceAt(world, abovePos)) {
                    // Whether or not to give the cactus a flower
                    double chance = height >= maxHeight ? 0.25 : 0.1;
                    if (world.random.nextDouble() <= chance) {
                        world.setBlockState(abovePos, Blocks.CACTUS_FLOWER.getDefaultState(), 3);
                        changed = true;
                        break; // Cactus has flower, abort
                    }
                } else if (age == maxAge && height < maxHeight) {
                    // We grow and track the new top cactus
                    height++;
                    world.setBlockState(abovePos, Blocks.CACTUS.getDefaultState(), 3);
                    age = 0;
                    state = state.with(CactusBlock.AGE, 0);
                    world.setBlockState(pos, state, 3);
                    changed = true;
                    continue;
                }
            }

            if (age < maxAge) {
                age++;
                state = state.with(CactusBlock.AGE, age);
                world.setBlockState(pos, state, 3);
                changed = true;
            }
        }

        return changed;
    }

}
