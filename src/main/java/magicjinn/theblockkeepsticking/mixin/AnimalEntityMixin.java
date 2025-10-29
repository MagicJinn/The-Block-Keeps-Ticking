package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(AnimalEntity.class)
public class AnimalEntityMixin implements TickingAccessor {
    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        PassiveEntity passiveEntity = (PassiveEntity) (Object) this;
        AnimalEntity animal = (AnimalEntity) (Object) this;
        int breedingCooldown = animal.getBreedingAge();
        if (breedingCooldown <= 0)
            return false;
        animal.setBreedingAge((int) Math.max(breedingCooldown, breedingCooldown - ticksToSimulate));
        return true;
    }

}
