package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.level.Level;

@Mixin(AgeableMob.class)
public class AgeableMobMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        AgeableMob ageableMob = (AgeableMob) (Object) this;

        boolean changed = false;
        int currentAge = ageableMob.getAge(); // Negative value for babies

        // This sucks
        if (ageableMob instanceof Chicken chickenEntity && currentAge >= 0) {
            // We can safely subtract, the check is <=
            chickenEntity.eggTime -= (int) ticksToSimulate;
            changed = true;
        } else if (currentAge == 0)
            return false;

        // Age up babies, "age" down adults to make their breeding cooldown pass
        // Move age towards zero
        ageableMob.setAge(TickingCalculator.MoveTowardsZero(currentAge, ticksToSimulate));

        return changed; // Entity was aged
    }
}
