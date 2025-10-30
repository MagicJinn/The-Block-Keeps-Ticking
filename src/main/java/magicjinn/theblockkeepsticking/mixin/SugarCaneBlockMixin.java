package magicjinn.theblockkeepsticking.mixin;
import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.block.Blocks;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin implements TickingAccessor {
    // Nothing to shadow...
    private final static int maxHeight = 3;
    private final static int maxAge = 15;

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, world);
        if (randomTicks <= 0)
            return false;

        // Calculate height
        int height = 1;
        while (world.getBlockState(pos.down(height)).isOf(Blocks.SUGAR_CANE) && height < maxHeight)
            height++;

        if (height >= maxHeight)
            return false;

        boolean changed = false;
        int age = state.get(SugarCaneBlock.AGE);
        BlockPos currentPos = pos;
        BlockState currentState = state;

        for (int t = 0; t < randomTicks; t++) {
            BlockPos abovePos = currentPos.up();

            if (age == maxAge && height < maxHeight && world.isAir(abovePos)) {
                // Place new sugar cane block
                world.setBlockState(abovePos, Blocks.SUGAR_CANE.getDefaultState(), 3);

                // Reset age of current block
                world.setBlockState(currentPos, currentState.with(SugarCaneBlock.AGE, 0), 3);

                // Refocus and recalibrate
                currentPos = abovePos;
                currentState = Blocks.SUGAR_CANE.getDefaultState();
                age = 0;
                height++;
                changed = true;
                continue;
            }

            // Increase age if below max
            if (age < maxAge) {
                age++;
                changed = true;
            }
        }

        // Update final state if we made changes
        if (changed) {
            world.setBlockState(currentPos, currentState.with(SugarCaneBlock.AGE, age), 3);
        }

        return changed;
    }
}
