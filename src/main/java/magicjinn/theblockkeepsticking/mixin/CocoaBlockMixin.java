package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(CocoaBlock.class)
public class CocoaBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        int randomTicks = TickingCalculator.RandomTickAmount(ticksToSimulate, level, 5);
        if (randomTicks <= 0)
            return false;

        int age = state.getValue(CocoaBlock.AGE);
        if (age >= CocoaBlock.MAX_AGE)
            return false;


        level.setBlock(pos,
                state.setValue(CocoaBlock.AGE, Math.min(CocoaBlock.MAX_AGE, age + randomTicks)), 2);
        return true;
    }
}
