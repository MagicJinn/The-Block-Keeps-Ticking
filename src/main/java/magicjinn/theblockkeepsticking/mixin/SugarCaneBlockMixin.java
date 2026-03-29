package magicjinn.theblockkeepsticking.mixin;
import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

@Mixin(SugarCaneBlock.class)
public class SugarCaneBlockMixin implements TickingAccessor {
    // Nothing to shadow... Thanks Mojang
    private final static int MAX_HEIGHT = 3;
    private final static int MAX_AGE = 15;

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        final int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level);
        if (randomTicks <= 0)
            return false;

        // Calculate height
        int height = 1;
        while (level.getBlockState(pos.below(height)).is(Blocks.SUGAR_CANE) && height < MAX_HEIGHT)
            height++;

        if (height >= MAX_HEIGHT)
            return false;

        boolean changed = false;
        int age = state.getValue(SugarCaneBlock.AGE);
        BlockPos currentPos = pos;
        BlockState currentState = state;

        for (int t = 0; t < randomTicks; t++) {
            BlockPos abovePos = currentPos.above();

            if (age == MAX_AGE && height < MAX_HEIGHT && level.isEmptyBlock(abovePos)) {
                // Place new sugar cane block
                level.setBlockAndUpdate(abovePos, Blocks.SUGAR_CANE.defaultBlockState());

                // Reset age of current block (no clue what 260 is)
                level.setBlock(currentPos, currentState.setValue(SugarCaneBlock.AGE, 0), 260);

                // Refocus and recalibrate
                currentPos = abovePos;
                currentState = Blocks.SUGAR_CANE.defaultBlockState();
                age = 0;
                height++;
                changed = true;
                continue;
            }

            // Increase age if below max
            if (age < MAX_AGE) {
                age++;
                changed = true;
            }
        }

        // Update final state if we made changes
        if (changed) {
            level.setBlock(currentPos, currentState.setValue(SugarCaneBlock.AGE, age), 260);
        }

        return changed;
    }
}
