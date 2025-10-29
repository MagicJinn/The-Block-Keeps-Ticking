package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingBlockAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin implements TickingBlockAccessor {
    private final static int maxHeight = 3;
    private final static int maxAge = 15;

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world);
        if (randomTicks <= 0)
            return false;

        int height = 1;
        while (world.getBlockState(pos.down(height)).isOf(Blocks.SUGAR_CANE) && height < maxHeight)
            height++;
        if (height >= maxHeight)
            return false;

        int age = state.get(SugarCaneBlock.AGE);
        if (age >= maxAge)
            return false;

        boolean changed = false;
        for (int t = 0; t < randomTicks; t++) {
            BlockPos abovePos = pos.up();

            if (world.isAir(abovePos)) {
                if (age == maxAge && height < maxHeight) {
                    world.setBlockState(abovePos, Blocks.SUGAR_CANE.getDefaultState(), 3);
                    age = 0;
                    state = state.with(SugarCaneBlock.AGE, 0);
                    world.setBlockState(pos, state, 3);
                    changed = true;
                    continue;
                }
            }

            if (age < 15) {
                age++;
                state = state.with(SugarCaneBlock.AGE, age);
                world.setBlockState(pos, state, 3);
                changed = true;
            }
        }

        return changed;
    }

}
