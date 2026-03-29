package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

@Mixin(CropBlock.class)
public class CropBlockMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        CropBlock crop = (CropBlock) (Object) this;

        int growth = TickingCalculator.CropGrowthAmount(ticksToSimulate, crop, level, state, pos);

        int age = crop.getAge(state);
        int maxAge = crop.getMaxAge();

        if (growth <= 0)
            return false;

        int newAge = Math.min(maxAge, age + growth);

        if (newAge <= age)
            return false;

        // Set the new age
        BlockState newStage = crop.getStateForAge(newAge);
        level.setBlock(pos, newStage, 2);

        return true; // Return true if the block state was changed
    }
}
