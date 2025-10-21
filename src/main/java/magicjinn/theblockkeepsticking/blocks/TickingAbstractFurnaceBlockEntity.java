package magicjinn.theblockkeepsticking.blocks;

import java.util.Optional;
import magicjinn.theblockkeepsticking.accessors.AbstractFurnaceAccessor;
import magicjinn.theblockkeepsticking.framework.ProcessingBlock;
import magicjinn.theblockkeepsticking.util.TickingResult;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.FuelRegistry;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ServerRecipeManager;
import net.minecraft.recipe.input.SingleStackRecipeInput;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class TickingAbstractFurnaceBlockEntity extends ProcessingBlock {
    public static final TickingAbstractFurnaceBlockEntity INSTANCE =
            new TickingAbstractFurnaceBlockEntity();

    @Override
    public Class<AbstractFurnaceBlockEntity> getType() {
        return AbstractFurnaceBlockEntity.class;
    }

    @Override
    public void Simulate(BlockEntity blockInstance, Long ticksToSimulate) {
        if (!(blockInstance instanceof AbstractFurnaceBlockEntity furnace))
            return;

        World world = furnace.getWorld();
        if (!(world instanceof ServerWorld serverWorld))
            return;

        // Accessor magic
        AbstractFurnaceAccessor accessor = (AbstractFurnaceAccessor) furnace;
        DefaultedList<ItemStack> inventory = accessor.getInventory();

        ItemStack input = inventory.get(accessor.getInputSlotIndex());
        ItemStack fuel = inventory.get(accessor.getFuelSlotIndex());
        ItemStack output = inventory.get(accessor.getOutputSlotIndex());

        FuelRegistry fuelRegistry = world.getFuelRegistry();
        int remainingBurnTime = accessor.getCurrentlyBurningFuelTimeRemaining();

        // still decrement fuel even if no input, only fair
        if (input.isEmpty()) {
            accessor.setCurrentlyBurningFuelTimeRemaining(
                    remainingBurnTime - ticksToSimulate.intValue());
            return; // Still exit early
        }


        // Find if there's a matching recipe
        Optional<? extends RecipeEntry<? extends AbstractCookingRecipe>> matchOpt = accessor
                .getMatchGetter().getFirstMatch(new SingleStackRecipeInput(input), serverWorld);
        if (matchOpt.isEmpty())
            return;

        // Copy recipe output
        ItemStack recipeOutput = matchOpt.get().value().result().copy();

        int recipeCookTime = AbstractFurnaceBlockEntity.getCookTime(serverWorld, furnace);
        if (recipeCookTime <= 0)
            return;

        // Compute fuel ticks
        int fuelTicksPerItem = fuelRegistry.isFuel(fuel) ? fuelRegistry.getFuelTicks(fuel) : 0;
        int totalFuelTicks = remainingBurnTime + (fuel.getCount() * fuelTicksPerItem);

        int maxByFuel =
                (int) Math.min(ticksToSimulate / recipeCookTime, totalFuelTicks / recipeCookTime);
        int maxByInput = input.getCount();
        int maxByOutput = 0;

        if (output.isEmpty()) {
            maxByOutput = recipeOutput.getMaxCount();
        } else if (ItemStack.areItemsAndComponentsEqual(output, recipeOutput)) {
            maxByOutput = output.getMaxCount() - output.getCount();
        } else { // Different item in output slot, exit early
            accessor.setCurrentlyBurningFuelTimeRemaining(
                    remainingBurnTime - ticksToSimulate.intValue());
            return;
        }

        int actualOps = Math.min(Math.min(maxByFuel, maxByInput), maxByOutput);
        if (actualOps <= 0)
            return;

        // Consume input
        input.decrement(actualOps);

        // Consume fuel
        int ticksNeeded = actualOps * recipeCookTime;
        int ticksConsumed = Math.max(0, ticksNeeded - remainingBurnTime);
        int fuelItemsConsumed =
                (fuelTicksPerItem > 0) ? (int) Math.ceil((double) ticksConsumed / fuelTicksPerItem)
                        : 0;
        fuel.decrement(fuelItemsConsumed);
        int newBurnTime = remainingBurnTime + (fuelItemsConsumed * fuelTicksPerItem) - ticksNeeded;

        // Apply output
        if (output.isEmpty()) {
            ItemStack newOut = recipeOutput.copy();
            newOut.setCount(actualOps);
            inventory.set(accessor.getOutputSlotIndex(), newOut);
        } else {
            output.increment(actualOps);
        }

        // Set new burn time
        accessor.setCurrentlyBurningFuelTimeRemaining(newBurnTime);
    }

}
