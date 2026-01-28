package magicjinn.apiexample.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import magicjinn.theblockkeepsticking.util.TickingAccessor;

@Mixin(ChestBlockEntity.class)
public class ChestMixin implements TickingAccessor {

    private static final int TICKS_PER_DIAMOND = 120;

    @Override
    public boolean Simulate(long ticksToSimulate, World world, BlockState state, BlockPos pos) {
        ChestBlockEntity chestEntity = (ChestBlockEntity) (Object) this; // cast to ChestBlockEntity

        int amountToAdd = (int) (ticksToSimulate / TICKS_PER_DIAMOND);
        int inventorySlots = chestEntity.size();

        boolean addedAny = false;

        // Get max stack size for diamonds
        int diamondMaxStackSize = new ItemStack(Items.DIAMOND).getMaxCount();

        for (int slot = 0; slot < inventorySlots; slot++) {
            if (amountToAdd <= 0)
                break;

            ItemStack stackInSlot = chestEntity.getStack(slot);
            if (!stackInSlot.isEmpty() && !stackInSlot.isOf(Items.DIAMOND)) // Don't overwrite non-diamond items
                continue;

            int currentStackSize = stackInSlot.getCount();
            int spaceInStack = diamondMaxStackSize - currentStackSize;

            if (spaceInStack > 0) {
                int toAdd = Math.min(amountToAdd, spaceInStack);

                if (stackInSlot.isEmpty())
                    chestEntity.setStack(slot, new ItemStack(Items.DIAMOND, toAdd));
                else
                    stackInSlot.increment(toAdd);

                amountToAdd -= toAdd;
                addedAny = true;
            }
        }
        return addedAny;
    }
}