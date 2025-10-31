package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import magicjinn.theblockkeepsticking.util.TickingCalculator;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(PassiveEntity.class)
public class PassiveEntityMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        PassiveEntity entity = (PassiveEntity) (Object) this;

        // Age up babies, "age" down adults to make their breeding cooldown pass
        int currentAge = entity.getBreedingAge(); // Negative value for babies

        if (currentAge == 0)
            return false;

        // Move age towards zero
        entity.setBreedingAge(TickingCalculator.MoveTowardsZero(currentAge, ticksToSimulate));

        return true; // Entity was aged
    }
}
