package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import magicjinn.theblockkeepsticking.util.TickingAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.level.Level;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin implements TickingAccessor {
    @Shadow
    @Final
    private NonNullList<ItemStack> items;
    @Shadow
    @Final
    private int[] cookingProgress;

    @Override
    public boolean Simulate(long ticksToSimulate, Level level, BlockState state, BlockPos pos) {
        // Check if campfire is lit
        if (!state.hasProperty(CampfireBlock.LIT) || !state.getValue(CampfireBlock.LIT))
            return false;

        boolean changed = false;
        for (int i = 0; i < items.size(); i++) {
            ItemStack item = items.get(i);
            if (!item.isEmpty()) {
                // If cookingProgress is over cookingTime, it ejects items,
                // so we can increment it safely without consequences
                cookingProgress[i] += (int) ticksToSimulate;
                changed = true;
            }
        }

        return changed;
    }
}
