package magicjinn.theblockkeepsticking.mixin;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import magicjinn.theblockkeepsticking.accessors.TickingBlockAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntity.class)
public class CampfireBlockEntityMixin implements TickingBlockAccessor {
    @Shadow @Final private DefaultedList<ItemStack> itemsBeingCooked;
    @Shadow @Final private int[] cookingTimes;
    @Shadow @Final private int[] cookingTotalTimes;

    @Override
    public boolean Simulate(Long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        // Check if campfire is lit
        if (!state.get(CampfireBlock.LIT))
            return false;

        boolean changed = false;
        for (int i = 0; i < itemsBeingCooked.size(); i++) {
            ItemStack item = itemsBeingCooked.get(i);
            if (!item.isEmpty()) {
                // Ensure items stay on the campfire for at least 2 seconds (40 ticks)
                cookingTimes[i] = Math.min(cookingTimes[i] + ticksToSimulate.intValue(),
                        cookingTotalTimes[i] - 20 * 2);
                changed = true;
            }
        }

        return changed;
    }
}
